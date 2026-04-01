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

import java.util.HashMap;
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

    // ==========================================
    // 🌟 企业级优化：消息漫游与红点回执相关接口
    // ==========================================

    // 1. 获取聊天历史接口 (兼容了原有的 userId/otherId 和新的 myUserId/friendId)
    @GetMapping("/history")
    public Map<String, Object> getChatHistory(
            @RequestParam(required = false) String userId,
            @RequestParam(required = false) String otherId,
            @RequestParam(required = false) String myUserId,
            @RequestParam(required = false) String friendId,
            @RequestParam(required = false) Long lastMessageId) { // 🌟 新增游标参数

        String uId = myUserId != null ? myUserId : userId;
        String fId = friendId != null ? friendId : otherId;

        List<ChatMessage> history;
        if (lastMessageId == null || lastMessageId <= 0) {
            // 如果前端没传，说明是第一次点开聊天框，拉取最新的 50 条
            history = chatMessageRepository.findChatHistory(uId, fId);
        } else {
            // 🚀 如果前端传了，说明用户正在下拉加载更多，去查比 lastMessageId 更老的 20 条
            history = chatMessageRepository.findChatHistoryBefore(uId, fId, lastMessageId);
        }

        Map<String, Object> result = new HashMap<>();
        result.put("code", 200);
        result.put("data", history);
        return result;
    }

    // 2. 获取针对某个好友的未读消息数量 (新增：用于渲染 🔴 小红点)
    @GetMapping("/unread-count")
    public Map<String, Object> getUnreadCount(@RequestParam String myUserId, @RequestParam String friendId) {
        Map<String, Object> result = new HashMap<>();
        long count = chatMessageRepository.countUnreadFromFriend(myUserId, friendId);
        result.put("code", 200);
        result.put("data", count);
        return result;
    }

    // 3. 将某个好友的消息标记为已读 (当用户点开聊天窗口时清空红点)
    // 兼容了原来的 /read 路径和新的 /mark-read 路径
    @PostMapping({"/read", "/mark-read"})
    public Map<String, Object> markMessagesAsRead(@RequestParam String myUserId, @RequestParam String friendId) {
        chatMessageRepository.markAsRead(myUserId, friendId);
        Map<String, Object> result = new HashMap<>();
        result.put("code", 200);
        result.put("msg", "消息已标记为已读");
        return result;
    }


    // ==========================================
    // 🤖 原有核心业务：AI 导师大模型流式对话
    // ==========================================

    // 流式对话接口（保留原封不动，负责明理导师AI对话）
    @PostMapping(value = "/qwen/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter streamChat(@RequestBody Map<String, String> request) {
        String userMessage = request.get("message");
        String userId = request.get("userId"); // 获取前端传来的 userId

        SseEmitter emitter = new SseEmitter(120000L); // 120秒超时

        // 🌟 修复：绑定超时和错误回调，避免前端异常断开导致 Tomcat 抛错崩溃
        emitter.onTimeout(emitter::complete);
        emitter.onError(e -> emitter.complete());

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
                        // 🌟 修复：严谨判空，防止大模型触发限流或敏感词返回空结果导致抛出空指针
                        if (result.getOutput() != null &&
                                result.getOutput().getChoices() != null &&
                                !result.getOutput().getChoices().isEmpty()) {

                            String chunk = result.getOutput().getChoices().get(0).getMessage().getContent();
                            if (chunk != null) {
                                aiResponseBuilder.append(chunk); // 累加流式输出的内容
                                try {
                                    emitter.send(Map.of("text", chunk));
                                } catch (Exception e) {
                                    // 如果推送过程中客户端主动断开，直接关闭流
                                    emitter.complete();
                                }
                            }
                        }
                    },
                    error -> {
                        // 🌟 修复：捕获流内部抛出的异常，友善兜底，防止直接切断 Socket
                        error.printStackTrace();
                        try {
                            emitter.send(Map.of("text", "\n[网络连接波动，请稍后再试]"));
                            emitter.complete();
                        } catch (Exception ignore) {
                            emitter.completeWithError(error);
                        }
                    },
                    () -> {
                        // 2. 当 AI 回答完毕（流结束）时，将【AI的完整回答】存入数据库
                        if (userId != null && !userId.isEmpty()) {
                            ChatMessage aiMsgObj = new ChatMessage();
                            aiMsgObj.setSenderId("0"); // "0" 代表明理导师
                            aiMsgObj.setReceiverId(userId);
                            aiMsgObj.setContent(aiResponseBuilder.toString());
                            chatMessageRepository.save(aiMsgObj);
                        }

                        try {
                            emitter.send(Map.of("text", "[DONE]"));
                        } catch (Exception ignore) {}
                        emitter.complete();
                    }
            );
        } catch (Exception e) {
            emitter.completeWithError(e);
        }

        return emitter;
    }
}