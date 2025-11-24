package com.cdnu.cgi.config;

import com.cdnu.cgi.service.config.ConfigService;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import javax.annotation.PostConstruct;

/**
 * AI配置类 - 单一模型架构
 * 只加载当前启用的AI模型配置
 */
@Setter
@Getter
@Component
@RequiredArgsConstructor
@ConfigurationProperties(prefix = "ai")
public class AIConfig {
    private final ConfigService configService;
    AIServiceConfig aIServiceConfig;
    private String serviceName;
    private Boolean serviceEnabled;

    private String systemPrompt;
    private CacheConfig cache = new CacheConfig();
    private LoggingConfig logging = new LoggingConfig();
    private RetryConfig retry = new RetryConfig();

    @PostConstruct
    public void init() {
        this.aIServiceConfig = configService.getAIServiceConfig();
        this.serviceName = aIServiceConfig.getServiceName();
        this.serviceEnabled = aIServiceConfig.getServiceEnabled();
    }

    @Getter
    @Setter
    public static class CacheConfig {
        private Boolean enabled = true;
        private Integer ttlMinutes = 60;
        private Integer maxSize = 1000;
    }

    @Getter
    @Setter
    public static class LoggingConfig {
        private Boolean enabled = true;
        private Boolean logRequests = true;
        private Boolean logResponses = false;
        private Boolean logErrors = true;
    }

    @Getter
    @Setter
    public static class RetryConfig {
        private Boolean enabled = true;
        private Integer maxAttempts = 3;
        private Integer delaySeconds = 2;
    }
}