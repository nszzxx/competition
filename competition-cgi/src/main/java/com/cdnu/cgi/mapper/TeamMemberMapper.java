package com.cdnu.cgi.mapper;

import com.cdnu.cgi.entity.TeamMember;
import com.cdnu.cgi.entity.TeamMemberDetailInfo;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 团队成员数据访问接口
 */
@Mapper
public interface TeamMemberMapper {
    
    /**
     * 根据ID查找团队成员
     */
    TeamMember selectById(Long id);
    
    /**
     * 根据团队ID查找成员
     */
    List<TeamMember> selectByTeamId(Long teamId);
    
    /**
     * 根据用户ID查找团队成员记录
     */
    List<TeamMember> selectByUserId(Long userId);
    
    /**
     * 根据团队ID和用户ID查找成员
     */
    TeamMember selectByTeamIdAndUserId(@Param("teamId") Long teamId, @Param("userId") Long userId);
    
    /**
     * 根据团队ID和状态查找成员
     */
    List<TeamMember> selectByTeamIdAndStatus(@Param("teamId") Long teamId, @Param("status") String status);
    
    /**
     * 根据用户ID和状态查找团队成员记录
     */
    List<TeamMember> selectByUserIdAndStatus(@Param("userId") Long userId, @Param("status") String status);
    
    /**
     * 统计团队活跃成员数量
     */
    Long countActiveTeamMembers(Long teamId);
    
    /**
     * 检查用户是否已经是团队活跃成员
     */
    boolean existsByTeamIdAndUserIdAndStatusActive(@Param("teamId") Long teamId, @Param("userId") Long userId);
    
    /**
     * 检查用户是否已经是团队成员（简化版本）
     */
    boolean existsByTeamIdAndUserId(@Param("teamId") Long teamId, @Param("userId") Long userId);
    
    /**
     * 查询所有团队成员
     */
    List<TeamMember> selectAll();
    
    /**
     * 插入团队成员
     */
    int insert(TeamMember teamMember);
    
    /**
     * 更新团队成员信息
     */
    int updateById(TeamMember teamMember);
    
    /**
     * 根据ID删除团队成员
     */
    int deleteById(Long id);
    
    /**
     * 根据团队ID删除所有成员
     */
    void deleteByTeamId(Long teamId);
    
    /**
     * 统计团队成员总数
     */
    long count();
    
    /**
     * 根据团队ID查询团队成员详细信息（包含技能和荣誉）
     * 调用数据库视图获取完整信息
     */
    List<TeamMemberDetailInfo> selectTeamMemberDetailsByTeamId(Long teamId);
    
    /**
     * 根据团队ID和用户ID查询特定成员的详细信息
     */
    TeamMemberDetailInfo selectTeamMemberDetailByTeamIdAndUserId(@Param("teamId") Long teamId, @Param("userId") Long userId);
}
