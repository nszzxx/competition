package com.cdnu.cgi.mapper;

import com.cdnu.cgi.dto.CompetitionTeamCardDto;
import com.cdnu.cgi.entity.Team;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 团队数据访问接口
 */
@Mapper
public interface TeamMapper {
    
    /**
     * 根据ID查找团队
     */
    Team selectById(Long id);
    
    /**
     * 根据竞赛ID查找团队
     */
    List<Team> selectByCompetitionId(Long competitionId);
    
    /**
     * 根据队长ID查找团队
     */
    List<Team> selectByLeaderId(Long leaderId);
    
    /**
     * 根据团队名称查找
     */
    Team selectByName(String name);
    
    /**
     * 检查团队名称是否存在
     */
    boolean existsByName(String name);
    
    /**
     * 根据竞赛ID和队长ID查找团队
     */
    Team selectByCompetitionIdAndLeaderId(@Param("competitionId") Long competitionId, @Param("leaderId") Long leaderId);
    
    /**
     * 根据团队名称模糊查询
     */
    List<Team> selectByNameContainingIgnoreCase(String name);
    
    /**
     * 根据团队名称和竞赛ID模糊查询
     */
    List<Team> selectByNameContainingIgnoreCaseAndCompetitionId(@Param("name") String name, @Param("competitionId") Long competitionId);
    
    /**
     * 查询所有团队
     */
    List<Team> selectAll();
    
    /**
     * 插入团队
     */
    int insert(Team team);
    
    /**
     * 更新团队信息
     */
    int updateById(Team team);
    
    /**
     * 根据ID删除团队
     */
    int deleteById(Long id);
    
    /**
     * 统计团队总数
     */
    long count();
    
    /**
     * 根据竞赛ID获取团队卡片信息
     */
    List<CompetitionTeamCardDto> selectCompetitionTeamCards(Long competitionId);
}
