package com.example.student.university_career_platform.controller;

import com.example.student.university_career_platform.service.ResumeGenService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/resume-gen")
@CrossOrigin
public class AiResumeController {

    @Autowired
    private ResumeGenService resumeGenService;

    @PostMapping("/generate")
    public ResponseEntity<?> generate(@RequestBody Map<String, String> request) {
        String rawInfo = request.get("rawInfo");
        if (rawInfo == null || rawInfo.trim().isEmpty()) {
            return ResponseEntity.badRequest().body("个人信息不能为空");
        }

        String markdownResume = resumeGenService.generateAiResume(rawInfo);
        return ResponseEntity.ok(Map.of("resumeContent", markdownResume));
    }
}
