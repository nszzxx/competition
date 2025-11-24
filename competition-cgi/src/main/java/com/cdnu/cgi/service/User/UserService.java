package com.cdnu.cgi.service.User;

import com.cdnu.cgi.entity.User;
import com.cdnu.cgi.entity.UserHonour;
import com.cdnu.cgi.entity.UserSkill;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

/**
 * 用户服务接口
 */
public interface UserService {
    
    // 获取所有用户
    List<User> getAllUsers();
    
    // 根据ID获取用户
    Optional<User> getUserById(Long id);
    
    // 根据用户名获取用户
    Optional<User> getUserByUsername(String username);
    
    // 根据邮箱获取用户
    Optional<User> getUserByEmail(String email);
    
    // 获取用户技能
    List<UserSkill> getUserSkills(Long userId);
    
    // 根据专业获取用户
    List<User> getUsersByMajor(String major);
    
    // 保存用户
    User saveUser(User user);
    
    // 删除用户
    void deleteUser(Long id);
    
    // 保存用户技能
    UserSkill saveUserSkill(UserSkill userSkill);
    
    // 删除用户技能
    void deleteUserSkill(Long id);
    
    // 用户注册
    String register(User user);
    
    // 用户登录
    User login(String username, String password);
    
    // 更新用户信息
    String updateUser(User user);
    
    // 删除用户（带返回消息）
    String deleteUserWithMessage(Long id);
    
    // 获取用户荣誉列表
    List<UserHonour> getUserHonours(Long userId);
    
    // 保存用户荣誉
    UserHonour saveUserHonour(UserHonour userHonour);
    
    // 删除用户荣誉
    void deleteUserHonour(Long honourId);
    
    // 根据ID获取用户荣誉
    UserHonour getUserHonourById(Long id);
    
    // 检查用户是否拥有某项荣誉
    boolean hasUserHonour(Long userId, String honourTitle);

    String uploadAvatar(Long id, MultipartFile avatar) throws IOException;

    String uploadHonourCertificate(Long userId, MultipartFile certificate) throws IOException;
}
