package com.cdnu.cgi.entity;

import lombok.Data;
import lombok.Getter;

import java.time.LocalDateTime;

/**
 * 团队申请实体类
 */
@Data
public class TeamApplication {
    
    private Long id;
    private Long userId;
    private Long teamId;
    private Long leaderId;
    private LocalDateTime applicationTime;
    private ApplicationStatus status;
    private String rejectionReason;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private String type; // 申请类型：invite（邀请）或 apply（请求加入）
    
    // 关联对象
    private User user;
    private Team team;
    private User leader;
    
    // 非数据库字段，用于前端显示
    private String teamName;
    private String competitionName;
    private String applicantName;
    private String applicantMajor;
    private String inviterName;
    private String message;
    
    /**
     * 申请状态枚举
     */
    @Getter
    public enum ApplicationStatus {
        PENDING("审核中"),
        APPROVED("已通过"),
        REJECTED("未通过");
        
        private final String description;
        
        ApplicationStatus(String description) {
            this.description = description;
        }

    }
    public TeamApplication(Long userId, Long teamId, Long leaderId) {
        this.userId = userId;
        this.teamId = teamId;
        this.leaderId = leaderId;
        this.status = ApplicationStatus.PENDING;
        this.applicationTime = LocalDateTime.now();
    }

}