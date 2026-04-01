package com.example.student.university_career_platform.service;

import com.example.student.university_career_platform.model.ChatMessage;
import com.example.student.university_career_platform.repository.ChatMessageRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
public class ChatMessageKafkaConsumer {

    @Autowired
    private ChatMessageRepository chatMessageRepository;

    /**
     * 🌟 监听名为 "chat_msg_topic" 的队列
     * 只要 WebSocket 把消息扔进来，这里就会自动触发落库
     * 就算数据库临时卡死，Kafka 也会帮我们把消息兜底存着，绝不丢失！
     */
    @KafkaListener(topics = "chat_msg_topic", groupId = "im-persist-group")
    public void consumeChatMessage(ChatMessage chatMessage) {
        try {
            chatMessageRepository.save(chatMessage);
            System.out.println("✅ [Kafka 削峰落盘] 消息已安全保存至数据库，MsgId: " + chatMessage.getId());
        } catch (Exception e) {
            System.err.println("❌ [Kafka 消费失败] 数据库异常，消息体: " + chatMessage.getContent());
            // 企业级处理：这里出错的话，可以将消息扔到“死信队列 (Dead Letter Queue)”后续人工补偿
            e.printStackTrace();
        }
    }
}