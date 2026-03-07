package com.example.student.university_career_platform.controller;

import com.example.student.university_career_platform.model.User;
import com.example.student.university_career_platform.repository.UserRepository;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/user")
@CrossOrigin
public class UserController {

    @Autowired
    private UserRepository userRepository;

    public static class ProfileUpdateRequest {
        @JsonProperty("nickname")
        public String nickName;

        @JsonProperty("age")
        public Integer age;

        @JsonProperty("gender")
        public String gender;

        @JsonProperty("bio")
        public String bio;

        // 🌟 新增：接收前端传来的学校/企业信息
        @JsonProperty("org")
        public String org;
    }

    @PostMapping("/profile")
    public ResponseEntity<?> updateProfile(@RequestBody ProfileUpdateRequest request) {
        String currentPhone = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByPhone(currentPhone).orElse(null);

        if (user == null) {
            return ResponseEntity.badRequest().body("用户不存在");
        }

        // 更新各个字段
        if (request.nickName != null && !request.nickName.isEmpty()) user.setNickName(request.nickName);
        if (request.age != null) user.setAge(request.age);
        if (request.gender != null) user.setGender(request.gender);
        if (request.bio != null) user.setBio(request.bio);

        // 🌟 新增：如果用户修改了学校/企业，就保存新的
        if (request.org != null && !request.org.isEmpty()) {
            user.setOrg(request.org);
        }

        if (user.getAvatar() == null || user.getAvatar().isEmpty()) {
            user.setAvatar("https://api.dicebear.com/7.x/adventurer/svg?seed=" + currentPhone);
        }

        userRepository.save(user);
        return ResponseEntity.ok("个人信息完善成功！");
    }

    @GetMapping("/info")
    public ResponseEntity<?> getUserInfo() {
        String currentPhone = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByPhone(currentPhone).orElse(null);
        if (user == null) {
            return ResponseEntity.status(404).body("用户不存在");
        }
        user.setPassword(null);
        return ResponseEntity.ok(user);
    }
}