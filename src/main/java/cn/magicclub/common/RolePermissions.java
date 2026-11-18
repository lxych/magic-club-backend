package cn.magicclub.common;

import cn.magicclub.entity.User;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 角色权限映射
 * <p>
 * 权限模型（两层）：
 * <pre>
 *   最终权限 = 角色默认权限 ∪ 用户自定义权限（增量授权）
 * </pre>
 * <ul>
 *     <li>角色默认权限定义了「这个身份本来就能做的事」；</li>
 *     <li>{@code mc_user.permissions} 只保存**额外的**权限码，用于给单个成员单独开放能力；</li>
 *     <li>ADMIN 恒为全部权限，不可削减；</li>
 *     <li>并集语义意味着自定义权限**只能加、不能减**，要收回能力请调整角色。</li>
 * </ul>
 *
 * @author demo
 * @since 2025-08-06
 */
public class RolePermissions {

    private static final Logger log = LoggerFactory.getLogger(RolePermissions.class);

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

    /** 普通成员基础权限：版主也必须具备，否则会出现"管理员能发帖、版主反而不能"的错位 */
    private static final Set<String> MEMBER_BASE = Set.of(
            PermissionConstants.FORUM_CREATE,
            PermissionConstants.RESOURCE_UPLOAD
    );

    private static final Map<String, Set<String>> ROLE_PERMISSION_MAP = new HashMap<>();

    static {
        // 管理员：全部权限
        ROLE_PERMISSION_MAP.put("ADMIN", Set.copyOf(ALL_PERMISSIONS));

        // 版主：成员基础能力 + 论坛管理 + 资源审核 + 公告收发 + 查看花名册
        Set<String> moderator = new LinkedHashSet<>(MEMBER_BASE);
        moderator.add(PermissionConstants.FORUM_DELETE);
        moderator.add(PermissionConstants.FORUM_TOP);
        moderator.add(PermissionConstants.FORUM_ESSENCE);
        moderator.add(PermissionConstants.RESOURCE_AUDIT);
        moderator.add(PermissionConstants.RESOURCE_DELETE);
        moderator.add(PermissionConstants.ANNOUNCEMENT_SEND);
        moderator.add(PermissionConstants.ANNOUNCEMENT_RECALL);
        moderator.add(PermissionConstants.USER_VIEW_ALL);
        ROLE_PERMISSION_MAP.put("MODERATOR", Collections.unmodifiableSet(moderator));

        // 普通成员：发帖 + 上传资源
        ROLE_PERMISSION_MAP.put("MEMBER", MEMBER_BASE);

        // 校友：只读（无写入权限）
        ROLE_PERMISSION_MAP.put("ALUMNI", Set.of());
    }

    /**
     * 获取用户实际生效的权限列表 = 角色默认 ∪ 自定义增量
     */
    public static List<String> getUserPermissions(User user) {
        if (user == null) {
            return Collections.emptyList();
        }
        // ADMIN 始终拥有全部权限，且不参与增量计算
        if ("ADMIN".equals(user.getRole())) {
            return new ArrayList<>(ALL_PERMISSIONS);
        }

        Set<String> effective = new LinkedHashSet<>(getPermissions(user.getRole()));
        effective.addAll(getCustomPermissions(user));
        return new ArrayList<>(effective);
    }

    /**
     * 只解析用户的自定义增量权限（不含角色默认）
     */
    public static List<String> getCustomPermissions(User user) {
        if (user == null) {
            return Collections.emptyList();
        }
        String raw = user.getPermissions();
        if (raw == null || raw.trim().isEmpty()) {
            return Collections.emptyList();
        }
        try {
            List<String> parsed = objectMapper.readValue(raw, new TypeReference<List<String>>() {});
            if (parsed == null) {
                return Collections.emptyList();
            }
            List<String> result = new ArrayList<>();
            for (String code : parsed) {
                // 过滤非法/空值，避免脏数据进入权限判断
                if (code != null && !code.trim().isEmpty()) {
                    result.add(code.trim());
                }
            }
            return result;
        } catch (Exception e) {
            // 解析失败回退到「无额外权限」，但必须留下日志：
            // 否则会出现「明明授权了却不生效」且完全无迹可查的情况
            log.warn("用户[{}]的 permissions 字段不是合法 JSON，已忽略自定义权限。原始值: {}，原因: {}",
                    user.getUsername(), raw, e.getMessage());
            return Collections.emptyList();
        }
    }

    /**
     * 检查角色默认权限是否包含指定权限码
     */
    public static boolean hasPermission(String role, String permission) {
        Set<String> permissions = ROLE_PERMISSION_MAP.get(role);
        return permissions != null && permissions.contains(permission);
    }

    /**
     * 获取角色默认权限
     */
    public static Set<String> getPermissions(String role) {
        return ROLE_PERMISSION_MAP.getOrDefault(role, Set.of());
    }

    private RolePermissions() {
    }
}
