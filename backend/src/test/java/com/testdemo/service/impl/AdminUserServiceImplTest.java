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
import com.testdemo.service.AuthorizationCacheService;
import com.testdemo.service.SysUserService;
import com.testdemo.vo.AdminUserVO;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AdminUserServiceImplTest {

    @Mock
    private SysUserService sysUserService;

    @Mock
    private SysRoleMapper sysRoleMapper;

    @Mock
    private SysUserRoleMapper sysUserRoleMapper;

    @Mock
    private AuthorizationCacheService authorizationCacheService;

    @InjectMocks
    private AdminUserServiceImpl service;

    @Test
    void shouldMapUsersToAdminViewObjects() {
        SysUser user = new SysUser();
        user.setId(1);
        user.setUsername("admin");
        user.setStatus(1);
        user.setCreateTime(LocalDateTime.now());

        SysUserRole relation = new SysUserRole();
        relation.setUserId(1);
        relation.setRoleId(2);

        SysRole role = new SysRole();
        role.setId(2);
        role.setName("manager");

        when(sysUserService.list(any(LambdaQueryWrapper.class))).thenReturn(List.of(user));
        when(sysUserRoleMapper.selectAll()).thenReturn(List.of(relation));
        when(sysRoleMapper.selectList(null)).thenReturn(List.of(role));

        List<AdminUserVO> result = service.listUsers();

        assertThat(result).hasSize(1);
        assertThat(result.getFirst().getUsername()).isEqualTo("admin");
        assertThat(result.getFirst().getRoleId()).isEqualTo(2);
        assertThat(result.getFirst().getRoleName()).isEqualTo("manager");
    }

    @Test
    void shouldRejectDuplicateUsername() {
        AdminUserCreateRequest request = new AdminUserCreateRequest();
        request.setUsername("admin");
        request.setPassword("123456");
        request.setStatus(1);
        request.setRoleId(1);
        when(sysUserService.count(any(LambdaQueryWrapper.class))).thenReturn(1L);

        OperationResult<Void> result = service.createUser(request);

        assertThat(result.isSuccess()).isFalse();
        assertThat(result.getCode()).isEqualTo(409);
        verify(sysUserService, never()).save(any());
    }

    @Test
    void shouldCreateUserAndRoleRelation() {
        AdminUserCreateRequest request = new AdminUserCreateRequest();
        request.setUsername("  tester  ");
        request.setPassword("123456");
        request.setStatus(1);
        request.setRoleId(3);

        SysRole role = new SysRole();
        role.setId(3);

        when(sysUserService.count(any(LambdaQueryWrapper.class))).thenReturn(0L);
        when(sysRoleMapper.selectById(3)).thenReturn(role);

        OperationResult<Void> result = service.createUser(request);

        assertThat(result.isSuccess()).isTrue();
        ArgumentCaptor<SysUser> userCaptor = ArgumentCaptor.forClass(SysUser.class);
        verify(sysUserService).save(userCaptor.capture());
        assertThat(userCaptor.getValue().getUsername()).isEqualTo("tester");
        verify(sysUserRoleMapper).insertRelation(any(SysUserRole.class));
    }

    @Test
    void shouldUpdateUserRoleAndClearCache() {
        AdminUserUpdateRequest request = new AdminUserUpdateRequest();
        request.setStatus(0);
        request.setRoleId(2);

        SysUser user = new SysUser();
        user.setId(8);
        SysRole role = new SysRole();
        role.setId(2);

        when(sysUserService.getById(8)).thenReturn(user);
        when(sysRoleMapper.selectById(2)).thenReturn(role);

        OperationResult<Void> result = service.updateUser(8, request);

        assertThat(result.isSuccess()).isTrue();
        verify(sysUserRoleMapper).deleteByUserId(8);
        verify(sysUserRoleMapper).insertRelation(any(SysUserRole.class));
        verify(authorizationCacheService).clearUserAuthorization(8);
    }

    @Test
    void shouldResetPasswordForExistingUser() {
        SysUser user = new SysUser();
        user.setId(5);
        when(sysUserService.getById(5)).thenReturn(user);
        when(sysUserService.updatePassword(5, "123456")).thenReturn(true);

        OperationResult<Void> result = service.resetPassword(5);

        assertThat(result.isSuccess()).isTrue();
    }
}
