package cn.magicclub.controller;

import cn.magicclub.common.Result;
import cn.magicclub.entity.Developer;
import cn.magicclub.service.DeveloperService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 开发人员Controller
 *
 * @author demo
 * @since 2025-04-13
 */
@RestController
@RequestMapping("/developer")
@RequiredArgsConstructor
public class DeveloperController {

    private final DeveloperService developerService;

    /**
     * 添加开发人员
     */
    @PostMapping
    public Result<Developer> addDeveloper(@RequestBody Developer developer) {
        Developer created = developerService.addDeveloper(developer);
        return Result.ok("添加成功", created);
    }

    /**
     * 更新开发人员信息
     */
    @PutMapping("/{id}")
    public Result<Boolean> updateDeveloper(@PathVariable Long id, @RequestBody Developer developer) {
        developer.setId(id);
        boolean success = developerService.updateDeveloper(developer);
        return Result.ok("更新成功", success);
    }

    /**
     * 删除开发人员
     */
    @DeleteMapping("/{id}")
    public Result<Boolean> deleteDeveloper(@PathVariable Long id) {
        boolean success = developerService.deleteDeveloper(id);
        return Result.ok("删除成功", success);
    }

    /**
     * 获取开发人员详情
     */
    @GetMapping("/{id}")
    public Result<Developer> getDeveloperDetail(@PathVariable Long id) {
        Developer developer = developerService.getDeveloperDetail(id);
        return Result.ok(developer);
    }

    /**
     * 获取所有在职开发人员
     */
    @GetMapping("/active")
    public Result<List<Developer>> getActiveDevelopers() {
        List<Developer> list = developerService.getActiveDevelopers();
        return Result.ok(list);
    }

    /**
     * 获取所有开发人员（包括离职）
     */
    @GetMapping("/all")
    public Result<List<Developer>> getAllDevelopers() {
        List<Developer> list = developerService.getAllDevelopers();
        return Result.ok(list);
    }

    /**
     * 按状态获取开发人员
     */
    @GetMapping("/list")
    public Result<List<Developer>> getDevelopersByStatus(@RequestParam Boolean isActive) {
        List<Developer> list = developerService.getDevelopersByStatus(isActive);
        return Result.ok(list);
    }

    /**
     * 设置开发人员在职状态
     */
    @PostMapping("/{id}/status")
    public Result<Boolean> setActiveStatus(@PathVariable Long id, @RequestParam Boolean isActive) {
        boolean success = developerService.setActiveStatus(id, isActive);
        return Result.ok(isActive ? "已设为在职" : "已设为离职", success);
    }
}
