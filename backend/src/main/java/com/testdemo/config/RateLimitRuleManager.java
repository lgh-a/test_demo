package com.testdemo.config;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.testdemo.entity.RateLimitRule;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.List;

/**
 * 动态限流规则管理器
 * 基于 Redis Hash 结构存储限流规则，支持规则的动态热更新
 * 规则存储在 sys:rate_limit:rules Hash 中，field 为 configKey，value 为 RateLimitRule JSON
 */
@Component
public class RateLimitRuleManager {

    private static final Logger log = LoggerFactory.getLogger(RateLimitRuleManager.class);

    /** Redis Hash 键，存储所有动态限流规则 */
    private static final String RULE_HASH_KEY = "sys:rate_limit:rules";

    private final StringRedisTemplate stringRedisTemplate;
    private final ObjectMapper objectMapper;

    public RateLimitRuleManager(StringRedisTemplate stringRedisTemplate, ObjectMapper objectMapper) {
        this.stringRedisTemplate = stringRedisTemplate;
        this.objectMapper = objectMapper;
    }

    /**
     * 获取动态限流规则
     * @param configKey 规则配置键
     * @return 规则对象，若不存在或异常返回 null
     */
    public RateLimitRule getRule(String configKey) {
        if (!StringUtils.hasText(configKey)) return null;
        try {
            Object ruleJson = stringRedisTemplate.opsForHash().get(RULE_HASH_KEY, configKey);
            if (ruleJson != null) {
                return objectMapper.readValue(ruleJson.toString(), RateLimitRule.class);
            }
        } catch (Exception e) {
            log.error("获取动态限流规则异常, configKey: {}", configKey, e);
        }
        return null;
    }

    /**
     * 保存限流规则（不重置计数）
     * 内部调用 saveRule(configKey, capacity, rate, false)
     */
    public void saveRule(String configKey, Integer capacity, Integer rate) {
        saveRule(configKey, capacity, rate, false);
    }

    /**
     * 保存限流规则，支持选择是否重置计数空间
     */
    public void saveRule(String configKey, Integer capacity, Integer rate, boolean reset) {
        try {
            RateLimitRule existingRule = getRule(configKey);
            Integer newVersion = 1;
            if (existingRule != null) {
                boolean capacityChanged = !java.util.Objects.equals(existingRule.getCapacity(), capacity);
                boolean rateChanged = !java.util.Objects.equals(existingRule.getRate(), rate);
                if (capacityChanged || rateChanged || reset) {
                    if (reset) {
                        newVersion = (existingRule.getVersion() != null ? existingRule.getVersion() : 1) + 1;
                    } else {
                        newVersion = existingRule.getVersion() != null ? existingRule.getVersion() : 1;
                    }
                } else {
                    newVersion = existingRule.getVersion() != null ? existingRule.getVersion() : 1;
                }
            }
            RateLimitRule rule = new RateLimitRule();
            rule.setCapacity(capacity);
            rule.setRate(rate);
            rule.setVersion(newVersion);
            String ruleJson = objectMapper.writeValueAsString(rule);
            stringRedisTemplate.opsForHash().put(RULE_HASH_KEY, configKey, ruleJson);
            log.info("动态限流规则已更新 -> configKey: {}, rule: {}, reset: {}", configKey, ruleJson, reset);
        } catch (JsonProcessingException e) {
            log.error("序列化限流规则失败", e);
        }
    }

    /**
     * 刷新单条规则到缓存
     */
    public void refreshCache(RateLimitRule rule) {
        if (rule == null || !StringUtils.hasText(rule.getConfigKey())) return;
        try {
            String ruleJson = objectMapper.writeValueAsString(rule);
            stringRedisTemplate.opsForHash().put(RULE_HASH_KEY, rule.getConfigKey(), ruleJson);
            log.info("刷新限流规则到缓存 -> configKey: {}, rule: {}", rule.getConfigKey(), ruleJson);
        } catch (JsonProcessingException e) {
            log.error("序列化限流规则失败", e);
        }
    }

    /**
     * 刷新所有规则到缓存
     */
    public void refreshAllCache(List<RateLimitRule> rules) {
        if (rules == null || rules.isEmpty()) {
            log.info("没有启用的限流规则需要刷新");
            return;
        }
        stringRedisTemplate.opsForHash().delete(RULE_HASH_KEY, rules.stream()
                .map(RateLimitRule::getConfigKey)
                .toArray());
        for (RateLimitRule rule : rules) {
            try {
                String ruleJson = objectMapper.writeValueAsString(rule);
                stringRedisTemplate.opsForHash().put(RULE_HASH_KEY, rule.getConfigKey(), ruleJson);
            } catch (JsonProcessingException e) {
                log.error("序列化限流规则失败, configKey: {}", rule.getConfigKey(), e);
            }
        }
        log.info("已刷新 {} 条限流规则到缓存", rules.size());
    }

    /**
     * 清除指定规则的缓存
     */
    public void clearCache(String configKey) {
        if (!StringUtils.hasText(configKey)) return;
        stringRedisTemplate.opsForHash().delete(RULE_HASH_KEY, configKey);
        log.info("已清除限流规则缓存 -> configKey: {}", configKey);
    }
}
