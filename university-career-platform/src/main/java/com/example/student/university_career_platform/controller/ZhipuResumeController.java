package com.example.student.university_career_platform.controller;

import com.example.student.university_career_platform.service.ZhipuResumeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/resume")
@CrossOrigin(origins = "*") // 👈 极其重要：允许 Vue 跨域访问，解决部分 403/CORS 报错！
public class ZhipuResumeController {

    @Autowired
    private ZhipuResumeService zhipuResumeService;

    @PostMapping("/zhipu/generate")
    public ResponseEntity<?> generateResume(@RequestBody Map<String, String> request) {
        String userRequirements = request.get("requirements");

        if (userRequirements == null || userRequirements.trim().isEmpty()) {
            return ResponseEntity.badRequest().body("请提供简历要求或个人经历");
        }

        try {
            // 将重活累活交给 Service 层去办
            String generatedMarkdown = zhipuResumeService.generateResume(userRequirements);
            return ResponseEntity.ok(generatedMarkdown);

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().body("简历生成失败，错误信息：" + e.getMessage());
        }
    }
}