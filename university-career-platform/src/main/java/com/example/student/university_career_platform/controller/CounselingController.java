package com.example.student.university_career_platform.controller;

import com.example.student.university_career_platform.service.CounselingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/counseling")
@CrossOrigin
public class CounselingController {

    @Autowired
    private CounselingService counselingService;

    @PostMapping("/chat")
    public ResponseEntity<?> chat(@RequestBody Map<String , String> request) {
        String userMessage = request.get("message");
        if (userMessage == null || userMessage.trim().isEmpty()) {
            return ResponseEntity.badRequest().body("消息不能为空");
        }

        String reply = counselingService.chatWithStudent(userMessage);
        return ResponseEntity.ok(Map.of("reply", reply));
    }
}
