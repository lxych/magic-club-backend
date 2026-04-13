package cn.magicclub.controller;

import cn.magicclub.common.JwtUtil;
import cn.magicclub.common.Result;
import cn.magicclub.dto.LoginRequest;
import cn.magicclub.entity.User;
import cn.magicclub.service.UserService;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * 用户Controller
 *
 * @author demo
 * @since 2025-04-13
 */
@RestController
@RequestMapping("/user")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;
    private final JwtUtil jwtUtil;

    /**
     * 用户注册
     */
    @PostMapping("/register")
    public Result<User> register(@RequestBody User user) {
        User registeredUser = userService.register(user);
        return Result.ok("注册成功", registeredUser);
    }

    /**
     * 用户登录
     */
    @PostMapping("/login")
    public Result<Map<String, Object>> login(@RequestBody LoginRequest request) {
        String token = userService.login(request.getUsername(), request.getPassword());
        
        Map<String, Object> data = new HashMap<>();
        data.put("token", token);
        data.put("tokenType", "Bearer");
        
        // 获取用户信息
        User user = userService.getByUsername(request.getUsername());
        if (user != null) {
            user.setPassword(null);
            data.put("userInfo", user);
        }
        
        return Result.ok("登录成功", data);
    }

    /**
     * 获取当前登录用户信息
     */
    @GetMapping("/info")
    public Result<User> getCurrentUser(@RequestHeader("Authorization") String token) {
        // 去掉Bearer前缀
        if (token.startsWith("Bearer ")) {
            token = token.substring(7);
        }
        
        Long userId = jwtUtil.getUserIdFromToken(token);
        if (userId == null) {
            return Result.error(401, "Token无效");
        }
        
        User user = userService.getById(userId);
        if (user == null) {
            return Result.error("用户不存在");
        }
        
        user.setPassword(null);
        return Result.ok(user);
    }

    /**
     * 更新用户信息
     */
    @PutMapping("/update")
    public Result<Boolean> updateUserInfo(@RequestHeader("Authorization") String token, 
                                          @RequestBody User user) {
        if (token.startsWith("Bearer ")) {
            token = token.substring(7);
        }
        
        Long userId = jwtUtil.getUserIdFromToken(token);
        if (userId == null) {
            return Result.error(401, "Token无效");
        }
        
        user.setId(userId);
        boolean success = userService.updateUserInfo(user);
        return Result.ok("更新成功", success);
    }

    /**
     * 修改密码
     */
    @PostMapping("/change-password")
    public Result<Boolean> changePassword(@RequestHeader("Authorization") String token,
                                          @RequestParam String oldPassword,
                                          @RequestParam String newPassword) {
        if (token.startsWith("Bearer ")) {
            token = token.substring(7);
        }
        
        Long userId = jwtUtil.getUserIdFromToken(token);
        if (userId == null) {
            return Result.error(401, "Token无效");
        }
        
        boolean success = userService.changePassword(userId, oldPassword, newPassword);
        return Result.ok("密码修改成功", success);
    }

    /**
     * 分页查询用户列表
     */
    @GetMapping("/list")
    public Result<IPage<User>> list(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) String keyword) {
        
        IPage<User> page = new Page<>(pageNum, pageSize);
        // 这里可以实现带关键词的查询
        IPage<User> resultPage = userService.page(page);
        
        // 清除密码
        resultPage.getRecords().forEach(user -> user.setPassword(null));
        
        return Result.ok(resultPage);
    }

    /**
     * 根据ID查询用户
     */
    @GetMapping("/{id}")
    public Result<User> getById(@PathVariable Long id) {
        User user = userService.getById(id);
        if (user == null) {
            return Result.error("用户不存在");
        }
        user.setPassword(null);
        return Result.ok(user);
    }

    /**
     * 上传头像
     */
    @PostMapping("/upload-avatar")
    public Result<String> uploadAvatar(@RequestHeader("Authorization") String token,
                                       @RequestParam String avatarUrl) {
        if (token.startsWith("Bearer ")) {
            token = token.substring(7);
        }
        
        Long userId = jwtUtil.getUserIdFromToken(token);
        if (userId == null) {
            return Result.error(401, "Token无效");
        }
        
        String url = userService.uploadAvatar(userId, avatarUrl);
        return Result.ok("头像上传成功", url);
    }

    /**
     * 获取毕业成员列表
     */
    @GetMapping("/graduated")
    public Result<IPage<User>> getGraduatedUsers(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize) {
        
        IPage<User> page = new Page<>(pageNum, pageSize);
        // 这里需要添加按isGraduated查询的条件
        IPage<User> resultPage = userService.page(page);
        
        resultPage.getRecords().forEach(user -> user.setPassword(null));
        return Result.ok(resultPage);
    }
}
