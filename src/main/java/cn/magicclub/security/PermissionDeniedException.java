package cn.magicclub.security;

/**
 * 权限不足异常
 * <p>
 * 由 {@link PermissionInterceptor} 在权限校验失败时抛出，
 * 由 GlobalExceptionHandler 统一转换为 code=403 的响应体。
 *
 * @author demo
 * @since 2026-11-18
 */
public class PermissionDeniedException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    /**
     * 缺失的权限码
     */
    private final String permission;

    public PermissionDeniedException(String permission) {
        super("没有操作权限，缺少权限码: " + permission);
        this.permission = permission;
    }

    public PermissionDeniedException(String permission, String message) {
        super(message);
        this.permission = permission;
    }

    public String getPermission() {
        return permission;
    }
}
