package com.example.student.university_career_platform.service;

import com.alibaba.dashscope.aigc.generation.Generation;
import com.alibaba.dashscope.aigc.generation.GenerationParam;
import com.alibaba.dashscope.aigc.generation.GenerationResult;
import com.alibaba.dashscope.common.Message;
import com.alibaba.dashscope.common.Role;
import io.reactivex.Flowable;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Arrays;

@Service
public class LlmClientService {

    @Value("${ai.aliyun.api-key}")
    private String apiKey;

    /**
     * 🚀 企业级真流式接口（专门给聊天用）
     * 返回 RxJava 的 Flowable 数据流，大模型每憋出一个字，流里就多一个字
     */
    public Flowable<GenerationResult> streamChat(String modelId, String systemPrompt, String userInput) {
        try {
            Generation gen = new Generation();
            Message systemMsg = Message.builder().role(Role.SYSTEM.getValue()).content(systemPrompt).build();
            Message userMsg = Message.builder().role(Role.USER.getValue()).content(userInput).build();

            GenerationParam param = GenerationParam.builder()
                    .apiKey(apiKey)
                    .model(modelId)
                    .messages(Arrays.asList(systemMsg, userMsg))
                    .resultFormat(GenerationParam.ResultFormat.MESSAGE)
                    // 🚨 核心灵魂：必须开启增量输出！这样每次传过来的才是一个字，而不是每次都把之前的拼接上
                    .incrementalOutput(true)
                    .build();

            // 发起流式长连接调用
            return gen.streamCall(param);
        } catch (Exception e) {
            throw new RuntimeException("大模型流式调用初始化失败", e);
        }
    }

    /**
     * 📦 保留原有的同步阻塞接口（专门给简历生成用，因为简历不需要打字机效果）
     */
    public String sendRequest(String modelId, String systemPrompt, String userInput) {
        try {
            Generation gen = new Generation();
            Message systemMsg = Message.builder().role(Role.SYSTEM.getValue()).content(systemPrompt).build();
            Message userMsg = Message.builder().role(Role.USER.getValue()).content(userInput).build();

            GenerationParam param = GenerationParam.builder()
                    .apiKey(apiKey)
                    .model(modelId)
                    .messages(Arrays.asList(systemMsg, userMsg))
                    .resultFormat(GenerationParam.ResultFormat.MESSAGE)
                    .build();

            // 同步等待完整结果返回
            GenerationResult result = gen.call(param);
            return result.getOutput().getChoices().get(0).getMessage().getContent();
        } catch (Exception e) {
            e.printStackTrace();
            return "服务暂时开小差了，请稍后再试。";
        }
    }
}