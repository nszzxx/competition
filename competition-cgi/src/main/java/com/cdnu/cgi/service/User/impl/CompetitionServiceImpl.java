package com.cdnu.cgi.service.User.impl;

import com.cdnu.cgi.config.ParticipateConfig;
import com.cdnu.cgi.dto.UserCompetitionDTO;
import com.cdnu.cgi.entity.Competition;
import com.cdnu.cgi.entity.CompetitionTeamUser;
import com.cdnu.cgi.entity.Team;
import com.cdnu.cgi.entity.TeamMember;
import com.cdnu.cgi.entity.User;
import com.cdnu.cgi.mapper.*;
import com.cdnu.cgi.service.User.CompetitionService;
import com.cdnu.cgi.service.config.ConfigService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.sql.Timestamp;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 竞赛服务实现类
 */
@Slf4j
@Service
@AllArgsConstructor
public class CompetitionServiceImpl implements CompetitionService {

    private CompetitionMapper competitionMapper;
    private CompetitionTeamUserMapper competitionTeamUserMapper;
    private TeamMapper teamMapper;
    private ConfigService configService;
    private UserMapper userMapper;
    private TeamMemberMapper teamMemberMapper;

    @Override
    public List<Competition> getAllCompetitions() {
        return competitionMapper.selectAll();
    }

    @Override
    public Optional<Competition> getCompetitionById(Long id) {
        Competition competition = competitionMapper.selectById(id);
        return Optional.ofNullable(competition);
    }

    @Override
    public List<Competition> getCompetitionsByCategory(String category) {
        return competitionMapper.selectByCategory(category);
    }

    @Override
    public List<Competition> searchCompetitions(String keyword) {
        if (keyword == null || keyword.trim().isEmpty()) {
            return getAllCompetitions();
        }
        return competitionMapper.selectByTitleContainingIgnoreCase(keyword);
    }

    @Override
    public List<Competition> searchCompetitions(String category, String keyword, String participationMode) {
        List<Competition> competitions = getAllCompetitions();

        return competitions.stream()
                .filter(competition -> {
                    boolean matches = true;

                    if (category != null && !category.trim().isEmpty()) {
                        matches = matches && competition.getCategory().equalsIgnoreCase(category);
                    }

                    if (keyword != null && !keyword.trim().isEmpty()) {
                        matches = matches && (
                                competition.getTitle().toLowerCase().contains(keyword.toLowerCase()) ||
                                        competition.getDescription().toLowerCase().contains(keyword.toLowerCase())
                        );
                    }

                    if (participationMode != null && !participationMode.trim().isEmpty()) {
                        // 检查参赛类型是否匹配
                        if (competition.getParticipationMode() != null) {
                            if (participationMode.equalsIgnoreCase("individual")) {
                                matches = matches && (competition.getParticipationMode().equalsIgnoreCase("individual") ||
                                        competition.getParticipationMode().equalsIgnoreCase("both"));
                            } else if (participationMode.equalsIgnoreCase("team")) {
                                matches = matches && (competition.getParticipationMode().equalsIgnoreCase("team") ||
                                        competition.getParticipationMode().equalsIgnoreCase("both"));
                            } else {
                                matches = matches && competition.getParticipationMode().equalsIgnoreCase(participationMode);
                            }
                        } else {
                            // 如果没有指定参赛类型，则默认为track字段匹配
                            matches = matches && competition.getTrack().equalsIgnoreCase(participationMode);
                        }
                    }

                    return matches;
                })
                .collect(Collectors.toList());
    }

    @Override
    public List<Competition> searchByKeyword(String keyword) {
        return searchCompetitions(keyword);
    }

    @Override
    public List<Competition> getActiveCompetitions() {
        Date now = new Date();
        return getAllCompetitions().stream()
                .filter(competition -> {
                    Date startTime = competition.getStartTime();
                    Date endTime = competition.getEndTime();
                    return startTime != null && endTime != null &&
                            now.after(startTime) && now.before(endTime);
                })
                .collect(Collectors.toList());
    }

    @Override
    public List<Competition> getCompetitionsByTag(String tag) {
        if (tag == null || tag.trim().isEmpty()) {
            return new ArrayList<>();
        }

        return competitionMapper.selectByTagsContaining(tag);
    }

    @Override
    public Competition saveCompetition(Competition competition) {
        // 如果是新竞赛（没有ID），设置创建时间
        if (competition.getId() == null) {
            competition.setCreatedAt(new Timestamp(System.currentTimeMillis()));
            competitionMapper.insert(competition);
        } else {
            competitionMapper.updateById(competition);
        }
        return competition;
    }

    @Override
    public void deleteCompetition(Long id) {
        competitionMapper.deleteById(id);
    }

