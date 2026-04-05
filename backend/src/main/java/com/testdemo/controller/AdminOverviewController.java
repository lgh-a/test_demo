package com.testdemo.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import com.testdemo.common.Result;
import com.testdemo.service.AdminOverviewService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/overview")
@RequiredArgsConstructor
@Tag(name = "Admin Overview", description = "Admin dashboard metrics")
public class AdminOverviewController {

    private final AdminOverviewService adminOverviewService;

    @GetMapping
    @SaCheckPermission("sys:user:list")
    @Operation(summary = "Get admin overview metrics")
    public Result<?> overview() {
        return Result.success(adminOverviewService.getOverview());
    }
}
