package com.cdnu.cgi.dto;

import lombok.Data;

/**
 * 竞赛团队卡片DTO
 * 对应视图 competition_team_cards
 */
@Data
public class CompetitionTeamCardDto {
    
    /**
     * 团队ID
     */
    private Long id;
    
    /**
     * 团队名称
     */
    private String name;
    
    /**
     * 团队描述
     */
    private String description;
    
    /**
     * 需要的技能
     */
    private String needSkills;
    
    /**
     * 竞赛标题
     */
    private String title;
    
    /**
     * 竞赛类别
     */
    private String category;
    
    /**
     * 队长ID
     */
    private Long leaderId;
    
    /**
     * 队长用户名
     */
    private String username;
    
    /**
     * 队长真实姓名
     */
    private String realName;
    
    /**
     * 队长显示名称（优先显示真实姓名，否则显示用户名）
     */
    private String leaderDisplayName;
    
    /**
     * 团队成员数量
     */
    private Integer teamMemberCount;
    
    /**
     * 团队技能（逗号分隔）
     */
    private String teamSkills;
    
    /**
     * 团队成员ID列表（逗号分隔）
     */
    private String memberIds;
    
    /**
     * 用户与团队的适配度（0-100）
     * 注意：这个字段需要在查询时动态计算，不是视图的一部分
     */
    private Integer matchScore;

    /**
     * 竞赛ID（来自 competition_team_user 表）
     */
    private Long competitionId;
}