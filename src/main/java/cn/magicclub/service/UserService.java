package cn.magicclub.service;

import cn.magicclub.entity.User;
import com.baomidou.mybatisplus.extension.service.IService;
import org.springframework.security.core.userdetails.UserDetailsService;

/**
 * 用户Service接口
 *
 * @author demo
 * @since 2025-04-13
 */
public interface UserService extends IService<User>, UserDetailsService {

    /**
     * 用户注册
     */
    User register(User user);

    /**
     * 用户登录
     */
    String login(String username, String password);

    /**
     * 根据用户名查询用户
     */
    User getByUsername(String username);

    /**
     * 根据学号查询用户
     */
    User getByStudentNo(String studentNo);

    /**
     * 更新用户信息
     */
    boolean updateUserInfo(User user);

    /**
     * 修改密码
     */
    boolean changePassword(Long userId, String oldPassword, String newPassword);

    /**
     * 上传头像
     */
    String uploadAvatar(Long userId, String avatarUrl);
}
