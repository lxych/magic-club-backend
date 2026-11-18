package cn.magicclub.security;

import cn.magicclub.common.RolePermissions;
import cn.magicclub.entity.User;
import cn.magicclub.mapper.UserMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;

/**
 * 权限计算服务
 * <p>
 * 统一从「当前登录用户」解析出**实际生效的权限集合**，作为后端所有鉴权判断的唯一入口。
 * 解析规则与前端、/user/{id}/permissions 接口完全一致：
 * <ol>
 *     <li>ADMIN 角色 → 拥有全部权限码</li>
 *     <li>用户表 permissions 字段（JSON 数组）非空 → 使用自定义权限</li>
 *     <li>否则 → 使用角色默认权限</li>
 * </ol>
 *
 * @author demo
 * @since 2026-11-18
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PermissionService {

    private final UserMapper userMapper;

    /**
     * 当前登录用户，未登录返回 null
     */
    public User currentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null
                || !authentication.isAuthenticated()
                || authentication instanceof AnonymousAuthenticationToken) {
            return null;
        }
        Object principal = authentication.getPrincipal();
        if (!(principal instanceof String)) {
            return null;
        }
        String username = (String) principal;
        if (username.isEmpty() || "anonymousUser".equals(username)) {
            return null;
        }
        return userMapper.selectByUsername(username);
    }

    /**
     * 当前登录用户实际拥有的权限码
     */
    public List<String> currentPermissions() {
        User user = currentUser();
        if (user == null) {
            return Collections.emptyList();
        }
        return RolePermissions.getUserPermissions(user);
    }

    /**
     * 是否拥有指定权限
     */
    public boolean has(String permission) {
        if (permission == null || permission.isEmpty()) {
            return true;
        }
        return currentPermissions().contains(permission);
    }

    /**
     * 是否拥有其中任意一个权限
     */
    public boolean hasAny(String... permissions) {
        List<String> owned = currentPermissions();
        for (String permission : permissions) {
            if (owned.contains(permission)) {
                return true;
            }
        }
        return false;
    }

    /**
     * 校验权限，不通过则抛出 {@link PermissionDeniedException}
     */
    public void check(String permission) {
        if (!has(permission)) {
            User user = currentUser();
            log.warn("权限校验失败: user={}, role={}, required={}",
                    user == null ? "anonymous" : user.getUsername(),
                    user == null ? "-" : user.getRole(),
                    permission);
            throw new PermissionDeniedException(permission);
        }
    }

    /**
     * 当前登录用户 ID，未登录返回 null
     */
    public Long currentUserId() {
        User user = currentUser();
        return user == null ? null : user.getId();
    }
}
