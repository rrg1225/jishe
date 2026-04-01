package com.example.student.university_career_platform.config;

import com.example.student.university_career_platform.service.WebSocketServer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.listener.PatternTopic;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;
import org.springframework.data.redis.listener.adapter.MessageListenerAdapter;

@Configuration
public class RedisChatPubSubConfig {

    @Bean
    public RedisMessageListenerContainer redisMessageListenerContainer(RedisConnectionFactory connectionFactory,
                                                                       MessageListenerAdapter listenerAdapter) {
        RedisMessageListenerContainer container = new RedisMessageListenerContainer();
        container.setConnectionFactory(connectionFactory);
        // 🌟 核心：订阅名为 "chat_channel" 的全网广播频道
        container.addMessageListener(listenerAdapter, new PatternTopic("chat_channel"));
        return container;
    }

    @Bean
    public MessageListenerAdapter listenerAdapter(WebSocketServer webSocketServer) {
        // 当 Redis 收到广播时，交给 WebSocketServer 的 onMessageReceiveFromRedis 方法去推给前端
        return new MessageListenerAdapter(webSocketServer, "onMessageReceiveFromRedis");
    }
}
