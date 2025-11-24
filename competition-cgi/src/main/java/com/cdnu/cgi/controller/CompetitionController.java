package com.cdnu.cgi.controller;

import com.cdnu.cgi.dto.UserCompetitionDTO;
import com.cdnu.cgi.entity.Competition;
import com.cdnu.cgi.entity.CompetitionTeamUser;
import com.cdnu.cgi.entity.User;
import com.cdnu.cgi.service.User.CompetitionService;
import com.cdnu.cgi.service.User.UserService;
import com.cdnu.cgi.service.User.impl.CompetitionServiceImpl;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@RestController
@RequestMapping("/api/competitions")
@RequiredArgsConstructor
public class CompetitionController {
    private final CompetitionService competitionService;
    private final UserService userService;

    /**
     * 获取所有竞赛
     */
    @GetMapping
    public ResponseEntity<List<Competition>> getAllCompetitions() {
        try {
            List<Competition> competitions = competitionService.getAllCompetitions();
            return ResponseEntity.ok(competitions);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }
    
    /**
     * 获取用户参与的竞赛
     */
    @GetMapping("/user/{userId}")
    public ResponseEntity<List<UserCompetitionDTO>> getUserCompetitions(@PathVariable Long userId) {
        try {
            List<UserCompetitionDTO> userCompetitions = competitionService.getUserCompetitions(userId);
            return ResponseEntity.ok(userCompetitions);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * 根据ID获取竞赛详情
     */
    @GetMapping("/{id}")
    public ResponseEntity<Competition> getCompetitionById(@PathVariable Long id) {
        try {
            Optional<Competition> competition = competitionService.getCompetitionById(id);
            return competition.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * 根据分类搜索竞赛
     */
    @GetMapping("/search")
    public ResponseEntity<List<Competition>> searchCompetitions(
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String participationMode) {
        try {
            List<Competition> competitions = competitionService.searchCompetitions(category, keyword, participationMode);
            return ResponseEntity.ok(competitions);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * 获取竞赛分类统计
     */
    @GetMapping("/categories")
    public ResponseEntity<Map<String, Long>> getCompetitionCategories() {
        try {
            Map<String, Long> categories = competitionService.getCompetitionCategories();
            return ResponseEntity.ok(categories);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * 获取热门竞赛
     */
    @GetMapping("/popular")
    public ResponseEntity<List<Competition>> getPopularCompetitions(@RequestParam(defaultValue = "10") int limit) {
        try {
            List<Competition> competitions = competitionService.getPopularCompetitions(limit);
            return ResponseEntity.ok(competitions);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * 获取即将开始的竞赛
     */
    @GetMapping("/upcoming")
    public ResponseEntity<List<Competition>> getUpcomingCompetitions(@RequestParam(defaultValue = "10") int limit) {
        try {
            List<Competition> competitions = competitionService.getUpcomingCompetitions(limit);
            return ResponseEntity.ok(competitions);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * 根据参赛模式获取竞赛
     */
    @GetMapping("/by-mode/{mode}")
    public ResponseEntity<List<Competition>> getCompetitionsByMode(@PathVariable String mode) {
        try {
            List<Competition> competitions = competitionService.getCompetitionsByParticipationMode(mode);
            return ResponseEntity.ok(competitions);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }
    
    /**
     * 根据参赛类型获取竞赛
     */
    @GetMapping("/by-participation-type/{type}")
    public ResponseEntity<List<Competition>> getCompetitionsByParticipationType(@PathVariable String type) {
        try {
            List<Competition> allCompetitions = competitionService.getAllCompetitions();
            List<Competition> filteredCompetitions = allCompetitions.stream()
                .filter(comp -> {
                    if (comp.getParticipationMode() == null) {
                        return false;
                    }
                    
                    if (type.equalsIgnoreCase("individual")) {
                        return comp.getParticipationMode().equalsIgnoreCase("individual") || 
                               comp.getParticipationMode().equalsIgnoreCase("both");
                    } else if (type.equalsIgnoreCase("team")) {
                        return comp.getParticipationMode().equalsIgnoreCase("team") || 
                               comp.getParticipationMode().equalsIgnoreCase("both");
                    } else {
                        return comp.getParticipationMode().equalsIgnoreCase(type);
                    }
                })
                .collect(Collectors.toList());
            return ResponseEntity.ok(filteredCompetitions);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }
    
    /**
     * 获取当前可以报名的竞赛
     */
    @GetMapping("/registerable")
    public ResponseEntity<List<Competition>> getRegisterableCompetitions() {
        try {
            CompetitionServiceImpl serviceImpl = (CompetitionServiceImpl) competitionService;
            List<Competition> competitions = serviceImpl.getCurrentlyRegisterableCompetitions();
            return ResponseEntity.ok(competitions);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }
    
    /**
     * 获取竞赛的报名状态
     */
    @GetMapping("/{id}/registration-status")
    public ResponseEntity<Map<String, Object>> getCompetitionRegistrationStatus(@PathVariable Long id) {
        try {
            Optional<Competition> competitionOpt = competitionService.getCompetitionById(id);
            if (!competitionOpt.isPresent()) {
                return ResponseEntity.notFound().build();
            }
            
            Competition competition = competitionOpt.get();
            CompetitionServiceImpl serviceImpl = (CompetitionServiceImpl) competitionService;
            int status = serviceImpl.getRegistrationStatus(competition);
            
            Map<String, Object> response = new HashMap<>();
            response.put("competitionId", id);
            response.put("status", status);
            
            String statusText;
            switch (status) {
                case 0:
                    statusText = "报名未开始";
                    break;
                case 1:
                    statusText = "报名中";
                    break;
                case 2:
                    statusText = "报名已结束";
                    break;
                default:
                    statusText = "未知状态";
            }
            response.put("statusText", statusText);
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }
    
    /**
     * 用户/团队报名参赛
     */
    @PostMapping("/participate")
    public ResponseEntity<String> participateCompetition(@RequestBody CompetitionTeamUser competitionTeamUser) {
        try {
            log.info("用户/团队发起报名，参数：" + competitionTeamUser);
            competitionService.participateCompetition(competitionTeamUser);
            return ResponseEntity.ok("报名成功");
        } catch (Exception e) {
            return ResponseEntity.status(500).body("报名失败：" + e.getMessage());
        }
    }

    /**
     * 用户取消参赛
     * @param competitionId 竞赛ID
     * @param userId 用户ID
     * @param teamId 团队ID（可选，个人参赛时为null）
     */
    @DeleteMapping("/participate")
    public ResponseEntity<String> cancelParticipation(
            @RequestParam Long competitionId,
            @RequestParam Long userId,
            @RequestParam(required = false) Long teamId) {
        try {
            log.info("取消参赛请求 - 竞赛ID: {}, 用户ID: {}, 团队ID: {}", competitionId, userId, teamId);
            competitionService.cancelParticipation(competitionId, userId, teamId);
            return ResponseEntity.ok("取消参赛成功");
        } catch (Exception e) {
            log.error("取消参赛失败", e);
            return ResponseEntity.status(500).body("取消参赛失败：" + e.getMessage());
        }
    }

    /**
     * 查询某竞赛所有参赛详情
     */
    @GetMapping("/{competitionId}/participants")
    public ResponseEntity<List<CompetitionTeamUser>> getCompetitionParticipants(@PathVariable Long competitionId) {
        try {
            List<CompetitionTeamUser> participants = competitionService.getCompetitionParticipants(competitionId);
            return ResponseEntity.ok(participants);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * 查询某团队所有参赛详情
     */
    @GetMapping("/team/{teamId}/participants")
    public ResponseEntity<List<CompetitionTeamUser>> getTeamParticipants(@PathVariable Long teamId) {
        try {
            List<CompetitionTeamUser> participants = competitionService.getTeamParticipants(teamId);
            return ResponseEntity.ok(participants);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * 查询某用户所有参赛详情
     */
    @GetMapping("/user/{userId}/participants")
    public ResponseEntity<List<CompetitionTeamUser>> getUserParticipationDetails(@PathVariable Long userId) {
        try {
            List<CompetitionTeamUser> details = competitionService.getUserParticipationDetails(userId);
            return ResponseEntity.ok(details);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * 查询某竞赛所有参赛个人
     */
    @GetMapping("/{competitionId}/participants/user")
    public ResponseEntity<List<User>> getCompetitionUserParticipationDetails(
            @PathVariable Long competitionId) {
        try {
            // 可根据需要使用 competitionId 过滤
            List<CompetitionTeamUser> users = competitionService.getCompetitionIndividualParticipants(competitionId);
            log.info("传入的竞赛id: " + competitionId);
            for(CompetitionTeamUser ctu : users) {
                log.info("用户id: " + ctu.getUserId());
            }
            List<User> userDetails = users.stream()
                    .map(ctu -> userService.getUserById(ctu.getUserId()))
                    .filter(Optional::isPresent)
                    .map(Optional::get)
                    .map(user -> {
                        User simpleUser = new User();
                        simpleUser.setId(user.getId());
                        simpleUser.setUsername(user.getUsername());
                        simpleUser.setRealName(user.getRealName());
                        simpleUser.setMajor(user.getMajor());
                        simpleUser.setAvatarUrl(user.getAvatarUrl());
                        simpleUser.setStatus(user.getStatus());
                        return simpleUser;
                    })
                    .collect(Collectors.toList());
            return ResponseEntity.ok(userDetails);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * 多条件查询参赛详情
     */
    @PostMapping("/participants/search")
    public ResponseEntity<List<CompetitionTeamUser>> getParticipationDetails(@RequestBody CompetitionTeamUser competitionTeamUser) {
        try {
            List<CompetitionTeamUser> details = competitionService.getParticipationDetails(competitionTeamUser);
            return ResponseEntity.ok(details);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * 导入依赖
     */
    static {
        // 确保导入HashMap
        new HashMap<String, Object>();
    }
}