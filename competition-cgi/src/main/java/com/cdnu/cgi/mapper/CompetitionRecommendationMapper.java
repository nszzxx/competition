package com.cdnu.cgi.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.cdnu.cgi.entity.CompetitionRecommendation;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 竞赛推荐数据访问接口
 */
@Mapper
public interface CompetitionRecommendationMapper extends BaseMapper<CompetitionRecommendation> {
    
    /**
     * 根据ID查找推荐记录
     */
    CompetitionRecommendation selectById(Long id);
    
    /**
     * 根据用户ID查找推荐记录
     */
    List<CompetitionRecommendation> selectByUserIdOrderByScoreDesc(Long userId);
    
    /**
     * 根据用户ID和竞赛ID查找推荐记录
     */
    CompetitionRecommendation selectByUserIdAndCompetitionId(@Param("userId") Long userId, @Param("competitionId") Long competitionId);
    
    /**
     * 根据分数范围查找推荐
     */
    List<CompetitionRecommendation> selectByUserIdAndScoreGreaterThanEqual(@Param("userId") Long userId, @Param("minScore") Float minScore);
    
    /**
     * 查询所有推荐记录
     */
    List<CompetitionRecommendation> selectAll();
    
    /**
     * 插入推荐记录
     */
    int insert(CompetitionRecommendation competitionRecommendation);
    
    /**
     * 更新推荐记录
     */
    int updateById(CompetitionRecommendation competitionRecommendation);
    
    /**
     * 根据ID删除推荐记录
     */
    int deleteById(Long id);
    
    /**
     * 删除用户的所有推荐记录
     */
    int deleteByUserId(Long userId);
    
    /**
     * 统计推荐记录总数
     */
    long count();
}