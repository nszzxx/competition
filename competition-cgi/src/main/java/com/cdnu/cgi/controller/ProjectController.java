package com.cdnu.cgi.controller;

import com.cdnu.cgi.entity.Project;
import com.cdnu.cgi.service.User.ProjectService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/projects")
public class ProjectController {

    @Autowired
    private ProjectService projectService;

    /**
     * 上传项目计划书
     */
    @PostMapping("/upload")
    public ResponseEntity<Map<String, Object>> uploadProjectDocument(
            @RequestParam("file") MultipartFile file,
            @RequestParam("competitionId") Long competitionId,
            @RequestParam(value = "teamId", required = false) Long teamId,
            @RequestParam(value = "userId", required = false) Long userId,
            @RequestParam("participationMode") String participationMode) {
        try {
            log.info("上传项目计划书 - 竞赛ID: {}, 团队ID: {}, 用户ID: {}, 参赛方式: {}",
                    competitionId, teamId, userId, participationMode);

            String result = projectService.uploadProjectDocument(competitionId, teamId, userId, participationMode, file);

            Map<String, Object> response = new HashMap<>();
            if (result.contains("成功")) {
                response.put("success", true);
                response.put("message", result);
                return ResponseEntity.ok(response);
            } else {
                response.put("success", false);
                response.put("message", result);
                return ResponseEntity.badRequest().body(response);
            }
        } catch (Exception e) {
            log.error("上传项目计划书失败", e);
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "上传失败：" + e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }
    }

    /**
     * 获取项目计划书信息
     */
    @GetMapping("/document")
    public ResponseEntity<Project> getProjectDocument(
            @RequestParam("competitionId") Long competitionId,
            @RequestParam(value = "teamId", required = false) Long teamId,
            @RequestParam(value = "userId", required = false) Long userId) {
        try {
            log.info("获取项目计划书 - 竞赛ID: {}, 团队ID: {}, 用户ID: {}", competitionId, teamId, userId);

            Project project = projectService.getProjectByCompetitionAndParticipant(competitionId, teamId, userId);

            if (project != null) {
                return ResponseEntity.ok(project);
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            log.error("获取项目计划书失败", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * 检查是否已上传项目计划书
     */
    @GetMapping("/check")
    public ResponseEntity<Map<String, Object>> checkProjectDocument(
            @RequestParam("competitionId") Long competitionId,
            @RequestParam(value = "teamId", required = false) Long teamId,
            @RequestParam(value = "userId", required = false) Long userId) {
        try {
            Project project = projectService.getProjectByCompetitionAndParticipant(competitionId, teamId, userId);

            Map<String, Object> response = new HashMap<>();
            response.put("hasDocument", project != null && project.getDocumentUrl() != null);
            if (project != null) {
                response.put("documentUrl", project.getDocumentUrl());
                response.put("updatedAt", project.getUpdatedAt());
            }

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("检查项目计划书失败", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    // 创建项目
    @PostMapping("/create")
    public String createProject(@RequestBody Project project) {
        return projectService.createProject(project);
    }

    // 获取所有项目
    @GetMapping("/list")
    public List<Project> getAllProjects() {
        return projectService.getAllProjects();
    }

    // 根据ID获取项目详情
    @GetMapping("/{id}")
    public Project getProjectById(@PathVariable Long id) {
        return projectService.getProjectById(id);
    }

    // 根据团队ID获取项目
    @GetMapping("/team/{teamId}")
    public List<Project> getProjectsByTeamId(@PathVariable Long teamId) {
        return projectService.getProjectsByTeamId(teamId);
    }

    // 搜索项目
    @GetMapping("/search")
    public List<Project> searchProjects(@RequestParam String keyword) {
        return projectService.searchProjects(keyword);
    }

    // 更新项目信息
    @PutMapping("/update")
    public String updateProject(@RequestBody Project project) {
        return projectService.updateProject(project);
    }

    // 删除项目
    @DeleteMapping("/{id}")
    public String deleteProject(@PathVariable Long id) {
        return projectService.deleteProject(id);
    }

    // 检查团队是否已有项目
    @GetMapping("/check/{teamId}")
    public boolean hasProjectByTeamId(@PathVariable Long teamId) {
        return projectService.hasProjectByTeamId(teamId);
    }
}
