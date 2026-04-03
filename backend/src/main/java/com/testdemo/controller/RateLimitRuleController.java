package com.testdemo.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.testdemo.common.Result;
import com.testdemo.config.RateLimitRuleManager;
import com.testdemo.entity.RateLimitRule;
import com.testdemo.service.RateLimitRuleService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/rate-limit/rules")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
@Tag(name = "限流规则管理", description = "动态限流规则的增删改查")
public class RateLimitRuleController {
    private final RateLimitRuleService rateLimitRuleService;
    private final RateLimitRuleManager rateLimitRuleManager;

    @GetMapping("/page")
    @SaCheckPermission("rate-limit:rule:list")
    @Operation(summary = "分页查询限流规则")
    public Result<?> page(
            @Parameter(description = "当前页") @RequestParam(defaultValue = "1") int current,
            @Parameter(description = "每页大小") @RequestParam(defaultValue = "10") int size,
            @Parameter(description = "配置键关键词") @RequestParam(required = false) String configKey,
            @Parameter(description = "状态") @RequestParam(required = false) Integer status) {
        IPage<RateLimitRule> page = rateLimitRuleService.pageRules(current, size, configKey, status);
        return Result.success(page);
    }

    @GetMapping("/{id}")
    @SaCheckPermission("rate-limit:rule:view")
    @Operation(summary = "查询规则详情")
    public Result<?> getById(@Parameter(description = "规则ID") @PathVariable Long id) {
        RateLimitRule rule = rateLimitRuleService.getById(id);
        return rule != null ? Result.success(rule) : Result.error("规则不存在");
    }

    @PostMapping
    @SaCheckPermission("rate-limit:rule:add")
    @Operation(summary = "新增限流规则")
    public Result<?> add(@RequestBody RateLimitRule rule) {
        boolean success = rateLimitRuleService.addRule(rule);
        return success ? Result.success(null) : Result.error("新增失败");
    }

    @PutMapping
    @SaCheckPermission("rate-limit:rule:edit")
    @Operation(summary = "更新限流规则")
    public Result<?> update(@RequestBody RateLimitRule rule) {
        if (rule.getId() == null) {
            return Result.error("规则ID不能为空");
        }
        boolean success = rateLimitRuleService.updateRule(rule);
        return success ? Result.success(null) : Result.error("更新失败");
    }

    @DeleteMapping("/{id}")
    @SaCheckPermission("rate-limit:rule:delete")
    @Operation(summary = "删除限流规则")
    public Result<?> delete(@Parameter(description = "规则ID") @PathVariable Long id) {
        boolean success = rateLimitRuleService.deleteRule(id);
        return success ? Result.success(null) : Result.error("删除失败");
    }

    @PostMapping("/refresh-cache")
    @SaCheckPermission("rate-limit:rule:refresh")
    @Operation(summary = "刷新所有规则到缓存")
    public Result<?> refreshCache() {
        List<RateLimitRule> rules = rateLimitRuleService.listEnabled();
        rateLimitRuleManager.refreshAllCache(rules);
        return Result.success("已刷新 " + rules.size() + " 条规则到缓存");
    }
}
