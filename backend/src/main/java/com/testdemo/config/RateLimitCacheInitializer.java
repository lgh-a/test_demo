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
        log.info("Initializing rate limit rule cache");
        try {
            List<RateLimitRule> rules = rateLimitRuleService.listEnabled();
            rateLimitRuleManager.refreshAllCache(rules);
            log.info("Rate limit rule cache initialized with {} enabled rules", rules.size());
        } catch (Exception e) {
            log.error("Failed to initialize rate limit rule cache", e);
        }
    }
}
