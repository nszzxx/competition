
package com.cdnu.cgi.entity;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 竞赛推荐实体类
 * 对应数据库表 competition_recommendation
 */
@Data
public class CompetitionRecommendation {
    
    private Long id;
    private Long userId;
    private Long competitionId;
    private Float score;
    private String suggestions;
    private LocalDateTime createdAt;
}