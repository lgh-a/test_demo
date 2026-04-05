package com.testdemo.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.testdemo.common.OperationResult;
import com.testdemo.dto.MenuSaveRequest;
import com.testdemo.entity.SysMenu;
import com.testdemo.mapper.SysMenuMapper;
import com.testdemo.mapper.SysRoleMenuMapper;
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
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SysMenuServiceImplTest {

    @Mock
    private SysMenuMapper sysMenuMapper;

    @Mock
    private SysRoleMenuMapper sysRoleMenuMapper;

    @Mock
    private AuthorizationCacheService authorizationCacheService;

    @InjectMocks
    private SysMenuServiceImpl service;

    @Test
    void shouldRejectSelfParentOnUpdate() {
        SysMenu existing = new SysMenu();
        existing.setId(5);
        when(sysMenuMapper.selectById(5)).thenReturn(existing);

        MenuSaveRequest request = new MenuSaveRequest();
        request.setParentId(5);
        request.setName("Menu");
        request.setType(1);

        OperationResult<Void> result = service.updateMenu(5, request);

        assertThat(result.isSuccess()).isFalse();
        assertThat(result.getCode()).isEqualTo(409);
        assertThat(result.getMessage()).isEqualTo("Menu cannot be its own parent");
        verify(sysMenuMapper, never()).updateById(any());
    }

    @Test
    void shouldRejectDeleteWhenChildMenusExist() {
        SysMenu existing = new SysMenu();
        existing.setId(10);
        when(sysMenuMapper.selectById(10)).thenReturn(existing);
        when(sysMenuMapper.selectCount(any(LambdaQueryWrapper.class))).thenReturn(1L);

        OperationResult<Void> result = service.deleteMenu(10);

        assertThat(result.isSuccess()).isFalse();
        assertThat(result.getCode()).isEqualTo(409);
        assertThat(result.getMessage()).isEqualTo("Menu has child menus and cannot be deleted");
        verify(sysRoleMenuMapper, never()).deleteByMenuId(any());
    }

    @Test
    void shouldInsertNormalizedMenuOnCreate() {
        MenuSaveRequest request = new MenuSaveRequest();
        request.setParentId(null);
        request.setName("  Dashboard  ");
        request.setPerms("  sys:dashboard:view  ");
        request.setType(1);
        request.setSort(null);
        request.setIcon("  home  ");
        request.setPath("  /dashboard  ");

        OperationResult<Void> result = service.createMenu(request);

        assertThat(result.isSuccess()).isTrue();
        ArgumentCaptor<SysMenu> captor = ArgumentCaptor.forClass(SysMenu.class);
        verify(sysMenuMapper).insert(captor.capture());
        SysMenu saved = captor.getValue();
        assertThat(saved.getParentId()).isZero();
        assertThat(saved.getName()).isEqualTo("Dashboard");
        assertThat(saved.getPerms()).isEqualTo("sys:dashboard:view");
        assertThat(saved.getSort()).isZero();
        assertThat(saved.getIcon()).isEqualTo("home");
        assertThat(saved.getPath()).isEqualTo("/dashboard");
    }

    @Test
    void shouldBuildStableTreeWithoutMutatingSourceEntities() {
        SysMenu root = new SysMenu();
        root.setId(1);
        root.setParentId(0);
        root.setName("Root");
        root.setSort(2);

        SysMenu child = new SysMenu();
        child.setId(2);
        child.setParentId(1);
        child.setName("Child");
        child.setSort(1);

        when(sysMenuMapper.selectList(any(LambdaQueryWrapper.class))).thenReturn(List.of(child, root));

        List<SysMenu> tree = service.treeMenus();

        assertThat(tree).hasSize(1);
        assertThat(tree.getFirst().getId()).isEqualTo(1);
        assertThat(tree.getFirst().getChildren()).hasSize(1);
        assertThat(tree.getFirst().getChildren().getFirst().getId()).isEqualTo(2);
        assertThat(root.getChildren()).isNull();
    }
}
