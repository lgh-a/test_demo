package com.testdemo.service.impl;

import cn.dev33.satoken.session.SaSession;
import cn.dev33.satoken.stp.StpUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.testdemo.entity.SysUserRole;
import com.testdemo.mapper.SysUserRoleMapper;
import com.testdemo.service.AuthorizationCacheService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class AuthorizationCacheServiceImpl implements AuthorizationCacheService {

    private static final String PERMISSION_LIST_KEY = "Permission_List";
    private static final String ROLE_LIST_KEY = "Role_List";

    private final SysUserRoleMapper sysUserRoleMapper;

    @Override
    public void clearUserAuthorization(Integer userId) {
        if (userId == null) {
            return;
        }
        SaSession session = StpUtil.getSessionByLoginId(userId, false);
        if (session == null) {
            return;
        }
        session.delete(PERMISSION_LIST_KEY);
        session.delete(ROLE_LIST_KEY);
    }

    @Override
    public void clearUsersAuthorization(Collection<Integer> userIds) {
        if (userIds == null || userIds.isEmpty()) {
            return;
        }
        userIds.stream()
                .filter(java.util.Objects::nonNull)
                .distinct()
                .forEach(this::clearUserAuthorization);
    }

    @Override
    public void clearRoleAuthorization(Integer roleId) {
        if (roleId == null) {
            return;
        }
        clearRolesAuthorization(List.of(roleId));
    }

    @Override
    public void clearRolesAuthorization(Collection<Integer> roleIds) {
        if (roleIds == null || roleIds.isEmpty()) {
            return;
        }
        Set<Integer> distinctRoleIds = roleIds.stream()
                .filter(java.util.Objects::nonNull)
                .collect(java.util.stream.Collectors.toCollection(LinkedHashSet::new));
        if (distinctRoleIds.isEmpty()) {
            return;
        }

        List<Integer> userIds = distinctRoleIds.stream()
                .flatMap(roleId -> sysUserRoleMapper.selectList(
                                new LambdaQueryWrapper<SysUserRole>().eq(SysUserRole::getRoleId, roleId))
                        .stream())
                .map(SysUserRole::getUserId)
                .distinct()
                .toList();
        clearUsersAuthorization(userIds);
    }
}
