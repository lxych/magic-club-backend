package cn.magicclub.config;

import cn.magicclub.common.JwtUtil;
import cn.magicclub.mapper.UserMapper;
import cn.magicclub.mapper.UserTokenMapper;
import cn.magicclub.security.JwtAuthenticationFilter;
import cn.magicclub.service.UserService;
import lombok.RequiredArgsConstructor;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

/**
 * Spring Security配置
 *
 * @author demo
 * @since 2025-04-13
 */
@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtUtil jwtUtil;
    private final UserService userService;
    private final UserMapper userMapper;
    private final UserTokenMapper userTokenMapper;

    /**
     * 密码编码器
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /**
     * 认证管理器
     */
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    /**
     * 认证提供者
     */
    @Bean
    public AuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
        authProvider.setUserDetailsService(userService);
        authProvider.setPasswordEncoder(passwordEncoder());
        return authProvider;
    }

    /**
     * JWT认证过滤器
     */
    @Bean
    public JwtAuthenticationFilter jwtAuthenticationFilter() {
        return new JwtAuthenticationFilter(jwtUtil, userMapper, userTokenMapper);
    }

    /**
     * 禁止 JWT 过滤器被 Servlet 容器重复注册
     * <p>
     * 只要 Filter 以 Bean 形式存在，Spring Boot 会自动把它注册成全局 Servlet Filter，
     * 于是同一个过滤器会在「Servlet 层」和「Security 过滤链」各跑一次。后果是未登录/手持过期
     * Token 的请求在到达 /user/login 之前就被拦下返回 401，表现为「怎么都登不上去」。
     * 这里显式关闭自动注册，让它只在 Security 过滤链中执行一次。
     */
    @Bean
    public FilterRegistrationBean<JwtAuthenticationFilter> jwtAuthenticationFilterRegistration(
            JwtAuthenticationFilter filter) {
        FilterRegistrationBean<JwtAuthenticationFilter> registration = new FilterRegistrationBean<>(filter);
        registration.setEnabled(false);
        return registration;
    }

    /**
     * CORS配置源（Spring Security层需要显式配置）
     */
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();
        config.addAllowedOriginPattern("*");
        config.addAllowedMethod("*");
        config.addAllowedHeader("*");
        config.setAllowCredentials(true);
        config.setMaxAge(3600L);
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }

    /**
     * 安全过滤链配置
     */
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .headers(h -> h.frameOptions(f -> f.sameOrigin()))
            // CORS（必须在 Security 层也配置）
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))
            
            // 禁用CSRF（前后端分离不需要）
            .csrf(AbstractHttpConfigurer::disable)
            
            // 配置授权规则
            .authorizeHttpRequests(auth -> auth
                // 认证入口、错误页与静态资源 - 不需要认证
                .requestMatchers("/user/login", "/user/register").permitAll()
                .requestMatchers("/uploads/**", "/error").permitAll()

                // 只读接口对外开放（GET），写操作一律需要登录
                .requestMatchers(HttpMethod.GET, "/config/**").permitAll()
                .requestMatchers(HttpMethod.GET, "/memorial/**").permitAll()
                .requestMatchers(HttpMethod.GET, "/developer/**").permitAll()
                .requestMatchers(HttpMethod.GET, "/forum/posts", "/forum/post/**", "/forum/user/**").permitAll()
                .requestMatchers(HttpMethod.GET, "/resource/**").permitAll()

                // 其余请求（含全部 POST/PUT/DELETE）必须携带有效 Token
                .anyRequest().authenticated()
            )
            
            // 无状态会话
            .sessionManagement(session -> 
                session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            )
            
            // 未认证时返回 401 而非 403
            .exceptionHandling(ex -> ex
                .authenticationEntryPoint((request, response, authException) -> {
                    response.setContentType("application/json;charset=UTF-8");
                    response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                    response.getWriter().write("{\"code\":401,\"message\":\"未登录或登录已过期\"}");
                })
            )
            
            // 认证提供者
            .authenticationProvider(authenticationProvider())
            
            // 添加JWT过滤器
            .addFilterBefore(jwtAuthenticationFilter(), UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}
