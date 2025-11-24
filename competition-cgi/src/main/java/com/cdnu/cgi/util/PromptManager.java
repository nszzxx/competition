package com.cdnu.cgi.util;

import com.cdnu.cgi.entity.Competition;
import com.cdnu.cgi.entity.User;
import com.cdnu.cgi.entity.UserSkill;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

/**
 * AI提示词管理器
 * 负责加载和管理AI提示词模板
 */
@Component
public class PromptManager {
    
    private static final Logger logger = LoggerFactory.getLogger(PromptManager.class);
    
    private JsonNode promptConfig;
    private final ObjectMapper objectMapper = new ObjectMapper();
    
    @PostConstruct
    public void init() {
        loadPromptConfig();
    }
    
    /**
     * 加载提示词配置文件
     */
    private void loadPromptConfig() {
        try {
            ClassPathResource resource = new ClassPathResource("ai-prompts.json");
            promptConfig = objectMapper.readTree(resource.getInputStream());
            logger.info("AI提示词配置加载成功");
        } catch (IOException e) {
            logger.error("加载AI提示词配置失败", e);
            throw new RuntimeException("无法加载AI提示词配置", e);
        }
    }
    
    /**
     * 获取系统基础提示词
     */
    public String getSystemPrompt() {
        String base = promptConfig.path("system").path("base").asText();
        String requirements = promptConfig.path("system").path("requirements").asText();
        return base + "\n\n" + requirements;
    }
    
    /**
     * 构建竞赛推荐提示词
     */
    public String buildRecommendationPrompt(User user, List<UserSkill> userSkills, List<Competition> competitions) {
        String template = getTemplate("recommendation");
        String userInfo = formatUserInfo(user, userSkills);
        String competitionList = formatCompetitionList(competitions);
        
        return template.replace("{userInfo}", userInfo)
                      .replace("{competitionList}", competitionList);
    }
    
    /**
     * 构建技能分析提示词
     */
    public String buildSkillAnalysisPrompt(User user, List<UserSkill> userSkills) {
        String template = getTemplate("skillAnalysis");
        String userInfo = formatUserInfo(user, userSkills);
        
        return template.replace("{userInfo}", userInfo);
    }
    
    /**
     * 构建聊天提示词
     */
    public String buildChatPrompt(String message, User user, List<UserSkill> userSkills) {
        String template = getTemplate("chat");
        String userInfo = formatUserInfo(user, userSkills);
        
        return template.replace("{userInfo}", userInfo)
                      .replace("{message}", message);
    }
    
    /**
     * 构建学习路径提示词
     */
    public String buildLearningPathPrompt(User user, List<UserSkill> userSkills, Competition competition) {
        String template = getTemplate("learningPath");
        String userInfo = formatUserInfo(user, userSkills);
        String competitionInfo = formatCompetitionInfo(competition);
        
        return template.replace("{userInfo}", userInfo)
                      .replace("{competitionInfo}", competitionInfo);
    }
    
    /**
     * 构建准备建议提示词
     */
    public String buildPreparationAdvicePrompt(User user, List<UserSkill> userSkills, Competition competition) {
        String template = getTemplate("preparationAdvice");
        String userInfo = formatUserInfo(user, userSkills);
        String competitionInfo = formatCompetitionInfo(competition);
        
        return template.replace("{userInfo}", userInfo)
                      .replace("{competitionInfo}", competitionInfo);
    }
    
    /**
     * 构建匹配度分析提示词
     */
    public String buildMatchScorePrompt(User user, List<UserSkill> userSkills, Competition competition) {
        String template = getTemplate("matchScore");
        String userInfo = formatUserInfo(user, userSkills);
        String competitionInfo = formatCompetitionInfo(competition);
        
        return template.replace("{userInfo}", userInfo)
                      .replace("{competitionInfo}", competitionInfo);
    }
    
    /**
     * 构建趋势分析提示词
     */
    public String buildTrendsPrompt() {
        return getTemplate("trends");
    }
    
    /**
     * 构建增强的趋势分析提示词（包含用户和竞赛信息）
     */
    public String buildEnhancedTrendsPrompt(User user, List<UserSkill> userSkills, 
                                          Competition participatedCompetition, Competition availableCompetition) {
        String baseTemplate = getTemplate("trends");
        StringBuilder enhancedPrompt = new StringBuilder(baseTemplate);
        
        // 添加用户信息
        if (user != null) {
            enhancedPrompt.append("\n\n用户背景信息：");
            enhancedPrompt.append(formatUserInfo(user, userSkills));
        }
        
        // 添加参与过的竞赛信息
        if (participatedCompetition != null) {
            enhancedPrompt.append("\n\n用户参与过的竞赛：");
            enhancedPrompt.append(formatCompetitionInfo(participatedCompetition));
        }
        
        // 添加目标竞赛信息
        if (availableCompetition != null) {
            enhancedPrompt.append("\n\n用户关注的竞赛：");
            enhancedPrompt.append(formatCompetitionInfo(availableCompetition));
        }
        
        // 添加个性化分析要求
        enhancedPrompt.append("\n\n请基于以上用户信息和竞赛背景，提供个性化的竞赛趋势分析，");
        enhancedPrompt.append("重点关注与用户专业背景和技能相关的竞赛发展趋势。");
        
        return enhancedPrompt.toString();
    }
    
    /**
     * 构建智能搜索提示词
     */
    public String buildIntelligentSearchPrompt(String query) {
        String template = getTemplate("intelligentSearch");
        return template.replace("{query}", query);
    }
    
    /**
     * 获取模板内容
     */
    private String getTemplate(String templateName) {
        return promptConfig.path("templates").path(templateName).path("content").asText();
    }
    
    /**
     * 格式化用户信息
     */
    private String formatUserInfo(User user, List<UserSkill> userSkills) {
        String template = promptConfig.path("formats").path("userInfo").asText();
        String skills = userSkills.isEmpty() ? "暂未录入" : 
                       userSkills.stream().map(UserSkill::getSkill).collect(Collectors.joining(", "));
        
        return template.replace("{username}", user.getUsername())
                      .replace("{major}", user.getMajor() != null ? user.getMajor() : "未知")
                      .replace("{skills}", skills);
    }
    
    /**
     * 格式化竞赛信息
     */
    private String formatCompetitionInfo(Competition competition) {
        if (competition == null) return "";
        
        String template = promptConfig.path("formats").path("competitionInfo").asText();
        return template.replace("{title}", competition.getTitle())
                      .replace("{category}", competition.getCategory())
                      .replace("{tags}", competition.getTags() != null ? competition.getTags() : "")
                      .replace("{organizer}", competition.getOrganizer() != null ? competition.getOrganizer() : "");
    }
    
    /**
     * 格式化竞赛列表
     */
    private String formatCompetitionList(List<Competition> competitions) {
        StringBuilder sb = new StringBuilder();
        String template = promptConfig.path("formats").path("competitionList").asText();
        
        for (int i = 0; i < Math.min(competitions.size(), 10); i++) {
            Competition comp = competitions.get(i);
            String formatted = template.replace("{index}", String.valueOf(i + 1))
                                     .replace("{title}", comp.getTitle())
                                     .replace("{category}", comp.getCategory())
                                     .replace("{track}", comp.getTrack())
                                     .replace("{tags}", comp.getTags() != null ? comp.getTags() : "");
            sb.append(formatted).append("\n");
        }
        
        return sb.toString();
    }
}