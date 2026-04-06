package com.testdemo.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
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
import com.testdemo.service.SysRoleService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class SysRoleServiceImpl extends ServiceImpl<SysRoleMapper, SysRole> implements SysRoleService {

    private static final Logger log = LoggerFactory.getLogger(SysRoleServiceImpl.class);

    private final SysRoleMapper sysRoleMapper;
    private final SysRoleMenuMapper sysRoleMenuMapper;
    private final SysUserRoleMapper sysUserRoleMapper;
    private final SysMenuMapper sysMenuMapper;
    private final AuthorizationCacheService authorizationCacheService;

    @Override
    public List<SysRole> listRoles() {
        return sysRoleMapper.selectList(new LambdaQueryWrapper<SysRole>().orderByAsc(SysRole::getId));
    }

    @Override
    public IPage<SysRole> pageRoles(int current, int size, String keyword) {
        LambdaQueryWrapper<SysRole> queryWrapper = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(keyword)) {
            String normalizedKeyword = keyword.trim();
            queryWrapper.and(wrapper -> wrapper.like(SysRole::getName, normalizedKeyword)
                    .or()
                    .like(SysRole::getRemark, normalizedKeyword));
        }
        queryWrapper.orderByAsc(SysRole::getId);
        return sysRoleMapper.selectPage(new Page<>(Math.max(current, 1), Math.max(size, 1)), queryWrapper);
    }

    @Override
    @Transactional
    public OperationResult<Void> createRole(RoleCreateRequest request) {
        long existing = sysRoleMapper.selectCount(new LambdaQueryWrapper<SysRole>().eq(SysRole::getName, request.getName().trim()));
        if (existing > 0) {
            log.warn("Rejected role creation because name already exists: {}", request.getName());
            return OperationResult.failure(409, "Role name already exists");
        }

        SysRole role = new SysRole();
        role.setName(request.getName().trim());
        role.setRemark(StringUtils.hasText(request.getRemark()) ? request.getRemark().trim() : null);
        sysRoleMapper.insert(role);
        log.info("Created role: roleId={}, roleName={}", role.getId(), role.getName());
        return OperationResult.success();
    }

    @Override
    @Transactional
    public OperationResult<Void> deleteRole(Integer id) {
        SysRole role = sysRoleMapper.selectById(id);
        if (role == null) {
            log.warn("Rejected role deletion because role was not found: roleId={}", id);
            return OperationResult.failure(404, "Role not found");
        }

        List<Integer> affectedUserIds = sysUserRoleMapper.selectByRoleId(id)
                .stream()
                .map(SysUserRole::getUserId)
                .distinct()
                .toList();

        sysRoleMenuMapper.deleteByRoleId(id);
        sysUserRoleMapper.deleteByRoleId(id);
        sysRoleMapper.deleteById(id);

        authorizationCacheService.clearUsersAuthorization(affectedUserIds);
        log.info("Deleted role: roleId={}, affectedUsers={}", id, affectedUserIds.size());
        return OperationResult.success();
    }

    @Override
    public List<Integer> getRoleMenuIds(Integer roleId) {
        return sysRoleMenuMapper.selectByRoleId(roleId)
                .stream()
                .map(SysRoleMenu::getMenuId)
                .toList();
    }

    @Override
    @Transactional
    public OperationResult<Void> assignRoleMenus(Integer roleId, List<Integer> menuIds) {
        if (sysRoleMapper.selectById(roleId) == null) {
            log.warn("Rejected role-menu assignment because role was not found: roleId={}", roleId);
            return OperationResult.failure(404, "Role not found");
        }

        Set<Integer> distinctMenuIds = menuIds == null ? Set.of() : menuIds.stream()
                .filter(java.util.Objects::nonNull)
                .collect(java.util.stream.Collectors.toCollection(LinkedHashSet::new));

        if (!distinctMenuIds.isEmpty()) {
            Long validMenuCount = sysMenuMapper.selectCount(new LambdaQueryWrapper<SysMenu>().in(SysMenu::getId, distinctMenuIds));
            if (validMenuCount == null || validMenuCount != distinctMenuIds.size()) {
                log.warn("Rejected role-menu assignment because some menus were not found: roleId={}, menuIds={}",
                        roleId, distinctMenuIds);
                return OperationResult.failure(404, "One or more menus do not exist");
            }
        }

        sysRoleMenuMapper.deleteByRoleId(roleId);
        for (Integer menuId : distinctMenuIds) {
            SysRoleMenu relation = new SysRoleMenu();
            relation.setRoleId(roleId);
            relation.setMenuId(menuId);
            sysRoleMenuMapper.insertRelation(relation);
        }

        authorizationCacheService.clearRoleAuthorization(roleId);
        log.info("Replaced role menus: roleId={}, menuCount={}", roleId, distinctMenuIds.size());
        return OperationResult.success();
    }
}
