package com.example.student.university_career_platform.controller;

import com.alibaba.dashscope.aigc.generation.GenerationResult;
import com.example.student.university_career_platform.service.CounselingService;
import com.example.student.university_career_platform.model.ChatMessage;
import com.example.student.university_career_platform.repository.ChatMessageRepository;
import io.reactivex.Flowable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/chat")
@CrossOrigin
public class ChatController {

    @Autowired
    private CounselingService counselingService;

    @Autowired
    private ChatMessageRepository chatMessageRepository;

    // 获取聊天历史接口
    @GetMapping("/history")
    public ResponseEntity<List<ChatMessage>> getChatHistory(@RequestParam String userId, @RequestParam String otherId) {
        List<ChatMessage> history = chatMessageRepository.findChatHistory(userId, otherId);
        return ResponseEntity.ok(history);
    }

    // 流式对话接口（修复了持久化问题）
    @PostMapping(value = "/qwen/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter streamChat(@RequestBody Map<String, String> request) {
        String userMessage = request.get("message");
        String userId = request.get("userId"); // 获取前端传来的 userId

        SseEmitter emitter = new SseEmitter(120000L);
        if (userMessage == null || userMessage.trim().isEmpty()) {
            emitter.completeWithError(new IllegalArgumentException("消息不能为空"));
            return emitter;
        }

        // 1. 在调用 AI 前，先将【用户的提问】存入数据库
        if (userId != null && !userId.isEmpty()) {
            ChatMessage userMsgObj = new ChatMessage();
            userMsgObj.setSenderId(userId);
            userMsgObj.setReceiverId("0"); // "0" 专属代表明理导师
            userMsgObj.setContent(userMessage);
            chatMessageRepository.save(userMsgObj);
        }

        try {
            Flowable<GenerationResult> flowable = counselingService.streamChatWithStudent(userMessage);

            // 使用 StringBuilder 收集大模型一段段吐出来的零碎文本
            StringBuilder aiResponseBuilder = new StringBuilder();

            flowable.subscribe(
                    result -> {
                        String chunk = result.getOutput().getChoices().get(0).getMessage().getContent();
                        if (chunk != null) {
                            aiResponseBuilder.append(chunk); // 累加流式输出的内容
                            emitter.send(Map.of("text", chunk));
                        }
                    },
                    error -> emitter.completeWithError(error),
                    () -> {
                        // 2. 当 AI 回答完毕（流结束）时，将【AI的完整回答】存入数据库
                        if (userId != null && !userId.isEmpty()) {
                            ChatMessage aiMsgObj = new ChatMessage();
                            aiMsgObj.setSenderId("0"); // "0" 代表明理导师
                            aiMsgObj.setReceiverId(userId);
                            aiMsgObj.setContent(aiResponseBuilder.toString());
                            chatMessageRepository.save(aiMsgObj);
                        }

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