package com.example.student.university_career_platform.service;

import com.example.student.university_career_platform.model.ChatMessage;
import com.example.student.university_career_platform.utils.DFAFilterUtil;
import com.example.student.university_career_platform.utils.JwtUtil;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import jakarta.websocket.*;
import jakarta.websocket.server.PathParam;
import jakarta.websocket.server.ServerEndpoint;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@ServerEndpoint("/chat/{userId}")
@Component
public class WebSocketServer {

    // 🌟 终极架构 1：支持多端同登！外层 Key 是 userId，内层 Key 是 SessionId
    private static final ConcurrentHashMap<String, ConcurrentHashMap<String, Session>> userSessions = new ConcurrentHashMap<>();
    private static final ObjectMapper objectMapper = new ObjectMapper();

    // ================= 🌟 静态资源注入区 =================
    private static JwtUtil jwtUtil;
    private static StringRedisTemplate redisTemplate;
    private static AsyncChatMessageService asyncChatMessageService;

    @Autowired
    public void setJwtUtil(JwtUtil jwtUtil) { WebSocketServer.jwtUtil = jwtUtil; }
    @Autowired
    public void setRedisTemplate(StringRedisTemplate redisTemplate) { WebSocketServer.redisTemplate = redisTemplate; }
    @Autowired
    public void setAsyncChatMessageService(AsyncChatMessageService asyncChatMessageService) { WebSocketServer.asyncChatMessageService = asyncChatMessageService; }
    // ======================================================

    @OnOpen
    public void onOpen(Session session, @PathParam("userId") String userId) {
        Map<String, List<String>> parameters = session.getRequestParameterMap();
        List<String> tokenParams = parameters.get("token");

        if (tokenParams == null || tokenParams.isEmpty()) { rejectConnection(session, "缺少 Token 凭证"); return; }
        if (!jwtUtil.validateToken(tokenParams.get(0))) { rejectConnection(session, "Token 无效或已过期"); return; }

        // 🌟 修复报错核心：将当前设备的 session 存入该用户的设备集合中（支持手机、电脑同时在线）
        userSessions.computeIfAbsent(userId, k -> new ConcurrentHashMap<>()).put(session.getId(), session);

        // 标记在线心跳
        redisTemplate.opsForValue().set("IM_ONLINE:" + userId, "1", 45, java.util.concurrent.TimeUnit.SECONDS);
        System.out.println("用户 [" + userId + "] 鉴权通过加入集群！当前总在线用户数: " + userSessions.size());
    }

    @OnClose
    public void onClose(Session session, @PathParam("userId") String userId) {
        ConcurrentHashMap<String, Session> sessions = userSessions.get(userId);
        if (sessions != null) {
            sessions.remove(session.getId()); // 🌟 只移除当前退出的这个设备
            if (sessions.isEmpty()) {
                userSessions.remove(userId); // 🌟 所有设备都断开了，才真正算离线
                redisTemplate.delete("IM_ONLINE:" + userId);
            }
        }
    }

