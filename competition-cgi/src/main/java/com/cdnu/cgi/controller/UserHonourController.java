package com.cdnu.cgi.controller;

import com.cdnu.cgi.entity.UserHonour;
import com.cdnu.cgi.service.User.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 用户荣誉控制器
 */
@RestController
@RequestMapping("/api/user-honours")
@RequiredArgsConstructor
public class UserHonourController {
    
    private final UserService userService;
    
    /**
     * 获取用户荣誉列表
     */
    @GetMapping("/user/{userId}")
    public ResponseEntity<?> getUserHonours(@PathVariable Long userId) {
        try {
            List<UserHonour> honours = userService.getUserHonours(userId);
            return ResponseEntity.ok(honours);
        } catch (Exception e) {
            Map<String, String> errorMap = new HashMap<>();
            errorMap.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(errorMap);
        }
    }
    
    /**
     * 添加用户荣誉（支持文件上传）
     */
    @PostMapping
    public ResponseEntity<?> addUserHonour(
            @RequestParam("userId") Long userId,
            @RequestParam("title") String title,
            @RequestParam("description") String description,
            @RequestParam("date") String date,
            @RequestParam(value = "certificate", required = false) MultipartFile certificate) {
        try {
            UserHonour userHonour = new UserHonour();
            userHonour.setUserId(userId);
            userHonour.setHonourTitle(title);
            userHonour.setDescription(description);

            // 处理获得时间
            if (date != null && !date.isEmpty()) {
                userHonour.setObtainedTime(LocalDateTime.parse(date + "T00:00:00"));
            } else {
                userHonour.setObtainedTime(LocalDateTime.now());
            }

            // 处理证书文件上传
            if (certificate != null && !certificate.isEmpty()) {
                String certificateUrl = userService.uploadHonourCertificate(userId, certificate);
                userHonour.setCertificateImageUrl(certificateUrl);
            }

            UserHonour savedHonour = userService.saveUserHonour(userHonour);
            return ResponseEntity.ok(savedHonour);
        } catch (Exception e) {
            Map<String, String> errorMap = new HashMap<>();
            errorMap.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(errorMap);
        }
    }
    
    /**
     * 更新用户荣誉（支持文件上传）
     */
    @PutMapping("/{id}")
    public ResponseEntity<?> updateUserHonour(
            @PathVariable Long id,
            @RequestParam("title") String title,
            @RequestParam("description") String description,
            @RequestParam("date") String date,
            @RequestParam(value = "certificate", required = false) MultipartFile certificate) {
        try {
            UserHonour existingHonour = userService.getUserHonourById(id);
            if (existingHonour == null) {
                Map<String, String> errorMap = new HashMap<>();
                errorMap.put("error", "荣誉不存在");
                return ResponseEntity.notFound().build();
            }

            existingHonour.setHonourTitle(title);
            existingHonour.setDescription(description);

            // 处理获得时间
            if (date != null && !date.isEmpty()) {
                existingHonour.setObtainedTime(LocalDateTime.parse(date + "T00:00:00"));
            }

            // 处理证书文件上传
            if (certificate != null && !certificate.isEmpty()) {
                Long userId = existingHonour.getUserId();
                String certificateUrl = userService.uploadHonourCertificate(userId, certificate);
                existingHonour.setCertificateImageUrl(certificateUrl);
            }

            UserHonour updatedHonour = userService.saveUserHonour(existingHonour);
            return ResponseEntity.ok(updatedHonour);
        } catch (Exception e) {
            Map<String, String> errorMap = new HashMap<>();
            errorMap.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(errorMap);
        }
    }
    
    /**
     * 删除用户荣誉
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteUserHonour(@PathVariable Long id) {
        try {
            UserHonour existingHonour = userService.getUserHonourById(id);
            if (existingHonour == null) {
                Map<String, String> errorMap = new HashMap<>();
                errorMap.put("error", "荣誉不存在");
                return ResponseEntity.notFound().build();
            }
            
            userService.deleteUserHonour(id);
            Map<String, String> successMap = new HashMap<>();
            successMap.put("message", "荣誉删除成功");
            return ResponseEntity.ok(successMap);
        } catch (Exception e) {
            Map<String, String> errorMap = new HashMap<>();
            errorMap.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(errorMap);
        }
    }
    
    /**
     * 根据ID获取用户荣誉
     */
    @GetMapping("/{id}")
    public ResponseEntity<?> getUserHonourById(@PathVariable Long id) {
        try {
            UserHonour honour = userService.getUserHonourById(id);
            if (honour != null) {
                return ResponseEntity.ok(honour);
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            Map<String, String> errorMap = new HashMap<>();
            errorMap.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(errorMap);
        }
    }
    
    /**
     * 检查用户是否拥有某项荣誉
     */
    @GetMapping("/check")
    public ResponseEntity<?> checkUserHonour(
            @RequestParam Long userId,
            @RequestParam String honourTitle) {
        try {
            boolean hasHonour = userService.hasUserHonour(userId, honourTitle);
            Map<String, Object> result = new HashMap<>();
            result.put("hasHonour", hasHonour);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            Map<String, String> errorMap = new HashMap<>();
            errorMap.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(errorMap);
        }
    }
}