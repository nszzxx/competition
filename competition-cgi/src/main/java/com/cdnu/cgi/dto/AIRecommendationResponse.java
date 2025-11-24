package com.cdnu.cgi.dto;

import lombok.Data;

import java.util.Date;
import java.util.List;

/**
 * AI推荐响应DTO
 */
@Data
public class AIRecommendationResponse {
    
    private List<Competition> competitions;
    private String summary;
    private Date timestamp;
    /**
     * 推荐的竞赛信息
     */
    @Data
    public static class Competition {
        private Long id;
        private String title;
        private String description;
        private String category;
        private Integer matchScore;
        private String reason;
        private String difficultyLevel;
        private String preparationTime;
        private List<SkillMatch> skillMatches;
    }
    
    /**
     * 技能匹配信息
     */
    @Data
    public static class SkillMatch {
        private String skillName;
        private Integer matchPercentage;
        private String level;
    }
}