    @Override
    public Map<String, Long> getCompetitionCategories() {
        List<Competition> competitions = getAllCompetitions();
        return competitions.stream()
                .collect(Collectors.groupingBy(
                        Competition::getCategory,
                        Collectors.counting()
                ));
    }

    @Override
    public List<Competition> getPopularCompetitions(int limit) {
        // 简化实现：返回最新的竞赛作为热门竞赛
        List<Competition> competitions = getAllCompetitions();
        competitions.sort((a, b) -> {
            if (a.getStartTime() == null && b.getStartTime() == null) return 0;
            if (a.getStartTime() == null) return 1;
            if (b.getStartTime() == null) return -1;
            return b.getStartTime().compareTo(a.getStartTime());
        });

        return competitions.stream()
                .limit(limit)
                .collect(Collectors.toList());
    }

    @Override
    public List<Competition> getUpcomingCompetitions(int limit) {
        Date now = new Date();
        return getAllCompetitions().stream()
                .filter(competition -> {
                    Date startTime = competition.getStartTime();
                    return startTime != null && now.before(startTime);
                })
                .sorted(Comparator.comparing(Competition::getStartTime))
                .limit(limit)
                .collect(Collectors.toList());
    }

    @Override
    public List<Competition> getCompetitionsByParticipationMode(String mode) {
        if (mode == null || mode.trim().isEmpty()) {
            return new ArrayList<>();
        }

        return getAllCompetitions().stream()
                .filter(competition -> {
                    String track = competition.getTrack();
                    return track != null && track.equalsIgnoreCase(mode);
                })
                .collect(Collectors.toList());
    }


    /**
     * 检查竞赛当前是否可以报名
     *
     * @param competition 竞赛对象
     * @return 报名状态：0-未开始报名，1-报名中，2-报名已结束
     */
    public int getRegistrationStatus(Competition competition) {
        if (competition == null || competition.getPatiStarttime() == null || competition.getPatiEndtime() == null) {
            return 2; // 如果没有设置报名时间，默认为已结束
        }

        Date now = new Date();
        Date startTime = new Date(competition.getPatiStarttime().getTime());
        Date endTime = new Date(competition.getPatiEndtime().getTime());

        if (now.before(startTime)) {
            return 0; // 报名未开始
        } else if (now.after(endTime)) {
            return 2; // 报名已结束
        } else {
            return 1; // 报名中
        }
    }

    /**
     * 获取当前可以报名的竞赛列表
     *
     * @return 可报名的竞赛列表
     */
    public List<Competition> getCurrentlyRegisterableCompetitions() {
        Date now = new Date();
        Timestamp currentTime = new Timestamp(now.getTime());
        // 如果有对应的Mapper方法，可以直接调用
        // return competitionMapper.selectCurrentlyRegisterable(currentTime);
        // 否则使用内存过滤
        return getAllCompetitions().stream()
                .filter(competition -> {
                    if (competition.getPatiStarttime() == null || competition.getPatiEndtime() == null) {
                        return false;
                    }

                    return !currentTime.before(competition.getPatiStarttime()) &&
                            !currentTime.after(competition.getPatiEndtime());
                })
                .collect(Collectors.toList());
    }

    // 从CompetitionServiceImplBusiness合并的方法

    /**
     * 创建新竞赛
     *
     * @param competition 竞赛对象
     * @return 操作结果消息
     */
    public String createCompetition(Competition competition) {
        try {
            competition.setCreatedAt(new Timestamp(System.currentTimeMillis()));
            competitionMapper.insert(competition);
            return "竞赛创建成功";
        } catch (Exception e) {
            return "竞赛创建失败：" + e.getMessage();
        }
    }

    /**
     * 更新竞赛信息
     * @param competition 竞赛对象
     * @return 操作结果消息
     */
    public String updateCompetition(Competition competition) {
        try {
            Competition existingCompetition = competitionMapper.selectById(competition.getId());
            if (existingCompetition != null) {
                competitionMapper.updateById(competition);
                return "竞赛更新成功";
            } else {
                return "竞赛不存在";
            }
        } catch (Exception e) {
            return "竞赛更新失败：" + e.getMessage();
        }
    }

    /**
     * 删除竞赛并返回操作结果消息
     *
     * @param id 竞赛ID
     * @return 操作结果消息
     */
    public String deleteCompetitionWithMessage(Long id) {
        try {
            Competition existingCompetition = competitionMapper.selectById(id);
            if (existingCompetition != null) {
                competitionMapper.deleteById(id);
                return "竞赛删除成功";
            } else {
                return "竞赛不存在";
            }
        } catch (Exception e) {
            return "竞赛删除失败：" + e.getMessage();
        }
    }

