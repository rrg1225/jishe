package com.example.student.university_career_platform.controller;

import com.example.student.university_career_platform.common.Result;
import com.example.student.university_career_platform.model.User;
import com.example.student.university_career_platform.repository.UserRepository;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.Objects;

@RestController
@RequestMapping("/api/user")
@CrossOrigin
public class UserController {

    @Autowired
    private UserRepository userRepository;

    @Data
    public static class ProfileUpdateRequest {
        @NotBlank(message = "昵称不能为空")
        @JsonProperty("nickname")
        public String nickName;

        @Min(value = 1, message = "年龄不能小于1")
        @Max(value = 120, message = "年龄不能大于120")
        public Integer age;

        public String gender;

        public String bio;

        public String org;
    }

    @Validated
    @PostMapping("/profile")
    public Result<?> updateProfile(@RequestBody ProfileUpdateRequest request) {
        String currentPhone = Objects.requireNonNull(SecurityContextHolder.getContext().getAuthentication()).getName();
        User user = userRepository.findByPhone(currentPhone).orElse(null);

        if (user == null) {
            return Result.error(400, "用户不存在");
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
        return Result.success(user);
    }

    @GetMapping("/info")
    public Result<User> getUserInfo() {
        String currentPhone = Objects.requireNonNull(SecurityContextHolder.getContext().getAuthentication()).getName();
        User user = userRepository.findByPhone(currentPhone).orElse(null);
        if (user == null) {
            return Result.error(404,"用户不存在");
        }
        user.setPassword(null);
        return Result.success(user);
    }

    /**
     * 模糊搜索用户 (按昵称或手机号)
     */
    @GetMapping("/search")
    public Result<?> searchUser(@RequestParam String keyword) {
        // 1. 调用刚才在 Repository 写的模糊搜索方法
        java.util.List<User> users = userRepository.findByNickNameContainingOrPhoneContaining(keyword, keyword);

        // 2. 过滤掉密码等敏感信息，只返回前端需要的字段
        java.util.List<java.util.Map<String, Object>> safeUserList = new java.util.ArrayList<>();
        for (User user : users) {
            java.util.Map<String, Object> map = new java.util.HashMap<>();
            // 前端好友表用 phone 作为字符串类型的 ID
            map.put("id", user.getPhone());
            map.put("nickName", user.getNickName());
            map.put("phone", user.getPhone());
            map.put("avatar", user.getAvatar());
            safeUserList.add(map);
        }

        return Result.success(safeUserList);
    }

}