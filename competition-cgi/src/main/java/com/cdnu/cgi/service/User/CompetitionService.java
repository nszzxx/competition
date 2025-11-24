package com.cdnu.cgi.service.User;

import com.cdnu.cgi.dto.UserCompetitionDTO;
import com.cdnu.cgi.entity.Competition;
import com.cdnu.cgi.entity.CompetitionTeamUser;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * 竞赛服务接口
 */
@Service
public interface CompetitionService {

    // 获取所有竞赛
    List<Competition> getAllCompetitions();

    // 根据ID获取竞赛
    Optional<Competition> getCompetitionById(Long id);

    // 根据类别获取竞赛
    List<Competition> getCompetitionsByCategory(String category);

    // 搜索竞赛
    List<Competition> searchCompetitions(String keyword);

    // 多条件搜索竞赛
    List<Competition> searchCompetitions(String category, String keyword, String participationMode);
    List<Competition> searchByKeyword(String keyword);

    // 获取正在进行的竞赛
    List<Competition> getActiveCompetitions();

    // 根据标签获取竞赛
    List<Competition> getCompetitionsByTag(String tag);

    // 保存竞赛
    Competition saveCompetition(Competition competition);

    // 删除竞赛
    void deleteCompetition(Long id);

    // 获取竞赛分类统计
    Map<String, Long> getCompetitionCategories();

    // 获取热门竞赛
    List<Competition> getPopularCompetitions(int limit);

    // 获取即将开始的竞赛
    List<Competition> getUpcomingCompetitions(int limit);

    // 根据参赛模式获取竞赛
    List<Competition> getCompetitionsByParticipationMode(String mode);

    // 获取用户参加的竞赛（包含参赛方式信息）
    List<UserCompetitionDTO> getUserCompetitions(Long userId);

    // 参赛（报名）
    void participateCompetition(CompetitionTeamUser competitionTeamUser);

    // 取消参赛
    void cancelParticipation(Long competitionId, Long userId, Long teamId);

    // 查询某竞赛所有参赛详情
    List<CompetitionTeamUser> getCompetitionParticipants(Long competitionId);

    // 查询某竞赛所有团队参赛详情
    List<CompetitionTeamUser> getCompetitionTeamParticipants(Long competitionId);

    // 查询某竞赛所有个人参赛详情
    List<CompetitionTeamUser> getCompetitionIndividualParticipants(Long competitionId);

    // 查询某团队所有参赛详情
    List<CompetitionTeamUser> getTeamParticipants(Long teamId);

    // 查询某用户所有参赛详情
    List<CompetitionTeamUser> getUserParticipationDetails(Long userId);

    // 多条件查询参赛详情
    List<CompetitionTeamUser> getParticipationDetails(CompetitionTeamUser competitionTeamUser);
}
