package com.cdnu.cgi.service.User.impl;

import com.cdnu.cgi.config.UserConfig;
import com.cdnu.cgi.entity.Project;
import com.cdnu.cgi.mapper.ProjectMapper;
import com.cdnu.cgi.service.User.ProjectService;
import com.cdnu.cgi.service.config.ConfigService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Date;
import java.util.List;
import java.util.UUID;

/**
 * 项目服务实现类
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ProjectServiceImpl implements ProjectService {


    private final ProjectMapper projectMapper;
    private final ConfigService configService;


    @Value("${app.file.upload-dir}")
    private String storageRootPath;

    @Override
    public String uploadProjectDocument(Long competitionId, Long teamId, Long userId, String participationMode, MultipartFile file) {
        try {
            // 获取项目路径配置
            UserConfig userConfig = configService.getUserConfig();
            String projectPath = userConfig.getProjectPath();

            if (projectPath == null || projectPath.isEmpty()) {
                return "项目路径配置不存在";
            }

            // 验证文件
            if (file == null || file.isEmpty()) {
                return "文件不能为空";
            }

            // 检查文件类型（支持 PDF, DOC, DOCX）
            String originalFilename = file.getOriginalFilename();
            if (originalFilename == null) {
                return "文件名无效";
            }

            String fileExtension = originalFilename.substring(originalFilename.lastIndexOf(".")).toLowerCase();
            if (!fileExtension.matches("\\.(pdf|doc|docx)$")) {
                return "仅支持上传 PDF、DOC 或 DOCX 格式的文件";
            }

            // 生成唯一文件名
            String uniqueFileName = UUID.randomUUID().toString() + fileExtension;

            // 确保上传目录存在
            // 从URL路径提取文件系统路径
            // 例如: http://localhost:82/graduate/project/ -> 需要转换为实际文件系统路径
            // 这里假设项目路径已经配置为文件系统路径
            String basePath = projectPath;
            if (projectPath.startsWith("http://")) {
                // 如果是URL，需要提取路径部分并映射到文件系统
                // 这里需要根据实际部署情况调整
                String relativePath = "/graduate/project";
                // 简单的解析逻辑
                try {
                    String pathPart = projectPath.substring(projectPath.indexOf("://") + 3);
                    if(pathPart.contains("/")) {
                        relativePath = pathPart.substring(pathPart.indexOf("/"));
                    }
                } catch (Exception e) {
                    log.warn("解析项目路径URL失败，使用默认相对路径", e);
                }
                basePath = Paths.get(storageRootPath + relativePath).toString();
            }else {
                basePath = projectPath;
            }

            Path uploadPath = Paths.get(basePath);
            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
            }

            // 保存文件
            Path filePath = uploadPath.resolve(uniqueFileName);
            file.transferTo(filePath.toFile());

            // 构建文件URL
            String documentUrl = projectPath + uniqueFileName;

            // 查找是否已存在项目记录
            Project existingProject = null;
            if ("team".equals(participationMode) && teamId != null) {
                existingProject = projectMapper.selectByCompetitionIdAndTeamId(competitionId, teamId);
            } else if ("individual".equals(participationMode) && userId != null) {
                existingProject = projectMapper.selectByCompetitionIdAndUserId(competitionId, userId);
            }

            Date now = new Date(System.currentTimeMillis());

            if (existingProject != null) {
                // 更新现有项目
                existingProject.setDocumentUrl(documentUrl);
                existingProject.setUpdatedAt(now);
                projectMapper.updateById(existingProject);
                return "项目计划书更新成功";
            } else {
                // 创建新项目记录
                Project project = new Project();
                project.setCompetitionId(competitionId);
                project.setTeamId(teamId);
                project.setUserId(userId);
                project.setParticipationMode(participationMode);
                project.setDocumentUrl(documentUrl);
                project.setTitle("项目计划书");
                project.setCreatedAt(now);
                projectMapper.insert(project);
                return "项目计划书上传成功";
            }

        } catch (IOException e) {
            log.error("文件上传失败", e);
            return "文件上传失败：" + e.getMessage();
        } catch (Exception e) {
            log.error("上传项目计划书失败", e);
            return "上传失败：" + e.getMessage();
        }
    }

    @Override
    public Project getProjectByCompetitionAndParticipant(Long competitionId, Long teamId, Long userId) {
        try {
            if (teamId != null && teamId > 0) {
                // 团队参赛
                return projectMapper.selectByCompetitionIdAndTeamId(competitionId, teamId);
            } else if (userId != null && userId > 0) {
                // 个人参赛
                return projectMapper.selectByCompetitionIdAndUserId(competitionId, userId);
            }
            return null;
        } catch (Exception e) {
            log.error("查询项目失败", e);
            return null;
        }
    }

    @Override
    public String createProject(Project project) {
        try {
            // 检查团队是否已有项目
            if (project.getTeamId() != null && projectMapper.existsByTeamId(project.getTeamId())) {
                return "该团队已提交项目";
            }

            // 设置创建和更新时间
            Date now = new Date(System.currentTimeMillis());
            project.setCreatedAt(now);
            projectMapper.insert(project);
            return "项目创建成功";
        } catch (Exception e) {
            log.error("项目创建失败", e);
            return "项目创建失败：" + e.getMessage();
        }
    }

    @Override
    public Project getProjectById(Long id) {
        return projectMapper.selectById(id);
    }

    @Override
    public List<Project> getAllProjects() {
        return projectMapper.selectAll();
    }

    @Override
    public List<Project> getProjectsByTeamId(Long teamId) {
        return projectMapper.selectByTeamId(teamId);
    }

    @Override
    public List<Project> searchProjects(String keyword) {
        return projectMapper.selectByTitleContaining(keyword);
    }

    @Override
    public String updateProject(Project project) {
        try {
            Project existingProject = projectMapper.selectById(project.getId());
            if (existingProject != null) {
                // 更新项目更新时间
                Date now = new Date(System.currentTimeMillis());
                project.setUpdatedAt(now);
                projectMapper.updateById(project);
                return "项目更新成功";
            } else {
                return "项目不存在";
            }
        } catch (Exception e) {
            log.error("项目更新失败", e);
            return "项目更新失败：" + e.getMessage();
        }
    }

    @Override
    public String deleteProject(Long id) {
        try {
            Project existingProject = projectMapper.selectById(id);
            if (existingProject != null) {
                projectMapper.deleteById(id);
                return "项目删除成功";
            } else {
                return "项目不存在";
            }
        } catch (Exception e) {
            log.error("项目删除失败", e);
            return "项目删除失败：" + e.getMessage();
        }
    }

    @Override
    public boolean hasProjectByTeamId(Long teamId) {
        return projectMapper.existsByTeamId(teamId);
    }
}
