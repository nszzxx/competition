package com.cdnu.cgi.service.User.impl;

import com.cdnu.cgi.dto.CompetitionTeamCardDto;
import com.cdnu.cgi.entity.*;
import com.cdnu.cgi.mapper.TeamApplicationMapper;
import com.cdnu.cgi.mapper.TeamMapper;
import com.cdnu.cgi.mapper.TeamMemberMapper;
import com.cdnu.cgi.mapper.UserComprehensiveInfoMapper;
import com.cdnu.cgi.mapper.UserMapper;
import com.cdnu.cgi.mapper.CompetitionTeamUserMapper;
import com.cdnu.cgi.service.User.MatchScoreService;
import com.cdnu.cgi.service.User.TeamService;
import com.cdnu.cgi.service.User.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.Date;
import java.util.stream.Collectors;

/**
 * 团队服务实现类
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class TeamServiceImpl implements TeamService {

    private final TeamMapper teamMapper;
    private final TeamMemberMapper teamMemberMapper;
    private final TeamApplicationMapper teamApplicationMapper;
    private final UserComprehensiveInfoMapper userComprehensiveInfoMapper;
    private final UserMapper userMapper;
    private final UserService userService;
    private final MatchScoreService matchScoreService;
    private final CompetitionTeamUserMapper competitionTeamUserMapper;
    
    @Override
    public void createTeam(Team team) {// 插入团队数据
        teamMapper.insert(team);
        
        // 自动将队长添加为团队成员
        if (team.getLeaderId() != null) {
            try {
                TeamMember leaderMember = new TeamMember();
                leaderMember.setTeamId(team.getId());
                leaderMember.setUserId(team.getLeaderId());
                leaderMember.setRole("队长"); // 设置为队长角色
                leaderMember.setJoinedAt(LocalDateTime.now());
                leaderMember.setStatus("active");
                
                teamMemberMapper.insert(leaderMember);
            } catch (Exception e) {
                // 如果添加队长失败，回滚团队创建
                teamMapper.deleteById(team.getId());
                throw new RuntimeException("创建团队失败：无法添加队长为团队成员 - " + e.getMessage());
            }
        }
    }
    
    @Override
    public List<Team> getAllTeams() {
        return teamMapper.selectAll();
    }
    
    @Override
    public Team getTeamById(Long id) {
        return teamMapper.selectById(id);
    }
    
    @Override
    public Team updateTeam(Long id, Team teamData) {
        Team team = teamMapper.selectById(id);
        if (team != null) {
            if (teamData.getName() != null) {
                team.setName(teamData.getName());
            }
            if (teamData.getDescription() != null) {
                team.setDescription(teamData.getDescription());
            }
            if (teamData.getNeedSkills() != null) {
                team.setNeedSkills(teamData.getNeedSkills());
            }
            teamMapper.updateById(team);
            return team;
        }
        return null;
    }
    
    @Override
    public boolean deleteTeam(Long id) {
        try {
            // 先删除团队成员
            teamMemberMapper.deleteByTeamId(id);
            // 再删除团队
            teamMapper.deleteById(id);
            return true;
        } catch (Exception e) {
            return false;
        }
    }
    
    @Override
    public TeamMember addTeamMember(Long teamId, Long userId, String role) {
        try {
            // 检查团队是否存在
            Team team = teamMapper.selectById(teamId);
            if (team == null) {
                throw new RuntimeException("团队不存在");
            }

            // 检查用户是否已经是团队成员
            if (teamMemberMapper.existsByTeamIdAndUserId(teamId, userId)) {
                throw new RuntimeException("用户已经是团队成员");
            }

            // 创建团队成员记录
            TeamMember teamMember = new TeamMember();
            teamMember.setTeamId(teamId);
            teamMember.setUserId(userId);
            teamMember.setRole(role); // role应该是"队员"或"队长"
            teamMember.setJoinedAt(LocalDateTime.now());
            teamMember.setStatus("active");

            teamMemberMapper.insert(teamMember);

            // ==================== 自动参赛逻辑 ====================
            // 查询该团队已参加的所有竞赛
            List<CompetitionTeamUser> teamCompetitions = competitionTeamUserMapper.selectByTeamId(teamId);

            if (teamCompetitions != null && !teamCompetitions.isEmpty()) {
                // 按竞赛ID分组，获取该团队参加的不同竞赛
                List<Long> competitionIds = teamCompetitions.stream()
                    .map(CompetitionTeamUser::getCompetitionId)
                    .distinct()
                    .collect(Collectors.toList());

                // 为新成员添加参赛记录
                for (Long competitionId : competitionIds) {
                    // 检查该用户是否已经参加了该竞赛（避免重复）
                    List<CompetitionTeamUser> existingRecords = competitionTeamUserMapper
                        .selectByUserIdAndCompetitionId(userId, competitionId);

                    if (existingRecords == null || existingRecords.isEmpty()) {
                        // 创建新的参赛记录
                        CompetitionTeamUser newParticipation = new CompetitionTeamUser();
                        newParticipation.setCompetitionId(competitionId);
                        newParticipation.setTeamId(teamId);
                        newParticipation.setUserId(userId);
                        newParticipation.setParticipationMode("team"); // 团队模式
                        // 参赛记录的role：队长 或 队员（与团队中的role一致）
                        newParticipation.setRole(role);
                        newParticipation.setCreateTime(new Date());

                        competitionTeamUserMapper.insert(newParticipation);
                        log.info(String.format("自动为新成员 %d 添加竞赛 %d 的参赛记录，角色：%s",
                            userId, competitionId, role));
                    }
                }
            }

            return teamMember;
        } catch (Exception e) {
            throw new RuntimeException("添加团队成员失败：" + e.getMessage());
        }
    }
    
    @Override
    public boolean removeTeamMember(Long teamId, Long userId) {
        try {
            // 检查团队是否存在以及当前用户是否为队长
            Team team = teamMapper.selectById(teamId);
            if (team != null && team.getLeaderId().equals(userId)) {
                throw new RuntimeException("队长不能被移除");
            }
            TeamMember teamMember = teamMemberMapper.selectByTeamIdAndUserId(teamId, userId);
            if (teamMember != null) {
                // 1. 删除团队成员记录
                teamMemberMapper.deleteById(teamMember.getId());

                // 2. 删除该用户与该团队相关的所有申请/邀请记录（无论状态如何）
                TeamApplication application = teamApplicationMapper.selectByUserIdAndTeamId(userId, teamId);
                if (application != null) {
                    teamApplicationMapper.deleteById(application.getId());
                    log.info(String.format("已删除用户 %d 与团队 %d 的申请/邀请记录", userId, teamId));
                }

                // 3. 删除该用户在该团队所有竞赛中的参赛记录
                List<CompetitionTeamUser> teamCompetitions = competitionTeamUserMapper.selectByTeamId(teamId);
                if (teamCompetitions != null && !teamCompetitions.isEmpty()) {
                    // 获取该团队参加的所有竞赛ID
                    List<Long> competitionIds = teamCompetitions.stream()
                        .map(CompetitionTeamUser::getCompetitionId)
                        .distinct()
                        .collect(Collectors.toList());

                    // 删除用户在这些竞赛中以该团队身份的参赛记录
                    for (Long competitionId : competitionIds) {
                        List<CompetitionTeamUser> userRecords = competitionTeamUserMapper
                            .selectByUserIdAndCompetitionId(userId, competitionId);

                        if (userRecords != null) {
                            for (CompetitionTeamUser record : userRecords) {
                                // 只删除该团队的参赛记录
                                if (teamId.equals(record.getTeamId())) {
                                    competitionTeamUserMapper.deleteByCompetitionIdTeamIdAndUserId(
                                        competitionId, teamId, userId);
                                    log.info(String.format("已删除用户 %d 在竞赛 %d 团队 %d 的参赛记录",
                                        userId, competitionId, teamId));
                                }
                            }
                        }
                    }
                }

                return true;
            }
            return false;
        } catch (Exception e) {
            throw new RuntimeException("移除团队成员失败：" + e.getMessage());
        }
    }
    
    @Override
    public List<TeamMember> getTeamMembers(Long teamId) {
        List<TeamMember> members = teamMemberMapper.selectByTeamIdAndStatus(teamId, "active");
        for (TeamMember member : members) {
            Optional<User> userInfo = userService.getUserById(member.getUserId());
            userInfo.ifPresent(user -> {
                member.setUsername(user.getUsername());
                member.setRealName(user.getRealName());
                member.setAvatarUrl(user.getAvatarUrl());
            });
        }
        return members;
    }
    
    @Override
    public List<TeamMemberDetailInfo> getTeamMemberDetails(Long teamId, Long currentUserId) {
        // 首先检查团队是否存在
        Team team = teamMapper.selectById(teamId);
        if (team == null) {
            throw new RuntimeException("团队不存在");
        }
        
        // 检查当前用户是否为团队成员
        boolean isTeamMember = teamMemberMapper.existsByTeamIdAndUserId(teamId, currentUserId);
        if (!isTeamMember) {
            throw new RuntimeException("请加入团队后再查看成员信息");
        }
        
        // 如果是团队成员，则可以查看详细信息
        return teamMemberMapper.selectTeamMemberDetailsByTeamId(teamId);
    }
    
    @Override
    public List<Team> getUserTeams(Long userId) {
        List<TeamMember> teamMembers = teamMemberMapper.selectByUserIdAndStatus(userId, "active");
        return teamMembers.stream()
                .map(tm -> {
                    Team team = teamMapper.selectById(tm.getTeamId());
                    if (team != null) {
                        int memberCount = teamMemberMapper.selectByTeamIdAndStatus(team.getId(), "active").size();
                        team.setMemberCount(memberCount);

                        // 填充队长姓名
                        if (team.getLeaderId() != null) {
                            Optional<User> leader = userService.getUserById(team.getLeaderId());
                            leader.ifPresent(user -> {
                                team.setLeaderName(user.getRealName() != null ? user.getRealName() : user.getUsername());
                            });
                        }
                    }
                    return team;
                })
                .filter(team -> team != null)
                .collect(Collectors.toList());
    }
    
    @Override
    public List<Team> getCompetitionTeams(Long competitionId) {
        return teamMapper.selectByCompetitionId(competitionId);
    }
    
    @Override
    public List<Team> searchTeams(String name, Long competitionId) {
        if (name == null && competitionId == null) {
            return teamMapper.selectAll();
        }
        
        return teamMapper.selectAll().stream()
                .filter(team -> {
                    boolean matches = true;
                    if (name != null && !name.trim().isEmpty()) {
                        matches = matches && team.getName().toLowerCase().contains(name.toLowerCase());
                    }
                    if (competitionId != null) {
                        matches = matches && team.getCompetitionId().equals(competitionId);
                    }
                    return matches;
                })
                .collect(Collectors.toList());
    }
    
    @Override
    public List<CompetitionTeamCardDto> getCompetitionTeamCards(Long competitionId) {
        return teamMapper.selectCompetitionTeamCards(competitionId);
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
    
    @Override
    public List<CompetitionTeamCardDto> getCompetitionTeamCardsWithMatchScore(Long competitionId, Long userId) {
        // 获取团队卡片基本信息
        List<CompetitionTeamCardDto> teamCards = teamMapper.selectCompetitionTeamCards(competitionId);
        
        if (userId == null || teamCards.isEmpty()) {
            return teamCards;
        }
        
        // 直接使用 MatchScoreService 的方法计算适配度
        return matchScoreService.calculateTeamCardsMatchScores(teamCards, userId);
    }
    
    // ==================== 团队申请相关方法实现 ====================
    
    @Override
    public TeamApplication applyToJoinTeam(Long userId, Long teamId) {
        // 检查用户是否存在
        UserComprehensiveInfo user = userComprehensiveInfoMapper.selectById(userId);
        if (user == null) {
            throw new RuntimeException("用户不存在");
        }

        // 检查团队是否存在
        Team team = teamMapper.selectById(teamId);
        if (team == null) {
            throw new RuntimeException("团队不存在");
        }

        // 检查用户是否已经是团队成员
        if (teamMemberMapper.existsByTeamIdAndUserId(teamId, userId)) {
            throw new RuntimeException("您已经是该团队成员");
        }

        // 检查是否已有申请记录
        TeamApplication existingApplication = teamApplicationMapper.selectByUserIdAndTeamId(userId, teamId);
        if (existingApplication != null) {
            if (existingApplication.getStatus() == TeamApplication.ApplicationStatus.PENDING) {
                // 如果之前的记录是邀请，现在用户申请加入，说明双向匹配，自动入队
                if ("invite".equals(existingApplication.getType())) {
                    // 更新邀请状态为已通过
                    existingApplication.setStatus(TeamApplication.ApplicationStatus.APPROVED);
                    teamApplicationMapper.updateById(existingApplication);

                    // 自动添加为团队成员
                    try {
                        addTeamMember(teamId, userId, "队员");
                        return existingApplication;
                    } catch (Exception e) {
                        throw new RuntimeException("自动加入团队失败：" + e.getMessage());
                    }
                }
                throw new RuntimeException("您已经申请过该团队，请等待审核");
            } else if (existingApplication.getStatus() == TeamApplication.ApplicationStatus.APPROVED) {
                throw new RuntimeException("您的申请已通过，请联系管理员");
            }
            // 如果之前被拒绝，可以重新申请，删除旧记录
            teamApplicationMapper.deleteById(existingApplication.getId());
        }

        // 创建新申请
        TeamApplication application = new TeamApplication(userId, teamId, team.getLeaderId());
        application.setType("apply"); // 设置为申请类型
        teamApplicationMapper.insert(application);

        return teamApplicationMapper.selectById(application.getId());
    }
    
    @Override
    public TeamApplication reviewApplication(Long applicationId, boolean approved, String rejectionReason) {
        TeamApplication application = teamApplicationMapper.selectById(applicationId);
        if (application == null) {
            throw new RuntimeException("申请记录不存在");
        }
        
        if (application.getStatus() != TeamApplication.ApplicationStatus.PENDING) {
            throw new RuntimeException("该申请已经被处理过");
        }
        
        // 更新申请状态
        if (approved) {
            application.setStatus(TeamApplication.ApplicationStatus.APPROVED);
            application.setRejectionReason(null);
            
            // 自动添加为团队成员
            try {
                addTeamMember(application.getTeamId(), application.getUserId(), "队员");
            } catch (Exception e) {
                throw new RuntimeException("添加团队成员失败：" + e.getMessage());
            }
        } else {
            application.setStatus(TeamApplication.ApplicationStatus.REJECTED);
            application.setRejectionReason(rejectionReason);
        }
        
        teamApplicationMapper.updateById(application);
        return teamApplicationMapper.selectById(applicationId);
    }
    
    @Override
    public List<TeamApplication> getTeamApplications(Long teamId) {
        return teamApplicationMapper.selectByTeamId(teamId);
    }
    
    @Override
    public List<TeamApplication> getUserApplications(Long userId) {
        return teamApplicationMapper.selectByUserId(userId);
    }
    
    @Override
    public List<TeamApplication> getPendingApplicationsByLeader(Long leaderId) {
        return teamApplicationMapper.selectPendingByLeaderId(leaderId);
    }
    
    @Override
    public boolean cancelApplication(Long applicationId, Long userId) {
        TeamApplication application = teamApplicationMapper.selectById(applicationId);
        if (application == null) {
            return false;
        }
        
        // 验证权限：只有申请人可以取消申请
        if (!application.getUserId().equals(userId)) {
            throw new RuntimeException("无权限取消此申请");
        }
        
        // 只有待审核状态的申请可以取消
        if (application.getStatus() != TeamApplication.ApplicationStatus.PENDING) {
            throw new RuntimeException("只能取消待审核状态的申请");
        }
        
        return teamApplicationMapper.deleteById(applicationId) > 0;
    }
    
    @Override
    public TeamApplication checkUserApplication(Long userId, Long teamId) {
        return teamApplicationMapper.selectByUserIdAndTeamId(userId, teamId);
    }
    
    @Override
    public int batchReviewApplications(List<Long> applicationIds, boolean approved, String rejectionReason) {
        if (applicationIds == null || applicationIds.isEmpty()) {
            return 0;
        }
        
        int successCount = 0;
        for (Long applicationId : applicationIds) {
            try {
                reviewApplication(applicationId, approved, rejectionReason);
                successCount++;
            } catch (Exception e) {
                // 记录错误但继续处理其他申请
                log.error("处理申请 " + applicationId + " 失败: " + e.getMessage());
            }
        }

        return successCount;
    }

    // ==================== 团队邀请相关方法实现 ====================

    @Override
    public TeamApplication inviteUserToTeam(Long teamId, Long inviterId, String identifier, String message) {
        // 检查团队是否存在
        Team team = teamMapper.selectById(teamId);
        if (team == null) {
            throw new RuntimeException("团队不存在");
        }

        // 验证邀请人是否是队长
        if (!team.getLeaderId().equals(inviterId)) {
            throw new RuntimeException("只有队长才能邀请成员");
        }

        // 根据标识符查找用户（支持用户名、邮箱、手机号）
        User invitedUser = userMapper.selectByUsernameOrEmailOrPhone(identifier);
        if (invitedUser == null) {
            throw new RuntimeException("找不到该用户，请检查用户名、邮箱或手机号");
        }

        Long userId = invitedUser.getId();

        // 检查用户是否已经是团队成员
        if (teamMemberMapper.existsByTeamIdAndUserId(teamId, userId)) {
            throw new RuntimeException("该用户已经是团队成员");
        }

        // 检查是否已有记录
        TeamApplication existingRecord = teamApplicationMapper.selectByUserIdAndTeamId(userId, teamId);
        if (existingRecord != null) {
            if (existingRecord.getStatus() == TeamApplication.ApplicationStatus.PENDING) {
                // 如果之前用户申请了，现在队长邀请，说明双向匹配，自动入队
                if ("apply".equals(existingRecord.getType())) {
                    // 更新申请状态为已通过
                    existingRecord.setStatus(TeamApplication.ApplicationStatus.APPROVED);
                    teamApplicationMapper.updateById(existingRecord);

                    // 自动添加为团队成员
                    try {
                        addTeamMember(teamId, userId, "队员");
                        return existingRecord;
                    } catch (Exception e) {
                        throw new RuntimeException("自动加入团队失败：" + e.getMessage());
                    }
                }
                throw new RuntimeException("已经向该用户发送过邀请，请等待响应");
            }
            // 如果之前被拒绝，可以重新邀请，删除旧记录
            if (existingRecord.getStatus() == TeamApplication.ApplicationStatus.REJECTED) {
                teamApplicationMapper.deleteById(existingRecord.getId());
            }
        }

        // 创建新邀请记录
        TeamApplication invitation = new TeamApplication(userId, teamId, team.getLeaderId());
        invitation.setType("invite"); // 设置为邀请类型
        invitation.setMessage(message);
        teamApplicationMapper.insert(invitation);

        return teamApplicationMapper.selectById(invitation.getId());
    }

    @Override
    public List<TeamApplication> getUserInvitations(Long userId) {
        // 获取用户收到的所有待处理邀请
        return teamApplicationMapper.selectByUserIdAndType(userId, "invite");
    }

    @Override
    public TeamApplication respondToInvitation(Long invitationId, Long userId, boolean accepted) {
        TeamApplication invitation = teamApplicationMapper.selectById(invitationId);
        if (invitation == null) {
            throw new RuntimeException("邀请记录不存在");
        }

        // 验证权限：只有被邀请人可以响应邀请
        if (!invitation.getUserId().equals(userId)) {
            throw new RuntimeException("无权限响应此邀请");
        }

        // 检查是否是邀请类型
        if (!"invite".equals(invitation.getType())) {
            throw new RuntimeException("该记录不是邀请");
        }

        if (invitation.getStatus() != TeamApplication.ApplicationStatus.PENDING) {
            throw new RuntimeException("该邀请已经被处理过");
        }

        // 更新邀请状态
        if (accepted) {
            invitation.setStatus(TeamApplication.ApplicationStatus.APPROVED);
            invitation.setRejectionReason(null);

            // 自动添加为团队成员
            try {
                addTeamMember(invitation.getTeamId(), invitation.getUserId(), "队员");
            } catch (Exception e) {
                throw new RuntimeException("加入团队失败：" + e.getMessage());
            }
        } else {
            invitation.setStatus(TeamApplication.ApplicationStatus.REJECTED);
            invitation.setRejectionReason("用户拒绝邀请");
        }

        teamApplicationMapper.updateById(invitation);
        return teamApplicationMapper.selectById(invitationId);
    }

    @Override
    @Transactional
    public int batchRespondToInvitations(List<Long> invitationIds, Long userId, boolean accepted) {
        if (invitationIds == null || invitationIds.isEmpty()) {
            return 0;
        }

        int successCount = 0;
        for (Long invitationId : invitationIds) {
            try {
                respondToInvitation(invitationId, userId, accepted);
                successCount++;
            } catch (Exception e) {
                // 记录错误但继续处理其他邀请
                log.info("批量响应邀请失败 ID: " + invitationId + ", 错误: " + e.getMessage());
            }
        }
        return successCount;
    }
}
