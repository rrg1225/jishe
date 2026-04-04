package com.example.student.university_career_platform.task;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.Set;

@Component
public class QosRetryTask {

    @Autowired
    private StringRedisTemplate redisTemplate;

    @Autowired
    private ObjectMapper objectMapper;

    /**
     * 🌟 每隔 3 秒扫描一次延迟队列，执行重传大业
     */
    @Scheduled(fixedRate = 3000)
    public void retryTimeoutMessages() {
        long now = System.currentTimeMillis();

        // 查出所有 Score (计划重传时间) 小于等于当前时间的消息 ID
        Set<String> timeoutMsgIds = redisTemplate.opsForZSet().rangeByScore("IM_QOS_ZSET", 0, now);

        if (timeoutMsgIds != null && !timeoutMsgIds.isEmpty()) {
            System.out.println("⚠️ [QoS 警报] 发现 " + timeoutMsgIds.size() + " 条消息超时未确认，开始执行重传补偿...");

            for (String msgId : timeoutMsgIds) {
                // 从 Hash 表中拿出具体的消息内容
                String msgJson = (String) redisTemplate.opsForHash().get("IM_QOS_HASH", msgId);
                if (msgJson != null) {
                    try {
                        JsonNode node = objectMapper.readTree(msgJson);
                        String toUserId = node.get("toUserId").asText();

                        // 给这条消息续命 8 秒，如果 8 秒后还没回执，下次还传它
                        redisTemplate.opsForZSet().add("IM_QOS_ZSET", msgId, now + 8000);

                        // 将消息重新丢进 Redis 广播，全网追杀推给前端
                        ObjectNode pubMsg = objectMapper.createObjectNode();
                        pubMsg.put("toUserId", toUserId);
                        pubMsg.put("content", msgJson);
                        redisTemplate.convertAndSend("chat_channel", pubMsg.toString());

                        System.out.println("🔄 消息 [" + msgId + "] 重传下发成功！");
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                } else {
                    // 如果只剩下 ID 没内容了（脏数据），直接清理掉
                    redisTemplate.opsForZSet().remove("IM_QOS_ZSET", msgId);
                }
            }
        }
    }
}