package com.cdnu.cgi.dto;

import lombok.Data;

import java.util.List;
import java.util.Map;

/**
 * AI聊天请求DTO
 */
@Data
public class AIChatRequest {
    
    private String message;
    private List<String> context;
    private Map<String, Object> userProfile;
    private Long userId; // 添加 userId 字段
    private Long conversationId; // 添加 conversationId 字段
    private String groupId; // 添加 groupId 字段
}