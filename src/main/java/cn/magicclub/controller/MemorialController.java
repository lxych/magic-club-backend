package cn.magicclub.controller;

import cn.magicclub.common.Result;
import cn.magicclub.entity.Memorial;
import cn.magicclub.service.MemorialService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 纪念墙Controller
 *
 * @author demo
 * @since 2025-04-13
 */
@RestController
@RequestMapping("/memorial")
@RequiredArgsConstructor
public class MemorialController {

    private final MemorialService memorialService;

    /**
     * 创建纪念墙记录
     */
    @PostMapping
    public Result<Memorial> createMemorial(@RequestBody Memorial memorial) {
        Memorial created = memorialService.createMemorial(memorial);
        return Result.ok("创建成功", created);
    }

    /**
     * 更新纪念墙记录
     */
    @PutMapping("/{id}")
    public Result<Boolean> updateMemorial(@PathVariable Long id, @RequestBody Memorial memorial) {
        memorial.setId(id);
        boolean success = memorialService.updateMemorial(memorial);
        return Result.ok("更新成功", success);
    }

    /**
     * 删除纪念墙记录
     */
    @DeleteMapping("/{id}")
    public Result<Boolean> deleteMemorial(@PathVariable Long id) {
        boolean success = memorialService.deleteMemorial(id);
        return Result.ok("删除成功", success);
    }

    /**
     * 获取纪念墙详情
     */
    @GetMapping("/{id}")
    public Result<Memorial> getMemorialDetail(@PathVariable Long id) {
        Memorial memorial = memorialService.getMemorialDetail(id);
        return Result.ok(memorial);
    }

    /**
     * 按年份查询纪念墙数据
     */
    @GetMapping("/year/{year}")
    public Result<List<Memorial>> getMemorialsByYear(@PathVariable Integer year) {
        List<Memorial> list = memorialService.getMemorialsByYear(year);
        return Result.ok(list);
    }

    /**
     * 获取所有毕业年份列表
     */
    @GetMapping("/years")
    public Result<List<Integer>> getAllGraduationYears() {
        List<Integer> years = memorialService.getAllGraduationYears();
        return Result.ok(years);
    }

    /**
     * 获取按年份分组的纪念墙数据
     */
    @GetMapping("/timeline")
    public Result<Map<Integer, List<Memorial>>> getMemorialsGroupByYear() {
        Map<Integer, List<Memorial>> result = memorialService.getMemorialsGroupByYear();
        return Result.ok(result);
    }

    /**
     * 将用户添加到纪念墙
     */
    @PostMapping("/add-user")
    public Result<Memorial> addToMemorial(@RequestParam Long userId,
                                          @RequestParam Integer graduationYear,
                                          @RequestParam(required = false) String message) {
        Memorial memorial = memorialService.addToMemorial(userId, graduationYear, message);
        return Result.ok("添加成功", memorial);
    }
}
