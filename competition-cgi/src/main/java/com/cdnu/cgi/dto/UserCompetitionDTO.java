package com.cdnu.cgi.dto;

import com.cdnu.cgi.entity.Competition;
import lombok.Data;

import java.util.Date;

/**
 * 用户竞赛DTO
 * 包含竞赛信息和用户参赛信息
 */
@Data
public class UserCompetitionDTO {
    // 竞赛基本信息
    private Competition competition;

    // 参赛信息
    private Long competitionTeamUserId;  // competition_team_user表的ID
    private String participationMode;     // 参赛方式: individual(个人), team(团队)
    private String role;                  // 角色: 队长、队员、个人
    private Long teamId;                  // 团队ID（团队参赛时有值）
    private String teamName;              // 团队名称（团队参赛时有值）
    private String rank;                  // 成绩/排名
    private Date createTime;              // 报名时间
}
