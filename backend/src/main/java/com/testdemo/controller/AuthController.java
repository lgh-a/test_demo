package com.testdemo.controller;

import cn.dev33.satoken.stp.StpUtil;
import cn.dev33.satoken.util.SaResult;
import com.testdemo.common.Result;
import com.testdemo.entity.SysUser;
import com.testdemo.service.SysUserService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class AuthController {

    private final SysUserService sysUserService;

    @PostMapping("/login")
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
    public Result<?> logout() {
        StpUtil.logout();
        return Result.success("Logged out successfully");
    }

    @GetMapping("/info")
    public Result<?> getInfo() {
        if (!StpUtil.isLogin()) {
            return Result.error("Not logged in");
        }
        long userId = StpUtil.getLoginIdAsLong();
        SysUser user = sysUserService.getById(userId);

        Map<String, Object> data = new HashMap<>();
        data.put("user", user);
        data.put("roles", StpUtil.getRoleList());
        data.put("permissions", StpUtil.getPermissionList());

        return Result.success(data);
    }
}
