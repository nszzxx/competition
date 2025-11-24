package com.cdnu.cgi.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.cdnu.cgi.entity.CompetitionTeamUser;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface CompetitionTeamUserMapper extends BaseMapper<CompetitionTeamUser> {

    /**
     * 根据竞赛ID查询所有参赛详情
     */
    List<CompetitionTeamUser> selectByCompetitionId(@Param("competitionId") Long competitionId);

    /**
     * 根据团队ID查询所有参赛详情
     */
    List<CompetitionTeamUser> selectByTeamId(@Param("teamId") Long teamId);

    /**
     * 根据用户ID查询所有参赛详情
     */
    List<CompetitionTeamUser> selectByUserId(@Param("userId") Long userId);

    /**
     * 根据用户ID和竞赛ID查询参赛详情（用于检查用户是否已参加某竞赛）
     */
    List<CompetitionTeamUser> selectByUserIdAndCompetitionId(@Param("userId") Long userId, @Param("competitionId") Long competitionId);

    /**
     * 根据多条件查询参赛详情
     */
    List<CompetitionTeamUser> selectByConditions(
            @Param("competitionId") Long competitionId,
            @Param("teamId") Long teamId,
            @Param("userId") Long userId,
            @Param("participationMode") String participationMode
    );

    /**
     * 查询某竞赛所有团队参赛详情
     */
    List<CompetitionTeamUser> getCompetitionTeamParticipants(Long competitionId);

    /**
     * 查询某竞赛所有个人参赛详情
     */
    List<CompetitionTeamUser> getCompetitionIndividualParticipants(Long competitionId);

    /**
     * 根据竞赛ID和用户ID删除参赛记录（个人参赛）
     */
    void deleteByCompetitionIdAndUserId(@Param("competitionId") Long competitionId, @Param("userId") Long userId);

    /**
     * 根据竞赛ID和团队ID删除所有参赛记录（团队参赛）
     */
    void deleteByCompetitionIdAndTeamId(@Param("competitionId") Long competitionId, @Param("teamId") Long teamId);

    /**
     * 精确删除某个用户在某个竞赛某个团队的参赛记录
     */
    void deleteByCompetitionIdTeamIdAndUserId(
        @Param("competitionId") Long competitionId,
        @Param("teamId") Long teamId,
        @Param("userId") Long userId
    );
}
