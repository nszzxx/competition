package com.cdnu.cgi.entity;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 用户荣誉实体类
 * 对应数据库表 user_honours
 */
@Data
public class UserHonour {
    
    private Long id;
    private Long userId;
    private String honourTitle;
    private String description;  // 添加荣誉描述字段
    private LocalDateTime obtainedTime;
    private String certificateImageUrl;
    private LocalDateTime createdAt;
    
    // 构造函数
    public UserHonour() {
        this.createdAt = LocalDateTime.now();
    }
    
    public UserHonour(Long userId, String honourTitle, LocalDateTime obtainedTime) {
        this.userId = userId;
        this.honourTitle = honourTitle;
        this.obtainedTime = obtainedTime;
        this.createdAt = LocalDateTime.now();
    }
    
    public UserHonour(Long userId, String honourTitle, String description, LocalDateTime obtainedTime) {
        this.userId = userId;
        this.honourTitle = honourTitle;
        this.description = description;
        this.obtainedTime = obtainedTime;
        this.createdAt = LocalDateTime.now();
    }
}