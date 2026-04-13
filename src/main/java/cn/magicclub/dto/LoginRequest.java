package cn.magicclub.dto;

import lombok.Data;

/**
 * 登录请求DTO
 *
 * @author demo
 * @since 2025-04-13
 */
@Data
public class LoginRequest {
    
    /**
     * 用户名
     */
    private String username;
    
    /**
     * 密码
     */
    private String password;
}
