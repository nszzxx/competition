package com.cdnu.cgi.service.User;

import com.cdnu.cgi.dto.CompetitionTeamCardDto;
import com.cdnu.cgi.entity.Team;
import com.cdnu.cgi.entity.TeamApplication;
import com.cdnu.cgi.entity.TeamMember;
import com.cdnu.cgi.entity.TeamMemberDetailInfo;

import java.util.List;

/**
 * 团队服务接口
 */
public interface TeamService {
    
    /**
     * 创建团队
     */
    void createTeam(Team team);
    
    /**
     * 获取所有团队
     */
    List<Team> getAllTeams();
    
    /**
     * 根据ID获取团队
     */
    Team getTeamById(Long id);
    
    /**
     * 更新团队
     */
    Team updateTeam(Long id, Team teamData);
    
    /**
     * 删除团队
     */
    boolean deleteTeam(Long id);
    
    /**
     * 添加团队成员
     */
    TeamMember addTeamMember(Long teamId, Long userId, String role);
    
    /**
     * 移除团队成员
     */
    boolean removeTeamMember(Long teamId, Long userId);
    
    /**
     * 获取团队成员
     */
    List<TeamMember> getTeamMembers(Long teamId);
    
    /**
     * 获取团队成员详细信息（包含技能和荣誉）
     * 需要验证查看权限：只有团队成员才能查看详细信息
     * @param teamId 团队ID
     * @param currentUserId 当前用户ID
     * @return 团队成员详细信息列表
     */
    List<TeamMemberDetailInfo> getTeamMemberDetails(Long teamId, Long currentUserId);
    
    /**
     * 获取用户参与的团队
     */
    List<Team> getUserTeams(Long userId);
    
    /**
     * 获取竞赛的所有团队
     */
    List<Team> getCompetitionTeams(Long competitionId);
    
    /**
     * 搜索团队
     */
    List<Team> searchTeams(String name, Long competitionId);
    
    /**
     * 获取竞赛团队卡片信息
     */
    List<CompetitionTeamCardDto> getCompetitionTeamCards(Long competitionId);

    /**
     * 获取竞赛团队卡片信息，并计算当前用户与团队的适配度
     * @param competitionId 竞赛ID
     * @param userId 当前用户ID
     * @return 团队卡片信息列表（包含适配度）
     */
    List<CompetitionTeamCardDto> getCompetitionTeamCardsWithMatchScore(Long competitionId, Long userId);
    
    // ==================== 团队申请相关方法 ====================
    
    /**
     * 申请加入团队
     * @param userId 申请用户ID
     * @param teamId 团队ID
     * @return 申请记录
     */
    TeamApplication applyToJoinTeam(Long userId, Long teamId);
    
    /**
     * 审核团队申请
     * @param applicationId 申请ID
     * @param approved 是否通过
     * @param rejectionReason 拒绝理由（如果不通过）
     * @return 更新后的申请记录
     */
    TeamApplication reviewApplication(Long applicationId, boolean approved, String rejectionReason);
    
    /**
     * 获取团队的所有申请
     * @param teamId 团队ID
     * @return 申请列表
     */
    List<TeamApplication> getTeamApplications(Long teamId);
    
    /**
     * 获取用户的所有申请
     * @param userId 用户ID
     * @return 申请列表
     */
    List<TeamApplication> getUserApplications(Long userId);
    
    /**
     * 获取队长待审核的申请
     * @param leaderId 队长ID
     * @return 待审核申请列表
     */
    List<TeamApplication> getPendingApplicationsByLeader(Long leaderId);
    
    /**
     * 取消申请
     * @param applicationId 申请ID
     * @param userId 用户ID（验证权限）
     * @return 是否成功
     */
    boolean cancelApplication(Long applicationId, Long userId);
    
    /**
     * 检查用户是否已申请该团队
     * @param userId 用户ID
     * @param teamId 团队ID
     * @return 申请记录，如果没有申请则返回null
     */
    TeamApplication checkUserApplication(Long userId, Long teamId);
    
    /**
     * 批量处理申请
     * @param applicationIds 申请ID列表
     * @param approved 是否通过
     * @param rejectionReason 拒绝理由
     * @return 处理成功的数量
     */
    int batchReviewApplications(List<Long> applicationIds, boolean approved, String rejectionReason);

    // ==================== 团队邀请相关方法 ====================

    /**
     * 邀请用户加入团队（通过用户名/邮箱/手机号）
     * @param teamId 团队ID
     * @param inviterId 邀请人ID（必须是队长）
     * @param identifier 用户标识（用户名/邮箱/手机号）
     * @param message 邀请消息
     * @return 邀请记录
     */
    TeamApplication inviteUserToTeam(Long teamId, Long inviterId, String identifier, String message);

    /**
     * 获取用户收到的邀请列表
     * @param userId 用户ID
     * @return 邀请列表
     */
    List<TeamApplication> getUserInvitations(Long userId);

    /**
     * 用户响应邀请（接受/拒绝）
     * @param invitationId 邀请ID
     * @param userId 用户ID（验证权限）
     * @param accepted 是否接受
     * @return 更新后的邀请记录
     */
    TeamApplication respondToInvitation(Long invitationId, Long userId, boolean accepted);

    /**
     * 批量响应邀请
     * @param invitationIds 邀请ID列表
     * @param userId 用户ID（验证权限）
     * @param accepted 是否接受
     * @return 处理成功的数量
     */
    int batchRespondToInvitations(List<Long> invitationIds, Long userId, boolean accepted);
}
