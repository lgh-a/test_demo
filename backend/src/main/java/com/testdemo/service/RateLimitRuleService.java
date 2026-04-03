package com.testdemo.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.testdemo.entity.RateLimitRule;

import java.util.List;

public interface RateLimitRuleService {

    /**
     * 分页查询规则列表
     */
    IPage<RateLimitRule> pageRules(int current, int size, String configKey, Integer status);

    /**
     * 根据 configKey 查询规则
     */
    RateLimitRule getByConfigKey(String configKey);

    /**
     * 新增规则
     */
    boolean addRule(RateLimitRule rule);

    /**
     * 更新规则
     */
    boolean updateRule(RateLimitRule rule);

    /**
     * 删除规则
     */
    boolean deleteRule(Long id);

    /**
     * 根据 ID 查询规则
     */
    RateLimitRule getById(Long id);

    /**
     * 获取启用的规则列表（供缓存预热）
     */
    List<RateLimitRule> listEnabled();
}
