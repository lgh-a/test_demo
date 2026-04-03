package com.testdemo.config;

import com.testdemo.entity.RateLimitRule;
import com.testdemo.service.RateLimitRuleService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class RateLimitCacheInitializer implements ApplicationRunner {
    private static final Logger log = LoggerFactory.getLogger(RateLimitCacheInitializer.class);

    private final RateLimitRuleService rateLimitRuleService;
    private final RateLimitRuleManager rateLimitRuleManager;

    @Override
    public void run(ApplicationArguments args) {
        log.info("开始初始化限流规则缓存...");
        try {
            List<RateLimitRule> rules = rateLimitRuleService.listEnabled();
            rateLimitRuleManager.refreshAllCache(rules);
            log.info("限流规则缓存初始化完成，共加载 {} 条规则", rules.size());
        } catch (Exception e) {
            log.error("限流规则缓存初始化失败", e);
        }
    }
}
