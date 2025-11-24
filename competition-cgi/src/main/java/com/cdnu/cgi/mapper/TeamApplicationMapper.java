package com.cdnu.cgi.mapper;

import com.cdnu.cgi.entity.TeamApplication;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 团队申请数据访问层
 */
@Mapper
public interface TeamApplicationMapper {
    
    /**
     * 插入团队申请
     */
    int insert(TeamApplication teamApplication);
    
    /**
     * 根据ID查询团队申请
     */
    TeamApplication selectById(Long id);
    
    /**
     * 根据用户ID和团队ID查询申请
     */
    TeamApplication selectByUserIdAndTeamId(@Param("userId") Long userId, @Param("teamId") Long teamId);
    
    /**
     * 更新团队申请
     */
    int updateById(TeamApplication teamApplication);
    
    /**
     * 删除团队申请
     */
    int deleteById(Long id);
    
    /**
     * 根据团队ID查询所有申请
     */
    List<TeamApplication> selectByTeamId(Long teamId);
    
    /**
     * 根据用户ID查询所有申请
     */
    List<TeamApplication> selectByUserId(Long userId);

    /**
     * 根据用户ID和type查询（用于区分申请和邀请）
     */
    List<TeamApplication> selectByUserIdAndType(@Param("userId") Long userId, @Param("type") String type);

    /**
     * 检查是否存在反向记录（用于自动入队逻辑）
     * 例如：如果A邀请B，检查是否存在B申请A的记录
     */
    TeamApplication selectReverseRecord(@Param("userId") Long userId, @Param("teamId") Long teamId, @Param("type") String type);
    
    /**
     * 根据队长ID查询待审核的申请
     */
    List<TeamApplication> selectPendingByLeaderId(Long leaderId);
    
    /**
     * 根据状态查询申请
     */
    List<TeamApplication> selectByStatus(@Param("status") TeamApplication.ApplicationStatus status);
    
    /**
     * 统计团队的申请数量
     */
    int countByTeamId(Long teamId);
    
    /**
     * 统计用户的申请数量
     */
    int countByUserId(Long userId);
    
    /**
     * 批量更新申请状态
     */
    int batchUpdateStatus(@Param("ids") List<Long> ids, @Param("status") TeamApplication.ApplicationStatus status);
}