    /**
     * 用户参加竞赛
     * 1. 检查当前参与的竞赛时间是否与参加过的竞赛时间冲突
     * 2. 用户作为团队长，只能同时参加3个竞赛,，同时检查团队成员作为队员身份是否报名超过3个竞赛
     * 3. 用户作为团队成员无法报名
     * 4. 用户作为个人，只能同时参加3个竞赛
     * 5. 插入参加记录
     */
    @Override
    public void participateCompetition(CompetitionTeamUser competitionTeamUser) {
        ParticipateConfig participateConfig = configService.getParticipateConfig();
        String participationMode = competitionTeamUser.getParticipationMode();
        List<CompetitionTeamUser> userCompetitions = competitionTeamUserMapper.selectByUserId(competitionTeamUser.getUserId());
        Competition newCompetition = competitionMapper.selectById(competitionTeamUser.getCompetitionId());
        List<TeamMember> teamMembers = teamMemberMapper.selectByTeamId(competitionTeamUser.getTeamId());
        isTimeConflict(newCompetition, userCompetitions);
        IdentityResult(competitionTeamUser, userCompetitions, participationMode, teamMembers, participateConfig);
        // 插入参加记录
        if (competitionTeamUser.getParticipationMode().equals("team") && !(competitionTeamUser.getTeamId() == null)) {
            for(TeamMember member : teamMembers){
                CompetitionTeamUser teamUser = new CompetitionTeamUser();
                teamUser.setCompetitionId(competitionTeamUser.getCompetitionId());
                teamUser.setUserId(member.getUserId());
                teamUser.setTeamId(competitionTeamUser.getTeamId());
                teamUser.setParticipationMode(competitionTeamUser.getParticipationMode());
                teamUser.setRole(member.getRole());
                teamUser.setCreateTime(new Date());
                competitionTeamUserMapper.insert(teamUser);
            }
        } else {
            competitionTeamUser.setCreateTime(new Date());
            competitionTeamUserMapper.insert(competitionTeamUser);
        }
    }

    /**
     * 验证身份以及数量冲突
     */
    private void IdentityResult(CompetitionTeamUser competitionTeamUser,
                                List<CompetitionTeamUser> userCompetitions, String participationMode,
                                List<TeamMember> teamMembers,
                                ParticipateConfig participateConfig) {
        // 检查用户参与竞赛数量限制
        long leaderCount = userCompetitions.stream()
                .filter(u -> "队长".equalsIgnoreCase(u.getRole()))
                .count();
        long individualCount = userCompetitions.stream()
                .filter(u -> "个人".equalsIgnoreCase(u.getRole()))
                .count();
        if ("team".equalsIgnoreCase(participationMode)) {
            // 检查团队成员是否以团队身份报名参加超过3个竞赛
            for (TeamMember member : teamMembers){
                User memberUser = userMapper.selectById(member.getUserId());
                List<CompetitionTeamUser> memberCompetitions = competitionTeamUserMapper.selectByUserId(member.getUserId());
                long memberIndividualCount = memberCompetitions.stream()
                        .filter(u -> "队员".equalsIgnoreCase(u.getRole()))
                        .count();
                if (memberIndividualCount >= participateConfig.getMemberMaxParticipants()) {
                    throw new RuntimeException(String.format("团队成员%s已以团队成员身份参加%d个竞赛，无法报名。",
                            memberUser.getRealName(),
                            participateConfig.getMemberMaxParticipants()) );
                }
            }
            if ("队长".equalsIgnoreCase(competitionTeamUser.getRole()) && leaderCount >= participateConfig.getTeamMaxParticipants()) {
                throw new RuntimeException(String.format("您只能带领团队同时参加%d个竞赛。",
                        participateConfig.getTeamMaxParticipants()));
            } else if ("队员".equalsIgnoreCase(competitionTeamUser.getRole())) {
                throw new RuntimeException("队员不能主动参赛，请联系团队长进行报名。");
            }
        } else if ("individual".equalsIgnoreCase(participationMode)) {
            if (individualCount >= participateConfig.getIndividualMaxParticipants()) {
                throw new RuntimeException(String.format("您只能以个人身份同时参加%d个竞赛。",
                        participateConfig.getIndividualMaxParticipants()));
            }
        } else {
            throw new RuntimeException("无效的参赛方式：" + participationMode);
        }
    }

    /**
     * 检查新竞赛时间是否与用户已参与的竞赛时间冲突
     * @param comp1 新竞赛
     * @param comp2 用户已参与的竞赛列表
     */
    private void isTimeConflict(Competition comp1, List<CompetitionTeamUser> comp2) {
        Date newStart = comp1.getStartTime();
        Date newEnd = comp1.getEndTime();
        for (CompetitionTeamUser u : comp2) {
            Competition oldCompetition = competitionMapper.selectById(u.getCompetitionId());
            if (oldCompetition != null) {
                Date oldStart = oldCompetition.getStartTime();
                Date oldEnd = oldCompetition.getEndTime();
                if (newStart != null && newEnd != null && oldStart != null && oldEnd != null) {
                    boolean conflict = !(newEnd.before(oldStart) || newStart.after(oldEnd));
                    if (conflict) {
                        throw new RuntimeException(String.format("当前竞赛与已经参加过的竞赛 %s  时间冲突。请合理选择并安排竞赛时间。", oldCompetition.getTitle()));
                    }
                }
            }
        }
    }

