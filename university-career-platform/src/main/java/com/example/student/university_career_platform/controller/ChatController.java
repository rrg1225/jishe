package com.example.student.university_career_platform.controller;

import com.alibaba.dashscope.aigc.generation.GenerationResult;
import com.example.student.university_career_platform.service.CounselingService;
import io.reactivex.Flowable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.Map;

@RestController
@RequestMapping("/api/chat")
@CrossOrigin
public class ChatController {

    @Autowired
    private CounselingService counselingService;

    @PostMapping(value = "/qwen/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter streamChat(@RequestBody Map<String, String> request) {
        String userMessage = request.get("message");
        SseEmitter emitter = new SseEmitter(120000L);

        if (userMessage == null || userMessage.trim().isEmpty()) {
            emitter.completeWithError(new IllegalArgumentException("消息不能为空"));
            return emitter;
        }

        try {
            Flowable<GenerationResult> flowable = counselingService.streamChatWithStudent(userMessage);

            flowable.subscribe(
                    result -> {
                        String chunk = result.getOutput().getChoices().get(0).getMessage().getContent();
                        if (chunk != null) {
                            // 🚀 核心修复：用 JSON 包装每一块文字，彻底解决丢字断句问题
                            emitter.send(Map.of("text", chunk));
                        }
                    },
                    error -> emitter.completeWithError(error),
                    () -> {
                        emitter.send(Map.of("text", "[DONE]"));
                        emitter.complete();
                    }
            );
        } catch (Exception e) {
            emitter.completeWithError(e);
        }
        return emitter;
    }
}