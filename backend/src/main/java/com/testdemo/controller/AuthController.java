package com.testdemo.controller;

import cn.dev33.satoken.stp.StpUtil;
import com.testdemo.common.Result;
import com.testdemo.common.exception.BusinessException;
import com.testdemo.dto.AuthLoginRequest;
import com.testdemo.dto.ChangePasswordRequest;
import com.testdemo.entity.SysUser;
import com.testdemo.service.SysUserService;
import com.testdemo.vo.AuthLoginResponse;
import com.testdemo.vo.AuthUserInfoResponse;
import com.testdemo.vo.UserInfoVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Tag(name = "Authentication", description = "Login, logout, and current user info")
public class AuthController {

    private final SysUserService sysUserService;

    @PostMapping("/login")
    @Operation(summary = "User login", description = "Login with username and password")
    @ApiResponse(responseCode = "200", description = "Login successful")
    @ApiResponse(responseCode = "401", description = "Invalid username or password")
    @ApiResponse(responseCode = "403", description = "Account disabled")
    public Result<?> login(@Valid @RequestBody AuthLoginRequest request) {
        SysUser user = sysUserService.login(request.getUsername(), request.getPassword());
        if (user == null) {
            throw new BusinessException(HttpStatus.UNAUTHORIZED, "Invalid username or password");
        }
        if (user.getStatus() == 0) {
            throw new BusinessException(HttpStatus.FORBIDDEN, "Account disabled");
        }

        StpUtil.login(user.getId());

        return Result.success(new AuthLoginResponse(
                StpUtil.getTokenName(),
                StpUtil.getTokenValue(),
                UserInfoVO.from(user)
        ));
    }

    @PostMapping("/logout")
    @Operation(summary = "User logout", description = "Clear current login state")
    @ApiResponse(responseCode = "200", description = "Logout successful")
    public Result<?> logout() {
        StpUtil.logout();
        return Result.success("Logged out successfully");
    }

    @GetMapping("/info")
    @Operation(summary = "Get current user info", description = "Get current user, roles, and permissions")
    @ApiResponse(responseCode = "200", description = "Query successful")
    @ApiResponse(responseCode = "401", description = "Not logged in or token expired")
    public Result<?> getInfo() {
        long userId = StpUtil.getLoginIdAsLong();
        SysUser user = sysUserService.getById(userId);

        return Result.success(new AuthUserInfoResponse(
                UserInfoVO.from(user),
                StpUtil.getRoleList(),
                StpUtil.getPermissionList()
        ));
    }

    @PostMapping("/change-password")
    @Operation(summary = "Change password", description = "Change password for the current user")
    @ApiResponse(responseCode = "200", description = "Password updated successfully")
    @ApiResponse(responseCode = "401", description = "Not logged in or token expired")
    public Result<?> changePassword(@Valid @RequestBody ChangePasswordRequest request) {
        Integer userId = StpUtil.getLoginIdAsInt();
        SysUser user = sysUserService.getById(userId);
        if (!sysUserService.matchesPassword(user, request.getOldPassword())) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "Old password is incorrect");
        }
        if (request.getOldPassword().equals(request.getNewPassword())) {
            throw new BusinessException(HttpStatus.CONFLICT, "New password must be different");
        }
        boolean updated = sysUserService.updatePassword(userId, request.getNewPassword());
        if (!updated) {
            throw new BusinessException(HttpStatus.INTERNAL_SERVER_ERROR, "Password update failed");
        }
        return Result.success("Password updated successfully");
    }
}
