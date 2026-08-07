package cn.magicclub.controller;

import cn.magicclub.common.JwtUtil;
import cn.magicclub.common.Result;
import cn.magicclub.common.RolePermissions;
import cn.magicclub.dto.LoginRequest;
import cn.magicclub.entity.OperationLog;
import cn.magicclub.entity.Post;
import cn.magicclub.entity.ResourceFile;
import cn.magicclub.entity.User;
import cn.magicclub.service.InviteCodeService;
import cn.magicclub.service.OperationLogService;
import cn.magicclub.service.PostService;
import cn.magicclub.service.ResourceFileService;
import cn.magicclub.service.SystemConfigService;
import cn.magicclub.service.UserService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

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
    private final InviteCodeService inviteCodeService;
    private final SystemConfigService systemConfigService;
    private final PostService postService;
    private final ResourceFileService resourceFileService;
    private final OperationLogService operationLogService;
    private final HttpServletRequest request;

    /**
     * 用户注册 (需要邀请码)
     */
    @PostMapping("/register")
    public Result<User> register(@RequestBody User user) {
        // 验证邀请码
        if (!inviteCodeService.validateInviteCode(user.getInviteCode())) {
            return Result.error(403, "邀请码无效或已过期");
        }
        
        // 设置默认角色为 MEMBER
        user.setRole("MEMBER");
        user.setForcedLogout(0);
        
        User registeredUser = userService.register(user);
        return Result.ok("注册成功", registeredUser);
    }

    /**
     * 用户登录
     */
    @PostMapping("/login")
    public Result<Map<String, Object>> login(@RequestBody LoginRequest request) {
        // 先验证用户名密码
        String token = userService.login(request.getUsername(), request.getPassword());
        
        // 检查是否被强制下线
        User user = userService.getByUsername(request.getUsername());
        if (user != null && Integer.valueOf(1).equals(user.getForcedLogout())) {
            return Result.error(403, "该账号已被管理员强制下线，请联系管理员");
        }
        
        Map<String, Object> data = new HashMap<>();
        data.put("token", token);
        data.put("tokenType", "Bearer");
        
        if (user != null) {
            user.setPassword(null);
            data.put("userInfo", user);
            // 返回实际权限列表
            List<String> permissions = RolePermissions.getUserPermissions(user);
            data.put("permissions", permissions);
        }
        
        return Result.ok("登录成功", data);
    }

    /**
     * 获取当前登录用户信息
     */
    @GetMapping("/info")
    public Result<Map<String, Object>> getCurrentUser(@RequestHeader("Authorization") String token) {
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
        
        // 返回用户信息和权限
        Map<String, Object> data = new HashMap<>();
        data.put("userInfo", user);
        List<String> permissions = RolePermissions.getUserPermissions(user);
        data.put("permissions", permissions);
        return Result.ok(data);
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
        
        // 构建查询条件
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(User::getDeleted, 0); // 未删除的用户
        
        // 关键字搜索
        if (keyword != null && !keyword.trim().isEmpty()) {
            wrapper.and(w -> w.like(User::getRealName, keyword)
                    .or()
                    .like(User::getUsername, keyword)
                    .or()
                    .like(User::getStudentNo, keyword));
        }
        
        // 按创建时间倒序
        wrapper.orderByDesc(User::getCreateTime);
        
        IPage<User> resultPage = userService.page(page, wrapper);
        
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
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) Integer graduationYear) {
        
        IPage<User> page = new Page<>(pageNum, pageSize);
        IPage<User> resultPage = userService.getGraduatedUsers(page, graduationYear);
        
        resultPage.getRecords().forEach(user -> user.setPassword(null));
        return Result.ok(resultPage);
    }

    /**
     * 添加毕业成员（将现有用户标记为毕业）
     */
    @PostMapping("/{id}/graduate")
    public Result<Boolean> markUserAsGraduated(@PathVariable Long id,
                                                @RequestParam Integer graduationYear,
                                                @RequestParam(required = false) String currentStatus) {
        boolean success = userService.markAsGraduated(id, graduationYear, currentStatus);
        return Result.ok("标记成功", success);
    }

    /**
     * 更新毕业成员信息
     */
    @PutMapping("/{id}/graduate")
    public Result<Boolean> updateGraduatedInfo(@PathVariable Long id,
                                                @RequestParam Integer graduationYear,
                                                @RequestParam(required = false) String currentStatus) {
        boolean success = userService.updateGraduatedInfo(id, graduationYear, currentStatus);
        return Result.ok("更新成功", success);
    }

    /**
     * 取消毕业标记（仅管理员）
     */
    @DeleteMapping("/{id}/graduate")
    public Result<Boolean> cancelGraduated(@PathVariable Long id) {
        boolean success = userService.cancelGraduated(id);
        return Result.ok("取消毕业标记成功", success);
    }

    /**
     * 获取当前邀请码
     */
    @GetMapping("/invite-code")
    public Result<String> getInviteCode(@RequestHeader("Authorization") String token) {
        String role = jwtUtil.getRoleFromToken(token.replace("Bearer ", ""));
        if (!"ADMIN".equals(role)) {
            return Result.error(403, "仅管理员可查看邀请码");
        }
        String code = inviteCodeService.getValidInviteCode();
        return Result.ok(code);
    }

    /**
     * 设置新邀请码
     */
    @PostMapping("/invite-code")
    public Result<Boolean> setInviteCode(@RequestHeader("Authorization") String token,
                                          @RequestParam String code) {
        String role = jwtUtil.getRoleFromToken(token.replace("Bearer ", ""));
        if (!"ADMIN".equals(role)) {
            return Result.error(403, "仅管理员可设置邀请码");
        }
        Long userId = jwtUtil.getUserIdFromToken(token);
        inviteCodeService.setNewInviteCode(code, userId);
        return Result.ok("邀请码设置成功", true);
    }

    /**
     * 作废当前邀请码
     */
    @DeleteMapping("/invite-code")
    public Result<Boolean> invalidateInviteCode(@RequestHeader("Authorization") String token,
                                                 @RequestParam String reason) {
        String role = jwtUtil.getRoleFromToken(token.replace("Bearer ", ""));
        if (!"ADMIN".equals(role)) {
            return Result.error(403, "仅管理员可作废邀请码");
        }
        Long userId = jwtUtil.getUserIdFromToken(token);
        inviteCodeService.invalidateCurrentCode(reason, userId);
        return Result.ok("邀请码已作废", true);
    }

    /**
     * 强制下线用户
     */
    @PostMapping("/{id}/force-logout")
    public Result<Boolean> forceLogout(@RequestHeader("Authorization") String token,
                                        @PathVariable Long id) {
        String role = jwtUtil.getRoleFromToken(token.replace("Bearer ", ""));
        if (!"ADMIN".equals(role)) {
            return Result.error(403, "仅管理员可强制下线用户");
        }
        
        User user = userService.getById(id);
        if (user == null) {
            return Result.error("用户不存在");
        }
        if ("ADMIN".equals(user.getRole())) {
            return Result.error(403, "不能强制下线管理员");
        }
        
        // 标记用户为强制下线
        user.setForcedLogout(1);
        userService.updateById(user);

        // 清除该用户所有 Token，立即踢下线
        userService.clearUserTokens(id);
        
        // 同时作废邀请码
        Long adminId = jwtUtil.getUserIdFromToken(token);
        inviteCodeService.invalidateCurrentCode("用户 " + user.getUsername() + " 被强制下线", adminId);
        
        return Result.ok("用户已被强制下线，邀请码已作废", true);
    }

    /**
     * 获取禁言状态
     */
    @GetMapping("/global-mute")
    public Result<Boolean> getGlobalMuteStatus(@RequestHeader("Authorization") String token) {
        String role = jwtUtil.getRoleFromToken(token.replace("Bearer ", ""));
        if (!"ADMIN".equals(role)) {
            return Result.error(403, "仅管理员可查看禁言状态");
        }
        boolean isMute = systemConfigService.isGlobalMute();
        return Result.ok(isMute);
    }

    /**
     * 设置禁言状态
     */
    @PostMapping("/global-mute")
    public Result<Boolean> setGlobalMuteStatus(@RequestHeader("Authorization") String token,
                                                @RequestParam boolean mute) {
        String role = jwtUtil.getRoleFromToken(token.replace("Bearer ", ""));
        if (!"ADMIN".equals(role)) {
            return Result.error(403, "仅管理员可设置禁言状态");
        }
        systemConfigService.setGlobalMute(mute);

        // 操作日志
        saveOperationLog(token, mute ? "MUTE_USER" : "UNMUTE_USER",
                "全局", "0",
                mute ? "开启全员禁言" : "关闭全员禁言");

        return Result.ok(mute ? "已开启全员禁言" : "已关闭全员禁言", true);
    }

    /**
     * 修改用户角色（仅管理员）
     */
    @PutMapping("/{id}/role")
    public Result<Boolean> updateUserRole(@RequestHeader("Authorization") String token,
                                           @PathVariable Long id,
                                           @RequestParam String role) {
        String adminRole = jwtUtil.getRoleFromToken(token.replace("Bearer ", ""));
        if (!"ADMIN".equals(adminRole)) {
            return Result.error(403, "仅管理员可修改用户角色");
        }

        // 验证角色值
        java.util.Set<String> validRoles = java.util.Set.of("ADMIN", "MODERATOR", "MEMBER", "ALUMNI");
        if (!validRoles.contains(role)) {
            return Result.error(400, "无效的角色: " + role);
        }

        User user = userService.getById(id);
        if (user == null) {
            return Result.error("用户不存在");
        }
        if ("ADMIN".equals(user.getRole())) {
            return Result.error(403, "不能修改管理员角色");
        }

        String oldRole = user.getRole();
        user.setRole(role);
        userService.updateById(user);

        // 如果角色变了，清除该用户的 token 使其重新登录
        if (!oldRole.equals(role)) {
            userService.clearUserTokens(id);
        }

        // 操作日志
        saveOperationLog(token, "ROLE_CHANGE",
                user.getUsername(),
                String.valueOf(id),
                "角色变更: " + oldRole + " → " + role);

        return Result.ok("角色已更新", true);
    }

    /**
     * 获取用户权限列表（仅管理员）
     */
    @GetMapping("/{id}/permissions")
    public Result<List<String>> getUserPermissions(@RequestHeader("Authorization") String token,
                                                    @PathVariable Long id) {
        String role = jwtUtil.getRoleFromToken(token.replace("Bearer ", ""));
        if (!"ADMIN".equals(role)) {
            return Result.error(403, "仅管理员可查看用户权限");
        }

        User user = userService.getById(id);
        if (user == null) {
            return Result.error("用户不存在");
        }

        List<String> permissions = RolePermissions.getUserPermissions(user);
        return Result.ok(permissions);
    }

    /**
     * 更新用户自定义权限（仅管理员）
     */
    @PutMapping("/{id}/permissions")
    public Result<Boolean> updateUserPermissions(@RequestHeader("Authorization") String token,
                                                  @PathVariable Long id,
                                                  @RequestBody Map<String, List<String>> body) {
        String role = jwtUtil.getRoleFromToken(token.replace("Bearer ", ""));
        if (!"ADMIN".equals(role)) {
            return Result.error(403, "仅管理员可修改用户权限");
        }

        User user = userService.getById(id);
        if (user == null) {
            return Result.error("用户不存在");
        }

        List<String> permissions = body.get("permissions");
        userService.updateUserPermissions(id, permissions);

        // 清除该用户 Token 使其重新登录以获取新权限
        userService.clearUserTokens(id);

        // 操作日志
        User targetUser = userService.getById(id);
        saveOperationLog(token, "PERMISSION_CHANGE",
                targetUser != null ? targetUser.getUsername() : String.valueOf(id),
                String.valueOf(id),
                "权限更新: " + (permissions != null ? String.join(", ", permissions) : "恢复默认"));

        return Result.ok("权限已更新", true);
    }

    // ==================== 私有辅助方法 ====================

    /**
     * 保存操作日志
     */
    private void saveOperationLog(String token, String action, String target, String targetId, String detail) {
        try {
            Long userId = jwtUtil.getUserIdFromToken(token.replace("Bearer ", ""));
            String username = jwtUtil.getUsernameFromToken(token.replace("Bearer ", ""));

            OperationLog log = new OperationLog();
            log.setUserId(userId);
            log.setUsername(username);
            log.setAction(action);
            log.setTarget(target);
            log.setTargetId(targetId);
            log.setDetail(detail);
            log.setIp(getClientIp());
            operationLogService.addLog(log);
        } catch (Exception e) {
            // 日志记录失败不影响业务
        }
    }

    /**
     * 获取客户端真实IP
     */
    private String getClientIp() {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("X-Real-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        // 多级代理取第一个IP
        if (ip != null && ip.contains(",")) {
            ip = ip.split(",")[0].trim();
        }
        return ip;
    }
}
