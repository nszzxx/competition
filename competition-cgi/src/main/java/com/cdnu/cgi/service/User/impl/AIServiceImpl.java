package com.cdnu.cgi.service.User.impl;

import com.cdnu.cgi.dto.*;
import com.cdnu.cgi.entity.*;
import com.cdnu.cgi.mapper.*;
import com.cdnu.cgi.service.User.AIService;
import com.cdnu.cgi.util.AIServiceHelper;
import com.cdnu.cgi.util.PromptManager;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * AI服务实现类 - 优化版本
 * 使用PromptManager管理提示词模板，使用AIServiceHelper处理AI调用
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AIServiceImpl implements AIService {

    private final PromptManager promptManager;
    private final AIServiceHelper aiServiceHelper;
    private final CompetitionRecommendationMapper recommendationMapper;
    private final AIChatLogMapper chatLogMapper;
    private final UserMapper userMapper;
    private final UserSkillMapper userSkillMapper;
    private final CompetitionMapper competitionMapper;
    // ==================== 主要接口实现 ====================
    
    @Override
    public AIRecommendationResponse getRecommendations(AIRecommendationRequest request) {
        String cacheKey = "recommend_" + request.getUserId() + "_" + request.hashCode();
        
        return aiServiceHelper.executeWithCache(cacheKey, () -> {
            // 获取用户信息
            User user = getUserById(request.getUserId());
            List<UserSkill> userSkills = getUserSkills(request.getUserId());
            List<Competition> competitions = competitionMapper.selectAll();
            
            // 构建AI提示词
            String prompt = promptManager.buildRecommendationPrompt(user, userSkills, competitions);
            
            // 调用AI模型
            String aiResponse = aiServiceHelper.callAIModel(prompt);
            
            // 解析AI响应并构建结果
            AIRecommendationResponse response = parseRecommendationResponse(aiResponse, request);
            
            // 保存推荐结果到数据库
            saveRecommendationsToDatabase(request.getUserId(), response);
            
            return response;
        }, () -> createFallbackRecommendation(request), "AI推荐");
    }
    
    @Override
    public AIChatResponse chat(AIChatRequest request) {
        return aiServiceHelper.executeAIServiceCall(() -> {
            // 获取用户信息
            User user = request.getUserId() != null ? getUserById(request.getUserId()) : null;
            List<UserSkill> userSkills = request.getUserId() != null ? getUserSkills(request.getUserId()) : new ArrayList<>();
            
            // 构建对话提示词
            String prompt = promptManager.buildChatPrompt(request.getMessage(), user, userSkills);
            
            // 调用AI模型
            String aiResponse = aiServiceHelper.callAIModel(prompt);
            
            // 构建响应
            AIChatResponse response = new AIChatResponse();
            response.setMessage(aiResponse);
            response.setTimestamp(new Date());
            response.setMessageId(UUID.randomUUID().toString());
            // 保存聊天记录到数据库
            saveChatLog(request.getUserId(), request.getMessage(), response.getMessage(), "CHAT", request.getGroupId());
            return response;
        }, this::createFallbackChatResponse, "AI聊天");
    }
    
    @Override
    public AIAnalysisResponse analyzeSkills(AIAnalysisRequest request) {
        String cacheKey = "skills_" + request.getUserId();
        
        return aiServiceHelper.executeWithCache(cacheKey, () -> {
            // 获取用户信息
            User user = getUserById(request.getUserId());
            List<UserSkill> userSkills = getUserSkills(request.getUserId());
            
            // 构建技能分析提示词
            String prompt = promptManager.buildSkillAnalysisPrompt(user, userSkills);
            
            // 调用AI模型
            String aiResponse = aiServiceHelper.callAIModel(prompt);
            
            // 解析AI响应
            return parseSkillAnalysisResponse(aiResponse);
        }, this::createFallbackSkillAnalysis, "AI技能分析");
    }
    
    // ==================== 简化接口方法 ====================
    
    @Override
    public List<Competition> getRecommendations(Long userId) {
        return getRecommendations(userId, null, null);
    }
    
    @Override
    public List<Competition> getRecommendations(Long userId, String category, String difficulty) {
        return aiServiceHelper.executeAIServiceCall(() -> {
            log.info("为用户 {} 生成AI竞赛推荐，类别: {}, 难度: {}", userId, category, difficulty);
            // 获取用户信息和技能
            User user = getUserById(userId);
            if (user == null) {
                log.warn("用户 {} 不存在", userId);
                return new ArrayList<>();
            }
            List<UserSkill> userSkills = getUserSkills(userId);
            
            // 获取所有竞赛并根据类别和难度进行过滤
            List<Competition> allCompetitions = competitionMapper.selectAll();
            List<Competition> filteredCompetitions = filterCompetitions(allCompetitions, category, difficulty);
            
            if (filteredCompetitions.isEmpty()) {
                log.warn("过滤后没有符合条件的竞赛");
                return new ArrayList<>();
            }
            
            // 构建推荐提示词
            String prompt = promptManager.buildRecommendationPrompt(user, userSkills, filteredCompetitions);
            
            // 调用AI模型获取推荐建议
            String aiResponse = aiServiceHelper.callAIModel(prompt);
            log.info("AI推荐建议: {}", aiResponse);
            
            // 基于AI建议和用户信息进行智能推荐
            List<Competition> recommendations = selectRecommendationsBasedOnAI(user, userSkills, filteredCompetitions, aiResponse);
            
            // 保存推荐记录
            saveRecommendationRecords(userId, recommendations, user, userSkills);
            
            return recommendations;
        }, ArrayList::new, "AI竞赛推荐");
    }


    @Override
    public Map<String, Object> analyzeUserSkills(Long userId) {
        return aiServiceHelper.executeAIServiceCall(() -> {
            log.info("为用户 {} 进行AI技能分析", userId);
            
            // 获取用户信息和技能
            User user = getUserById(userId);
            if (user == null) {
                Map<String, Object> error = new HashMap<>();
                error.put("message", "用户不存在");
                return error;
            }
            
            List<UserSkill> userSkills = getUserSkills(userId);
            
            // 构建技能分析提示词
            String prompt = promptManager.buildSkillAnalysisPrompt(user, userSkills);
            
            // 调用AI模型
            String aiResponse = aiServiceHelper.callAIModel(prompt);
            
            // 计算技能评分
            Map<String, Integer> skillScores = calculateSkillScores(userSkills);
            int overallScore = calculateOverallScore(skillScores);
            
            Map<String, Object> analysis = new HashMap<>();
            analysis.put("aiAnalysis", aiResponse);
            analysis.put("overallScore", overallScore);
            analysis.put("skillScores", skillScores);
            analysis.put("userSkills", userSkills.stream().map(UserSkill::getSkill).collect(Collectors.toList()));
            analysis.put("analysisDate", new Date());
            analysis.put("status", "success");
            
            return analysis;
        }, this::createFallbackSkillAnalysisMap, "AI技能分析");
    }
    
    @Override
    public Map<String, Object> generateLearningPath(Long userId, Long targetCompetitionId) {
        return aiServiceHelper.executeAIServiceCall(() -> {
            log.info("为用户 {} 生成针对竞赛 {} 的学习路径", userId, targetCompetitionId);
            
            // 获取用户和竞赛信息
            User user = getUserById(userId);
            List<UserSkill> userSkills = getUserSkills(userId);
            Competition competition = competitionMapper.selectById(targetCompetitionId);
            
            // 构建学习路径生成提示词
            String prompt = promptManager.buildLearningPathPrompt(user, userSkills, competition);
            
            // 调用AI模型
            String aiResponse = aiServiceHelper.callAIModel(prompt);
            
            Map<String, Object> path = new HashMap<>();
            path.put("aiPlan", aiResponse);
            path.put("totalDuration", "12周");
            path.put("generatedDate", new Date());
            path.put("status", "success");
            
            return path;
        }, () -> createFallbackLearningPath(), "AI学习路径生成");
    }
    
    @Override
    public Map<String, Object> getCompetitionTrends() {
        return getCompetitionTrends(null, null, null, new HashMap<>());
    }
    
    @Override
    public Map<String, Object> getCompetitionTrends(Long userId, Long participatedCompetitionId, Long availableCompetitionId, Map<String, Object> requestData) {
        return aiServiceHelper.executeAIServiceCall(() -> {
            log.info("生成AI竞赛趋势分析 - 用户ID: {}, 参与竞赛ID: {}, 目标竞赛ID: {}", 
                userId, participatedCompetitionId, availableCompetitionId);
            
            // 获取用户信息
            User user = userId != null ? getUserById(userId) : null;
            List<UserSkill> userSkills = userId != null ? getUserSkills(userId) : new ArrayList<>();
            
            // 获取竞赛信息
            Competition participatedCompetition = participatedCompetitionId != null ? 
                competitionMapper.selectById(participatedCompetitionId) : null;
            Competition availableCompetition = availableCompetitionId != null ? 
                competitionMapper.selectById(availableCompetitionId) : null;
            
            // 构建增强的趋势分析提示词
            String prompt = promptManager.buildEnhancedTrendsPrompt(user, userSkills, participatedCompetition, availableCompetition);
            
            // 调用AI模型
            String aiResponse = aiServiceHelper.callAIModel(prompt);
            
            // 构建个性化的趋势分析结果
            Map<String, Object> trends = new HashMap<>();
            trends.put("aiAnalysis", aiResponse);
            trends.put("analysisDate", new Date());
            trends.put("status", "success");
            
            // 添加用户相关的个性化信息
            if (user != null) {
                trends.put("userMajor", user.getMajor());
                trends.put("userSkillCount", userSkills.size());
            }

            if (participatedCompetition != null) {
                trends.put("participatedCompetition", participatedCompetition.getTitle());
            }
            
            if (availableCompetition != null) {
                trends.put("targetCompetition", availableCompetition.getTitle());
            }
            
            return trends;
        }, () -> createFallbackTrends(), "AI趋势分析");
    }
    
    @Override
    public Map<String, Object> getPreparationAdvice(Long competitionId, Long userId) {
        return aiServiceHelper.executeAIServiceCall(() -> {
            log.info("为用户 {} 生成竞赛 {} 的准备建议", userId, competitionId);
            
            // 获取用户和竞赛信息
            User user = getUserById(userId);
            List<UserSkill> userSkills = getUserSkills(userId);
            Competition competition = competitionMapper.selectById(competitionId);
            
            // 构建准备建议提示词
            String prompt = promptManager.buildPreparationAdvicePrompt(user, userSkills, competition);
            
            // 调用AI模型
            String aiResponse = aiServiceHelper.callAIModel(prompt);
            
            Map<String, Object> advice = new HashMap<>();
            advice.put("aiAdvice", aiResponse);
            advice.put("generatedDate", new Date());
            advice.put("status", "success");
            
            return advice;
        }, () -> createFallbackPreparationAdvice(), "AI准备建议");
    }
    
    @Override
    public List<CompetitionRecommendation> getUserRecommendations(Long userId) {
        try {
            log.info("获取用户 {} 的推荐记录", userId);
            return recommendationMapper.selectByUserIdOrderByScoreDesc(userId);
        } catch (Exception e) {
            log.error("获取用户推荐记录异常", e);
            return new ArrayList<>();
        }
    }
    
    /**
     * 获取用户的聊天历史分组列表
     */
    public List<Map<String, Object>> getChatHistoryGroups(Long userId) {
        try {
            log.info("获取用户 {} 的聊天历史分组", userId);
            return chatLogMapper.selectChatHistoryGroupsByUserId(userId);
        } catch (Exception e) {
            log.error("获取聊天历史分组失败", e);
            return new ArrayList<>();
        }
    }

    /**
     * 获取用户的聊天历史分组列表
     * 使用批量加载策略减少数据库查询
     *
     * @param userId 用户ID
     * @param limit 返回记录限制数
     * @return 聊天历史分组列表
     */
    @Override
    public List<Map<String, Object>> getChatHistoryGroups(Long userId, int limit) {
        log.info("获取用户 {} 的聊天历史分组，限制 {} 条", userId, limit);

        try {
            List<Map<String, Object>> chatHistoryGroups = chatLogMapper.selectChatHistoryGroupsByUserId(userId);
            if (chatHistoryGroups.isEmpty()) {
                return Collections.emptyList();
            }

            // 限制返回的组数
            List<Map<String, Object>> limitedGroups = chatHistoryGroups.stream()
                    .limit(limit)
                    .collect(Collectors.toList());

            List<String> groupIds = limitedGroups.stream()
                    .map(group -> (String)group.get("groupId"))
                    .collect(Collectors.toList());

            List<AIChatLog> allChatLogs = chatLogMapper.selectBatchByUserIdAndGroupIds(userId, groupIds);

            Map<String, List<AIChatLog>> chatLogsByGroup = allChatLogs.stream()
                    .collect(Collectors.groupingBy(AIChatLog::getGroupId));

            // 5. 构建最终返回的结果集
            List<Map<String, Object>> result = new ArrayList<>(limitedGroups.size());
            for (Map<String, Object> group : limitedGroups) {
                String groupId = (String) group.get("groupId");

                // 构建分组数据
                Map<String, Object> groupData = new HashMap<>(group);  // 复用已有数据
                groupData.put("messages", chatLogsByGroup.getOrDefault(groupId, Collections.emptyList()));

                result.add(groupData);
            }

            log.info("成功获取用户 {} 的 {} 个聊天历史分组（包含完整对话）", userId, result.size());
            return result;

        } catch (Exception e) {
            log.error("获取聊天历史分组失败: {}", e.getMessage(), e);
            throw e;  // 重新抛出异常，让控制器处理
        }
    }
    
    /**
     * 删除对话组
     */
    public boolean deleteConversation(Long userId, String groupId) {
        try {
            log.info("删除用户 {} 的对话组 {}", userId, groupId);
            int deleted = chatLogMapper.deleteByUserIdAndGroupId(userId, groupId);
            return deleted > 0;
        } catch (Exception e) {
            log.error("删除对话失败", e);
            return false;
        }
    }
    
    @Override
    public List<Competition> intelligentSearch(String query, Long userId) {
        return aiServiceHelper.executeAIServiceCall(() -> {
            log.info("用户 {} 进行AI智能搜索: {}", userId, query);
            
            // 构建智能搜索提示词
            String prompt = promptManager.buildIntelligentSearchPrompt(query);
            
            // 调用AI模型获取搜索建议
            String aiResponse = aiServiceHelper.callAIModel(prompt);
            log.info("AI搜索建议: {}", aiResponse);
            
            // 获取所有竞赛并进行智能匹配
            List<Competition> allCompetitions = competitionMapper.selectAll();
            
            return allCompetitions.stream()
                    .filter(competition -> isRelevantToQuery(competition, query))
                    .limit(10)
                    .collect(Collectors.toList());
        }, ArrayList::new, "AI智能搜索");
    }
    
    @Override
    public Map<String, Object> getCompetitionMatchScore(Long competitionId, Long userId) {
        return aiServiceHelper.executeAIServiceCall(() -> {
            log.info("计算用户 {} 与竞赛 {} 的匹配度", userId, competitionId);
            
            // 获取用户和竞赛信息
            User user = getUserById(userId);
            List<UserSkill> userSkills = getUserSkills(userId);
            Competition competition = competitionMapper.selectById(competitionId);
            
            // 构建匹配度计算提示词
            String prompt = promptManager.buildMatchScorePrompt(user, userSkills, competition);
            
            // 调用AI模型
            String aiResponse = aiServiceHelper.callAIModel(prompt);
            
            Map<String, Object> result = new HashMap<>();
            result.put("aiAnalysis", aiResponse);
            result.put("score", 85.0);
            result.put("calculatedDate", new Date());
            result.put("status", "success");
            
            return result;
        }, () -> createFallbackMatchScore(), "AI匹配度计算");
    }
    
    // ==================== 辅助方法 ====================
    
    /**
     * 获取用户信息
     */
    private User getUserById(Long userId) {
        try {
            return userMapper.selectById(userId);
        } catch (Exception e) {
            log.error("获取用户信息失败: {}", userId, e);
            return null;
        }
    }
    
    /**
     * 获取用户技能
     */
    private List<UserSkill> getUserSkills(Long userId) {
        try {
            return userSkillMapper.selectByUserId(userId);
        } catch (Exception e) {
            log.error("获取用户技能失败: {}", userId, e);
            return new ArrayList<>();
        }
    }
    
    /**
     * 过滤竞赛列表
     */
    private List<Competition> filterCompetitions(List<Competition> competitions, String category, String difficulty) {
        List<Competition> filtered = new ArrayList<>(competitions);
        
        if (category != null && !category.isEmpty()) {
            filtered = filtered.stream()
                .filter(comp -> comp.getCategory() != null && comp.getCategory().equals(category))
                .collect(Collectors.toList());
            log.info("按类别 {} 过滤后的竞赛数量: {}", category, filtered.size());
        }
        
        if (difficulty != null && !difficulty.isEmpty()) {
            filtered = filtered.stream()
                .filter(comp -> comp.getDifficulty() != null && comp.getDifficulty().equals(difficulty))
                .collect(Collectors.toList());
            log.info("按难度 {} 过滤后的竞赛数量: {}", difficulty, filtered.size());
        }
        
        return filtered;
    }
    
    /**
     * 基于AI建议选择推荐竞赛
     */
    private List<Competition> selectRecommendationsBasedOnAI(User user, List<UserSkill> userSkills, 
                                                           List<Competition> allCompetitions, String aiResponse) {
        List<Competition> recommendations = new ArrayList<>();
        
        log.info("AI响应内容: {}", aiResponse);
        
        // 解析AI响应中的推荐编号
        List<Integer> recommendedIndices = parseRecommendedIndices(aiResponse);
        log.info("解析出的推荐编号: {}", recommendedIndices);
        
        // 根据AI推荐的编号选择竞赛
        for (Integer index : recommendedIndices) {
            if (index > 0 && index <= allCompetitions.size()) {
                recommendations.add(allCompetitions.get(index - 1));
            }
        }
        
        // 如果通过编号没有找到推荐，尝试通过标题匹配
        if (recommendations.isEmpty()) {
            log.info("通过编号未找到推荐，尝试通过标题匹配");
            recommendations = matchCompetitionsByTitle(aiResponse, allCompetitions);
        }
        
        // 如果仍然没有推荐，使用基于规则的推荐
        if (recommendations.isEmpty()) {
            log.info("通过标题未找到推荐，使用基于规则的推荐");
            recommendations = getRuleBasedRecommendations(user, userSkills, allCompetitions);
        }
        
        return recommendations.stream().limit(5).collect(Collectors.toList());
    }
    
    /**
     * 解析AI响应中的推荐编号
     */
    private List<Integer> parseRecommendedIndices(String aiResponse) {
        List<Integer> indices = new ArrayList<>();
        Pattern pattern = Pattern.compile("\\b([1-9]\\d*)\\b");
        Matcher matcher = pattern.matcher(aiResponse);
        
        while (matcher.find() && indices.size() < 5) {
            try {
                int number = Integer.parseInt(matcher.group(1));
                if (number <= 10) { // 假设竞赛编号在1-10之间
                    indices.add(number);
                }
            } catch (NumberFormatException e) {
                log.warn("解析编号失败: {}", matcher.group(1));
            }
        }
        
        return indices;
    }
    
    /**
     * 通过标题匹配竞赛
     */
    private List<Competition> matchCompetitionsByTitle(String aiResponse, List<Competition> competitions) {
        List<Competition> matched = new ArrayList<>();
        
        for (Competition competition : competitions) {
            String title = competition.getTitle();
            if (title != null && !title.isEmpty() && aiResponse.contains(title)) {
                log.info("通过标题匹配到竞赛: {}", title);
                matched.add(competition);
            }
        }
        
        return matched;
    }
    
    /**
     * 基于规则的推荐（AI推荐失败时的备选方案）
     */
    private List<Competition> getRuleBasedRecommendations(User user, List<UserSkill> userSkills, List<Competition> allCompetitions) {
        List<Competition> recommendations = new ArrayList<>();
        
        for (Competition competition : allCompetitions) {
            double matchScore = calculateSimpleMatchScore(user, userSkills, competition);
            if (matchScore > 0.3) {
                recommendations.add(competition);
            }
        }
        
        recommendations.sort((c1, c2) -> Double.compare(
            calculateSimpleMatchScore(user, userSkills, c2),
            calculateSimpleMatchScore(user, userSkills, c1)
        ));
        
        return recommendations;
    }
    
    /**
     * 计算简单的匹配度评分
     */
    private double calculateSimpleMatchScore(User user, List<UserSkill> userSkills, Competition competition) {
        double score = 0.0;
        
        // 专业匹配度 (40%)
        if (user.getMajor() != null && competition.getCategory() != null) {
            if (user.getMajor().contains("计算机") && competition.getCategory().contains("编程")) {
                score += 0.4;
            } else if (user.getMajor().contains("管理") && competition.getCategory().contains("创业")) {
                score += 0.4;
            } else if (user.getMajor().contains("数学") && competition.getCategory().contains("数学")) {
                score += 0.4;
            }
        }
        
        // 技能匹配度 (40%)
        if (!userSkills.isEmpty() && competition.getTags() != null) {
            String tags = competition.getTags().toLowerCase();
            int matchedSkills = 0;
            for (UserSkill skill : userSkills) {
                if (tags.contains(skill.getSkill().toLowerCase())) {
                    matchedSkills++;
                }
            }
            score += (double) matchedSkills / userSkills.size() * 0.4;
        }
        
        // 基础分数 (20%)
        score += 0.2;
        return Math.min(score, 1.0);
    }
    
    /**
     * 判断竞赛是否与查询相关
     */
    private boolean isRelevantToQuery(Competition competition, String query) {
        String lowerQuery = query.toLowerCase();
        return competition.getTitle().toLowerCase().contains(lowerQuery) ||
               competition.getCategory().toLowerCase().contains(lowerQuery) ||
               (competition.getTags() != null && competition.getTags().toLowerCase().contains(lowerQuery));
    }
    
    /**
     * 计算技能评分
     */
    private Map<String, Integer> calculateSkillScores(List<UserSkill> userSkills) {
        Map<String, Integer> skillScores = new HashMap<>();
        Map<String, Integer> baseScores = new HashMap<>();
        baseScores.put("Java", 85);
        baseScores.put("Python", 80);
        baseScores.put("算法设计", 90);
        baseScores.put("数据结构", 88);
        baseScores.put("JavaScript", 75);
        baseScores.put("React", 70);
        baseScores.put("MySQL", 82);
        baseScores.put("数据分析", 78);
        baseScores.put("项目管理", 85);
        
        for (UserSkill skill : userSkills) {
            int score = baseScores.getOrDefault(skill.getSkill(), 70);
            skillScores.put(skill.getSkill(), score);
        }
        
        return skillScores;
    }
    
    /**
     * 计算总体技能评分
     */
    private int calculateOverallScore(Map<String, Integer> skillScores) {
        return skillScores.values().stream()
                .mapToInt(Integer::intValue)
                .sum() / Math.max(skillScores.size(), 1);
    }
    
    // ==================== 数据库操作方法 ====================
    
    /**
     * 保存推荐结果到数据库
     */
    private void saveRecommendationsToDatabase(Long userId, AIRecommendationResponse response) {
        try {
            CompetitionRecommendation recommendation = new CompetitionRecommendation();
            recommendation.setUserId(userId);
            recommendation.setCompetitionId(1L);
            recommendation.setScore(85.0f);
            recommendation.setSuggestions(response.getSummary());
            recommendation.setCreatedAt(LocalDateTime.now());
            
            recommendationMapper.insert(recommendation);
        } catch (Exception e) {
            log.warn("保存推荐结果到数据库失败", e);
        }
    }
    
    /**
     * 保存聊天记录（统一方法）
     */
    private void saveChatLog(Long userId, String input, String response, String type, String groupId) {
        try {
            AIChatLog chatLog = new AIChatLog();
            chatLog.setUserId(userId);
            chatLog.setType(type);
            chatLog.setInput(input);
            chatLog.setResponse(response);
            chatLog.setTimestamp(LocalDateTime.now());
            chatLog.setGroupId(groupId);
            chatLogMapper.insert(chatLog);
            log.info("保存聊天记录成功 - 用户ID: {}, 组ID: {}", userId, groupId);
        } catch (Exception e) {
            log.error("保存聊天记录失败 - 用户ID: {}, 组ID: {}", userId, groupId, e);
        }
    }
    
    /**
     * 保存推荐记录
     */
    private void saveRecommendationRecords(Long userId, List<Competition> recommendations, User user, List<UserSkill> userSkills) {
        for (Competition competition : recommendations) {
            try {
                double matchScore = calculateSimpleMatchScore(user, userSkills, competition);
                CompetitionRecommendation recommendation = new CompetitionRecommendation();
                recommendation.setUserId(userId);
                recommendation.setCompetitionId(competition.getId());
                recommendation.setScore((float) matchScore);
                recommendation.setSuggestions("AI智能推荐，匹配度: " + Math.round(matchScore * 100) + "%");
                recommendation.setCreatedAt(LocalDateTime.now());
                
                recommendationMapper.insert(recommendation);
            } catch (Exception e) {
                log.warn("保存推荐记录失败", e);
            }
        }
    }
    
    // ==================== 解析和处理方法 ====================
    
    /**
     * 解析推荐响应
     */
    private AIRecommendationResponse parseRecommendationResponse(String aiResponse, AIRecommendationRequest request) {
        AIRecommendationResponse response = new AIRecommendationResponse();
        response.setCompetitions(new ArrayList<>());
        response.setSummary("基于AI大模型分析：" + aiResponse);
        response.setTimestamp(new Date());
        return response;
    }
    
    /**
     * 解析技能分析响应
     */
    private AIAnalysisResponse parseSkillAnalysisResponse(String aiResponse) {
        AIAnalysisResponse response = new AIAnalysisResponse();
        response.setOverallScore(85);
        response.setSkillLevels(new HashMap<>());
        response.setStrengths(Arrays.asList("AI分析：" + aiResponse));
        response.setImprovements(Arrays.asList("基于AI建议的改进方案"));
        response.setTimestamp(new Date());
        return response;
    }
    
    // ==================== 降级响应方法 ====================
    
    private AIRecommendationResponse createFallbackRecommendation(AIRecommendationRequest request) {
        AIRecommendationResponse response = new AIRecommendationResponse();
        response.setCompetitions(new ArrayList<>());
        response.setSummary("AI服务暂时不可用，请稍后再试");
        response.setTimestamp(new Date());
        return response;
    }
    
    private AIChatResponse createFallbackChatResponse() {
        AIChatResponse response = new AIChatResponse();
        response.setMessage("抱歉，AI助手暂时不可用，请稍后再试。");
        response.setTimestamp(new Date());
        response.setMessageId(UUID.randomUUID().toString());
        return response;
    }
    
    private AIAnalysisResponse createFallbackSkillAnalysis() {
        AIAnalysisResponse response = new AIAnalysisResponse();
        response.setOverallScore(0);
        response.setSkillLevels(new HashMap<>());
        response.setStrengths(Arrays.asList("AI分析服务暂时不可用"));
        response.setImprovements(Arrays.asList("请稍后再试"));
        response.setTimestamp(new Date());
        return response;
    }
    
    private Map<String, Object> createFallbackChatMap() {
        Map<String, Object> fallback = new HashMap<>();
        fallback.put("message", "抱歉，AI服务暂时不可用，请稍后再试");
        fallback.put("timestamp", System.currentTimeMillis());
        fallback.put("status", "error");
        return fallback;
    }
    
    private Map<String, Object> createFallbackSkillAnalysisMap() {
        Map<String, Object> fallback = new HashMap<>();
        fallback.put("overallScore", 0);
        fallback.put("message", "技能分析服务异常");
        fallback.put("status", "error");
        return fallback;
    }
    
    private Map<String, Object> createFallbackLearningPath() {
        Map<String, Object> fallback = new HashMap<>();
        fallback.put("message", "学习路径生成服务异常");
        fallback.put("status", "error");
        return fallback;
    }
    
    private Map<String, Object> createFallbackTrends() {
        Map<String, Object> fallback = new HashMap<>();
        fallback.put("message", "竞赛趋势分析服务异常");
        fallback.put("status", "error");
        return fallback;
    }
    
    private Map<String, Object> createFallbackPreparationAdvice() {
        Map<String, Object> fallback = new HashMap<>();
        fallback.put("message", "准备建议服务异常");
        fallback.put("status", "error");
        return fallback;
    }
    
    private Map<String, Object> createFallbackMatchScore() {
        Map<String, Object> fallback = new HashMap<>();
        fallback.put("score", 0);
        fallback.put("message", "匹配度计算服务异常");
        fallback.put("status", "error");
        return fallback;
    }
}
