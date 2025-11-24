package com.cdnu.cgi.entity;

import lombok.Data;

import java.sql.Date;
import java.sql.Timestamp;

/**
 * 竞赛实体类
 * 对应数据库表 competitions
 */
@Data
public class Competition {
    private Long id;
    private String title;
    private String organizer;
    private String difficulty;
    private String category;
    private String track;
    private String description;
    private Timestamp startTime;
    private Timestamp endTime;
    private Date patiStarttime; // 报名开始时间
    private Date patiEndtime;   // 报名结束时间
    private String participationMode; // 参赛类型：individual(个人)、team(团队)、both(两者都可)
    private String officialUrl;
    private String tags;
    private String rulesJson;
    private Timestamp createdAt;
}
