package cn.magicclub.service;

import cn.magicclub.entity.User;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import org.springframework.security.core.userdetails.UserDetailsService;

import java.util.List;

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

    /**
     * 获取毕业成员列表
     */
    IPage<User> getGraduatedUsers(IPage<User> page, Integer graduationYear);

    /**
     * 标记用户为毕业
     */
    boolean markAsGraduated(Long userId, Integer graduationYear, String currentStatus);

    /**
     * 更新毕业成员信息
     */
    boolean updateGraduatedInfo(Long userId, Integer graduationYear, String currentStatus);

    /**
     * 取消毕业标记
     */
    boolean cancelGraduated(Long userId);

    /**
     * 清除用户所有Token（强制下线）
     */
    void clearUserTokens(Long userId);

    /**
     * 获取用户实际权限列表（自定义权限 > 角色默认）
     */
    List<String> getUserPermissions(Long userId);

    /**
     * 更新用户自定义权限
     * @param userId 用户ID
     * @param permissions 权限列表，传 null 或空列表表示恢复角色默认权限
     */
    void updateUserPermissions(Long userId, List<String> permissions);
}
