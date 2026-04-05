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
import java.util.Objects;

/**
 * Manages dynamic rate-limit rules stored in Redis.
 */
@Component
public class RateLimitRuleManager {

    private static final Logger log = LoggerFactory.getLogger(RateLimitRuleManager.class);

    /** Redis hash key containing all dynamic rate-limit rules. */
    private static final String RULE_HASH_KEY = "sys:rate_limit:rules";

    private final StringRedisTemplate stringRedisTemplate;
    private final ObjectMapper objectMapper;

    public RateLimitRuleManager(StringRedisTemplate stringRedisTemplate, ObjectMapper objectMapper) {
        this.stringRedisTemplate = stringRedisTemplate;
        this.objectMapper = objectMapper;
    }

    /**
     * Returns a dynamic rate-limit rule by config key, or null when missing.
     */
    public RateLimitRule getRule(String configKey) {
        if (!StringUtils.hasText(configKey)) {
            return null;
        }
        try {
            Object ruleJson = stringRedisTemplate.opsForHash().get(RULE_HASH_KEY, configKey);
            if (ruleJson != null) {
                return objectMapper.readValue(ruleJson.toString(), RateLimitRule.class);
            }
        } catch (Exception e) {
            log.error("Failed to load rate limit rule from cache: configKey={}", configKey, e);
        }
        return null;
    }

    /**
     * Saves a rule without resetting its counters.
     */
    public void saveRule(String configKey, Integer capacity, Integer rate) {
        saveRule(configKey, capacity, rate, false);
    }

    /**
     * Saves a rule and optionally resets its counter namespace.
     */
    public void saveRule(String configKey, Integer capacity, Integer rate, boolean reset) {
        try {
            RateLimitRule existingRule = getRule(configKey);
            Integer newVersion = 1;
            if (existingRule != null) {
                boolean capacityChanged = !Objects.equals(existingRule.getCapacity(), capacity);
                boolean rateChanged = !Objects.equals(existingRule.getRate(), rate);
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
            log.info("Updated dynamic rate limit rule: configKey={}, reset={}, rule={}", configKey, reset, ruleJson);
        } catch (JsonProcessingException e) {
            log.error("Failed to serialize rate limit rule", e);
        }
    }

    /**
     * Refreshes a single rule in cache.
     */
    public void refreshCache(RateLimitRule rule) {
        if (rule == null || !StringUtils.hasText(rule.getConfigKey())) {
            return;
        }
        try {
            String ruleJson = objectMapper.writeValueAsString(rule);
            stringRedisTemplate.opsForHash().put(RULE_HASH_KEY, rule.getConfigKey(), ruleJson);
            log.info("Refreshed rate limit rule cache: configKey={}, rule={}", rule.getConfigKey(), ruleJson);
        } catch (JsonProcessingException e) {
            log.error("Failed to serialize rate limit rule", e);
        }
    }

    /**
     * Refreshes all enabled rules in cache.
     */
    public void refreshAllCache(List<RateLimitRule> rules) {
        if (rules == null || rules.isEmpty()) {
            log.info("No enabled rate limit rules to refresh");
            return;
        }

        stringRedisTemplate.opsForHash().delete(
                RULE_HASH_KEY,
                rules.stream().map(RateLimitRule::getConfigKey).toArray()
        );

        for (RateLimitRule rule : rules) {
            try {
                String ruleJson = objectMapper.writeValueAsString(rule);
                stringRedisTemplate.opsForHash().put(RULE_HASH_KEY, rule.getConfigKey(), ruleJson);
            } catch (JsonProcessingException e) {
                log.error("Failed to serialize rate limit rule: configKey={}", rule.getConfigKey(), e);
            }
        }
        log.info("Refreshed {} enabled rate limit rules into cache", rules.size());
    }

    /**
     * Clears one rule from cache.
     */
    public void clearCache(String configKey) {
        if (!StringUtils.hasText(configKey)) {
            return;
        }
        stringRedisTemplate.opsForHash().delete(RULE_HASH_KEY, configKey);
        log.info("Cleared rate limit rule cache: configKey={}", configKey);
    }
}
