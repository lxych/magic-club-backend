package cn.magicclub.config;

import cn.magicclub.mapper.UserMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class PasswordGenerator implements CommandLineRunner {
    
    private final UserMapper userMapper;
    
    @Override
    public void run(String... args) {
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        String password = "admin123456";
        String encoded = encoder.encode(password);
        
        // 更新 admin 用户密码
        userMapper.updatePasswordByUsername("admin", encoded);
        
        System.out.println("========================================");
        System.out.println("✅ admin 密码已更新为 BCrypt 格式");
        System.out.println("明文密码: " + password);
        System.out.println("BCrypt密码: " + encoded);
        System.out.println("========================================");
    }
}
