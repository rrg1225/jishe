package com.example.student.university_career_platform.service;

import com.example.student.university_career_platform.model.ChatMessage;
import com.example.student.university_career_platform.repository.ChatMessageRepository;
import com.example.student.university_career_platform.utils.DFAFilterUtil; // 🌟 引入新写的算法
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.websocket.*;
import jakarta.websocket.server.PathParam;
import jakarta.websocket.server.ServerEndpoint;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.concurrent.ConcurrentHashMap;

@ServerEndpoint("/chat/{userId}")
@Component
public class WebSocketServer {

    // 记录当前在线连接数
    private static final ConcurrentHashMap<String, Session> sessionPool = new ConcurrentHashMap<>();

    // 解决 WebSocket 不能直接注入 Spring Bean 的问题
    private static ChatMessageRepository chatMessageRepository;

    // 使用 Jackson 解析前端传来的 JSON 字符串
    private static final ObjectMapper objectMapper = new ObjectMapper();

    @Autowired
    public void setChatMessageRepository(ChatMessageRepository chatMessageRepository) {
        WebSocketServer.chatMessageRepository = chatMessageRepository;
    }

    @OnOpen
    public void onOpen(Session session, @PathParam("userId") String userId) {
        sessionPool.put(userId, session);
        System.out.println("用户 [" + userId + "] 加入聊天！当前在线人数为: " + sessionPool.size());
    }

    @OnClose
    public void onClose(@PathParam("userId") String userId) {
        sessionPool.remove(userId);
        System.out.println("用户 [" + userId + "] 断开连接！当前在线人数为: " + sessionPool.size());
    }

    @OnMessage
    public void onMessage(String message, @PathParam("userId") String userId) {
        System.out.println("收到来自用户 [" + userId + "] 的消息: " + message);

        try {
            // 1. 解析前端传来的结构化 JSON
            JsonNode jsonNode = objectMapper.readTree(message);
            String messageType = jsonNode.has("messageType") ? jsonNode.get("messageType").asText() : "TEXT";
            String toUserId = jsonNode.get("toUserId").asText();
            String content = jsonNode.get("content").asText();

            // ==========================================
            // 🌟 杀手锏接入：DFA 毫秒级敏感词过滤！
            // 无论用户发送什么长篇大论，只要包含违法词，瞬间被替换为星号 ***
            // ==========================================
            content = DFAFilterUtil.replaceSensitiveWord(content);

            // 2. 存入 MySQL 数据库 (删除了原有的 fileName 和 fileSize，完美适配你的实体类)
            ChatMessage chatMessage = new ChatMessage();
            chatMessage.setSenderId(userId);
            chatMessage.setReceiverId(toUserId);
            chatMessage.setMessageType(messageType);
            chatMessage.setContent(content); // 🚨 注意：存入数据库的已经是清洗过的内容，保障平台安全
            chatMessage.setIsRead(0);

            chatMessageRepository.save(chatMessage);
            System.out.println("消息已清洗并成功保存至数据库！");

            // 3. 转发给目标用户 (如果他在线)
            Session toSession = sessionPool.get(toUserId);
            if (toSession != null && toSession.isOpen()) {
                toSession.getBasicRemote().sendText(content); // 推送的也是安全内容
                System.out.println("已将消息实时推送给目标用户 [" + toUserId + "]");
            } else {
                System.out.println("目标用户 [" + toUserId + "] 不在线，已作为离线消息存储。");
            }

        } catch (Exception e) {
            System.err.println("解析或发送消息失败: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @OnError
    public void onError(Session session, Throwable error) {
        System.err.println("WebSocket 发生错误！");
        error.printStackTrace();
    }

    public void sendAllMessage(String message) {
        for (Session session : sessionPool.values()) {
            try {
                if (session != null && session.isOpen()) {
                    session.getBasicRemote().sendText(message);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 🌟 保留原有优化：后端主动向指定用户推送消息（专供 AI 简历生成等后台任务调用）
     */
    public void sendMessageToUser(String userId, String message) {
        Session session = sessionPool.get(userId);
        if (session != null && session.isOpen()) {
            try {
                session.getBasicRemote().sendText(message);
                System.out.println("后台已成功向用户 [" + userId + "] 推送系统通知！");
            } catch (Exception e) {
                System.err.println("推送系统通知失败：" + e.getMessage());
                e.printStackTrace();
            }
        } else {
            System.out.println("推送通知失败：用户 [" + userId + "] 当前未连接全局通知。");
        }
    }
}