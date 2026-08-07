package cn.magicclub.common;

import cn.magicclub.entity.User;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 角色权限映射
 *
 * @author demo
 * @since 2025-08-06
 */
public class RolePermissions {

    private static final ObjectMapper objectMapper = new ObjectMapper();

    /** 全部权限码列表 */
    public static final List<String> ALL_PERMISSIONS = List.of(
            PermissionConstants.FORUM_CREATE,
            PermissionConstants.FORUM_DELETE,
            PermissionConstants.FORUM_TOP,
            PermissionConstants.FORUM_ESSENCE,
            PermissionConstants.RESOURCE_UPLOAD,
            PermissionConstants.RESOURCE_AUDIT,
            PermissionConstants.RESOURCE_DELETE,
            PermissionConstants.ANNOUNCEMENT_SEND,
            PermissionConstants.ANNOUNCEMENT_RECALL,
            PermissionConstants.USER_MANAGE,
            PermissionConstants.USER_VIEW_ALL,
            PermissionConstants.SYSTEM_CONFIG,
            PermissionConstants.LOG_VIEW,
            PermissionConstants.ROLE_MANAGE
    );

    private static final Map<String, Set<String>> ROLE_PERMISSION_MAP = new HashMap<>();

    static {
        // 管理员：全部权限
        ROLE_PERMISSION_MAP.put("ADMIN", Set.copyOf(ALL_PERMISSIONS));

        // 版主：论坛管理、资源审批、公告发送、花名册
        ROLE_PERMISSION_MAP.put("MODERATOR", Set.of(
                PermissionConstants.FORUM_DELETE,
                PermissionConstants.FORUM_TOP,
                PermissionConstants.FORUM_ESSENCE,
                PermissionConstants.RESOURCE_AUDIT,
                PermissionConstants.ANNOUNCEMENT_SEND,
                PermissionConstants.ANNOUNCEMENT_RECALL,
                PermissionConstants.USER_VIEW_ALL
        ));

        // 普通成员：发帖、上传资源
        ROLE_PERMISSION_MAP.put("MEMBER", Set.of(
                PermissionConstants.FORUM_CREATE,
                PermissionConstants.RESOURCE_UPLOAD
        ));

        // 校友：只读（无写入权限）
        ROLE_PERMISSION_MAP.put("ALUMNI", Set.of());
    }

    /**
     * 获取用户实际拥有的权限列表
     * - ADMIN 始终拥有全部权限
     * - 如果用户有自定义权限集（permissions 字段不为空），使用自定义权限
     * - 否则使用角色默认权限
     */
    public static List<String> getUserPermissions(User user) {
        // ADMIN 始终拥有全部权限
        if ("ADMIN".equals(user.getRole())) {
            return new ArrayList<>(ALL_PERMISSIONS);
        }
        // 如果用户有自定义权限集，解析 JSON 返回
        if (user.getPermissions() != null && !user.getPermissions().trim().isEmpty()) {
            try {
                return objectMapper.readValue(user.getPermissions(), new TypeReference<List<String>>() {});
            } catch (Exception e) {
                // 解析失败，回退到角色默认权限
            }
        }
        // 使用角色默认权限
        return new ArrayList<>(getPermissions(user.getRole()));
    }

    /**
     * 检查角色是否拥有指定权限
     */
    public static boolean hasPermission(String role, String permission) {
        Set<String> permissions = ROLE_PERMISSION_MAP.get(role);
        return permissions != null && permissions.contains(permission);
    }

    /**
     * 获取角色拥有的所有权限码
     */
    public static Set<String> getPermissions(String role) {
        return ROLE_PERMISSION_MAP.getOrDefault(role, Set.of());
    }

    private RolePermissions() {
    }
}
