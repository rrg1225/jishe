package com.example.student.university_career_platform.service;

import com.alibaba.dashscope.aigc.generation.Generation;
import com.alibaba.dashscope.aigc.generation.GenerationParam;
import com.alibaba.dashscope.aigc.generation.GenerationResult;
import com.alibaba.dashscope.common.Message;
import com.alibaba.dashscope.common.Role;
import com.example.student.university_career_platform.model.ChatMessage;
import com.example.student.university_career_platform.repository.ChatMessageRepository;
import io.reactivex.Flowable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Arrays;

@Service
public class LlmClientService {

    @Value("${ai.aliyun.api-key}")
    private String apiKey;

    @Autowired
    private ChatMessageRepository chatMessageRepository;

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

    public String chatWithMingliMentor(String userId, String userMessage) {
        // 1. 保存用户的真实提问 (注意 ID 全部改为 String 类型)
        ChatMessage userMsg = new ChatMessage();
        userMsg.setSenderId(userId);
        userMsg.setReceiverId("0"); // "0" 代表明理导师
        userMsg.setContent(userMessage);
        // 你的 ChatMessage 实体类自带 @PrePersist 自动生成时间，无需手动 setGmtCreated
        chatMessageRepository.save(userMsg);

        // 2. 调用大模型获取 AI 回复
        // 这里借用你类里已经写好的参数，你可以根据实际需要调整 Prompt
        String modelId = "qwen2.5-7b-instruct-3eb6d099b67a";
        String systemPrompt = "你是明理导师。请友善地解答学生的学业和未来困惑。";
        String aiResponse = sendRequest(modelId, systemPrompt, userMessage);

        // 3. 保存 AI 导师的回答
        ChatMessage aiMsg = new ChatMessage();
        aiMsg.setSenderId("0"); // "0" 代表明理导师
        aiMsg.setReceiverId(userId);
        aiMsg.setContent(aiResponse);
        chatMessageRepository.save(aiMsg);

        return aiResponse;
    }

}