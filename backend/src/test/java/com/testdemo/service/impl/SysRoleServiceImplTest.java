package com.testdemo.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.testdemo.common.OperationResult;
import com.testdemo.dto.RoleCreateRequest;
import com.testdemo.entity.SysMenu;
import com.testdemo.entity.SysRole;
import com.testdemo.entity.SysRoleMenu;
import com.testdemo.entity.SysUserRole;
import com.testdemo.mapper.SysMenuMapper;
import com.testdemo.mapper.SysRoleMapper;
import com.testdemo.mapper.SysRoleMenuMapper;
import com.testdemo.mapper.SysUserRoleMapper;
import com.testdemo.service.AuthorizationCacheService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SysRoleServiceImplTest {

    @Mock
    private SysRoleMapper sysRoleMapper;

    @Mock
    private SysRoleMenuMapper sysRoleMenuMapper;

    @Mock
    private SysUserRoleMapper sysUserRoleMapper;

    @Mock
    private SysMenuMapper sysMenuMapper;

    @Mock
    private AuthorizationCacheService authorizationCacheService;

    @InjectMocks
    private SysRoleServiceImpl service;

    @Test
    void shouldRejectDuplicateRoleName() {
        RoleCreateRequest request = new RoleCreateRequest();
        request.setName(" admin ");
        when(sysRoleMapper.selectCount(any(LambdaQueryWrapper.class))).thenReturn(1L);

        OperationResult<Void> result = service.createRole(request);

        assertThat(result.isSuccess()).isFalse();
        assertThat(result.getCode()).isEqualTo(409);
        assertThat(result.getMessage()).isEqualTo("Role name already exists");
        verify(sysRoleMapper, never()).insert(any());
    }

    @Test
    void shouldTrimAndInsertRole() {
        RoleCreateRequest request = new RoleCreateRequest();
        request.setName(" admin ");
        request.setRemark(" test role ");
        when(sysRoleMapper.selectCount(any(LambdaQueryWrapper.class))).thenReturn(0L);

        OperationResult<Void> result = service.createRole(request);

        assertThat(result.isSuccess()).isTrue();
        ArgumentCaptor<SysRole> captor = ArgumentCaptor.forClass(SysRole.class);
        verify(sysRoleMapper).insert(captor.capture());
        assertThat(captor.getValue().getName()).isEqualTo("admin");
        assertThat(captor.getValue().getRemark()).isEqualTo("test role");
    }

    @Test
    void shouldReplaceRoleMenusAndClearCache() {
        SysRole role = new SysRole();
        role.setId(7);
        when(sysRoleMapper.selectById(7)).thenReturn(role);
        when(sysMenuMapper.selectCount(any(LambdaQueryWrapper.class))).thenReturn(2L);

        OperationResult<Void> result = service.assignRoleMenus(7, List.of(1, 1, 2));

        assertThat(result.isSuccess()).isTrue();
        verify(sysRoleMenuMapper).deleteByRoleId(7);
        verify(sysRoleMenuMapper, times(2)).insertRelation(any(SysRoleMenu.class));
        verify(authorizationCacheService).clearRoleAuthorization(7);
    }

    @Test
    void shouldDeleteRoleRelationsAndClearAffectedUsers() {
        SysRole role = new SysRole();
        role.setId(9);
        SysUserRole relation = new SysUserRole();
        relation.setUserId(11);
        relation.setRoleId(9);

        when(sysRoleMapper.selectById(9)).thenReturn(role);
        when(sysUserRoleMapper.selectByRoleId(9)).thenReturn(List.of(relation));

        OperationResult<Void> result = service.deleteRole(9);

        assertThat(result.isSuccess()).isTrue();
        verify(sysRoleMenuMapper).deleteByRoleId(9);
        verify(sysUserRoleMapper).deleteByRoleId(9);
        verify(sysRoleMapper).deleteById(9);
        verify(authorizationCacheService).clearUsersAuthorization(List.of(11));
    }
}
