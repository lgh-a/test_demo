package com.testdemo.config;

import cn.dev33.satoken.interceptor.SaInterceptor;
import cn.dev33.satoken.router.SaRouter;
import cn.dev33.satoken.stp.StpUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

@Configuration
@RequiredArgsConstructor
public class WebConfig implements WebMvcConfigurer {

    private final AppProperties appProperties;

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/roms/**")
                .addResourceLocations(toResourceLocation(appProperties.getStorage().getRomsPath(), "roms"));
        registry.addResourceHandler("/thumbs/**")
                .addResourceLocations(toResourceLocation(appProperties.getStorage().getThumbsPath(), "thumbs"));
    }

    @Override
    public void addViewControllers(ViewControllerRegistry registry) {
        registry.addRedirectViewController("/swagger-bootstrap-ui.html", "/doc.html");
        registry.addRedirectViewController("/swagger-ui.html", "/doc.html");
        registry.addRedirectViewController("/swagger-ui/index.html", "/doc.html");
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(new SaInterceptor(handler -> {
            SaRouter.match("/api/**")
                    .notMatch(
                            "/api/auth/login",
                            "/api/categories/consoles",
                            "/api/categories/series",
                            "/api/games/list",
                            "/api/demo/sendSms"
                    )
                    .check(r -> StpUtil.checkLogin());
        })).addPathPatterns("/**")
                .excludePathPatterns(
                        "/doc.html",
                        "/webjars/**",
                        "/v3/api-docs/**",
                        "/swagger-ui/**",
                        "/swagger-ui.html",
                        "/swagger-bootstrap-ui.html",
                        "/favicon.ico"
                );
    }

    @org.springframework.context.annotation.Bean
    public CorsFilter corsFilter() {
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowCredentials(appProperties.getSecurity().getCors().isAllowCredentials());
        config.setAllowedOrigins(appProperties.getSecurity().getCors().getAllowedOrigins());
        config.setAllowedHeaders(List.of("*"));
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS"));
        source.registerCorsConfiguration("/**", config);
        return new CorsFilter(source);
    }

    private String toResourceLocation(String path, String defaultFolderName) {
        String resolvedPath = resolveStoragePath(path, defaultFolderName);
        String normalized = resolvedPath.replace("\\", "/");
        if (!normalized.endsWith("/")) {
            normalized = normalized + "/";
        }
        return normalized.startsWith("file:") ? normalized : "file:" + normalized;
    }

    private String resolveStoragePath(String configuredPath, String defaultFolderName) {
        if (configuredPath != null && !configuredPath.isBlank()) {
            return configuredPath;
        }

        Path workingDirectory = Paths.get(System.getProperty("user.dir")).toAbsolutePath().normalize();
        Path[] candidates = new Path[] {
                workingDirectory.resolve("data").resolve(defaultFolderName),
                workingDirectory.resolve("../data").resolve(defaultFolderName),
                workingDirectory.resolve("backend/../data").resolve(defaultFolderName),
                workingDirectory.resolve("../backend/../data").resolve(defaultFolderName)
        };

        for (Path candidate : candidates) {
            Path normalizedCandidate = candidate.normalize();
            if (Files.isDirectory(normalizedCandidate)) {
                return normalizedCandidate.toString();
            }
        }

        return workingDirectory.resolve("data").resolve(defaultFolderName).normalize().toString();
    }
}
