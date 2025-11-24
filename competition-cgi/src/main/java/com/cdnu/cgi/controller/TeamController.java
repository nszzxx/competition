package com.cdnu.cgi.controller;

import com.cdnu.cgi.dto.CompetitionTeamCardDto;
import com.cdnu.cgi.entity.Team;
import com.cdnu.cgi.entity.TeamMember;
import com.cdnu.cgi.entity.TeamMemberDetailInfo;
import com.cdnu.cgi.service.User.MatchScoreService;
import com.cdnu.cgi.service.User.TeamService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
 
@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/teams")
public class TeamController {
    private final TeamService teamService;
    private final MatchScoreService matchScoreService;
    
    /**
     * 创建团队
     */
    @PostMapping
    public ResponseEntity<?> createTeam(@RequestBody Map<String, Object> request) {
        try {
            Team team = new Team();
            team.setName((String) request.get("name"));
            team.setCompetitionId(Long.valueOf(request.get("competitionId").toString()));
            team.setLeaderId(Long.valueOf(request.get("leaderId").toString()));
            team.setDescription((String) request.get("description"));
            
            // 处理招募技能，支持中英文逗号
            String needSkills = (String) request.getOrDefault("needSkills", "");
            // 将中文逗号替换为英文逗号
            needSkills = needSkills.replace("，", ",");
            team.setNeedSkills(needSkills);
            
            teamService.createTeam(team);
            return ResponseEntity.ok(team);
        } catch (Exception e) {
            Map<String, String> errorMap = new HashMap<>();
            errorMap.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(errorMap);
        }
    }

