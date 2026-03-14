package com.example.student.university_career_platform.service;

import com.example.student.university_career_platform.model.ChatMessage;
import com.example.student.university_career_platform.repository.ChatMessageRepository;
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

    /**
     * 连接建立成功调用的方法
     */
    @OnOpen
    public void onOpen(Session session, @PathParam("userId") String userId) {
        sessionPool.put(userId, session);
        System.out.println("用户 [" + userId + "] 加入聊天！当前在线人数为: " + sessionPool.size());
    }

    /**
     * 连接关闭调用的方法
     */
    @OnClose
    public void onClose(@PathParam("userId") String userId) {
        sessionPool.remove(userId);
        System.out.println("用户 [" + userId + "] 断开连接！当前在线人数为: " + sessionPool.size());
    }

    /**
     * 收到客户端消息后调用的方法
     */
    @OnMessage
    public void onMessage(String message, @PathParam("userId") String userId) {
        System.out.println("收到来自用户 [" + userId + "] 的消息: " + message);

        try {
            // 1. 解析前端传来的企业级结构化 JSON
            JsonNode jsonNode = objectMapper.readTree(message);
            String messageType = jsonNode.has("messageType") ? jsonNode.get("messageType").asText() : "TEXT";
            String toUserId = jsonNode.get("toUserId").asText();
            String content = jsonNode.get("content").asText();

            String fileName = jsonNode.has("fileName") ? jsonNode.get("fileName").asText() : null;
            Long fileSize = jsonNode.has("fileSize") ? jsonNode.get("fileSize").asLong() : null;

            // 2. 存入 MySQL 数据库
            ChatMessage chatMessage = new ChatMessage();
            chatMessage.setSenderId(userId);
            chatMessage.setReceiverId(toUserId);
            chatMessage.setMessageType(messageType); // 记录类型
            chatMessage.setContent(content);
            chatMessage.setIsRead(0); // 默认未读
            chatMessage.setFileName(fileName);       //  记录文件名
            chatMessage.setFileSize(fileSize);       //  记录文件大小
            // 注意：因为用了 JPA 的 @PrePersist，这里不需要手动 set 时间！

            chatMessageRepository.save(chatMessage);
            System.out.println("消息已成功保存至数据库！");

            // 3. 转发给目标用户 (如果他在线)
            Session toSession = sessionPool.get(toUserId);
            if (toSession != null && toSession.isOpen()) {
                toSession.getBasicRemote().sendText(content);
                System.out.println("已将消息实时推送给目标用户 [" + toUserId + "]");
            } else {
                System.out.println("目标用户 [" + toUserId + "] 不在线，已作为离线消息存储。");
            }

        } catch (Exception e) {
            System.err.println("解析或发送消息失败: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * 发生错误时调用
     */
    @OnError
    public void onError(Session session, Throwable error) {
        System.err.println("WebSocket 发生错误！");
        error.printStackTrace();
    }
}
