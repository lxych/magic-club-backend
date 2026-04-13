import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

public class GenPwd {
    public static void main(String[] args) {
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        String raw = "admin123456";
        String encoded = encoder.encode(raw);
        System.out.println(encoded);
        // 验证
        System.out.println("验证: " + encoder.matches(raw, encoded));
    }
}
