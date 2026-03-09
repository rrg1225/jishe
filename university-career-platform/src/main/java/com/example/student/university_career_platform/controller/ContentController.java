package com.example.student.university_career_platform.controller;

import com.example.student.university_career_platform.model.Content;
import com.example.student.university_career_platform.model.User;
import com.example.student.university_career_platform.repository.ContentRepository;
import com.example.student.university_career_platform.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/content")
@CrossOrigin
public class ContentController {

    @Autowired
    private ContentRepository contentRepository;
    @Autowired
    private UserRepository userRepository;

    @PostMapping("/publish")
    public ResponseEntity<?> publishContent(@RequestBody Content request) {
        // 安全获取当前登录用户
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated() || "anonymousUser".equals(authentication.getName())) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("请先登录");
        }
        String phone = authentication.getName();
        User user = userRepository.findByPhone(phone).orElse(null);



         // 将作者信息填入内容对象中
        request.setAuthorPhone(phone);
        request.setAuthorName(user != null ? user.getNickName() : "匿名校友");

        // 存入数据库
        contentRepository.save(request);
        return ResponseEntity.ok("发布成功");
    }
}