    // 🌟 注意这里：增加 Session currentSession 参数，用于精准回复心跳
    @OnMessage
    public void onMessage(String message, Session currentSession, @PathParam("userId") String userId) {
        try {
            JsonNode jsonNode = objectMapper.readTree(message);

            // 1. 拦截心跳包 (防 NPE 炸弹)
            if (jsonNode.has("type") && "PING".equals(jsonNode.get("type").asText())) {
                if (currentSession.isOpen()) {
                    currentSession.getBasicRemote().sendText("{\"type\":\"PONG\"}");
                    redisTemplate.expire("IM_ONLINE:" + userId, 45, java.util.concurrent.TimeUnit.SECONDS);
                }
                return;
            }

            // 🚀 终极优化 2：拦截 QoS 客户端回执 (消息绝对必达)
            if (jsonNode.has("type") && "CLIENT_ACK".equals(jsonNode.get("type").asText())) {
                String msgId = jsonNode.get("msgId").asText();
                // 收到前端确认，从 Redis 待办队列中删掉这条消息
                redisTemplate.delete("QOS_PENDING:" + userId + ":" + msgId);
                System.out.println("✅ 收到 QoS 回执，消息 [" + msgId + "] 确定已送达到客户端！");
                return;
            }

            // 🚀 终极优化 3：拦截“对方正在输入...” (毫秒级信令，不入库)
            if (jsonNode.has("type") && "TYPING".equals(jsonNode.get("type").asText())) {
                String toUserId = jsonNode.get("toUserId").asText();
                ObjectNode typingMsg = objectMapper.createObjectNode();
                typingMsg.put("toUserId", toUserId);
                typingMsg.put("content", "{\"type\":\"TYPING\", \"fromUserId\":\"" + userId + "\"}");
                redisTemplate.convertAndSend("chat_channel", typingMsg.toString());
                return;
            }

            // --- 下面是正常的聊天消息处理 ---
            if (!jsonNode.has("toUserId") || !jsonNode.has("content")) return;

            String messageType = jsonNode.has("messageType") ? jsonNode.get("messageType").asText() : "TEXT";
            String toUserId = jsonNode.get("toUserId").asText();
            String content = DFAFilterUtil.replaceSensitiveWord(jsonNode.get("content").asText());

            ChatMessage chatMessage = new ChatMessage();
            chatMessage.setSenderId(userId);
            chatMessage.setReceiverId(toUserId);
            chatMessage.setMessageType(messageType);
            chatMessage.setContent(content);
            chatMessage.setIsRead(0);

            // 异步落盘
            asyncChatMessageService.saveMessageAsync(chatMessage);

            // 🚀 QoS 机制核心：生成唯一 MsgId，存入 Redis 待确认队列（10秒过期）
            // 真实场景下，如果10秒没收到 CLIENT_ACK，会有个定时任务拿出来重新发送
            String msgId = String.valueOf(System.currentTimeMillis());
            ObjectNode frontendMsg = objectMapper.createObjectNode();
            frontendMsg.put("type", "CHAT");
            frontendMsg.put("msgId", msgId);
            frontendMsg.put("messageType", messageType);
            frontendMsg.put("content", content);
            frontendMsg.put("fromUserId", userId);

            String finalMsgStr = frontendMsg.toString();
            redisTemplate.opsForValue().set("QOS_PENDING:" + toUserId + ":" + msgId, finalMsgStr, 10, java.util.concurrent.TimeUnit.SECONDS);

            // 集群广播给目标用户
            ObjectNode pubMsg = objectMapper.createObjectNode();
            pubMsg.put("toUserId", toUserId);
            pubMsg.put("content", finalMsgStr);
            redisTemplate.convertAndSend("chat_channel", pubMsg.toString());

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 集群广播拦截：由 Redis 触发，向本地的所有设备推送
     */
    public void onMessageReceiveFromRedis(String message) {
        try {
            JsonNode node = objectMapper.readTree(message);
            String toUserId = node.get("toUserId").asText();
            String content = node.get("content").asText();

            // 🌟 多端同登发送核心：查出该用户的所有在线设备，全部推送一遍！
            ConcurrentHashMap<String, Session> sessions = userSessions.get(toUserId);
            if (sessions != null && !sessions.isEmpty()) {
                for (Session s : sessions.values()) {
                    if (s.isOpen()) {
                        s.getBasicRemote().sendText(content);
                    }
                }
                System.out.println("⚡ 集群多端打击：已向本地机器上用户 [" + toUserId + "] 的所有设备推送了消息！");
            }
        } catch (Exception e) {
            System.err.println("处理 Redis 广播失败: " + e.getMessage());
        }
    }

    @OnError
    public void onError(Session session, Throwable error) {
        error.printStackTrace();
    }

    private void rejectConnection(Session session, String reason) {
        try {
            session.close(new CloseReason(CloseReason.CloseCodes.VIOLATED_POLICY, reason));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 系统级通知（如好友申请），同样走 Redis 集群广播架构！
     */
    public static void sendMessageToUser(String toUserId, String messageJSON) {
        try {
            if (redisTemplate != null) {
                ObjectNode pubMsg = objectMapper.createObjectNode();
                pubMsg.put("toUserId", toUserId);
                pubMsg.put("content", messageJSON);
                redisTemplate.convertAndSend("chat_channel", pubMsg.toString());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 全局广播（修复了之前的报错）
     */
    public void sendAllMessage(String message) {
        for (ConcurrentHashMap<String, Session> sessions : userSessions.values()) {
            for (Session session : sessions.values()) {
                if (session.isOpen()) {
                    try { session.getBasicRemote().sendText(message); } catch (Exception e) { e.printStackTrace(); }
                }
            }
        }
    }
}