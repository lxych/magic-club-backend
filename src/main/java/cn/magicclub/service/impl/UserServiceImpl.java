package cn.magicclub.service.impl;

import cn.magicclub.common.JwtUtil;
import cn.magicclub.common.RolePermissions;
import cn.magicclub.entity.User;
import cn.magicclub.entity.UserToken;
import cn.magicclub.mapper.UserMapper;
import cn.magicclub.mapper.UserTokenMapper;
import cn.magicclub.service.UserService;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

/**
 * 用户Service实现类
 *
 * @author demo
 * @since 2025-04-13
 */
@Service
@RequiredArgsConstructor
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService, UserDetailsService {

    private final UserMapper userMapper;
    private final UserTokenMapper userTokenMapper;
    private final JwtUtil jwtUtil;
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    @Override
    public org.springframework.security.core.userdetails.UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userMapper.selectByUsername(username);
        if (user == null) {
            throw new UsernameNotFoundException("用户不存在: " + username);
        }
        if (user.getStatus() == 0) {
            throw new UsernameNotFoundException("账号已被禁用: " + username);
        }
        return new org.springframework.security.core.userdetails.User(
                user.getUsername(),
                user.getPassword(),
                Collections.singletonList(new org.springframework.security.core.authority.SimpleGrantedAuthority("ROLE_" + user.getRole()))
        );
    }

    @Override
    @Transactional
    public User register(User user) {
        // 检查用户名是否已存在
        if (getByUsername(user.getUsername()) != null) {
            throw new RuntimeException("用户名已存在");
        }

        // 检查学号是否已存在
        if (StringUtils.hasText(user.getStudentNo()) && getByStudentNo(user.getStudentNo()) != null) {
            throw new RuntimeException("学号已被注册");
        }

        // 加密密码
        user.setPassword(passwordEncoder.encode(user.getPassword()));

        // 设置默认值
        if (user.getRole() == null) {
            user.setRole("MEMBER");
        }
        if (user.getStatus() == null) {
            user.setStatus(1);
        }
        if (user.getIsGraduated() == null) {
            user.setIsGraduated(0);
        }

        // 保存用户
        userMapper.insert(user);

        // 清除密码后返回
        user.setPassword(null);
        return user;
    }

    @Override
    public String login(String username, String password) {
        // 查询用户
        User user = getByUsername(username);
        if (user == null) {
            throw new RuntimeException("用户名或密码错误");
        }

        // 检查用户状态
        if (user.getStatus() == 0) {
            throw new RuntimeException("账号已被禁用");
        }

        // 验证密码
        if (!passwordEncoder.matches(password, user.getPassword())) {
            throw new RuntimeException("用户名或密码错误");
        }

        // 生成JWT token
        String jwt = jwtUtil.generateToken(user.getId(), user.getUsername(), user.getRole());

        // 清除旧 Token，写入新 Token（单设备登录）
        userTokenMapper.deleteByUserId(user.getId());

        // 重置强制下线标记（允许重新登录）
        if (Integer.valueOf(1).equals(user.getForcedLogout())) {
            user.setForcedLogout(0);
            userMapper.updateById(user);
        }

        UserToken userToken = new UserToken();
        userToken.setUserId(user.getId());
        userToken.setToken(jwt);
        userToken.setLoginTime(LocalDateTime.now());
        userToken.setExpireTime(LocalDateTime.now().plusDays(1));
        userToken.setStatus(1);
        userTokenMapper.insert(userToken);

        return jwt;
    }

    @Override
    public User getByUsername(String username) {
        return userMapper.selectByUsername(username);
    }

    @Override
    public User getByStudentNo(String studentNo) {
        return userMapper.selectByStudentNo(studentNo);
    }

    @Override
    @Transactional
    public boolean updateUserInfo(User user) {
        // 不允许修改用户名和密码
        user.setUsername(null);
        user.setPassword(null);

        return userMapper.updateById(user) > 0;
    }

    @Override
    @Transactional
    public boolean changePassword(Long userId, String oldPassword, String newPassword) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new RuntimeException("用户不存在");
        }

        // 验证旧密码
        if (!passwordEncoder.matches(oldPassword, user.getPassword())) {
            throw new RuntimeException("原密码错误");
        }

        // 更新密码
        user.setPassword(passwordEncoder.encode(newPassword));
        return userMapper.updateById(user) > 0;
    }

    @Override
    @Transactional
    public String uploadAvatar(Long userId, String avatarUrl) {
        User user = new User();
        user.setId(userId);
        user.setAvatar(avatarUrl);
        userMapper.updateById(user);
        return avatarUrl;
    }

    @Override
    public IPage<User> getGraduatedUsers(IPage<User> page, Integer graduationYear) {
        return userMapper.selectGraduatedUsers(page, graduationYear);
    }

    @Override
    @Transactional
    public boolean markAsGraduated(Long userId, Integer graduationYear, String currentStatus) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new RuntimeException("用户不存在");
        }
        
        user.setIsGraduated(1);
        user.setGraduationYear(graduationYear);
        user.setRole("ALUMNI"); // 标记为校友角色
        if (StringUtils.hasText(currentStatus)) {
            // 可以添加 currentStatus 字段到 User 实体
        }
        
        return userMapper.updateById(user) > 0;
    }

    @Override
    @Transactional
    public boolean updateGraduatedInfo(Long userId, Integer graduationYear, String currentStatus) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new RuntimeException("用户不存在");
        }
        
        if (!user.getIsGraduated().equals(1)) {
            throw new RuntimeException("该用户不是毕业成员");
        }
        
        user.setGraduationYear(graduationYear);
        // 可以添加 currentStatus 字段到 User 实体
        
        return userMapper.updateById(user) > 0;
    }

    @Override
    @Transactional
    public boolean cancelGraduated(Long userId) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new RuntimeException("用户不存在");
        }
        
        user.setIsGraduated(0);
        user.setGraduationYear(null);
        user.setRole("MEMBER"); // 恢复为普通成员
        
        return userMapper.updateById(user) > 0;
    }

    @Override
    @Transactional
    public void clearUserTokens(Long userId) {
        userTokenMapper.deleteByUserId(userId);
    }

    @Override
    public List<String> getUserPermissions(Long userId) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            return Collections.emptyList();
        }
        return RolePermissions.getUserPermissions(user);
    }

    @Override
    @Transactional
    public void updateUserPermissions(Long userId, List<String> permissions) {
        User target = userMapper.selectById(userId);
        if (target == null) {
            throw new RuntimeException("用户不存在");
        }

        // 只保存「角色默认之外」的增量权限。
        // 前端提交的是完整勾选集，这里过滤掉角色自带的，避免：
        //   1. 自定义字段里塞满角色默认权限，看不出真正额外授了什么；
        //   2. 后续调整角色默认权限时，用户被旧快照"锁死"。
        java.util.Set<String> roleDefaults = RolePermissions.getPermissions(target.getRole());
        List<String> extras = new java.util.ArrayList<>();
        if (permissions != null) {
            for (String code : permissions) {
                String c = code == null ? null : code.trim();
                if (c != null && !c.isEmpty() && !roleDefaults.contains(c) && !extras.contains(c)) {
                    extras.add(c);
                }
            }
        }

        if (extras.isEmpty()) {
            // 没有额外权限：清空字段（NULL），最终权限回落到角色默认
            LambdaUpdateWrapper<User> wrapper = new LambdaUpdateWrapper<User>()
                    .eq(User::getId, userId)
                    .set(User::getPermissions, null);
            userMapper.update(null, wrapper);
        } else {
            try {
                ObjectMapper mapper = new ObjectMapper();
                User update = new User();
                update.setId(userId);
                update.setPermissions(mapper.writeValueAsString(extras));
                userMapper.updateById(update);
            } catch (JsonProcessingException e) {
                throw new RuntimeException("权限序列化失败", e);
            }
        }
    }
}
