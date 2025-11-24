package com.cdnu.cgi.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.cdnu.cgi.entity.UserComprehensiveInfo;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 用户综合信息数据访问层
 */
@Mapper
public interface UserComprehensiveInfoMapper extends BaseMapper<UserComprehensiveInfo> {
    
    /**
     * 根据ID查询用户综合信息
     */
    UserComprehensiveInfo selectById(Long id);
    
    /**
     * 根据用户名查询用户综合信息
     */
    UserComprehensiveInfo selectByUsername(String username);
    
    /**
     * 查询所有用户综合信息
     */
    List<UserComprehensiveInfo> selectAll();
    
    /**
     * 根据ID列表查询用户综合信息
     */
    List<UserComprehensiveInfo> selectByIds(@Param("ids") List<Long> ids);
    
    /**
     * 根据专业查询用户
     */
    List<UserComprehensiveInfo> selectByMajor(String major);
    
    /**
     * 根据技能查询用户
     */
    List<UserComprehensiveInfo> selectBySkill(String skill);
    
    /**
     * 搜索用户（支持姓名、用户名、专业、技能）
     */
    List<UserComprehensiveInfo> searchUsers(@Param("keyword") String keyword);
    
    /**
     * 分页查询用户综合信息
     */
    List<UserComprehensiveInfo> selectWithPagination(@Param("offset") int offset, @Param("limit") int limit);
    
    /**
     * 统计用户总数
     */
    int countUsers();
}