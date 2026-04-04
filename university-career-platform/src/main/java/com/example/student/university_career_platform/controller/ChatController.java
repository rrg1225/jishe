package com.example.student.university_career_platform.controller;

import com.alibaba.dashscope.aigc.generation.GenerationResult;
import com.example.student.university_career_platform.model.ChatMessage;
import com.example.student.university_career_platform.repository.ChatMessageRepository;
import com.example.student.university_career_platform.service.CounselingService;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.reactivex.Flowable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.time.ZoneId;
import java.util.*;

@RestController
@RequestMapping("/api/chat")
@CrossOrigin
public class ChatController {

    @Autowired
    private CounselingService counselingService;

    @Autowired
    private ChatMessageRepository chatMessageRepository;

    @Autowired
    private StringRedisTemplate redisTemplate;

    @Autowired
    private ObjectMapper objectMapper;

    // ==========================================
    // 🌟 终极优化：IM 智能路由网关 (Dispatcher)
    // ==========================================
    @GetMapping("/dispatcher")
    public Map<String, Object> getBestClusterNode() {
        Map<String, Object> result = new HashMap<>();

        // 从 Redis ZSET 中查出当前连接数最少（分数最低）的 1 台服务器！
        Set<String> bestServers = redisTemplate.opsForZSet().range("IM_CLUSTER_LOAD", 0, 0);

        String bestNode = "120.27.205.204:8080"; // 兜底默认节点
        if (bestServers != null && !bestServers.isEmpty()) {
            bestNode = bestServers.iterator().next(); // 拿到最优节点 IP:Port
        }

        result.put("code", 200);
        result.put("data", "ws://" + bestNode); // 拼装成 ws 协议返回
        result.put("msg", "调度成功，已为您分配全网最空闲节点");
        return result;
    }

    // ==========================================
    // 聊天历史记录 (缓存优化版)
    // ==========================================
    @GetMapping("/history")
    public Map<String, Object> getChatHistory(
            @RequestParam(required = false) String userId,
            @RequestParam(required = false) String otherId,
            @RequestParam(required = false) String myUserId,
            @RequestParam(required = false) String friendId) {

        String uId = myUserId != null ? myUserId : userId;
        String fId = friendId != null ? friendId : otherId;
        String roomKey = "CHAT_CACHE:" + uId + ":" + fId;

        List<ChatMessage> finalHistory = new ArrayList<>();

        Set<String> cachedMessages = redisTemplate.opsForZSet().range(roomKey, 0, -1);

        if (cachedMessages != null && !cachedMessages.isEmpty()) {
            for (String msgStr : cachedMessages) {
                try {
                    finalHistory.add(objectMapper.readValue(msgStr, ChatMessage.class));
                } catch (Exception e) {}
            }
        } else {
            finalHistory = chatMessageRepository.findChatHistory(uId, fId);
            if (!finalHistory.isEmpty()) {
                for (ChatMessage msg : finalHistory) {
                    try {
                        long time = System.currentTimeMillis();
                        if (msg.getGmtCreated() != null) {
                            time = msg.getGmtCreated().atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();
                        }
                        redisTemplate.opsForZSet().add(roomKey, objectMapper.writeValueAsString(msg), time);
                    } catch (Exception e) {}
                }
                redisTemplate.expire(roomKey, 7, java.util.concurrent.TimeUnit.DAYS);
            }
        }

        Map<String, Object> result = new HashMap<>();
        result.put("code", 200);
        result.put("data", finalHistory);
        return result;
    }

    @GetMapping("/unread-count")
    public Map<String, Object> getUnreadCount(@RequestParam String myUserId, @RequestParam String friendId) {
        Map<String, Object> result = new HashMap<>();
        long count = chatMessageRepository.countUnreadFromFriend(myUserId, friendId);
        result.put("code", 200);
        result.put("data", count);
        return result;
    }

    @PostMapping({"/read", "/mark-read"})
    public Map<String, Object> markMessagesAsRead(@RequestParam String myUserId, @RequestParam String friendId) {
        chatMessageRepository.markAsRead(myUserId, friendId);
        Map<String, Object> result = new HashMap<>();
        result.put("code", 200);
        result.put("msg", "消息已标记为已读");
        return result;
    }

    // ==========================================
    // AI 导师大模型流式对话 (保留原逻辑)
    // ==========================================
    @PostMapping(value = "/qwen/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter streamChat(@RequestBody Map<String, String> request) {
        String userMessage = request.get("message");
        String userId = request.get("userId");

        SseEmitter emitter = new SseEmitter(120000L);
        emitter.onTimeout(emitter::complete);
        emitter.onError(e -> emitter.complete());

        if (userMessage == null || userMessage.trim().isEmpty()) {
            emitter.completeWithError(new IllegalArgumentException("消息不能为空"));
            return emitter;
        }

        if (userId != null && !userId.isEmpty()) {
            ChatMessage userMsgObj = new ChatMessage();
            userMsgObj.setSenderId(userId);
            userMsgObj.setReceiverId("0");
            userMsgObj.setContent(userMessage);
            chatMessageRepository.save(userMsgObj);
        }

        try {
            Flowable<GenerationResult> flowable = counselingService.streamChatWithStudent(userMessage);
            StringBuilder aiResponseBuilder = new StringBuilder();

            flowable.subscribe(
                    result -> {
                        if (result.getOutput() != null &&
                                result.getOutput().getChoices() != null &&
                                !result.getOutput().getChoices().isEmpty()) {
                            String chunk = result.getOutput().getChoices().get(0).getMessage().getContent();
                            if (chunk != null) {
                                aiResponseBuilder.append(chunk);
                                try {
                                    emitter.send(Map.of("text", chunk));
                                } catch (Exception e) {
                                    emitter.complete();
                                }
                            }
                        }
                    },
                    error -> {
                        error.printStackTrace();
                        try {
                            emitter.send(Map.of("text", "\n[网络连接波动，请稍后再试]"));
                            emitter.complete();
                        } catch (Exception ignore) {
                            emitter.completeWithError(error);
                        }
                    },
                    () -> {
                        if (userId != null && !userId.isEmpty()) {
                            ChatMessage aiMsgObj = new ChatMessage();
                            aiMsgObj.setSenderId("0");
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