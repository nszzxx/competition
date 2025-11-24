package com.cdnu.cgi.entity;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * 团队成员详细信息实体类
 * 对应数据库视图 team_member_detail_view
 */
@Data
public class TeamMemberDetailInfo {
    
    private Long teamId;
    private String teamName;
    private Long userId;
    private String role;
    private LocalDateTime joinedAt;
    private String status;
    private String skills; // 技能字符串，逗号分隔
    private String honours; // 荣誉字符串，逗号分隔，格式：title(date)|certificateUrl
    
    // 用户基本信息（可能需要关联查询）
    private String username;
    private String realName;
    private String email;
    private String major;
    private String avatarUrl;
    
    /**
     * 荣誉信息内部类
     */
    @Data
    public static class HonourInfo {
        private String title;
        private String obtainedTime;
        private String certificateImageUrl;
    }
    
    /**
     * 获取显示名称（优先使用真实姓名）
     */
    public String getDisplayName() {
        return (realName != null && !realName.trim().isEmpty()) ? realName : username;
    }
    
    /**
     * 检查是否为队长
     */
    public boolean isLeader() {
        return "leader".equalsIgnoreCase(role);
    }
    
    /**
     * 检查是否为活跃成员
     */
    public boolean isActive() {
        return "active".equalsIgnoreCase(status);
    }
    
    /**
     * 解析荣誉字符串为荣誉对象列表
     * 格式：title(date)|certificateUrl,title2(date2)|certificateUrl2
     */
    public List<HonourInfo> getHonourList() {
        if (honours == null || honours.trim().isEmpty()) {
            return new ArrayList<>();
        }
        
        List<HonourInfo> honourList = new ArrayList<>();
        String[] honourArray = honours.split(",");
        
        for (String honour : honourArray) {
            if (honour.trim().isEmpty()) continue;
            
            HonourInfo honourInfo = new HonourInfo();
            
            // 检查是否包含证书图片URL（用|分隔）
            String[] parts = honour.split("\\|");
            String titleAndDate = parts[0];
            String certificateUrl = parts.length > 1 ? parts[1] : null;
            
            // 解析标题和日期：title(date)
            int startIndex = titleAndDate.lastIndexOf("(");
            int endIndex = titleAndDate.lastIndexOf(")");
            
            if (startIndex > 0 && endIndex > startIndex) {
                String title = titleAndDate.substring(0, startIndex).trim();
                String dateStr = titleAndDate.substring(startIndex + 1, endIndex).trim();
                
                honourInfo.setTitle(title);
                honourInfo.setObtainedTime(dateStr);
                honourInfo.setCertificateImageUrl(certificateUrl);
                honourList.add(honourInfo);
            }
        }
        
        return honourList;
    }
    
    /**
     * 解析技能字符串为技能列表
     */
    public List<String> getSkillList() {
        if (skills == null || skills.trim().isEmpty()) {
            return new ArrayList<>();
        }
        
        List<String> skillList = new ArrayList<>();
        String[] skillArray = skills.split(",");
        
        for (String skill : skillArray) {
            if (skill.trim().isEmpty()) continue;
            skillList.add(skill.trim());
        }
        
        return skillList;
    }
}
