package com.testdemo.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.testdemo.common.OperationResult;
import com.testdemo.dto.AdminUserCreateRequest;
import com.testdemo.dto.AdminUserUpdateRequest;
import com.testdemo.entity.SysRole;
import com.testdemo.entity.SysUser;
import com.testdemo.entity.SysUserRole;
import com.testdemo.mapper.SysRoleMapper;
import com.testdemo.mapper.SysUserRoleMapper;
import com.testdemo.service.AdminUserService;
import com.testdemo.service.AuthorizationCacheService;
import com.testdemo.service.SysUserService;
import com.testdemo.vo.AdminUserVO;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AdminUserServiceImpl implements AdminUserService {

    private static final Logger log = LoggerFactory.getLogger(AdminUserServiceImpl.class);

    private final SysUserService sysUserService;
    private final SysRoleMapper sysRoleMapper;
    private final SysUserRoleMapper sysUserRoleMapper;
    private final AuthorizationCacheService authorizationCacheService;

    @Override
    public List<AdminUserVO> listUsers() {
        List<SysUser> users = sysUserService.list(new LambdaQueryWrapper<SysUser>().orderByDesc(SysUser::getId));
        List<SysUserRole> userRoles = sysUserRoleMapper.selectAll();
        Map<Integer, Integer> roleIdByUserId = userRoles.stream()
                .collect(Collectors.toMap(SysUserRole::getUserId, SysUserRole::getRoleId, (a, b) -> a));
        Map<Integer, SysRole> roleMap = sysRoleMapper.selectList(null).stream()
                .collect(Collectors.toMap(SysRole::getId, Function.identity()));

        return users.stream()
                .map(user -> {
                    Integer roleId = roleIdByUserId.get(user.getId());
                    SysRole role = roleId != null ? roleMap.get(roleId) : null;
                    return new AdminUserVO(
                            user.getId(),
                            user.getUsername(),
                            user.getStatus(),
                            user.getCreateTime(),
                            roleId,
                            role != null ? role.getName() : null
                    );
                })
                .toList();
    }

    @Override
    @Transactional
    public OperationResult<Void> createUser(AdminUserCreateRequest request) {
        long existing = sysUserService.count(new LambdaQueryWrapper<SysUser>()
                .eq(SysUser::getUsername, request.getUsername()));
        if (existing > 0) {
            log.warn("Rejected user creation because username already exists: {}", request.getUsername());
            return OperationResult.failure(409, "Username already exists");
        }

        if (sysRoleMapper.selectById(request.getRoleId()) == null) {
            log.warn("Rejected user creation because role was not found: roleId={}", request.getRoleId());
            return OperationResult.failure(404, "Role not found");
        }

        SysUser user = new SysUser();
        user.setUsername(request.getUsername().trim());
        user.setPassword(request.getPassword());
        user.setStatus(request.getStatus());
        sysUserService.save(user);

        SysUserRole relation = new SysUserRole();
        relation.setUserId(user.getId());
        relation.setRoleId(request.getRoleId());
        sysUserRoleMapper.insertRelation(relation);
        log.info("Created admin user: userId={}, username={}, roleId={}, status={}",
                user.getId(), user.getUsername(), request.getRoleId(), request.getStatus());
        return OperationResult.success();
    }

    @Override
    @Transactional
    public OperationResult<Void> updateUser(Integer userId, AdminUserUpdateRequest request) {
        if (sysUserService.getById(userId) == null) {
            log.warn("Rejected user update because user was not found: userId={}", userId);
            return OperationResult.failure(404, "User not found");
        }
        if (sysRoleMapper.selectById(request.getRoleId()) == null) {
            log.warn("Rejected user update because role was not found: userId={}, roleId={}", userId, request.getRoleId());
            return OperationResult.failure(404, "Role not found");
        }

        SysUser user = new SysUser();
        user.setId(userId);
        user.setStatus(request.getStatus());
        sysUserService.updateById(user);

        sysUserRoleMapper.deleteByUserId(userId);
        SysUserRole relation = new SysUserRole();
        relation.setUserId(userId);
        relation.setRoleId(request.getRoleId());
        sysUserRoleMapper.insertRelation(relation);
        authorizationCacheService.clearUserAuthorization(userId);
        log.info("Updated admin user: userId={}, roleId={}, status={}", userId, request.getRoleId(), request.getStatus());
        return OperationResult.success();
    }

    @Override
    @Transactional
    public OperationResult<Void> resetPassword(Integer userId) {
        if (sysUserService.getById(userId) == null) {
            log.warn("Rejected password reset because user was not found: userId={}", userId);
            return OperationResult.failure(404, "User not found");
        }
        if (!sysUserService.updatePassword(userId, "123456")) {
            log.error("Failed to reset password for userId={}", userId);
            return OperationResult.failure(500, "Password reset failed");
        }
        log.info("Reset password for userId={}", userId);
        return OperationResult.success();
    }
}
