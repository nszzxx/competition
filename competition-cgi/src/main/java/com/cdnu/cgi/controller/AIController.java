package com.cdnu.cgi.controller;

import com.cdnu.cgi.dto.AIChatRequest;
import com.cdnu.cgi.dto.AIChatResponse;
import com.cdnu.cgi.entity.AIChatLog;
import com.cdnu.cgi.entity.Competition;
import com.cdnu.cgi.entity.CompetitionRecommendation;
import com.cdnu.cgi.mapper.AIChatLogMapper;
import com.cdnu.cgi.service.User.AIService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/ai")
@RequiredArgsConstructor
public class AIController {
    private final AIService aiService;
    private final AIChatLogMapper chatLogMapper;

    /**
     * 获取AI推荐竞赛
     */
    @PostMapping("/recommendations")
    public ResponseEntity<List<Competition>> getRecommendations(@RequestBody Map<String, Object> requestData) {
        try {
            Long userId = Long.valueOf(requestData.get("userId").toString());
            
            // 获取类别和难度过滤条件
            String category = requestData.containsKey("category") ? 
                requestData.get("category").toString() : null;
            String difficulty = requestData.containsKey("difficulty") ? 
                requestData.get("difficulty").toString() : null;
            
            log.info("获取AI推荐，用户ID: {}, 类别: {}, 难度: {}", userId, category, difficulty);
            
            // 调用服务层方法，传入过滤条件
            List<Competition> recommendations = aiService.getRecommendations(userId, category, difficulty);
            
            // 记录返回的推荐结果数量
            log.info("返回推荐结果数量: {}", recommendations.size());
            if (recommendations.isEmpty()) {
                log.warn("没有找到符合条件的推荐结果");
            } else {
                // 记录所有推荐结果的详情，帮助调试
                for (int i = 0; i < recommendations.size(); i++) {
                    Competition rec = recommendations.get(i);
                    log.info("推荐 #{}: ID={}, 标题={}, 类别={}, 难度={}, 标签={}", 
                        i+1, rec.getId(), rec.getTitle(), rec.getCategory(), 
                        rec.getDifficulty(), rec.getTags());
                }
            }
            
            return ResponseEntity.ok(recommendations);
        } catch (Exception e) {
            log.error("获取AI推荐失败", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * AI聊天接口
     */
    @PostMapping("/chat")
    public ResponseEntity<Map<String, Object>> chat(@RequestBody Map<String, Object> requestData) {
        try {
            String message = requestData.get("message").toString();
            Long userId = requestData.containsKey("userId") ?
                Long.valueOf(requestData.get("userId").toString()) : 1L; // 默认用户ID为1L
            String groupId = requestData.containsKey("groupId") ?
                requestData.get("groupId").toString() : null;

            // 构建AIChatRequest对象
            AIChatRequest chatRequest = new AIChatRequest();
            chatRequest.setMessage(message);
            chatRequest.setUserId(userId);
            chatRequest.setGroupId(groupId);

            // 调用AIService的chat方法
            AIChatResponse chatResponse = aiService.chat(chatRequest);

            // 构建返回结果
            Map<String, Object> response = new HashMap<>();
            response.put("message", chatResponse.getMessage());
            response.put("timestamp", chatResponse.getTimestamp());
            response.put("messageId", chatResponse.getMessageId());

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("AI聊天失败", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * 技能分析
     */
    @PostMapping("/analyze-skills")
    public ResponseEntity<Map<String, Object>> analyzeSkills(@RequestBody Map<String, Object> requestData) {
        try {
            Long userId = Long.valueOf(requestData.get("userId").toString());
            Map<String, Object> analysis = aiService.analyzeUserSkills(userId);
            return ResponseEntity.ok(analysis);
        } catch (Exception e) {
            log.error("技能分析失败", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * 生成学习路径
     */
    @PostMapping("/learning-path")
    public ResponseEntity<Map<String, Object>> generateLearningPath(@RequestBody Map<String, Object> requestData) {
        try {
            Long userId = Long.valueOf(requestData.get("userId").toString());
            Long targetCompetitionId = requestData.containsKey("targetCompetitionId") ? 
                Long.valueOf(requestData.get("targetCompetitionId").toString()) : null;
            
            Map<String, Object> learningPath = aiService.generateLearningPath(userId, targetCompetitionId);
            return ResponseEntity.ok(learningPath);
        } catch (Exception e) {
            log.error("生成学习路径失败", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * 获取竞赛趋势分析
     */
    @PostMapping("/trends")
    public ResponseEntity<Map<String, Object>> getCompetitionTrends(@RequestBody Map<String, Object> requestData) {
        try {
            // 提取用户信息和竞赛信息
            Long userId = requestData.containsKey("userId") && requestData.get("userId") != null ? 
                Long.valueOf(requestData.get("userId").toString()) : null;
            Long participatedCompetitionId = requestData.containsKey("participatedCompetitionId") && requestData.get("participatedCompetitionId") != null ? 
                Long.valueOf(requestData.get("participatedCompetitionId").toString()) : null;
            Long availableCompetitionId = requestData.containsKey("availableCompetitionId") && requestData.get("availableCompetitionId") != null ? 
                Long.valueOf(requestData.get("availableCompetitionId").toString()) : null;
            
            log.info("获取竞赛趋势分析 - 用户ID: {}, 参与竞赛ID: {}, 目标竞赛ID: {}", 
                userId, participatedCompetitionId, availableCompetitionId);
            
            Map<String, Object> trends = aiService.getCompetitionTrends(userId, participatedCompetitionId, availableCompetitionId, requestData);
            return ResponseEntity.ok(trends);
        } catch (Exception e) {
            log.error("获取竞赛趋势失败", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * 获取准备建议
     */
    @PostMapping("/preparation-advice")
    public ResponseEntity<Map<String, Object>> getPreparationAdvice(@RequestBody Map<String, Object> requestData) {
        try {
            Long competitionId = Long.valueOf(requestData.get("competitionId").toString());
            Long userId = Long.valueOf(requestData.get("userId").toString());
            
            Map<String, Object> advice = aiService.getPreparationAdvice(competitionId, userId);
            return ResponseEntity.ok(advice);
        } catch (Exception e) {
            log.error("获取准备建议失败", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * 获取用户的推荐记录
     */
    @GetMapping("/recommendations/{userId}")
    public ResponseEntity<List<CompetitionRecommendation>> getUserRecommendations(@PathVariable Long userId) {
        try {
            List<CompetitionRecommendation> recommendations = aiService.getUserRecommendations(userId);
            return ResponseEntity.ok(recommendations);
        } catch (Exception e) {
            log.error("获取用户推荐记录失败", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * 智能搜索
     */
    @PostMapping("/search")
    public ResponseEntity<List<Competition>> intelligentSearch(@RequestBody Map<String, Object> requestData) {
        try {
            // 安全地获取查询参数，如果不存在则使用空字符串
            String query = requestData.containsKey("query") && requestData.get("query") != null ? 
                requestData.get("query").toString() : "";
                
            // 记录接收到的查询参数
            log.info("接收到智能搜索请求，查询内容: {}", query);
            
            if (query.isEmpty()) {
                log.warn("查询内容为空，返回空结果");
                return ResponseEntity.ok(new ArrayList<>());
            }
            
            Long userId = requestData.containsKey("userId") && requestData.get("userId") != null ? 
                Long.valueOf(requestData.get("userId").toString()) : null;
            
            List<Competition> results = aiService.intelligentSearch(query, userId);
            log.info("智能搜索返回结果数量: {}", results.size());
            
            return ResponseEntity.ok(results);
        } catch (Exception e) {
            log.error("智能搜索失败", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * 获取竞赛匹配度
     */
    @PostMapping("/match-score")
    public ResponseEntity<Map<String, Object>> getMatchScore(@RequestBody Map<String, Object> requestData) {
        try {
            Long competitionId = Long.valueOf(requestData.get("competitionId").toString());
            Long userId = Long.valueOf(requestData.get("userId").toString());
            
            Map<String, Object> matchScore = aiService.getCompetitionMatchScore(competitionId, userId);
            return ResponseEntity.ok(matchScore);
        } catch (Exception e) {
            log.error("获取竞赛匹配度失败", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * 获取用户聊天历史分组
     * 使用缓存减少数据库查询，并优化错误处理
     */
    @GetMapping("/chat/history/{userId}")
    public ResponseEntity<?> getChatHistory(@PathVariable Long userId,
                                            @RequestParam(defaultValue = "20") int limit) {
        try {
            // 调用服务层方法获取聊天历史
            List<Map<String, Object>> result = aiService.getChatHistoryGroups(userId, limit);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", "获取聊天历史失败");
            errorResponse.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    /**
     * 获取特定对话的聊天记录
     */
    @GetMapping("/chat/conversation/{userId}/{groupId}")
    public ResponseEntity<?> getConversationHistory(@PathVariable Long userId, 
                                                   @PathVariable String groupId) {
        try {
            log.info("获取用户 {} 的对话组 {} 历史", userId, groupId);
            
            // 使用 group_id 查询对话记录
            List<AIChatLog> chatLogs = chatLogMapper.selectByUserIdAndGroupId(userId, groupId);
            
            log.info("成功获取用户 {} 对话组 {} 的 {} 条记录", userId, groupId, chatLogs.size());
            return ResponseEntity.ok(chatLogs);
            
        } catch (Exception e) {
            log.error("获取对话历史失败", e);
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", "获取对话历史失败: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(errorResponse);
        }
    }
    
    /**
     * 删除特定对话组的聊天记录
     */
    @DeleteMapping("/chat/conversation/{userId}/{groupId}")
    public ResponseEntity<?> deleteConversation(@PathVariable Long userId, 
                                              @PathVariable String groupId) {
        try {
            log.info("删除用户 {} 的对话组 {}", userId, groupId);
            
            // 使用 group_id 删除对话记录
            int deletedCount = chatLogMapper.deleteByUserIdAndGroupId(userId, groupId);
            
            log.info("成功删除用户 {} 对话组 {} 的 {} 条记录", userId, groupId, deletedCount);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "对话记录已删除");
            response.put("deletedCount", deletedCount);
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("删除对话历史失败", e);
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", "删除对话历史失败: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(errorResponse);
        }
    }
}