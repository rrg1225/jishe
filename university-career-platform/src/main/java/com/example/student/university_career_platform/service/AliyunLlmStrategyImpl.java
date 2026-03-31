package com.example.student.university_career_platform.service;

import com.alibaba.dashscope.aigc.generation.Generation;
import com.alibaba.dashscope.aigc.generation.GenerationParam;
import com.alibaba.dashscope.aigc.generation.GenerationResult;
import com.alibaba.dashscope.common.Message;
import com.alibaba.dashscope.common.Role;
import io.reactivex.Flowable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import java.util.Collections;

@Service("aliyunLlmStrategy")
public class AliyunLlmStrategyImpl implements LlmStrategy {

    private static final Logger log = LoggerFactory.getLogger(AliyunLlmStrategyImpl.class);

    @Value("${ai.aliyun.api-key}")
    private String apiKey;

    @Override
    public String sendRequest(String modelId, String systemPrompt, String userInput) {
        try {
            Generation gen = new Generation();
            Message systemMsg = Message.builder().role(Role.SYSTEM.getValue()).content(systemPrompt).build();
            Message userMsg = Message.builder().role(Role.USER.getValue()).content(userInput).build();

            GenerationParam param = GenerationParam.builder()
                    .apiKey(apiKey)
                    .model(modelId)
                    .messages(java.util.Arrays.asList(systemMsg, userMsg))
                    .resultFormat(GenerationParam.ResultFormat.MESSAGE)
                    .build();

            GenerationResult result = gen.call(param);
            return result.getOutput().getChoices().get(0).getMessage().getContent();
        } catch (Exception e) {
            log.error("阿里云 LLM 调用失败: ", e);
            return "【引擎服务异常】AI 暂时无法响应，请稍后再试。";
        }
    }

    @Override
    public Flowable<GenerationResult> streamChat(String modelId, String systemPrompt, String userInput) {
        try {
            Generation gen = new Generation();
            Message systemMsg = Message.builder().role(Role.SYSTEM.getValue()).content(systemPrompt).build();
            Message userMsg = Message.builder().role(Role.USER.getValue()).content(userInput).build();

            GenerationParam param = GenerationParam.builder()
                    .apiKey(apiKey)
                    .model(modelId)
                    .messages(java.util.Arrays.asList(systemMsg, userMsg))
                    .resultFormat(GenerationParam.ResultFormat.MESSAGE)
                    .incrementalOutput(true)
                    .build();

            return gen.streamCall(param);
        } catch (Exception e) {
            log.error("阿里云流式调用初始化失败: ", e);
            throw new RuntimeException("流式接口不可用");
        }
    }
}