package com.example.student.university_career_platform.service;

import com.example.student.university_career_platform.utils.DFAFilterUtil;
import com.example.student.university_career_platform.utils.JwtUtil;
import com.example.student.university_career_platform.utils.SnowflakeIdWorker;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import jakarta.websocket.*;
import jakarta.websocket.server.PathParam;
import jakarta.websocket.server.ServerEndpoint;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

@ServerEndpoint("/chat/{userId}")
@Component
public class WebSocketServer {

    // 存放当前节点在线用户的 Session：Map<UserId, Map<SessionId, Session>>
    private static final ConcurrentHashMap<String, ConcurrentHashMap<String, Session>> userSessions = new ConcurrentHashMap<>();
    private static final ObjectMapper objectMapper = new ObjectMapper();

    private static JwtUtil jwtUtil;
    private static StringRedisTemplate redisTemplate;
    private static KafkaTemplate<String, Object> kafkaTemplate;

    // 当前服务器节点的对外暴露地址
    private static final String CURRENT_NODE_ADDR = "120.27.205.204:8080";

    @Autowired
    public void setJwtUtil(JwtUtil jwtUtil) { WebSocketServer.jwtUtil = jwtUtil; }
    @Autowired
    public void setRedisTemplate(StringRedisTemplate redisTemplate) { WebSocketServer.redisTemplate = redisTemplate; }
    @Autowired
    public void setKafkaTemplate(KafkaTemplate<String, Object> kafkaTemplate) { WebSocketServer.kafkaTemplate = kafkaTemplate; }

    @OnOpen
    public void onOpen(Session session, @PathParam("userId") String userId) {
        Map<String, List<String>> parameters = session.getRequestParameterMap();
        List<String> tokenParams = parameters.get("token");

        if (tokenParams == null || tokenParams.isEmpty()) {
            rejectConnection(session, "缺少 Token 凭证");
            return;
        }
        if (!jwtUtil.validateToken(tokenParams.get(0))) {
            rejectConnection(session, "Token 无效或已过期");
            return;
        }

        userSessions.computeIfAbsent(userId, k -> new ConcurrentHashMap<>()).put(session.getId(), session);
        redisTemplate.opsForValue().set("IM_ONLINE:" + userId, "1", 45, TimeUnit.SECONDS);

        // 网关优化：增加当前节点负载计数
        redisTemplate.opsForZSet().incrementScore("IM_CLUSTER_LOAD", CURRENT_NODE_ADDR, 1);

        System.out.println("用户 [" + userId + "] 加入集群！当前节点在线总人数: " + userSessions.size());
    }

    @OnClose
    public void onClose(Session session, @PathParam("userId") String userId) {
        ConcurrentHashMap<String, Session> sessions = userSessions.get(userId);
        if (sessions != null) {
            sessions.remove(session.getId());
            if (sessions.isEmpty()) {
                userSessions.remove(userId);
                redisTemplate.delete("IM_ONLINE:" + userId);
            }
        }
        // 网关优化：减少负载计数
        redisTemplate.opsForZSet().incrementScore("IM_CLUSTER_LOAD", CURRENT_NODE_ADDR, -1);
    }

    @OnMessage
    public void onMessage(String message, Session currentSession, @PathParam("userId") String userId) {
        try {
            JsonNode jsonNode = objectMapper.readTree(message);

            // QoS 确认拦截：客户端收到消息后回传 ACK
            if (jsonNode.has("type") && "CLIENT_ACK".equals(jsonNode.get("type").asText())) {
                String msgId = jsonNode.get("msgId").asText();
                redisTemplate.opsForZSet().remove("IM_QOS_ZSET", msgId);
                redisTemplate.opsForHash().delete("IM_QOS_HASH", msgId);
                System.out.println("✅ [QoS] 收到消息 " + msgId + " 的回执，停止重传");
                return;
            }

            if (!jsonNode.has("toUserId") || !jsonNode.has("content")) return;

            String toUserId = jsonNode.get("toUserId").asText();
            String content = DFAFilterUtil.replaceSensitiveWord(jsonNode.get("content").asText());
            String messageType = jsonNode.has("messageType") ? jsonNode.get("messageType").asText() : "TEXT";

            // 使用雪花算法生成唯一消息ID
            String msgId = String.valueOf(SnowflakeIdWorker.getInstance().nextId());

            ObjectNode frontendMsg = objectMapper.createObjectNode();
            frontendMsg.put("type", "CHAT");
            frontendMsg.put("msgId", msgId);
            frontendMsg.put("toUserId", toUserId);
            frontendMsg.put("messageType", messageType);
            frontendMsg.put("content", content);
            frontendMsg.put("fromUserId", userId);

            String finalMsgStr = frontendMsg.toString();

            // QoS 机制：存入 Redis 延迟队列，若8秒内没收到 ACK 则重传
            long retryTime = System.currentTimeMillis() + 8000;
            redisTemplate.opsForZSet().add("IM_QOS_ZSET", msgId, retryTime);
            redisTemplate.opsForHash().put("IM_QOS_HASH", msgId, finalMsgStr);

            // 通过 Redis 发布消息，触发集群内所有节点（包括自己）尝试投递
            ObjectNode pubMsg = objectMapper.createObjectNode();
            pubMsg.put("toUserId", toUserId);
            pubMsg.put("content", finalMsgStr);
            redisTemplate.convertAndSend("chat_channel", pubMsg.toString());

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 🌟 关键补充：解决“找不到符号 sendAllMessage”
     * 用于向当前服务器节点连接的所有客户端广播消息
     */
    public void sendAllMessage(String message) {
        userSessions.forEach((userId, sessions) -> {
            sessions.values().forEach(session -> {
                if (session.isOpen()) {
                    try {
                        session.getBasicRemote().sendText(message);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            });
        });
    }

    /**
     * 🌟 逻辑优化：接收 Redis 频道消息后的处理逻辑
     */
    public void onMessageReceiveFromRedis(String message) {
        try {
            JsonNode node = objectMapper.readTree(message);
            String toUserId = node.get("toUserId").asText();
            String content = node.get("content").asText();

            // 尝试在本地节点寻找接收者并投递
            sendToUser(toUserId, content);
        } catch (Exception e) {
            System.err.println("处理 Redis 广播失败: " + e.getMessage());
        }
    }

    /**
     * 🌟 辅助方法：发送消息给指定用户的所有在线设备
     */
    private void sendToUser(String toUserId, String content) {
        ConcurrentHashMap<String, Session> sessions = userSessions.get(toUserId);
        if (sessions != null && !sessions.isEmpty()) {
            for (Session s : sessions.values()) {
                if (s.isOpen()) {
                    try {
                        s.getBasicRemote().sendText(content);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
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

    public static void sendMessageToUser(String toUserId, String message) {
        // 逻辑：直接通过 Redis 广播，这样无论用户在哪个集群节点都能收到
        try {
            ObjectMapper mapper = new ObjectMapper();
            ObjectNode pubMsg = mapper.createObjectNode();
            pubMsg.put("toUserId", toUserId);
            pubMsg.put("content", message);

            // 发送到 Redis 频道，由所有节点的 onMessageReceiveFromRedis 消费
            redisTemplate.convertAndSend("chat_channel", pubMsg.toString());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}