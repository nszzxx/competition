package com.cdnu.cgi.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 团队成员实体类
 * 对应数据库表 team_members
 */
@Data
public class TeamMember {
    private Long id;
    private Long teamId;
    private Long userId;
    private String role;
    private LocalDateTime joinedAt;
    private String status;
    @TableField(exist = false)
    private String username; // 非当前表字段，存储用户名
    @TableField(exist = false)
    private String realName; // 非当前表字段，存储用户姓名
    @TableField(exist = false)
    private String avatarUrl; // 非当前表字段，存储用户头像URL
}