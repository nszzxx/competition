package com.cdnu.cgi.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.cdnu.cgi.entity.Competition;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.sql.Timestamp;
import java.util.List;

/**
 * 竞赛数据访问接口
 */
@Mapper
public interface CompetitionMapper extends BaseMapper<Competition> {
    
    /**
     * 根据ID查找竞赛
     */
    Competition selectById(Long id);
    
    /**
     * 根据类别查找竞赛
     */
    List<Competition> selectByCategory(String category);

    
    /**
     * 根据标题模糊查询（忽略大小写）
     */
    List<Competition> selectByTitleContainingIgnoreCase(String title);
    
    /**
     * 查找正在进行的竞赛
     */
    List<Competition> selectActiveCompetitions();
    
    /**
     * 根据标签查找竞赛
     */
    List<Competition> selectByTagsContaining(String tag);
    
    /**
     * 查询所有竞赛
     */
    List<Competition> selectAll();
    
    /**
     * 根据参赛类型查询竞赛
     */
    List<Competition> selectByParticipationType(String participationMode);
    
    /**
     * 查询报名时间在指定范围内的竞赛
     */
    List<Competition> selectByRegistrationTimeRange(@Param("startTime") Timestamp startTime,
                                                    @Param("endTime") Timestamp endTime);
    
    /**
     * 查询当前可以报名的竞赛
     */
    List<Competition> selectCurrentlyRegisterable(@Param("currentTime") Timestamp currentTime);
    
    /**
     * 插入竞赛
     */
    int insert(Competition competition);
    
    /**
     * 更新竞赛信息
     */
    int updateById(Competition competition);
    
    /**
     * 根据ID删除竞赛
     */
    int deleteById(Long id);
    
    /**
     * 统计竞赛总数
     */
    long count();
}
