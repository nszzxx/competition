package com.cdnu.cgi.dto;

import lombok.Data;

import java.util.Date;

/**
 * AI聊天响应DTO
 */
@Data
public class AIChatResponse {
    
    private String message;
    private Date timestamp;
    private String messageId;
}