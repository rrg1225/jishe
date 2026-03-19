package com.example.student.university_career_platform.controller;

import com.example.student.university_career_platform.service.ResumeGenService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/resume")
public class AiResumeController {

    @Autowired
    private ResumeGenService resumeGenService;

    /**
     * AI 一键生成/润色简历接口
     */
    @PostMapping("/ai-generate")
    public ResponseEntity<?> aiGenerateResume(@RequestBody Map<String, Object> rawResumeData) {
        try {
            // 调用 Service 层，将用户填写的草稿交给大模型处理
            Map<String, Object> optimizedData = resumeGenService.optimizeResume(rawResumeData);

            // 返回给前端，前端拿到后直接跳转到 ResumeFinalEdit.vue 并展示
            return ResponseEntity.ok(optimizedData);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().body("AI 生成失败：" + e.getMessage());
        }
    }
}
