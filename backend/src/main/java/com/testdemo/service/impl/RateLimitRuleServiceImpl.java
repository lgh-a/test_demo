package com.testdemo.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.testdemo.config.RateLimitRuleManager;
import com.testdemo.entity.RateLimitRule;
import com.testdemo.mapper.RateLimitRuleMapper;
import com.testdemo.service.RateLimitRuleService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;

@Service
@RequiredArgsConstructor
public class RateLimitRuleServiceImpl implements RateLimitRuleService {
    private final RateLimitRuleMapper rateLimitRuleMapper;
    private final RateLimitRuleManager rateLimitRuleManager;

    @Override
    public IPage<RateLimitRule> pageRules(int current, int size, String configKey, Integer status) {
        Page<RateLimitRule> page = new Page<>(current, size);
        LambdaQueryWrapper<RateLimitRule> queryWrapper = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(configKey)) {
            queryWrapper.like(RateLimitRule::getConfigKey, configKey);
        }
        if (status != null) {
            queryWrapper.eq(RateLimitRule::getStatus, status);
        }
        queryWrapper.orderByDesc(RateLimitRule::getId);
        return rateLimitRuleMapper.selectPage(page, queryWrapper);
    }

    @Override
    public RateLimitRule getByConfigKey(String configKey) {
        LambdaQueryWrapper<RateLimitRule> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(RateLimitRule::getConfigKey, configKey);
        return rateLimitRuleMapper.selectOne(queryWrapper);
    }

    @Override
    public boolean addRule(RateLimitRule rule) {
        int rows = rateLimitRuleMapper.insert(rule);
        if (rows > 0) {
            rateLimitRuleManager.refreshCache(rule);
        }
        return rows > 0;
    }

    @Override
    public boolean updateRule(RateLimitRule rule) {
        int rows = rateLimitRuleMapper.updateById(rule);
        if (rows > 0) {
            rateLimitRuleManager.refreshCache(rule);
        }
        return rows > 0;
    }

    @Override
    public boolean deleteRule(Long id) {
        RateLimitRule rule = rateLimitRuleMapper.selectById(id);
        if (rule != null) {
            rateLimitRuleManager.clearCache(rule.getConfigKey());
        }
        return rateLimitRuleMapper.deleteById(id) > 0;
    }

    @Override
    public RateLimitRule getById(Long id) {
        return rateLimitRuleMapper.selectById(id);
    }

    @Override
    public List<RateLimitRule> listEnabled() {
        LambdaQueryWrapper<RateLimitRule> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(RateLimitRule::getStatus, 1);
        return rateLimitRuleMapper.selectList(queryWrapper);
    }
}
