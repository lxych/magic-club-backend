package cn.magicclub.common;

/**
 * 权限码常量
 *
 * @author demo
 * @since 2025-08-06
 */
public class PermissionConstants {

    // 论坛权限
    public static final String FORUM_CREATE = "forum:create";
    public static final String FORUM_DELETE = "forum:delete";
    public static final String FORUM_TOP = "forum:top";
    public static final String FORUM_ESSENCE = "forum:essence";

    // 资源权限
    public static final String RESOURCE_UPLOAD = "resource:upload";
    public static final String RESOURCE_AUDIT = "resource:audit";
    public static final String RESOURCE_DELETE = "resource:delete";

    // 公告权限
    public static final String ANNOUNCEMENT_SEND = "announcement:send";
    public static final String ANNOUNCEMENT_RECALL = "announcement:recall";

    // 用户管理
    public static final String USER_MANAGE = "user:manage";
    public static final String USER_VIEW_ALL = "user:viewAll";

    // 系统管理
    public static final String SYSTEM_CONFIG = "system:config";
    public static final String LOG_VIEW = "log:view";
    public static final String ROLE_MANAGE = "role:manage";

    private PermissionConstants() {
    }
}
