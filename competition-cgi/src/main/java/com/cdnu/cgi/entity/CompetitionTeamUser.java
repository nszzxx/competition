package com.cdnu.cgi.entity;

import lombok.Data;

import java.util.Date;

@Data
public class CompetitionTeamUser {
    /**
     *  竞赛ID
     */
    private Long competitionId;
    /**
     *  团队ID
     */
    private Long teamId;
    /**
     *  用户ID
     */
    private Long userId;
    /**
     *  参赛方式 enum both(个人和团队皆可), individual(个人), team(团队)
     */
    private String participationMode;
    /**
     *  成绩
     */
    private String rank;
    /**
     *  时间
     */
    private Date createTime;
    /**
     *  身份
     */
    private String role;
}
