package com.testdemo.config;

import com.testdemo.annotation.DynamicRateLimit;
import com.testdemo.entity.RateLimitRule;
import com.testdemo.service.RateLimitRuleService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.aop.framework.AopProxyUtils;
import org.springframework.context.ApplicationContext;
import org.springframework.core.MethodIntrospector;
import org.springframework.core.annotation.AnnotatedElementUtils;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.lang.reflect.Method;
import java.util.HashSet;
import java.util.Set;

@Component
@RequiredArgsConstructor
public class RateLimitAnnotationSynchronizer {

    private static final Logger log = LoggerFactory.getLogger(RateLimitAnnotationSynchronizer.class);

    private final ApplicationContext applicationContext;
    private final RateLimitRuleService rateLimitRuleService;

    /**
     * 启动时扫描 Spring Bean 上的 @DynamicRateLimit，并把可落库的默认规则补录到规则表。
     */
    public void syncAnnotatedRules() {
        String[] beanNames = applicationContext.getBeanDefinitionNames();
        Set<String> scannedConfigKeys = new HashSet<>();
        int discovered = 0;
        int inserted = 0;

        for (String beanName : beanNames) {
            Object bean;
            try {
                bean = applicationContext.getBean(beanName);
            } catch (Exception e) {
                log.debug("Skipped rate limit annotation scan for bean {} because it could not be resolved", beanName, e);
                continue;
            }

            // 兼容 AOP 代理场景，拿到业务类本身的方法定义。
            Class<?> targetClass = AopProxyUtils.ultimateTargetClass(bean);
            if (targetClass == null || !isProjectClass(targetClass)) {
                continue;
            }

            // 只挑出真正声明了 @DynamicRateLimit 的方法，避免遍历全部方法逐个判断。
            Set<Method> methods = MethodIntrospector.selectMethods(targetClass,
                    (MethodIntrospector.MetadataLookup<DynamicRateLimit>) method ->
                            AnnotatedElementUtils.findMergedAnnotation(method, DynamicRateLimit.class)
            ).keySet();

            for (Method method : methods) {
                DynamicRateLimit annotation = AnnotatedElementUtils.findMergedAnnotation(method, DynamicRateLimit.class);
                if (annotation == null) {
                    continue;
                }

                discovered++;
                String configKey = annotation.configKey().trim();
                if (!StringUtils.hasText(configKey)) {
                    log.warn("Skipped @DynamicRateLimit sync because configKey is blank: {}#{}",
                            targetClass.getSimpleName(), method.getName());
                    continue;
                }

                // 同一个 configKey 在一次启动扫描中只处理一次，避免重复插入或重复告警。
                if (!scannedConfigKeys.add(configKey)) {
                    log.warn("Skipped duplicate @DynamicRateLimit configKey during startup scan: configKey={}, location={}#{}",
                            configKey, targetClass.getSimpleName(), method.getName());
                    continue;
                }

                // service 层会再做一次按 configKey 的幂等校验，数据库已存在时直接跳过。
                if (rateLimitRuleService.syncAnnotatedRule(buildRule(targetClass, method, annotation))) {
                    inserted++;
                }
            }
        }

        log.info("Completed @DynamicRateLimit startup sync: discovered={}, inserted={}, skipped={}",
                discovered, inserted, discovered - inserted);
    }

    private boolean isProjectClass(Class<?> targetClass) {
        Package targetPackage = targetClass.getPackage();
        return targetPackage != null && targetPackage.getName().startsWith("com.testdemo");
    }

    /**
     * 把注解里的默认限流配置映射成一条规则表记录。
     */
    private RateLimitRule buildRule(Class<?> targetClass, Method method, DynamicRateLimit annotation) {
        RateLimitRule rule = new RateLimitRule();
        rule.setConfigKey(annotation.configKey().trim());
        rule.setName(targetClass.getSimpleName() + "#" + method.getName());
        rule.setDescription("Auto synced from @DynamicRateLimit on "
                + targetClass.getName() + "#" + method.getName());
        rule.setLimitType(annotation.limitType().name());
        rule.setCapacity(annotation.capacity());
        rule.setRate(annotation.rate());
        rule.setGuestCapacity(annotation.guestCapacity());
        rule.setGuestRate(annotation.guestRate());
        rule.setStatus(1);
        rule.setVersion(1);
        return rule;
    }
}
