package com.example.student.university_career_platform.controller;

import com.example.student.university_career_platform.service.QwenChatService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/chat")
@CrossOrigin(origins = "*") // 允许 Vue 前端跨域调用
public class QwenChatController {

    @Autowired
    private QwenChatService qwenChatService;

    @PostMapping("/qwen/stream")
    public SseEmitter chatStream(@RequestBody Map<String, List<Map<String, String>>> requestBody) {
        List<Map<String, String>> historyMessages = requestBody.get("messages");

        // 创建 SseEmitter，0L 表示不设置超时时间，防止长对话断开
        SseEmitter emitter = new SseEmitter(0L);

        // 将任务甩给 Service 层处理
        qwenChatService.streamChat(historyMessages, emitter);

        return emitter;
    }
}