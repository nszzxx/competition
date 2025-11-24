package com.cdnu.cgi.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import lombok.Data;

import java.sql.Timestamp;

/**
 * 团队实体类
 * 对应数据库表 teams
 */
@Data
public class Team {
    private Long id;
    private String name;
    private String description;
    private Long leaderId;
    private Long competitionId;
    private Long openInvolve;
    private String needSkills;
    private Long maxMembers;
    private Timestamp createdAt;

    // 非数据库字段
    @TableField(exist = false)
    private Integer MemberCount;

    @TableField(exist = false)
    private String leaderName; // 队长姓名

    @TableField(exist = false)
    private String competitionName; // 竞赛名称
}
