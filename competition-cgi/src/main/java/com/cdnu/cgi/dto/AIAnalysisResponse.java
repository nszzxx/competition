package com.cdnu.cgi.dto;

import lombok.Data;

import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * AI分析响应DTO
 */
@Data
public class AIAnalysisResponse {
    
    private Integer overallScore;
    private Map<String, SkillLevel> skillLevels;
    private List<String> strengths;
    private List<String> improvements;
    private Date timestamp;
    
    /**
     * 技能水平信息
     */
    @Data
    public static class SkillLevel {
        private Integer score;
        private String level;
        private String suggestion;
    }
}