package com.testdemo.controller;

import cn.dev33.satoken.stp.StpUtil;
import com.testdemo.common.Result;
import com.testdemo.entity.SysUser;
import com.testdemo.service.SysUserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
@Tag(name = "认证管理", description = "用户登录、登出、信息查询")
public class AuthController {

    private final SysUserService sysUserService;

    @PostMapping("/login")
    @Operation(summary = "用户登录", description = "使用用户名密码登录，返回token信息")
    @ApiResponse(responseCode = "200", description = "登录成功")
    @ApiResponse(responseCode = "401", description = "用户名或密码错误")
    @ApiResponse(responseCode = "403", description = "账户已被禁用")
    public Result<?> login(@RequestBody Map<String, String> body) {
        String username = body.get("username");
        String password = body.get("password");

        SysUser user = sysUserService.login(username, password);
        if (user == null) {
            return Result.error("Invalid username or password");
        }
        if (user.getStatus() == 0) {
            return Result.error("Account disabled");
        }

        StpUtil.login(user.getId());

        Map<String, Object> data = new HashMap<>();
        data.put("tokenName", StpUtil.getTokenName());
        data.put("tokenValue", StpUtil.getTokenValue());
        data.put("userInfo", user);

        return Result.success(data);
    }

    @PostMapping("/logout")
    @Operation(summary = "用户登出", description = "清除当前登录状态")
    @ApiResponse(responseCode = "200", description = "登出成功")
    public Result<?> logout() {
        StpUtil.logout();
        return Result.success("Logged out successfully");
    }

    @GetMapping("/info")
    @Operation(summary = "获取用户信息", description = "获取当前登录用户的信息、角色和权限")
    @ApiResponse(responseCode = "200", description = "获取成功")
    @ApiResponse(responseCode = "401", description = "未登录或token已过期")
    public Result<?> getInfo() {
        long userId = StpUtil.getLoginIdAsLong();
        SysUser user = sysUserService.getById(userId);

        Map<String, Object> data = new HashMap<>();
        data.put("user", user);
        data.put("roles", StpUtil.getRoleList());
        data.put("permissions", StpUtil.getPermissionList());

        return Result.success(data);
    }
}
