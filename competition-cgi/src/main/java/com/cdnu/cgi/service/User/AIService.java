package com.cdnu.cgi.service.User;

import com.cdnu.cgi.dto.*;
import com.cdnu.cgi.entity.Competition;
import com.cdnu.cgi.entity.CompetitionRecommendation;

import java.util.List;
import java.util.Map;

/**
 * AI服务接口
 */
public interface AIService {
    
    /**
     * 获取AI推荐
     */
    AIRecommendationResponse getRecommendations(AIRecommendationRequest request);
    
    /**
     * AI聊天
     */
    AIChatResponse chat(AIChatRequest request);
    
    /**
     * 技能分析
     */
    AIAnalysisResponse analyzeSkills(AIAnalysisRequest request);
    
    // 为了兼容 AIController 中的方法调用，添加以下方法
    
    /**
     * 根据用户ID获取推荐（简化版本）
     */
    List<Competition> getRecommendations(Long userId);
    
    /**
     * 根据用户ID、类别和难度获取推荐
     */
    List<Competition> getRecommendations(Long userId, String category, String difficulty);
    
    /**
     * 分析用户技能（简化版本）
     */
    Map<String, Object> analyzeUserSkills(Long userId);
    
    /**
     * 生成学习路径
     */
    Map<String, Object> generateLearningPath(Long userId, Long targetCompetitionId);
    
    /**
     * 获取竞赛趋势
     */
    Map<String, Object> getCompetitionTrends();
    
    /**
     * 获取竞赛趋势（带用户和竞赛信息）
     */
    Map<String, Object> getCompetitionTrends(Long userId, Long participatedCompetitionId, Long availableCompetitionId, Map<String, Object> requestData);
    
    /**
     * 获取准备建议
     */
    Map<String, Object> getPreparationAdvice(Long competitionId, Long userId);
    
    /**
     * 获取用户推荐
     */
    List<CompetitionRecommendation> getUserRecommendations(Long userId);
    
    /**
     * 智能搜索
     */
    List<Competition> intelligentSearch(String query, Long userId);
    
    /**
     * 获取竞赛匹配度评分
     */
    Map<String, Object> getCompetitionMatchScore(Long competitionId, Long userId);

    List<Map<String, Object>> getChatHistoryGroups(Long userId, int limit);
}