package cn.magicclub.security;

import lombok.RequiredArgsConstructor;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * 接口级权限拦截器
 * <p>
 * 只对标注了 {@link RequirePermission} 的 Controller 方法生效，未标注的接口不产生额外开销
 * （不会额外查库）。校验失败抛出 {@link PermissionDeniedException}，由全局异常处理器
 * 统一返回 code=403。
 *
 * @author demo
 * @since 2026-11-18
 */
@Component
@RequiredArgsConstructor
public class PermissionInterceptor implements HandlerInterceptor {

    private final PermissionService permissionService;

    @Override
    public boolean preHandle(@NonNull HttpServletRequest request,
                             @NonNull HttpServletResponse response,
                             @NonNull Object handler) {

        if (!(handler instanceof HandlerMethod)) {
            return true;
        }

        HandlerMethod handlerMethod = (HandlerMethod) handler;

        // 方法级注解优先，其次取类级注解
        RequirePermission required = handlerMethod.getMethodAnnotation(RequirePermission.class);
        if (required == null) {
            required = handlerMethod.getBeanType().getAnnotation(RequirePermission.class);
        }
        if (required == null) {
            return true;
        }

        // 多个权限码之间为「且」关系
        for (String permission : required.value()) {
            permissionService.check(permission);
        }
        return true;
    }
}
