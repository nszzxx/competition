package com.cdnu.cgi.entity;

import lombok.Data;

import java.sql.Date;

/**
 * 项目实体类
 * 对应数据库表 projects
 */
@Data
public class Project {
    private Long id;
    private Long competitionId;
    private Long teamId;
    private Long userId;  // 如果是个人参赛，记录用户ID
    private String title;
    private String description;
    private String documentUrl;
    private String participationMode; // individual 或 team
    private Date createdAt;
    private Date updatedAt;

}
