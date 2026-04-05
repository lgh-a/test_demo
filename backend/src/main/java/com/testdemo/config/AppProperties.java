package com.testdemo.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Data
@Component
@ConfigurationProperties(prefix = "app")
public class AppProperties {

    private Storage storage = new Storage();
    private Security security = new Security();

    @Data
    public static class Storage {
        private String romsPath;
        private String thumbsPath;
    }

    @Data
    public static class Security {
        private Cors cors = new Cors();
    }

    @Data
    public static class Cors {
        private boolean allowCredentials = true;
        private List<String> allowedOrigins = new ArrayList<>();
    }
}
