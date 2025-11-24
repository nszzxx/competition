package com.cdnu.cgi.entity;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * 用户综合信息实体类（对应 user_comprehensive_info 视图）
 */
@Getter
@Setter
public class UserComprehensiveInfo {
    
    private Long id;
    private String username;
    private String realName;
    private String email;
    private String phone;
    private String major;
    private String avatarUrl;
    private LocalDateTime registerTime;
    private String status;
    private String skills; // 技能字符串，逗号分隔
    private String honours; // 荣誉字符串，分号分隔

    
    // 解析后的技能列表
    private List<String> skillList;
    
    // 解析后的荣誉列表
    private List<UserHonour> honourList;
    
    /**
     * 用户荣誉内部类
     */
    @Data
    private static class UserHonour {
        // Getter 和 Setter
        private String title;
        private String obtainedDate;
        private String certificateImageUrl;
        public UserHonour(String title, String obtainedDate, String certificateImageUrl) {
            this.title = title;
            this.obtainedDate = obtainedDate;
            this.certificateImageUrl = certificateImageUrl;
        }
    }
    
    // 构造函数
    public UserComprehensiveInfo() {}

    public void setSkills(String skills) {
        this.skills = skills;
        // 自动解析技能列表
        parseSkillList();
    }

    public void setHonours(String honours) {
        this.honours = honours;
        // 自动解析荣誉列表
        parseHonourList();
    }
    
    public List<String> getSkillList() {
        if (skillList == null) {
            parseSkillList();
        }
        return skillList;
    }

    public List<UserHonour> getHonourList() {
        if (honourList == null) {
            parseHonourList();
        }
        return honourList;
    }

    /**
     * 解析技能字符串为列表
     */
    private void parseSkillList() {
        skillList = new ArrayList<>();
        if (skills != null && !skills.trim().isEmpty()) {
            String[] skillArray = skills.split(",");
            for (String skill : skillArray) {
                String trimmedSkill = skill.trim();
                if (!trimmedSkill.isEmpty()) {
                    skillList.add(trimmedSkill);
                }
            }
        }
    }
    
    /**
     * 解析荣誉字符串为列表
     */
    private void parseHonourList() {
        honourList = new ArrayList<>();
        if (honours != null && !honours.trim().isEmpty()) {
            String[] honourArray = honours.split(";");
            for (String honour : honourArray) {
                String trimmedHonour = honour.trim();
                if (!trimmedHonour.isEmpty()) {
                    String[] parts = trimmedHonour.split("\\|");
                    if (parts.length >= 2) {
                        String title = parts[0];
                        String date = parts[1];
                        String imageUrl = parts.length > 2 ? parts[2] : "";
                        if (imageUrl.isEmpty()) {
                            imageUrl = null;
                        }
                        honourList.add(new UserHonour(title, date, imageUrl));
                    }
                }
            }
        }
    }
    
    /**
     * 获取显示名称（优先使用真实姓名）
     */
    public String getDisplayName() {
        return (realName != null && !realName.trim().isEmpty()) ? realName : username;
    }
    
    /**
     * 检查用户是否有特定技能
     */
    public boolean hasSkill(String skill) {
        List<String> skills = getSkillList();
        return skills.stream().anyMatch(s -> s.equalsIgnoreCase(skill.trim()));
    }
    
    /**
     * 获取技能数量
     */
    public int getSkillCount() {
        return getSkillList().size();
    }
    
    /**
     * 获取荣誉数量
     */
    public int getHonourCount() {
        return getHonourList().size();
    }
}