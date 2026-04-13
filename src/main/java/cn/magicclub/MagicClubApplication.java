package cn.magicclub;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * 魔术社团网站启动类
 *
 * @author demo
 * @since 2025-04-13
 */
@SpringBootApplication
@MapperScan("cn.magicclub.mapper")
public class MagicClubApplication {

    public static void main(String[] args) {
        SpringApplication.run(MagicClubApplication.class, args);
        System.out.println("╔════════════════════════════════════════════════════╗");
        System.out.println("║     🎩 魔术社团网站系统启动成功 🎩                  ║");
        System.out.println("║                                                    ║");
        System.out.println("║     API地址: http://localhost:8080/api            ║");
        System.out.println("╚════════════════════════════════════════════════════╝");
    }
}
