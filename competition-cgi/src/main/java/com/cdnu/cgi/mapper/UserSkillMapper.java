package com.cdnu.cgi.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.cdnu.cgi.entity.UserSkill;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 用户技能数据访问接口
 */
@Mapper
public interface UserSkillMapper extends BaseMapper<UserSkill> {
    
    /**
     * 根据ID查找用户技能
     */
    UserSkill selectById(Long id);
    
    /**
     * 根据用户ID查找技能
     */
    List<UserSkill> selectByUserId(Long userId);
    
    /**
     * 根据技能名称查找用户
     */
    List<UserSkill> selectBySkill(String skill);
    
    /**
     * 根据用户ID和技能名称查找
     */
    UserSkill selectByUserIdAndSkill(@Param("userId") Long userId, @Param("skill") String skill);
    
    /**
     * 检查用户是否拥有某项技能
     */
    boolean existsByUserIdAndSkill(@Param("userId") Long userId, @Param("skill") String skill);
    
    /**
     * 统计拥有某项技能的用户数量
     */
    Long countBySkill(String skill);
    
    /**
     * 获取所有技能列表（去重）
     */
    List<String> selectAllDistinctSkills();
    
    /**
     * 查询所有用户技能
     */
    List<UserSkill> selectAll();
    
    /**
     * 插入用户技能
     */
    int insert(UserSkill userSkill);
    
    /**
     * 更新用户技能
     */
    int updateById(UserSkill userSkill);
    
    /**
     * 根据ID删除用户技能
     */
    int deleteById(Long id);
    
    /**
     * 删除用户的所有技能
     */
    int deleteByUserId(Long userId);
    
    /**
     * 统计用户技能总数
     */
    long count();
}