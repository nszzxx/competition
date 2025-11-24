package com.cdnu.cgi.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 用户技能实体类
 * 对应数据库表 user_skills
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserSkill {
    
    private Long id;
    private Long userId;
    private String skill;
    
    // 构造函数
    public UserSkill(Long userId, String skill) {
        this.userId = userId;
        this.skill = skill;
    }
}
