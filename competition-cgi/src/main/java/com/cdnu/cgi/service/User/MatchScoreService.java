package com.cdnu.cgi.service.User;

import com.cdnu.cgi.dto.CompetitionTeamCardDto;

import java.util.List;
import java.util.Map;

/**
 * 适配度计算服务接口
 */
public interface MatchScoreService {
    
    /**
     * 计算用户与团队的适配度
     * @param teamId 团队ID
     * @param userId 用户ID
     * @return 适配度分数（0-100）
     */
    int calculateMatchScore(Long teamId, Long userId);
    
    /**
     * 批量计算用户与多个团队的适配度
     * @param teamIds 团队ID列表
     * @param userId 用户ID
     * @return 团队ID到适配度分数的映射
     */
    Map<Long, Integer> calculateBatchMatchScores(List<Long> teamIds, Long userId);
    
    /**
     * 为团队卡片列表计算适配度
     * @param teamCards 团队卡片列表
     * @param userId 用户ID
     * @return 带有适配度的团队卡片列表
     */
    List<CompetitionTeamCardDto> calculateTeamCardsMatchScores(List<CompetitionTeamCardDto> teamCards, Long userId);
}
