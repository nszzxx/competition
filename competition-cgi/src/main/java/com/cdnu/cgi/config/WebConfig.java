package com.cdnu.cgi.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Web配置类，用于配置全局CORS策略
 */
@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Value("${app.file.upload-dir}")
    private String storageRootPath;

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
                .allowedOriginPatterns("*") // 前端开发服务器地址
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                .allowedHeaders("*")
                .allowCredentials(true)
                .maxAge(3600); // 1小时内不需要再预检（发OPTIONS请求）
    }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {

        String resourceLocation = "file:" + storageRootPath + "/graduate/";

        // 确保路径以 / 结尾
        if (!resourceLocation.endsWith("/")) {
            resourceLocation += "/";
        }

        registry.addResourceHandler("/graduate/**")
                .addResourceLocations(resourceLocation);
    }
}