package com.cdnu.cgi.config;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.context.annotation.Configuration;

/**
 * MyBatis配置类
 */
@Configuration
@MapperScan("com.cdnu.cgi.mapper")
public class MyBatisConfig {
    // MyBatis相关配置可以在这里添加
}