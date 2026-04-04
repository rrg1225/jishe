package com.example.student.university_career_platform.service;

import com.example.student.university_career_platform.model.ChatMessage;
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

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@ServerEndpoint("/chat/{userId}")
@Component
public class WebSocketServer {

    private static final ConcurrentHashMap<String, ConcurrentHashMap<String, Session>> userSessions = new ConcurrentHashMap<>();
    private static final ObjectMapper objectMapper = new ObjectMapper();

    private static JwtUtil jwtUtil;
    private static StringRedisTemplate redisTemplate;
    private static KafkaTemplate<String, Object> kafkaTemplate;

    // 🌟 智能网关优化：当前服务器节点的对外暴露地址 (企业级应用中，这里应通过配置文件或代码动态获取本机公网IP和端口)
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

        if (tokenParams == null || tokenParams.isEmpty()) { rejectConnection(session, "缺少 Token 凭证"); return; }
        if (!jwtUtil.validateToken(tokenParams.get(0))) { rejectConnection(session, "Token 无效或已过期"); return; }

        userSessions.computeIfAbsent(userId, k -> new ConcurrentHashMap<>()).put(session.getId(), session);
        redisTemplate.opsForValue().set("IM_ONLINE:" + userId, "1", 45, java.util.concurrent.TimeUnit.SECONDS);

        // 🌟 网关优化核心：向 Redis 汇报当前节点的负载情况（当前节点连接数 +1）
        redisTemplate.opsForZSet().incrementScore("IM_CLUSTER_LOAD", CURRENT_NODE_ADDR, 1);

        System.out.println("用户 [" + userId + "] 鉴权通过加入集群！在线设备数: " + userSessions.get(userId).size());
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
        // 🌟 网关优化核心：连接断开，减轻负载（当前节点连接数 -1）
        redisTemplate.opsForZSet().incrementScore("IM_CLUSTER_LOAD", CURRENT_NODE_ADDR, -1);
    }

    @OnMessage
    public void onMessage(String message, Session currentSession, @PathParam("userId") String userId) {
        try {
            JsonNode jsonNode = objectMapper.readTree(message);
            // ... PING 和 TYPING 拦截保持不变 ...

            // 🌟 QoS 改造 1：精准拦截回执，从重传队列彻底抹除！
            if (jsonNode.has("type") && "CLIENT_ACK".equals(jsonNode.get("type").asText())) {
                String msgId = jsonNode.get("msgId").asText();
                // 收到确认，从 ZSET 队列和 HASH 详情中删除，宣告安全抵达！
                redisTemplate.opsForZSet().remove("IM_QOS_ZSET", msgId);
                redisTemplate.opsForHash().delete("IM_QOS_HASH", msgId);
                System.out.println("✅ [QoS] 收到消息 " + msgId + " 的已读/送达回执！");
                return;
            }

            if (!jsonNode.has("toUserId") || !jsonNode.has("content")) return;
            String messageType = jsonNode.has("messageType") ? jsonNode.get("messageType").asText() : "TEXT";
            String toUserId = jsonNode.get("toUserId").asText();
            String content = DFAFilterUtil.replaceSensitiveWord(jsonNode.get("content").asText());

            // ... Kafka 落盘和 ChatCache 缓存代码保持不变 ...

            // 🌟 QoS 改造 2：使用雪花算法生成终极 MsgId
            String msgId = String.valueOf(SnowflakeIdWorker.getInstance().nextId());

            ObjectNode frontendMsg = objectMapper.createObjectNode();
            frontendMsg.put("type", "CHAT");
            frontendMsg.put("msgId", msgId);
            frontendMsg.put("toUserId", toUserId); // 把接收人存进去，重传时要用
            frontendMsg.put("messageType", messageType);
            frontendMsg.put("content", content);
            frontendMsg.put("fromUserId", userId);

            String finalMsgStr = frontendMsg.toString();

            // 🌟 QoS 改造 3：将消息放入 ZSET 延迟队列 (Score为当前时间 + 8秒超时)
            long retryTime = System.currentTimeMillis() + 8000;
            redisTemplate.opsForZSet().add("IM_QOS_ZSET", msgId, retryTime);
            redisTemplate.opsForHash().put("IM_QOS_HASH", msgId, finalMsgStr); // 保存消息体

            // 第一次投递集群广播
            ObjectNode pubMsg = objectMapper.createObjectNode();
            pubMsg.put("toUserId", toUserId);
            pubMsg.put("content", finalMsgStr);
            redisTemplate.convertAndSend("chat_channel", pubMsg.toString());

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void onMessageReceiveFromRedis(String message) {
        try {
            JsonNode node = objectMapper.readTree(message);
            String toUserId = node.get("toUserId").asText();
            String content = node.get("content").asText();

            ConcurrentHashMap<String, Session> sessions = userSessions.get(toUserId);
            if (sessions != null && !sessions.isEmpty()) {
                for (Session s : sessions.values()) {
                    if (s.isOpen()) { s.getBasicRemote().sendText(content); }
                }
            }
        } catch (Exception e) {
            System.err.println("处理 Redis 广播失败: " + e.getMessage());
        }
    }

    @OnError
    public void onError(Session session, Throwable error) { error.printStackTrace(); }

    private void rejectConnection(Session session, String reason) {
        try { session.close(new CloseReason(CloseReason.CloseCodes.VIOLATED_POLICY, reason)); } catch (Exception e) { e.printStackTrace(); }
    }
}