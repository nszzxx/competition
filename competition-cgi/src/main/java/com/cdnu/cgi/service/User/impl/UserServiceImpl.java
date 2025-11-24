package com.cdnu.cgi.service.User.impl;

import com.cdnu.cgi.config.UserConfig;
import com.cdnu.cgi.entity.User;
import com.cdnu.cgi.entity.UserHonour;
import com.cdnu.cgi.entity.UserSkill;
import com.cdnu.cgi.mapper.UserHonourMapper;
import com.cdnu.cgi.mapper.UserMapper;
import com.cdnu.cgi.mapper.UserSkillMapper;
import com.cdnu.cgi.service.User.UserService;
import com.cdnu.cgi.service.config.ConfigService;
import com.cdnu.cgi.util.MD5Util;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Timestamp;
import java.util.List;
import java.util.Optional;

/**
 * 用户服务实现类
 */
@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserMapper userMapper;
    private final UserSkillMapper userSkillMapper;
    private final UserHonourMapper userHonourMapper;
    private final ConfigService configService;

    @Value("${app.file.upload-dir}")
    private String storageRootPath;

    @Override
    public List<User> getAllUsers() {
        return userMapper.selectAll();
    }
    
    @Override
    public Optional<User> getUserById(Long id) {
        User user = userMapper.selectById(id);
        return Optional.ofNullable(user);
    }
    
    @Override
    public User saveUser(User user) {
        if (user.getId() == null) {
            userMapper.insert(user);
        } else {
            userMapper.updateById(user);
        }
        return user;
    }

    @Override
    public String updateUser(User user) {
        try {
            User existingUser = userMapper.selectById(user.getId());
            UserConfig userConfig = configService.getUserConfig();
            if (existingUser != null) {
                // 检查用户名是否与其他用户冲突
                User existingUserByUsername = userMapper.selectByUsername(user.getUsername());
                if (existingUserByUsername != null && !existingUserByUsername.getId().equals(user.getId())) {
                    return "用户名已存在";
                }
                // 检查邮箱是否已被其他用户使用
                if (user.getEmail() != null) {
                    User existingEmailUser = userMapper.selectByEmail(user.getEmail());
                    if (existingEmailUser != null && !existingEmailUser.getId().equals(user.getId())) {
                        return "邮箱已被其他用户使用";
                    }
                }
                userMapper.updateById(user);
                return "用户更新成功";
            } else {
                return "用户不存在";
            }
        } catch (Exception e) {
            return "用户更新失败：" + e.getMessage();
        }
    }
    
    @Override
    public void deleteUser(Long id) {
        userMapper.deleteById(id);
    }
    
    @Override
    public User login(String username, String password) {
        User user = userMapper.selectByUsername(username);
        if (user == null) {
            return null;
        }
        
        // 使用MD5Util进行密码验证
        if (!user.getPassword().equals(MD5Util.md5(password))) return null;
        return user;
    }
    
    @Override
    public String register(User user) {
        try {
            // 检查用户名是否已存在
            if (userMapper.existsByUsername(user.getUsername())) {
                return "用户名已存在";
            }
            
            // 检查邮箱是否已存在
            if (user.getEmail() != null && userMapper.existsByEmail(user.getEmail())) {
                return "邮箱已被注册";
            }
            
            // 加密密码
            user.setPassword(MD5Util.md5(user.getPassword()));
            
            // 设置默认角色为学生
            user.setRoleId(3); // 学生角色ID
            
            // 设置注册时间和状态
            user.setRegisterTime(new Timestamp(System.currentTimeMillis()));
            user.setStatus(0);
            
            userMapper.insert(user);
            return "注册成功";
        } catch (Exception e) {
            return "注册失败：" + e.getMessage();
        }
    }
    
    @Override
    public List<User> getUsersByMajor(String major) {
        return userMapper.selectByMajor(major);
    }
    
    @Override
    public List<UserSkill> getUserSkills(Long userId) {
        return userSkillMapper.selectByUserId(userId);
    }
    
    @Override
    public UserSkill saveUserSkill(UserSkill userSkill) {
        if (userSkill.getId() == null) {
            userSkillMapper.insert(userSkill);
        } else {
            userSkillMapper.updateById(userSkill);
        }
        return userSkill;
    }
    
    @Override
    public void deleteUserSkill(Long skillId) {
        userSkillMapper.deleteById(skillId);
    }
    
    @Override
    public Optional<User> getUserByUsername(String username) {
        User user = userMapper.selectByUsername(username);
        return Optional.ofNullable(user);
    }
    
    @Override
    public Optional<User> getUserByEmail(String email) {
        User user = userMapper.selectByEmail(email);
        return Optional.ofNullable(user);
    }
    
    @Override
    public String deleteUserWithMessage(Long id) {
        try {
            User existingUser = userMapper.selectById(id);
            if (existingUser != null) {
                userMapper.deleteById(id);
                return "用户删除成功";
            } else {
                return "用户不存在";
            }
        } catch (Exception e) {
            return "用户删除失败：" + e.getMessage();
        }
    }
    
    public List<User> searchUsers(String keyword) {
        if (keyword == null || keyword.trim().isEmpty()) {
            return userMapper.selectAll();
        }
        
        // 简化搜索实现
        return userMapper.selectAll().stream()
                .filter(user -> 
                    (user.getUsername() != null && user.getUsername().contains(keyword)) ||
                    (user.getRealName() != null && user.getRealName().contains(keyword)) ||
                    (user.getMajor() != null && user.getMajor().contains(keyword))
                )
                .collect(java.util.stream.Collectors.toList());
    }
    
    /**
     * 获取用户荣誉列表
     */
    public List<UserHonour> getUserHonours(Long userId) {
        return userHonourMapper.selectByUserId(userId);
    }
    
    /**
     * 添加用户荣誉
     */
    public UserHonour saveUserHonour(UserHonour userHonour) {
        if (userHonour.getId() == null) {
            userHonourMapper.insert(userHonour);
        } else {
            userHonourMapper.updateById(userHonour);
        }
        return userHonour;
    }
    
    /**
     * 删除用户荣誉
     */
    public void deleteUserHonour(Long honourId) {
        userHonourMapper.deleteById(honourId);
    }
    
    /**
     * 根据ID获取用户荣誉
     */
    public UserHonour getUserHonourById(Long id) {
        return userHonourMapper.selectById(id);
    }
    
    /**
     * 检查用户是否拥有某项荣誉
     */
    public boolean hasUserHonour(Long userId, String honourTitle) {
        return userHonourMapper.existsByUserIdAndHonourTitle(userId, honourTitle);
    }

    /**
     * 上传用户头像
     * 将图片保存为 userId + 图片后缀 的形式
     */
    @Override
    public String uploadAvatar(Long userId, MultipartFile file) throws IOException {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("文件不能为空");
        }

        // 获取配置的头像保存路径
        UserConfig userConfig = configService.getUserConfig();
        String basePath = userConfig.getAvatarPath();

        // 获取文件后缀
        String originalFilename = file.getOriginalFilename();
        String extension = "";
        if (originalFilename != null && originalFilename.contains(".")) {
            extension = originalFilename.substring(originalFilename.lastIndexOf("."));
        }

        // 生成新文件名：userId + 后缀
        String newFileName = userId + extension;

        // 构建完整的文件保存路径（去除URL前缀，获取实际文件系统路径）
        // 假设basePath格式为 "http://localhost:82/graduate/avatar/"
        // 需要转换为实际文件系统路径
        String fileSystemPath = convertUrlToFilePath(basePath);
        Path directoryPath = Paths.get(fileSystemPath);

        // 确保目录存在
        if (!Files.exists(directoryPath)) {
            Files.createDirectories(directoryPath);
        }

        // 保存文件
        Path filePath = directoryPath.resolve(newFileName);
        file.transferTo(filePath.toFile());

        // 返回访问URL
        return basePath + newFileName;
    }

    /**
     * 上传荣誉证书
     * 将证书保存为 userId + 原证书名 的形式
     */
    public String uploadHonourCertificate(Long userId, MultipartFile file) throws IOException {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("文件不能为空");
        }

        // 获取配置的荣誉证书保存路径
        UserConfig userConfig = configService.getUserConfig();
        String basePath = userConfig.getHonorPath();

        // 获取原始文件名
        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null || originalFilename.isEmpty()) {
            originalFilename = "certificate";
        }

        // 生成新文件名：userId + _ + 原文件名
        String newFileName = userId + "_" + originalFilename;

        // 构建完整的文件保存路径
        String fileSystemPath = convertUrlToFilePath(basePath);
        Path directoryPath = Paths.get(fileSystemPath);

        // 确保目录存在
        if (!Files.exists(directoryPath)) {
            Files.createDirectories(directoryPath);
        }

        // 保存文件
        Path filePath = directoryPath.resolve(newFileName);
        file.transferTo(filePath.toFile());

        // 返回访问URL
        return basePath + newFileName;
    }

    /**
     * 将URL路径转换为文件系统路径
     */
    private String convertUrlToFilePath(String urlPath) {
        // Apache服务器的根目录路径
        String relativePath = urlPath;
        if (urlPath.contains("://")) {
            // 提取路径部分
            relativePath = urlPath.substring(urlPath.indexOf("://") + 3);
            if (relativePath.contains("/")) {
                relativePath = relativePath.substring(relativePath.indexOf("/"));
            } else {
                relativePath = "/";
            }
        }

        // 如果不是以 / 开头，补上
        if (!relativePath.startsWith("/") && !relativePath.startsWith("\\")) {
            relativePath = File.separator + relativePath;
        }

        // 确保使用系统正确的分隔符
        relativePath = relativePath.replace("/", File.separator);

        // 拼接配置的根目录和相对路径
        // 最终路径类似：./uploads/graduate/avatar/
        return Paths.get(storageRootPath).toString() + relativePath;

    }
}