    /**
     * 获取团队信息
     */
    @GetMapping("/{id}")
    public ResponseEntity<?> getTeamById(@PathVariable Long id) {
        try {
            Team team = teamService.getTeamById(id);
            log.info("Fetched team: {}", team);
            if (team != null) {
                return ResponseEntity.ok(team);
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            Map<String, String> errorMap = new HashMap<>();
            errorMap.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(errorMap);
        }
    }

    /**
     * 获取团队成员
     */
    @GetMapping("/{id}/members")
    public ResponseEntity<?> getTeamMembers(@PathVariable Long id) {
        try {
            List<TeamMember> members = teamService.getTeamMembers(id);
            log.info("Fetched members for team {}: {}", id, members);
            return ResponseEntity.ok(members);
        } catch (Exception e) {
            Map<String, String> errorMap = new HashMap<>();
            errorMap.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(errorMap);
        }
    }

    /**
     * 获取团队成员详细信息（包含技能和荣誉）
     * 需要验证查看权限：只有团队成员才能查看详细信息
     */
    @GetMapping("/{id}/members/details")
    public ResponseEntity<?> getTeamMemberDetails(@PathVariable Long id, @RequestParam Long currentUserId) {
        try {
            List<TeamMemberDetailInfo> memberDetails = teamService.getTeamMemberDetails(id, currentUserId);
            return ResponseEntity.ok(memberDetails);
        } catch (Exception e) {
            Map<String, String> errorMap = new HashMap<>();
            errorMap.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(errorMap);
        }
    }

    /**
     * 添加团队成员
     */
    @PostMapping("/{id}/members")
    public ResponseEntity<?> addTeamMember(@PathVariable Long id, @RequestBody Map<String, Object> memberData) {
        try {
            Long userId = Long.valueOf(memberData.get("userId").toString());
            String role = memberData.getOrDefault("role", "成员").toString();
            
            TeamMember member = teamService.addTeamMember(id, userId, role);
            return ResponseEntity.ok(member);
        } catch (Exception e) {
            Map<String, String> errorMap = new HashMap<>();
            errorMap.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(errorMap);
        }
    }

    /**
     * 移除团队成员
     */
    @DeleteMapping("/{teamId}/members/{userId}")
    public ResponseEntity<?> removeTeamMember(@PathVariable Long teamId, @PathVariable Long userId) {
        try {
            boolean result = teamService.removeTeamMember(teamId, userId);
            if (result) {
                Map<String, String> successMap = new HashMap<>();
                successMap.put("message", "成员移除成功");
                return ResponseEntity.ok(successMap);
            } else {
                Map<String, String> errorMap = new HashMap<>();
                errorMap.put("error", "成员不存在");
                return ResponseEntity.badRequest().body(errorMap);
            }
        } catch (Exception e) {
            Map<String, String> errorMap = new HashMap<>();
            errorMap.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(errorMap);
        }
    }

    /**
     * 获取用户参与的团队
     */
    @GetMapping("/user/{userId}")
    public ResponseEntity<?> getUserTeams(@PathVariable Long userId) {
        try {
            List<Team> teams = teamService.getUserTeams(userId);
            return ResponseEntity.ok(teams);
        } catch (Exception e) {
            Map<String, String> errorMap = new HashMap<>();
            errorMap.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(errorMap);
        }
    }

    
    /**
     * 获取竞赛的所有团队卡片
     */
    @GetMapping("/competition/{competitionId}/cards")
    public ResponseEntity<?> getCompetitionTeamCards(@PathVariable Long competitionId) {
        try {
            List<CompetitionTeamCardDto> teamCards = teamService.getCompetitionTeamCards(competitionId);
            return ResponseEntity.ok(teamCards);
        } catch (Exception e) {
            Map<String, String> errorMap = new HashMap<>();
            errorMap.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(errorMap);
        }
    }
    
    /**
     * 获取竞赛的所有团队卡片，包含用户匹配度
     */
    @GetMapping("/competition/{competitionId}/cards/match")
    public ResponseEntity<?> getCompetitionTeamCardsWithMatchScore(
            @PathVariable Long competitionId,
            @RequestParam Long userId) {
        try {
            // 直接调用服务方法获取带有匹配度的团队卡片
            List<CompetitionTeamCardDto> teamCards = teamService.getCompetitionTeamCardsWithMatchScore(competitionId, userId);
            return ResponseEntity.ok(teamCards);
        } catch (Exception e) {
            Map<String, String> errorMap = new HashMap<>();
            errorMap.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(errorMap);
        }
    }

    /**
     * 更新团队信息
     */
    @PutMapping("/{id}")
    public ResponseEntity<?> updateTeam(@PathVariable Long id, @RequestBody Team teamData) {
        try {
            // 处理招募技能，支持中英文逗号
            if (teamData.getNeedSkills() != null) {
                String needSkills = teamData.getNeedSkills();
                // 将中文逗号替换为英文逗号
                needSkills = needSkills.replace("，", ",");
                teamData.setNeedSkills(needSkills);
            }
            
            Team updatedTeam = teamService.updateTeam(id, teamData);
            return ResponseEntity.ok(updatedTeam);
        } catch (Exception e) {
            Map<String, String> errorMap = new HashMap<>();
            errorMap.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(errorMap);
        }
    }

    /**
     * 解散团队
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteTeam(@PathVariable Long id) {
        try {
            boolean result = teamService.deleteTeam(id);
            if (result) {
                Map<String, String> successMap = new HashMap<>();
                successMap.put("message", "团队删除成功");
                return ResponseEntity.ok(successMap);
            } else {
                Map<String, String> errorMap = new HashMap<>();
                errorMap.put("error", "团队不存在");
                return ResponseEntity.badRequest().body(errorMap);
            }
        } catch (Exception e) {
            Map<String, String> errorMap = new HashMap<>();
            errorMap.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(errorMap);
        }
    }

    /**
     * 搜索团队
     */
    @GetMapping("/search")
    public ResponseEntity<?> searchTeams(@RequestParam(required = false) String name,
                                       @RequestParam(required = false) Long competitionId) {
        try {
            List<Team> teams = teamService.searchTeams(name, competitionId);
            return ResponseEntity.ok(teams);
        } catch (Exception e) {
            Map<String, String> errorMap = new HashMap<>();
            errorMap.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(errorMap);
        }
    }

    /**
     * 获取所有团队
     */
    @GetMapping
    public ResponseEntity<?> getAllTeams() {
        try {
            List<Team> teams = teamService.getAllTeams();
            return ResponseEntity.ok(teams);
        } catch (Exception e) {
            Map<String, String> errorMap = new HashMap<>();
            errorMap.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(errorMap);
        }
    }
}