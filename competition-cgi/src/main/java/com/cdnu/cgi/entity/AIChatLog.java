package com.cdnu.cgi.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * AI聊天日志实体类
 * 对应数据库表 ai_chat_logs
 */
@Data
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler", "user"})
public class AIChatLog {
    
    private Long id;
    private Long userId;
    private String type;
    private String input;
    private String response;
    private LocalDateTime timestamp;
    private String groupId;
    
    // 构造函数
    public AIChatLog() {
        this.timestamp = LocalDateTime.now();
    }
}