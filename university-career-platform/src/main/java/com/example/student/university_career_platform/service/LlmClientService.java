package com.example.student.university_career_platform.service;

import com.alibaba.dashscope.aigc.generation.GenerationResult;
import com.example.student.university_career_platform.model.ChatMessage;
import com.example.student.university_career_platform.repository.ChatMessageRepository;
import com.example.student.university_career_platform.service.strategy.LlmStrategy;
import io.reactivex.Flowable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public class LlmClientService {

    @Autowired
    private Map<String, LlmStrategy> llmStrategyMap;

    @Value("${ai.engine.active:aliyunLlmStrategy}")
    private String activeEngine;

    @Value("${ai.mentor.model-id:qwen-max}")
    private String mentorModelId;

    @Value("${ai.mentor.system-prompt:你是一个专业的职业规划导师}")
    private String mentorSystemPrompt;

    @Autowired
    private ChatMessageRepository chatMessageRepository;

    private LlmStrategy getActiveStrategy() {
        LlmStrategy strategy = llmStrategyMap.get(activeEngine);
        if (strategy == null) {
            return llmStrategyMap.get("aliyunLlmStrategy"); // 找不到配置就强行走阿里
        }
        return strategy;
    }

    /**
     * 🚀 修复点：这里的调用必须传入 3 个参数，不能只传 1 个！
     */
    public Flowable<GenerationResult> streamChat(String modelId, String systemPrompt, String userInput) {
        // 原来您这里可能只写了 getActiveStrategy().streamChat(userInput)，导致报错
        return getActiveStrategy().streamChat(modelId, systemPrompt, userInput);
    }

    /**
     * 📦 同步接口：同样确保 3 参数完整
     */
    public String sendRequest(String modelId, String systemPrompt, String userInput) {
        return getActiveStrategy().sendRequest(modelId, systemPrompt, userInput);
    }

    /**
     * AI 导师对话逻辑
     */
    public String chatWithMingliMentor(String userId, String userMessage) {
        ChatMessage userMsg = new ChatMessage();
        userMsg.setSenderId(userId);
        userMsg.setReceiverId("0");
        userMsg.setContent(userMessage);
        chatMessageRepository.save(userMsg);

        // 调用时传入正确的 3 个参数
        String aiResponse = getActiveStrategy().sendRequest(mentorModelId, mentorSystemPrompt, userMessage);

        ChatMessage aiMsg = new ChatMessage();
        aiMsg.setSenderId("0");
        aiMsg.setReceiverId(userId);
        aiMsg.setContent(aiResponse);
        chatMessageRepository.save(aiMsg);

        return aiResponse;
    }
}