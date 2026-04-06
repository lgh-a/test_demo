package com.testdemo.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.testdemo.config.RateLimitRuleManager;
import com.testdemo.entity.RateLimitRule;
import com.testdemo.mapper.RateLimitRuleMapper;
import com.testdemo.service.RateLimitRuleService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;

@Service
@RequiredArgsConstructor
public class RateLimitRuleServiceImpl extends ServiceImpl<RateLimitRuleMapper, RateLimitRule> implements RateLimitRuleService {
    private static final Logger log = LoggerFactory.getLogger(RateLimitRuleServiceImpl.class);

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
        boolean saved = rateLimitRuleMapper.insert(rule) > 0;
        if (saved) {
            syncRuleCache(rule, null);
            log.info("Created rate limit rule: ruleId={}, configKey={}, status={}",
                    rule.getId(), rule.getConfigKey(), rule.getStatus());
        } else {
            log.warn("Failed to create rate limit rule: configKey={}", rule.getConfigKey());
        }
        return saved;
    }

    @Override
    public boolean updateRule(RateLimitRule rule) {
        RateLimitRule existingRule = rateLimitRuleMapper.selectById(rule.getId());
        if (existingRule == null) {
            log.warn("Rejected rate limit rule update because rule was not found: ruleId={}", rule.getId());
            return false;
        }
        LambdaUpdateWrapper<RateLimitRule> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.eq(RateLimitRule::getId, rule.getId())
                .set(RateLimitRule::getConfigKey, rule.getConfigKey())
                .set(RateLimitRule::getName, rule.getName())
                .set(RateLimitRule::getDescription, rule.getDescription())
                .set(RateLimitRule::getLimitType, rule.getLimitType())
                .set(RateLimitRule::getCapacity, rule.getCapacity())
                .set(RateLimitRule::getRate, rule.getRate())
                .set(RateLimitRule::getGuestCapacity, rule.getGuestCapacity())
                .set(RateLimitRule::getGuestRate, rule.getGuestRate())
                .set(RateLimitRule::getStatus, rule.getStatus())
                .set(rule.getVersion() != null, RateLimitRule::getVersion, rule.getVersion());
        boolean updated = rateLimitRuleMapper.update(null, updateWrapper) > 0;
        if (updated) {
            syncRuleCache(rule, existingRule.getConfigKey());
            log.info("Updated rate limit rule: ruleId={}, configKey={}, previousConfigKey={}, status={}",
                    rule.getId(), rule.getConfigKey(), existingRule.getConfigKey(), rule.getStatus());
        } else {
            log.warn("Failed to update rate limit rule: ruleId={}, configKey={}", rule.getId(), rule.getConfigKey());
        }
        return updated;
    }

    @Override
    public boolean deleteRule(Long id) {
        RateLimitRule rule = rateLimitRuleMapper.selectById(id);
        if (rule != null) {
            rateLimitRuleManager.clearCache(rule.getConfigKey());
            log.info("Deleting rate limit rule: ruleId={}, configKey={}", id, rule.getConfigKey());
        } else {
            log.warn("Rejected rate limit rule deletion because rule was not found: ruleId={}", id);
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

    @Override
    public boolean syncAnnotatedRule(RateLimitRule rule) {
        if (rule == null || !StringUtils.hasText(rule.getConfigKey())) {
            return false;
        }

        String configKey = rule.getConfigKey().trim();
        RateLimitRule existingRule = getByConfigKey(configKey);
        if (existingRule != null) {
            log.info("Skipped annotated rate limit rule sync because configKey already exists: configKey={}, ruleId={}",
                    configKey, existingRule.getId());
            return false;
        }

        rule.setId(null);
        rule.setConfigKey(configKey);
        if (rule.getStatus() == null) {
            rule.setStatus(1);
        }
        if (rule.getVersion() == null) {
            rule.setVersion(1);
        }

        boolean saved = rateLimitRuleMapper.insert(rule) > 0;
        if (saved) {
            syncRuleCache(rule, null);
            log.info("Synced annotated rate limit rule into database: configKey={}, ruleId={}",
                    rule.getConfigKey(), rule.getId());
            return true;
        }

        log.warn("Failed to sync annotated rate limit rule into database: configKey={}", rule.getConfigKey());
        return false;
    }

    private void syncRuleCache(RateLimitRule rule, String previousConfigKey) {
        if (StringUtils.hasText(previousConfigKey) && !previousConfigKey.equals(rule.getConfigKey())) {
            rateLimitRuleManager.clearCache(previousConfigKey);
        }
        if (rule.getStatus() != null && rule.getStatus() == 1) {
            rateLimitRuleManager.refreshCache(rule);
        } else {
            rateLimitRuleManager.clearCache(rule.getConfigKey());
        }
    }
}
