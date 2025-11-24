package com.cdnu.cgi.service.User;

import com.cdnu.cgi.entity.Project;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * 项目服务接口
 */
public interface ProjectService {

    /**
     * 上传项目计划书
     * @param competitionId 竞赛ID
     * @param teamId 团队ID（团队参赛时使用）
     * @param userId 用户ID（个人参赛时使用）
     * @param participationMode 参赛方式 (individual/team)
     * @param file 文件
     * @return 操作结果
     */
    String uploadProjectDocument(Long competitionId, Long teamId, Long userId, String participationMode, MultipartFile file);

    /**
     * 获取项目计划书信息
     * @param competitionId 竞赛ID
     * @param teamId 团队ID（团队参赛时使用）
     * @param userId 用户ID（个人参赛时使用）
     * @return 项目信息
     */
    Project getProjectByCompetitionAndParticipant(Long competitionId, Long teamId, Long userId);

    /**
     * 创建项目
     */
    String createProject(Project project);

    /**
     * 根据ID获取项目
     */
    Project getProjectById(Long id);

    /**
     * 获取所有项目
     */
    List<Project> getAllProjects();

    /**
     * 根据团队ID获取项目
     */
    List<Project> getProjectsByTeamId(Long teamId);

    /**
     * 搜索项目
     */
    List<Project> searchProjects(String keyword);

    /**
     * 更新项目信息
     */
    String updateProject(Project project);

    /**
     * 删除项目
     */
    String deleteProject(Long id);

    /**
     * 检查团队是否已有项目
     */
    boolean hasProjectByTeamId(Long teamId);
}