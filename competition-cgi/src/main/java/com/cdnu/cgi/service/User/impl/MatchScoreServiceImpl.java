package com.cdnu.cgi.service.User.impl;

import com.cdnu.cgi.dto.CompetitionTeamCardDto;
import com.cdnu.cgi.entity.UserSkill;
import com.cdnu.cgi.mapper.TeamMapper;
import com.cdnu.cgi.mapper.UserSkillMapper;
import com.cdnu.cgi.service.User.MatchScoreService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 匹配度计算服务实现类
 */
@Service
@RequiredArgsConstructor
public class MatchScoreServiceImpl implements MatchScoreService {

    private final UserSkillMapper userSkillMapper;
    private final TeamMapper teamMapper;

    @Override
    public int calculateMatchScore(Long teamId, Long userId) {
        // 获取用户技能
        List<UserSkill> userSkills = userSkillMapper.selectByUserId(userId);
        if (userSkills == null || userSkills.isEmpty()) {
            return 20; // 用户没有技能，返回较低的匹配度
        }
        
        // 获取团队所需技能 - 使用TeamMapper
        String teamNeededSkills = teamMapper.selectById(teamId).getNeedSkills();
        if (teamNeededSkills == null || teamNeededSkills.trim().isEmpty()) {
            return 60; // 团队没有特定需求，返回中等匹配度
        }
        
        // 解析团队所需技能（支持中英文逗号）
        List<String> teamSkillsList = parseSkills(teamNeededSkills);
        if (teamSkillsList.isEmpty()) {
            return 60;
        }
        
        // 解析用户技能
        List<String> userSkillsList = userSkills.stream()
                .map(UserSkill::getSkill)
                .collect(Collectors.toList());
                
        // 计算匹配的技能数量
        long matchCount = userSkillsList.stream()
                .filter(teamSkillsList::contains)
                .count();
                
        // 计算匹配百分比
        double matchPercentage = (double) matchCount / teamSkillsList.size();
        
        // 转换为0-100的分数，最低20分
        int score = (int) (20 + matchPercentage * 80);
        return Math.min(100, score);
    }
    
    @Override
    public Map<Long, Integer> calculateBatchMatchScores(List<Long> teamIds, Long userId) {
        if (teamIds == null || teamIds.isEmpty() || userId == null) {
            return Collections.emptyMap();
        }
        
        // 获取用户技能
        List<UserSkill> userSkills = userSkillMapper.selectByUserId(userId);
        List<String> userSkillsList = userSkills.stream()
                .map(UserSkill::getSkill)
                .collect(Collectors.toList());
                
        // 获取所有团队信息
        Map<Long, Integer> result = new HashMap<>();
        for (Long teamId : teamIds) {
            // 使用TeamMapper获取团队信息
            String teamNeededSkills = teamMapper.selectById(teamId).getNeedSkills();
            
            if (teamNeededSkills == null || teamNeededSkills.trim().isEmpty()) {
                result.put(teamId, 60); // 团队没有特定需求，返回中等匹配度
                continue;
            }
            
            // 解析团队所需技能（支持中英文逗号）
            List<String> teamSkillsList = parseSkills(teamNeededSkills);
            
            if (teamSkillsList.isEmpty()) {
                result.put(teamId, 60);
                continue;
            }

            if (userSkillsList.isEmpty()) {
                result.put(teamId, 20); // 用户没有技能，返回较低的匹配度
                continue;
            }

            // 计算匹配的技能数量
            long matchCount = userSkillsList.stream()
                    .filter(teamSkillsList::contains)
                    .count();

            // 计算匹配百分比
            double matchPercentage = (double) matchCount / teamSkillsList.size();

            // 转换为0-100的分数，最低20分
            int score = (int) (20 + matchPercentage * 80);
            result.put(teamId, Math.min(100, score));
        }

        return result;
    }

    @Override
    public List<CompetitionTeamCardDto> calculateTeamCardsMatchScores(List<CompetitionTeamCardDto> teamCards, Long userId) {
        if (teamCards == null || teamCards.isEmpty() || userId == null) {
            return teamCards;
        }

        // 获取用户技能
        List<UserSkill> userSkills = userSkillMapper.selectByUserId(userId);
        List<String> userSkillsList = userSkills.stream()
                .map(UserSkill::getSkill)
                .collect(Collectors.toList());
        
        // 获取用户ID字符串，用于检查用户是否已经是团队成员
        String userIdStr = String.valueOf(userId);

        // 计算每个团队卡片的匹配度
        for (CompetitionTeamCardDto teamCard : teamCards) {
            // 检查用户是否已经是团队成员
            if (teamCard.getMemberIds() != null && Arrays.asList(teamCard.getMemberIds().split(",")).contains(userIdStr)) {
                // 如果用户已经是团队成员，则匹配度为100%
                teamCard.setMatchScore(100);
                continue;
            }
            
            // 如果团队已经有技能，可以直接使用团队技能与用户技能比较
            if (teamCard.getTeamSkills() != null && !teamCard.getTeamSkills().trim().isEmpty()) {
                // 团队已有技能，计算用户技能与团队已有技能的互补性
                List<String> teamExistingSkills = parseSkills(teamCard.getTeamSkills());
                
                // 如果用户有团队已有的技能，则匹配度较低（团队已经有这些技能了）
                Set<String> duplicateSkills = userSkillsList.stream()
                        .filter(teamExistingSkills::contains)
                        .collect(Collectors.toSet());
                
                // 如果重复技能占用户技能的比例高，则匹配度降低
                if (!userSkillsList.isEmpty()) {
                    double duplicateRatio = (double) duplicateSkills.size() / userSkillsList.size();
                    if (duplicateRatio > 0.7) {
                        // 技能重复率高，降低匹配度
                        teamCard.setMatchScore(40);
                        continue;
                    }
                }
            }
            
            // 处理团队所需技能
            String teamNeededSkills = teamCard.getNeedSkills();
            
            if (teamNeededSkills == null || teamNeededSkills.trim().isEmpty()) {
                teamCard.setMatchScore(60); // 团队没有特定需求，返回中等匹配度
                continue;
            }

            // 解析团队所需技能（支持中英文逗号）
            List<String> teamSkillsList = parseSkills(teamNeededSkills);
            
            if (teamSkillsList.isEmpty()) {
                teamCard.setMatchScore(60);
                continue;
            }

            if (userSkillsList.isEmpty()) {
                teamCard.setMatchScore(20); // 用户没有技能，返回较低的匹配度
                continue;
            }

            // 计算匹配的技能数量
            long matchCount = userSkillsList.stream()
                    .filter(teamSkillsList::contains)
                    .count();

            // 计算匹配百分比
            double matchPercentage = (double) matchCount / teamSkillsList.size();

            // 转换为0-100的分数，最低20分
            int score = (int) (20 + matchPercentage * 80);
            teamCard.setMatchScore(Math.min(100, score));
        }

        return teamCards;
    }
    
    /**
     * 处理技能字符串，支持中英文逗号
     * @param skills 技能字符串
     * @return 处理后的技能列表
     */
    private List<String> parseSkills(String skills) {
        if (skills == null || skills.trim().isEmpty()) {
            return new ArrayList<>();
        }
        
        // 替换中文逗号为英文逗号
        String normalizedSkills = skills.replace("，", ",");
        
        // 分割并去除空白
        return Arrays.stream(normalizedSkills.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .collect(Collectors.toList());
    }
}