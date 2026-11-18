package cn.magicclub.security;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 接口权限声明
 * <p>
 * 标注在 Controller 方法（或整个 Controller）上，由 {@link PermissionInterceptor}
 * 在进入方法前完成校验。多个权限码之间是「并且」关系，全部满足才放行。
 * <p>
 * 权限来源：用户自定义权限（User.permissions，JSON 数组）优先，
 * 否则回退到角色默认权限（{@link cn.magicclub.common.RolePermissions}）；ADMIN 恒为全部权限。
 *
 * @author demo
 * @since 2026-11-18
 */
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface RequirePermission {

    /**
     * 需要的权限码，见 {@link cn.magicclub.common.PermissionConstants}
     */
    String[] value();
}
