package com.cdnu.cgi.controller;

import com.cdnu.cgi.entity.TeamApplication;
import com.cdnu.cgi.service.User.TeamService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 团队申请控制器
 */
@RestController
@RequestMapping("/api/team-applications")
@RequiredArgsConstructor
public class TeamApplicationController {
    
    private final TeamService teamService;

    /**
     * 申请加入团队
     */
    @PostMapping("/apply")
    public ResponseEntity<?> applyToJoinTeam(@RequestBody Map<String, Object> request) {
        try {
            Long userId = Long.valueOf(request.get("userId").toString());
            Long teamId = Long.valueOf(request.get("teamId").toString());
            
            TeamApplication application = teamService.applyToJoinTeam(userId, teamId);
            
            // 确保返回完整的申请信息
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "申请提交成功，请等待队长审核",
                "data", application
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "message", e.getMessage()
            ));
        }
    }
    
    /**
     * 审核申请
     */
    @PutMapping("/{applicationId}/review")
    public ResponseEntity<?> reviewApplication(
            @PathVariable Long applicationId,
            @RequestBody Map<String, Object> request) {
        try {
            boolean approved = Boolean.parseBoolean(request.get("approved").toString());
            String rejectionReason = request.get("rejectionReason") != null ? 
                request.get("rejectionReason").toString() : null;
            
            TeamApplication application = teamService.reviewApplication(applicationId, approved, rejectionReason);
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", approved ? "申请已通过" : "申请已拒绝",
                "data", application
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "message", e.getMessage()
            ));
        }
    }
    
    /**
     * 获取团队的所有申请
     */
    @GetMapping("/team/{teamId}")
    public ResponseEntity<?> getTeamApplications(@PathVariable Long teamId) {
        try {
            List<TeamApplication> applications = teamService.getTeamApplications(teamId);
            return ResponseEntity.ok(Map.of(
                "success", true,
                "data", applications
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "message", e.getMessage()
            ));
        }
    }
    
    /**
    /**
     * 获取用户的所有申请
     */
    @GetMapping("/user/{userId}")
    public ResponseEntity<?> getUserApplications(@PathVariable Long userId) {
        try {
            List<TeamApplication> applications = teamService.getUserApplications(userId);
            // 确保返回格式一致
            return ResponseEntity.ok(Map.of(
                "success", true,
                "data", applications
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "message", e.getMessage()
            ));
        }
    }
    
    /**
     * 获取队长待审核的申请
     */
    @GetMapping("/pending/leader/{leaderId}")
    public ResponseEntity<?> getPendingApplicationsByLeader(@PathVariable Long leaderId) {
        try {
            List<TeamApplication> applications = teamService.getPendingApplicationsByLeader(leaderId);
            return ResponseEntity.ok(Map.of(
                "success", true,
                "data", applications
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "message", e.getMessage()
            ));
        }
    }
    
    /**
    /**
     * 取消申请
     */
    @DeleteMapping("/{applicationId}")
    public ResponseEntity<?> cancelApplication(
            @PathVariable Long applicationId,
            @RequestParam Long userId) {
        try {
            boolean success = teamService.cancelApplication(applicationId, userId);
            if (success) {
                return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "申请已取消"
                ));
            } else {
                return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", "取消申请失败"
                ));
            }
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "message", e.getMessage()
            ));
        }
    }
    
    /**
     * 检查用户申请状态
     */
    @GetMapping("/check")
    public ResponseEntity<?> checkUserApplication(
            @RequestParam Long userId,
            @RequestParam Long teamId) {
        try {
            TeamApplication application = teamService.checkUserApplication(userId, teamId);
            return ResponseEntity.ok(Map.of(
                "success", true,
                "data", application,
                "hasApplication", application != null
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "message", e.getMessage()
            ));
        }
    }
    
    /**
     * 批量审核申请
     */
    @PutMapping("/batch-review")
    public ResponseEntity<?> batchReviewApplications(@RequestBody Map<String, Object> request) {
        try {
            @SuppressWarnings("unchecked")
            List<Object> rawIds = (List<Object>) request.get("applicationIds");
            // 将 Integer/Long 转换为 Long
            List<Long> applicationIds = rawIds.stream()
                .map(id -> Long.valueOf(id.toString()))
                .collect(java.util.stream.Collectors.toList());

            boolean approved = Boolean.parseBoolean(request.get("approved").toString());
            String rejectionReason = request.get("rejectionReason") != null ?
                request.get("rejectionReason").toString() : null;

            int successCount = teamService.batchReviewApplications(applicationIds, approved, rejectionReason);
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", String.format("成功处理 %d 个申请", successCount),
                "processedCount", successCount
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "message", e.getMessage()
            ));
        }
    }

    // ==================== 邀请相关接口 ====================
    /**
     * 邀请用户加入团队
     */
    @PostMapping("/invite")
    public ResponseEntity<?> inviteUserToTeam(@RequestBody Map<String, Object> request) {
        try {
            Long teamId = Long.valueOf(request.get("teamId").toString());
            Long inviterId = Long.valueOf(request.get("inviterId").toString());
            String identifier = request.get("identifier").toString(); // 用户名/邮箱/手机号
            String message = request.get("message") != null ? request.get("message").toString() : "";

            TeamApplication invitation = teamService.inviteUserToTeam(teamId, inviterId, identifier, message);

            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "邀请发送成功",
                "data", invitation
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "message", e.getMessage()
            ));
        }
    }

    /**
     * 获取用户收到的邀请列表
     */
    @GetMapping("/invitations/user/{userId}")
    public ResponseEntity<?> getUserInvitations(@PathVariable Long userId) {
        try {
            List<TeamApplication> invitations = teamService.getUserInvitations(userId);
            return ResponseEntity.ok(Map.of(
                "success", true,
                "data", invitations
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "message", e.getMessage()
            ));
        }
    }

    /**
     * 响应邀请（接受/拒绝）
     */
    @PutMapping("/invitations/{invitationId}/respond")
    public ResponseEntity<?> respondToInvitation(
            @PathVariable Long invitationId,
            @RequestBody Map<String, Object> request) {
        try {
            Long userId = Long.valueOf(request.get("userId").toString());
            boolean accepted = Boolean.parseBoolean(request.get("accepted").toString());

            TeamApplication invitation = teamService.respondToInvitation(invitationId, userId, accepted);
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", accepted ? "已接受邀请" : "已拒绝邀请",
                "data", invitation
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "message", e.getMessage()
            ));
        }
    }

    /**
     * 批量响应邀请
     */
    @PutMapping("/batch-respond")
    public ResponseEntity<?> batchRespondToInvitations(@RequestBody Map<String, Object> request) {
        try {
            @SuppressWarnings("unchecked")
            List<Object> rawIds = (List<Object>) request.get("invitationIds");
            // 将 Integer/Long 转换为 Long
            List<Long> invitationIds = rawIds.stream()
                .map(id -> Long.valueOf(id.toString()))
                .collect(java.util.stream.Collectors.toList());

            Long userId = Long.valueOf(request.get("userId").toString());
            boolean accepted = Boolean.parseBoolean(request.get("accepted").toString());

            int successCount = teamService.batchRespondToInvitations(invitationIds, userId, accepted);
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", String.format("成功处理 %d 个邀请", successCount),
                "processedCount", successCount
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "message", e.getMessage()
            ));
        }
    }
}