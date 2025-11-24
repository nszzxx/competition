package com.cdnu.cgi.controller;

import com.cdnu.cgi.entity.User;
import com.cdnu.cgi.entity.UserSkill;
import com.cdnu.cgi.service.User.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor
public class UserController {
    private final UserService userService;

    /**
     * 获取用户信息
     */
    @GetMapping("/{id}")
    public ResponseEntity<User> getUserById(@PathVariable Long id) {
        try {
            Optional<User> userOpt = userService.getUserById(id);
            if (userOpt.isPresent()) {
                return ResponseEntity.ok(userOpt.get());
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * 批量获取用户信息
     */
    @PostMapping("/batch")
    public ResponseEntity<Map<String, Object>> getUsersByIds(@RequestBody Map<String, Object> request) {
        try {
            @SuppressWarnings("unchecked")
            List<Long> userIds = (List<Long>) request.get("userIds");
            
            Map<Long, User> userMap = new HashMap<>();
            for (Long userId : userIds) {
                Optional<User> userOpt = userService.getUserById(userId);
                if (userOpt.isPresent()) {
                    userMap.put(userId, userOpt.get());
                }
            }
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "data", userMap
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "message", e.getMessage()
            ));
        }
    }

    /**
     * 更新用户信息
     */
    @PutMapping("/{id}")
    public ResponseEntity<Map<String, String>> updateUser(@PathVariable Long id, @RequestBody User user) {
        try {
            user.setId(id);
            String result = userService.updateUser(user);
            Map<String, String> response = new HashMap<>();
            response.put("message", result);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(errorResponse);
        }
    }

    /**
     * 获取用户技能
     */
    @GetMapping("/{id}/skills")
    public ResponseEntity<List<UserSkill>> getUserSkills(@PathVariable Long id) {
        try {
            List<UserSkill> skills = userService.getUserSkills(id);
            return ResponseEntity.ok(skills);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * 添加用户技能
     */
    @PostMapping("/{id}/skills")
    public ResponseEntity<UserSkill> addUserSkill(@PathVariable Long id, @RequestBody Map<String, String> skillData) {
        try {
            String skillName = skillData.get("skill");
            // 创建 UserSkill 对象并保存
            UserSkill userSkill = new UserSkill();
            userSkill.setUserId(id);
            userSkill.setSkill(skillName);
            UserSkill savedSkill = userService.saveUserSkill(userSkill);
            return ResponseEntity.ok(savedSkill);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * 删除用户技能
     */
    @DeleteMapping("/{userId}/skills/{skillId}")
    public ResponseEntity<Void> removeUserSkill(@PathVariable Long userId, @PathVariable Long skillId) {
        try {
            userService.deleteUserSkill(skillId);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * 获取用户统计信息
     */
    @GetMapping("/{id}/stats")
    public ResponseEntity<Map<String, Object>> getUserStats(@PathVariable Long id) {
        try {
            // 简化实现，返回基本统计信息
            Map<String, Object> stats = new HashMap<>();
            stats.put("userId", id);
            stats.put("skillCount", userService.getUserSkills(id).size());
            stats.put("message", "用户统计信息");
            return ResponseEntity.ok(stats);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * 用户登录
     */
    @PostMapping("/login")
    public ResponseEntity<User> login(@RequestBody Map<String, String> credentials) {
        try {
            String username = credentials.get("username");
            String password = credentials.get("password");
            User user = userService.login(username, password);
            if (user != null) {
                return ResponseEntity.ok(user);
            } else {
                return ResponseEntity.status(401).build();
            }
        } catch (Exception e) {
            return ResponseEntity.status(500).build();
        }
    }

    /**
     * 用户注册
     */
    @PostMapping("/register")
    public ResponseEntity<Map<String, Object>> register(@RequestBody User user) {
        try {
            String result = userService.register(user);
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", result);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(errorResponse);
        }
    }

    /**
     * 获取所有用户（管理员功能）
     */
    @GetMapping
    public ResponseEntity<List<User>> getAllUsers() {
        try {
            List<User> users = userService.getAllUsers();
            return ResponseEntity.ok(users);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * 根据专业搜索用户
     */
    @GetMapping("/search")
    public ResponseEntity<List<User>> searchUsers(@RequestParam(required = false) String major,
                                                  @RequestParam(required = false) String skill) {
        try {
            List<User> users;
            if (major != null && !major.isEmpty()) {
                users = userService.getUsersByMajor(major);
            } else {
                users = userService.getAllUsers();
            }
            return ResponseEntity.ok(users);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * 上传用户头像
     */
    @PostMapping("/{id}/avatar")
    public ResponseEntity<Map<String, Object>> uploadAvatar(
            @PathVariable Long id,
            @RequestParam("avatar") MultipartFile avatar) {
        try {
            String avatarUrl = userService.uploadAvatar(id, avatar);

            // 更新用户头像URL
            Optional<User> userOpt = userService.getUserById(id);
            if (userOpt.isPresent()) {
                User user = userOpt.get();
                user.setAvatarUrl(avatarUrl);
                userService.saveUser(user);
            }

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("avatarUrl", avatarUrl);
            response.put("message", "头像上传成功");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "头像上传失败: " + e.getMessage());
            return ResponseEntity.badRequest().body(errorResponse);
        }
    }
}