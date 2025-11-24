package com.cdnu.cgi.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.cdnu.cgi.entity.UserHonour;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 用户荣誉数据访问接口
 */
@Mapper
public interface UserHonourMapper extends BaseMapper<UserHonour> {
    
    /**
     * 根据ID查找用户荣誉
     */
    UserHonour selectById(Long id);
    
    /**
     * 根据用户ID查找荣誉
     */
    List<UserHonour> selectByUserId(Long userId);
    
    /**
     * 根据荣誉标题查找
     */
    List<UserHonour> selectByHonourTitle(String honourTitle);
    
    /**
     * 根据用户ID和荣誉标题查找
     */
    UserHonour selectByUserIdAndHonourTitle(@Param("userId") Long userId, @Param("honourTitle") String honourTitle);
    
    /**
     * 检查用户是否拥有某项荣誉
     */
    boolean existsByUserIdAndHonourTitle(@Param("userId") Long userId, @Param("honourTitle") String honourTitle);
    
    /**
     * 统计拥有某项荣誉的用户数量
     */
    Long countByHonourTitle(String honourTitle);
    
    /**
     * 获取所有荣誉标题列表（去重）
     */
    List<String> selectAllDistinctHonourTitles();
    
    /**
     * 查询所有用户荣誉
     */
    List<UserHonour> selectAll();
    
    /**
     * 插入用户荣誉
     */
    int insert(UserHonour userHonour);
    
    /**
     * 更新用户荣誉
     */
    int updateById(UserHonour userHonour);
    
    /**
     * 根据ID删除用户荣誉
     */
    int deleteById(Long id);
    
    /**
     * 删除用户的所有荣誉
     */
    int deleteByUserId(Long userId);
    
    /**
     * 统计用户荣誉总数
     */
    long count();
}