package com.testdemo.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // Map /roms/** to D:/project/test_demo/data/roms/
        registry.addResourceHandler("/roms/**")
                .addResourceLocations("file:///D:/project/test_demo/data/roms/");
        // Map /thumbs/** to D:/project/test_demo/data/thumbs/
        registry.addResourceHandler("/thumbs/**")
                .addResourceLocations("file:///D:/project/test_demo/data/thumbs/");
    }

    @org.springframework.context.annotation.Bean
    public org.springframework.web.filter.CorsFilter corsFilter() {
        org.springframework.web.cors.UrlBasedCorsConfigurationSource source = new org.springframework.web.cors.UrlBasedCorsConfigurationSource();
        org.springframework.web.cors.CorsConfiguration config = new org.springframework.web.cors.CorsConfiguration();
        config.setAllowCredentials(true);
        // Do not use addAllowedOrigin("*") when allowCredentials is true, use
        // AllowedOriginPatterns
        config.addAllowedOriginPattern("*");
        config.addAllowedHeader("*");
        config.addAllowedMethod("*");
        source.registerCorsConfiguration("/**", config);
        return new org.springframework.web.filter.CorsFilter(source);
    }
}
