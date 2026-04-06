package com.testdemo.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.testdemo.annotation.LimitType;
import com.testdemo.common.Result;
import com.testdemo.common.exception.BusinessException;
import com.testdemo.config.RateLimitRuleManager;
import com.testdemo.dto.RateLimitRuleCreateRequest;
import com.testdemo.dto.RateLimitRuleUpdateRequest;
import com.testdemo.entity.RateLimitRule;
import com.testdemo.service.RateLimitRuleService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/rate-limit/rules")
@RequiredArgsConstructor
@Validated
@Tag(name = "Rate Limit Rules", description = "Dynamic rate limit rule management")
public class RateLimitRuleController {

    private final RateLimitRuleService rateLimitRuleService;
    private final RateLimitRuleManager rateLimitRuleManager;

    @GetMapping("/page")
    @SaCheckPermission("rate-limit:rule:list")
    @Operation(summary = "Page rate limit rules")
    public Result<?> page(
            @Parameter(description = "Current page")
            @RequestParam(defaultValue = "1")
            @Min(value = 1, message = "current must be greater than 0")
            int current,
            @Parameter(description = "Page size")
            @RequestParam(defaultValue = "10")
            @Min(value = 1, message = "size must be greater than 0")
            int size,
            @Parameter(description = "Config key keyword")
            @RequestParam(required = false)
            String configKey,
            @Parameter(description = "Status")
            @RequestParam(required = false)
            @Min(value = 0, message = "status must be 0 or 1")
            @Max(value = 1, message = "status must be 0 or 1")
            Integer status) {
        IPage<RateLimitRule> page = rateLimitRuleService.pageRules(current, size, configKey, status);
        return Result.success(page);
    }

    @GetMapping("/{id}")
    @SaCheckPermission("rate-limit:rule:view")
    @Operation(summary = "Get rate limit rule detail")
    public Result<?> getById(@Parameter(description = "Rule ID") @PathVariable Long id) {
        RateLimitRule rule = rateLimitRuleService.getById(id);
        if (rule == null) {
            throw new BusinessException(HttpStatus.NOT_FOUND, "Rule not found");
        }
        return Result.success(rule);
    }

    @PostMapping
    @SaCheckPermission("rate-limit:rule:add")
    @Operation(summary = "Create rate limit rule")
    public Result<?> add(@Valid @RequestBody RateLimitRuleCreateRequest request) {
        LimitType limitType = resolveLimitType(request.getLimitType());
        if (limitType == null) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "Unsupported limitType");
        }
        if (rateLimitRuleService.getByConfigKey(request.getConfigKey().trim()) != null) {
            throw new BusinessException(HttpStatus.CONFLICT, "configKey already exists");
        }
        boolean success = rateLimitRuleService.addRule(toEntity(request, limitType));
        if (!success) {
            throw new BusinessException(HttpStatus.INTERNAL_SERVER_ERROR, "Create failed");
        }
        return Result.success(null);
    }

    @PutMapping
    @SaCheckPermission("rate-limit:rule:edit")
    @Operation(summary = "Update rate limit rule")
    public Result<?> update(@Valid @RequestBody RateLimitRuleUpdateRequest request) {
        LimitType limitType = resolveLimitType(request.getLimitType());
        if (limitType == null) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "Unsupported limitType");
        }
        RateLimitRule existing = rateLimitRuleService.getById(request.getId());
        if (existing == null) {
            throw new BusinessException(HttpStatus.NOT_FOUND, "Rule not found");
        }
        RateLimitRule sameConfigKeyRule = rateLimitRuleService.getByConfigKey(request.getConfigKey().trim());
        if (sameConfigKeyRule != null && !sameConfigKeyRule.getId().equals(request.getId())) {
            throw new BusinessException(HttpStatus.CONFLICT, "configKey already exists");
        }
        RateLimitRule rule = toEntity(request, limitType);
        rule.setId(request.getId());
        rule.setVersion(resolveRuleVersion(existing, request));
        boolean success = rateLimitRuleService.updateRule(rule);
        if (!success) {
            throw new BusinessException(HttpStatus.INTERNAL_SERVER_ERROR, "Update failed");
        }
        return Result.success(null);
    }

    @DeleteMapping("/{id}")
    @SaCheckPermission("rate-limit:rule:delete")
    @Operation(summary = "Delete rate limit rule")
    public Result<?> delete(@Parameter(description = "Rule ID") @PathVariable Long id) {
        boolean success = rateLimitRuleService.deleteRule(id);
        if (!success) {
            throw new BusinessException(HttpStatus.NOT_FOUND, "Delete failed");
        }
        return Result.success(null);
    }

    @PostMapping("/refresh-cache")
    @SaCheckPermission("rate-limit:rule:refresh")
    @Operation(summary = "Refresh all enabled rules to cache")
    public Result<?> refreshCache() {
        List<RateLimitRule> rules = rateLimitRuleService.listEnabled();
        rateLimitRuleManager.refreshAllCache(rules);
        return Result.success("Refreshed " + rules.size() + " rules to cache");
    }

    private RateLimitRule toEntity(RateLimitRuleCreateRequest request, LimitType limitType) {
        RateLimitRule rule = new RateLimitRule();
        rule.setConfigKey(request.getConfigKey().trim());
        rule.setName(trimToNull(request.getName()));
        rule.setDescription(trimToNull(request.getDescription()));
        rule.setLimitType(limitType.name());
        rule.setCapacity(request.getCapacity());
        rule.setRate(request.getRate());
        rule.setGuestCapacity(request.getGuestCapacity());
        rule.setGuestRate(request.getGuestRate());
        rule.setStatus(request.getStatus());
        return rule;
    }

    private String trimToNull(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    private LimitType resolveLimitType(String limitType) {
        return LimitType.resolve(limitType);
    }

    private Integer resolveRuleVersion(RateLimitRule existing, RateLimitRuleUpdateRequest request) {
        Integer currentVersion = existing.getVersion() != null ? existing.getVersion() : 1;
        if (Boolean.TRUE.equals(request.getResetCurrentWindow())) {
            return currentVersion + 1;
        }
        return currentVersion;
    }
}