    @Override
    public void cancelParticipation(Long competitionId, Long userId, Long teamId) {
        if (teamId == null || teamId == 0) {
            // 个人参赛：根据竞赛ID和用户ID删除记录
            log.info("取消个人参赛 - 竞赛ID: {}, 用户ID: {}", competitionId, userId);
            competitionTeamUserMapper.deleteByCompetitionIdAndUserId(competitionId, userId);
        } else {
            // 团队参赛：根据竞赛ID和团队ID删除所有记录
            log.info("取消团队参赛 - 竞赛ID: {}, 团队ID: {}", competitionId, teamId);
            competitionTeamUserMapper.deleteByCompetitionIdAndTeamId(competitionId, teamId);
        }
    }

    @Override
    public List<CompetitionTeamUser> getCompetitionParticipants(Long competitionId) {
        return competitionTeamUserMapper.selectByCompetitionId(competitionId);
    }

    @Override
    public List<CompetitionTeamUser> getTeamParticipants(Long teamId) {
        return competitionTeamUserMapper.selectByTeamId(teamId);
    }

    @Override
    public List<CompetitionTeamUser> getCompetitionTeamParticipants(Long competitionId){
        return competitionTeamUserMapper.getCompetitionTeamParticipants(competitionId);
    }

    @Override
    public List<CompetitionTeamUser> getCompetitionIndividualParticipants(Long competitionId){
        return competitionTeamUserMapper.getCompetitionIndividualParticipants(competitionId);
    }

    @Override
    public List<CompetitionTeamUser> getUserParticipationDetails(Long userId) {
        return competitionTeamUserMapper.selectByUserId(userId);
    }

    @Override
    public List<CompetitionTeamUser> getParticipationDetails(CompetitionTeamUser competitionTeamUser) {
        return competitionTeamUserMapper.selectByConditions(
            competitionTeamUser.getCompetitionId(),
            competitionTeamUser.getTeamId(),
            competitionTeamUser.getUserId(),
            competitionTeamUser.getParticipationMode()
        );
    }

    /**
     * 获取用户参加的竞赛（包含参赛方式信息）
     * @param userId 用户ID
     * @return 用户参加的竞赛列表，包含参赛信息
     */
    @Override
    public List<UserCompetitionDTO> getUserCompetitions(Long userId) {
        // 1. 获取用户所有参赛记录
        List<CompetitionTeamUser> participationDetails = competitionTeamUserMapper.selectByUserId(userId);

        // 2. 去重（用户可能以队长和队员的身份同时存在于同一个竞赛中）
        // 按competitionId分组，每个竞赛只取第一条记录（优先队长角色）
        Map<Long, CompetitionTeamUser> uniqueParticipations = participationDetails.stream()
                .sorted((a, b) -> {
                    // 队长优先于队员和个人
                    if ("队长".equals(a.getRole())) return -1;
                    if ("队长".equals(b.getRole())) return 1;
                    return 0;
                })
                .collect(Collectors.toMap(
                        CompetitionTeamUser::getCompetitionId,
                        ctu -> ctu,
                        (existing, replacement) -> existing, // 保留第一个（队长角色）
                        LinkedHashMap::new
                ));

        // 3. 为每条参赛记录构建DTO
        List<UserCompetitionDTO> userCompetitions = new ArrayList<>();
        for (CompetitionTeamUser ctu : uniqueParticipations.values()) {
            UserCompetitionDTO dto = new UserCompetitionDTO();

            // 设置竞赛信息
            Competition competition = competitionMapper.selectById(ctu.getCompetitionId());
            dto.setCompetition(competition);

            // 设置参赛信息
            dto.setCompetitionTeamUserId(ctu.getUserId()); // 这里应该存competition_team_user表的ID,但实体类中没有
            dto.setParticipationMode(ctu.getParticipationMode());
            dto.setRole(ctu.getRole());
            dto.setRank(ctu.getRank());
            dto.setCreateTime(ctu.getCreateTime());

            // 如果是团队参赛，获取团队信息
            if ("team".equalsIgnoreCase(ctu.getParticipationMode()) && ctu.getTeamId() != null) {
                dto.setTeamId(ctu.getTeamId());
                Team team = teamMapper.selectById(ctu.getTeamId());
                if (team != null) {
                    dto.setTeamName(team.getName());
                }
            }

            userCompetitions.add(dto);
        }

        return userCompetitions;
    }
}
