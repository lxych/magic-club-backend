package cn.magicclub.controller;

import cn.magicclub.common.Result;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 系统配置Controller
 *
 * @author demo
 * @since 2025-04-13
 */
@RestController
@RequestMapping("/config")
public class ConfigController {

    /**
     * 获取部门列表
     */
    @GetMapping("/departments")
    public Result<List<String>> getDepartments() {
        List<String> departments = Arrays.asList("组织部", "宣传部", "外联部", "技术部", "财务部", "表演部");
        return Result.ok(departments);
    }

    /**
     * 获取帖子分类
     */
    @GetMapping("/post-categories")
    public Result<List<Map<String, String>>> getPostCategories() {
        List<Map<String, String>> categories = Arrays.asList(
            createCategory("GENERAL", "综合讨论"),
            createCategory("MAGIC", "魔术技巧"),
            createCategory("SHOW", "演出交流"),
            createCategory("RESOURCE", "资源分享")
        );
        return Result.ok(categories);
    }

    /**
     * 获取资源分类
     */
    @GetMapping("/resource-categories")
    public Result<List<Map<String, String>>> getResourceCategories() {
        List<Map<String, String>> categories = Arrays.asList(
            createCategory("TUTORIAL", "魔术教程"),
            createCategory("SCRIPT", "剧本资料"),
            createCategory("PROP", "道具制作"),
            createCategory("PERFORMANCE", "演出视频"),
            createCategory("OTHER", "其他")
        );
        return Result.ok(categories);
    }

    /**
     * 获取性别选项
     */
    @GetMapping("/genders")
    public Result<List<Map<String, Object>>> getGenders() {
        List<Map<String, Object>> genders = Arrays.asList(
            createGender(0, "未知"),
            createGender(1, "男"),
            createGender(2, "女")
        );
        return Result.ok(genders);
    }

    /**
     * 获取用户角色
     */
    @GetMapping("/roles")
    public Result<List<Map<String, String>>> getRoles() {
        List<Map<String, String>> roles = Arrays.asList(
            createCategory("ADMIN", "管理员"),
            createCategory("MEMBER", "成员"),
            createCategory("ALUMNI", "校友")
        );
        return Result.ok(roles);
    }

    /**
     * 获取所有配置
     */
    @GetMapping("/all")
    public Result<Map<String, Object>> getAllConfigs() {
        Map<String, Object> config = new HashMap<>();
        config.put("departments", Arrays.asList("组织部", "宣传部", "外联部", "技术部", "财务部", "表演部"));
        config.put("postCategories", Arrays.asList(
            createCategory("GENERAL", "综合讨论"),
            createCategory("MAGIC", "魔术技巧"),
            createCategory("SHOW", "演出交流"),
            createCategory("RESOURCE", "资源分享")
        ));
        config.put("resourceCategories", Arrays.asList(
            createCategory("TUTORIAL", "魔术教程"),
            createCategory("SCRIPT", "剧本资料"),
            createCategory("PROP", "道具制作"),
            createCategory("PERFORMANCE", "演出视频"),
            createCategory("OTHER", "其他")
        ));
        return Result.ok(config);
    }

    private Map<String, String> createCategory(String code, String name) {
        Map<String, String> map = new HashMap<>();
        map.put("code", code);
        map.put("name", name);
        return map;
    }

    private Map<String, Object> createGender(int code, String name) {
        Map<String, Object> map = new HashMap<>();
        map.put("code", code);
        map.put("name", name);
        return map;
    }
}
