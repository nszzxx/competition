package com.cdnu.cgi.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.cdnu.cgi.entity.Project;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 项目数据访问接口
 */
@Mapper
public interface ProjectMapper extends BaseMapper<Project> {

    /**
     * 根据ID查找项目
     */
    Project selectById(Long id);

    /**
     * 根据团队ID查找项目
     */
    List<Project> selectByTeamId(Long teamId);

    /**
     * 根据竞赛ID和团队ID查找项目
     */
    Project selectByCompetitionIdAndTeamId(@Param("competitionId") Long competitionId, @Param("teamId") Long teamId);

    /**
     * 根据竞赛ID和用户ID查找项目（个人参赛）
     */
    Project selectByCompetitionIdAndUserId(@Param("competitionId") Long competitionId, @Param("userId") Long userId);

    /**
     * 根据标题模糊查询
     */
    List<Project> selectByTitleContaining(String title);

    /**
     * 检查团队是否已有项目
     */
    boolean existsByTeamId(Long teamId);

    /**
     * 查询所有项目
     */
    List<Project> selectAll();

    /**
     * 插入项目
     */
    int insert(Project project);

    /**
     * 更新项目信息
     */
    int updateById(Project project);

    /**
     * 根据ID删除项目
     */
    int deleteById(Long id);

    /**
     * 统计项目总数
     */
    long count();
}