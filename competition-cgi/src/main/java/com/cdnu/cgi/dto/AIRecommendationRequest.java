package com.cdnu.cgi.dto;

import lombok.Data;

import java.util.List;

/**
 * AI推荐请求DTO
 */
@Data
public class AIRecommendationRequest {
    
    private Long userId;
    private List<String> skills;
    private List<String> interests;
    private String level;
    private Integer limit;
    private Preference preference;
    
    /**
     * 用户偏好设置
     */
    @Data
    public static class Preference {
        private List<String> preferredCategories;
        private String difficultyLevel;
    }
}