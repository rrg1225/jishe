package com.example.student.university_career_platform.service;

import com.example.student.university_career_platform.model.ChatMessage;
import com.example.student.university_career_platform.repository.ChatMessageRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AsyncChatMessageService {

    @Autowired
    private ChatMessageRepository chatMessageRepository;

    // 🌟 复用您 AsyncConfig 里配好的 "aiTaskExecutor" 线程池
    @Async("aiTaskExecutor")
    @Transactional
    public void saveMessageAsync(ChatMessage chatMessage) {
        chatMessageRepository.save(chatMessage);
        System.out.println("✅ [异步落盘] 消息已保存至数据库，完美避开主线程阻塞！");
    }
}