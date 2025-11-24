package com.cdnu.cgi.entity;

import lombok.Data;

/**
 * 用户实体类
 * 对应数据库表 users
 */
@Data
public class User {
    private Long id;
    private String username;
    private String password;
    private String realName;
    private String email;
    private String phone;
    private String major;
    private String avatarUrl;
    private Integer roleId;
    private java.sql.Timestamp registerTime;
    private Boolean isLeader;
    private Integer status;
}
