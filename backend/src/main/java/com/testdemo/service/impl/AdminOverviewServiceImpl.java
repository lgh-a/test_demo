package com.testdemo.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.testdemo.entity.RateLimitRule;
import com.testdemo.entity.SysMenu;
import com.testdemo.entity.SysRole;
import com.testdemo.entity.SysUser;
import com.testdemo.mapper.RateLimitRuleMapper;
import com.testdemo.mapper.SysMenuMapper;
import com.testdemo.mapper.SysRoleMapper;
import com.testdemo.mapper.SysUserMapper;
import com.testdemo.service.AdminOverviewService;
import com.testdemo.vo.AdminOverviewVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AdminOverviewServiceImpl implements AdminOverviewService {

    private final SysUserMapper sysUserMapper;
    private final SysRoleMapper sysRoleMapper;
    private final SysMenuMapper sysMenuMapper;
    private final RateLimitRuleMapper rateLimitRuleMapper;

    @Override
    public AdminOverviewVO getOverview() {
        long totalUsers = sysUserMapper.selectCount(null);
        long enabledUsers = sysUserMapper.selectCount(new LambdaQueryWrapper<SysUser>().eq(SysUser::getStatus, 1));
        long totalRoles = sysRoleMapper.selectCount(null);
        long totalMenus = sysMenuMapper.selectCount(null);
        long activeRateLimitRules = rateLimitRuleMapper.selectCount(
                new LambdaQueryWrapper<RateLimitRule>().eq(RateLimitRule::getStatus, 1)
        );

        return new AdminOverviewVO(totalUsers, enabledUsers, totalRoles, totalMenus, activeRateLimitRules);
    }
}
