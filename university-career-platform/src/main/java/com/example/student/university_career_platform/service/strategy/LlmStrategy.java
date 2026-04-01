package com.example.student.university_career_platform.service.strategy;

import com.alibaba.dashscope.aigc.generation.GenerationResult;
import io.reactivex.Flowable;

public interface LlmStrategy {
    // 统一为 3 个参数：模型ID、系统提示词、用户输入
    String sendRequest(String modelId, String systemPrompt, String userInput);
    Flowable<GenerationResult> streamChat(String modelId, String systemPrompt, String userInput);
}