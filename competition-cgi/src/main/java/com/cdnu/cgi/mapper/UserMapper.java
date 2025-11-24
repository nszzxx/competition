package com.cdnu.cgi.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.cdnu.cgi.entity.User;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * 用户数据访问接口
 */
@Mapper
public interface UserMapper extends BaseMapper<User> {
    
    /**
     * 根据ID查找用户
     */
    User selectById(Long id);
    
    /**
     * 根据用户名查找用户
     */
    User selectByUsername(String username);
    
    /**
     * 根据邮箱查找用户
     */
    User selectByEmail(String email);

    /**
     * 根据手机号查找用户
     */
    User selectByPhone(String phone);

    /**
     * 根据用户名、邮箱或手机号查找用户（用于邀请功能）
     */
    User selectByUsernameOrEmailOrPhone(String identifier);
    
    /**
     * 根据专业查找用户列表
     */
    List<User> selectByMajor(String major);
    
    /**
     * 检查用户名是否存在
     */
    boolean existsByUsername(String username);
    
    /**
     * 检查邮箱是否存在
     */
    boolean existsByEmail(String email);
    
    /**
     * 查询所有用户
     */
    List<User> selectAll();
    
    /**
     * 插入用户
     */
    int insert(User user);
    
    /**
     * 更新用户信息
     */
    int updateById(User user);
    
    /**
     * 根据ID删除用户
     */
    int deleteById(Long id);
    
    /**
     * 统计用户总数
     */
    long count();
}