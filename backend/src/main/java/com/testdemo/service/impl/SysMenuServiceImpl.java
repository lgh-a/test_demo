package com.testdemo.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.testdemo.common.OperationResult;
import com.testdemo.dto.MenuSaveRequest;
import com.testdemo.entity.SysMenu;
import com.testdemo.mapper.SysMenuMapper;
import com.testdemo.mapper.SysRoleMenuMapper;
import com.testdemo.service.AuthorizationCacheService;
import com.testdemo.service.SysMenuService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SysMenuServiceImpl implements SysMenuService {

    private static final Logger log = LoggerFactory.getLogger(SysMenuServiceImpl.class);

    private final SysMenuMapper sysMenuMapper;
    private final SysRoleMenuMapper sysRoleMenuMapper;
    private final AuthorizationCacheService authorizationCacheService;

    @Override
    public List<SysMenu> listMenus() {
        return sysMenuMapper.selectList(new LambdaQueryWrapper<SysMenu>().orderByAsc(SysMenu::getSort, SysMenu::getId));
    }

    @Override
    public IPage<SysMenu> pageMenus(int current, int size, String keyword) {
        LambdaQueryWrapper<SysMenu> queryWrapper = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(keyword)) {
            String normalizedKeyword = keyword.trim();
            queryWrapper.and(wrapper -> wrapper.like(SysMenu::getName, normalizedKeyword)
                    .or()
                    .like(SysMenu::getPerms, normalizedKeyword)
                    .or()
                    .like(SysMenu::getPath, normalizedKeyword));
        }
        queryWrapper.orderByAsc(SysMenu::getSort).orderByAsc(SysMenu::getId);
        return sysMenuMapper.selectPage(new Page<>(Math.max(current, 1), Math.max(size, 1)), queryWrapper);
    }

    @Override
    public List<SysMenu> treeMenus() {
        List<SysMenu> allMenus = listMenus();
        Map<Integer, List<SysMenu>> childrenMap = allMenus.stream()
                .collect(Collectors.groupingBy(menu -> menu.getParentId() == null ? 0 : menu.getParentId()));
        return buildMenuTree(childrenMap, 0);
    }

    @Override
    @Transactional
    public OperationResult<Void> createMenu(MenuSaveRequest request) {
        Integer parentId = normalizeParentId(request.getParentId());
        if (!parentExists(parentId)) {
            log.warn("Rejected menu creation because parent was not found: parentId={}, name={}", parentId, request.getName());
            return OperationResult.failure(404, "Parent menu not found");
        }

        SysMenu menu = new SysMenu();
        applyRequest(menu, request);
        menu.setParentId(parentId);
        sysMenuMapper.insert(menu);
        log.info("Created menu: menuId={}, parentId={}, name={}, type={}", menu.getId(), parentId, menu.getName(), menu.getType());
        return OperationResult.success();
    }

    @Override
    @Transactional
    public OperationResult<Void> updateMenu(Integer id, MenuSaveRequest request) {
        SysMenu existing = sysMenuMapper.selectById(id);
        if (existing == null) {
            log.warn("Rejected menu update because menu was not found: menuId={}", id);
            return OperationResult.failure(404, "Menu not found");
        }

        Integer parentId = normalizeParentId(request.getParentId());
        if (id.equals(parentId)) {
            log.warn("Rejected menu update because menu cannot be its own parent: menuId={}", id);
            return OperationResult.failure(409, "Menu cannot be its own parent");
        }
        if (!parentExists(parentId)) {
            log.warn("Rejected menu update because parent was not found: menuId={}, parentId={}", id, parentId);
            return OperationResult.failure(404, "Parent menu not found");
        }
        if (isDescendantParent(id, parentId)) {
            log.warn("Rejected menu update because parent is a child of the menu: menuId={}, parentId={}", id, parentId);
            return OperationResult.failure(409, "Parent menu cannot be a child of the current menu");
        }

        SysMenu menu = new SysMenu();
        menu.setId(id);
        applyRequest(menu, request);
        menu.setParentId(parentId);
        sysMenuMapper.updateById(menu);

        List<Integer> affectedRoleIds = getRoleIdsByMenuId(id);
        authorizationCacheService.clearRolesAuthorization(affectedRoleIds);
        log.info("Updated menu: menuId={}, parentId={}, affectedRoles={}", id, parentId, affectedRoleIds.size());
        return OperationResult.success();
    }

    @Override
    @Transactional
    public OperationResult<Void> deleteMenu(Integer id) {
        SysMenu existing = sysMenuMapper.selectById(id);
        if (existing == null) {
            log.warn("Rejected menu deletion because menu was not found: menuId={}", id);
            return OperationResult.failure(404, "Menu not found");
        }

        Long childCount = sysMenuMapper.selectCount(
                new LambdaQueryWrapper<SysMenu>().eq(SysMenu::getParentId, id));
        if (childCount > 0) {
            log.warn("Rejected menu deletion because child menus still exist: menuId={}, childCount={}", id, childCount);
            return OperationResult.failure(409, "Menu has child menus and cannot be deleted");
        }

        List<Integer> affectedRoleIds = getRoleIdsByMenuId(id);
        sysRoleMenuMapper.deleteByMenuId(id);
        sysMenuMapper.deleteById(id);
        authorizationCacheService.clearRolesAuthorization(affectedRoleIds);
        log.info("Deleted menu: menuId={}, affectedRoles={}", id, affectedRoleIds.size());
        return OperationResult.success();
    }

    private Integer normalizeParentId(Integer parentId) {
        return parentId == null ? 0 : parentId;
    }

    private boolean parentExists(Integer parentId) {
        return parentId == null || parentId == 0 || sysMenuMapper.selectById(parentId) != null;
    }

    private boolean isDescendantParent(Integer menuId, Integer parentId) {
        Integer currentParentId = parentId;
        while (currentParentId != null && currentParentId != 0) {
            if (menuId.equals(currentParentId)) {
                return true;
            }
            SysMenu parent = sysMenuMapper.selectById(currentParentId);
            if (parent == null) {
                return false;
            }
            currentParentId = parent.getParentId();
        }
        return false;
    }

    private void applyRequest(SysMenu menu, MenuSaveRequest request) {
        menu.setName(request.getName().trim());
        menu.setPerms(StringUtils.hasText(request.getPerms()) ? request.getPerms().trim() : null);
        menu.setType(request.getType());
        menu.setSort(request.getSort() == null ? 0 : request.getSort());
        menu.setIcon(StringUtils.hasText(request.getIcon()) ? request.getIcon().trim() : null);
        menu.setPath(StringUtils.hasText(request.getPath()) ? request.getPath().trim() : null);
    }

    private List<Integer> getRoleIdsByMenuId(Integer menuId) {
        return sysRoleMenuMapper.selectByMenuId(menuId)
                .stream()
                .map(com.testdemo.entity.SysRoleMenu::getRoleId)
                .distinct()
                .toList();
    }

    private List<SysMenu> buildMenuTree(Map<Integer, List<SysMenu>> childrenMap, Integer parentId) {
        List<SysMenu> children = childrenMap.getOrDefault(parentId, List.of());
        List<SysMenu> result = new ArrayList<>(children.size());
        for (SysMenu source : children.stream().sorted(menuComparator()).toList()) {
            SysMenu menu = copyMenu(source);
            List<SysMenu> nestedChildren = buildMenuTree(childrenMap, menu.getId());
            menu.setChildren(nestedChildren.isEmpty() ? null : nestedChildren);
            result.add(menu);
        }
        return result;
    }

    private Comparator<SysMenu> menuComparator() {
        return Comparator.comparing(SysMenu::getSort, Comparator.nullsFirst(Integer::compareTo))
                .thenComparing(SysMenu::getId, Comparator.nullsFirst(Integer::compareTo));
    }

    private SysMenu copyMenu(SysMenu source) {
        SysMenu target = new SysMenu();
        target.setId(source.getId());
        target.setParentId(source.getParentId());
        target.setName(source.getName());
        target.setPerms(source.getPerms());
        target.setType(source.getType());
        target.setSort(source.getSort());
        target.setIcon(source.getIcon());
        target.setPath(source.getPath());
        return target;
    }
}
