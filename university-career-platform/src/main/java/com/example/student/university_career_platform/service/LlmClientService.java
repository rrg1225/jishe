package com.example.student.university_career_platform.service;

import com.alibaba.dashscope.aigc.generation.GenerationResult;
import com.example.student.university_career_platform.model.ChatMessage;
import com.example.student.university_career_platform.repository.ChatMessageRepository;
import com.example.student.university_career_platform.service.strategy.LlmStrategy;
import io.reactivex.Flowable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.List;
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

    // 🌟 注入本地知识库（向量检索）
    @Autowired
    private LocalVectorDb localVectorDb;

    private LlmStrategy getActiveStrategy() {
        LlmStrategy strategy = llmStrategyMap.get(activeEngine);
        if (strategy == null) {
            return llmStrategyMap.get("aliyunLlmStrategy");
        }
        return strategy;
    }

    /**
     * 流式聊天；检索与完整用户载荷可分离（如 Counseling 用原文检索、把动态指令一并交给模型）。
     */
    public Flowable<GenerationResult> streamChat(String modelId, String systemPrompt, String userInput) {
        return streamChat(modelId, systemPrompt, userInput, userInput);
    }

    public Flowable<GenerationResult> streamChat(String modelId, String systemPrompt, String ragQuery, String fullUserPayload) {
        String finalInput = fullUserPayload;
        if (shouldAugmentWithKnowledge(modelId, systemPrompt)) {
            finalInput = injectKnowledge(ragQuery, fullUserPayload);
        }
        return getActiveStrategy().streamChat(modelId, systemPrompt, finalInput);
    }

    public String sendRequest(String modelId, String systemPrompt, String userInput) {
        return sendRequest(modelId, systemPrompt, userInput, userInput);
    }

    public String sendRequest(String modelId, String systemPrompt, String ragQuery, String fullUserPayload) {
        String finalInput = fullUserPayload;
        if (shouldAugmentWithKnowledge(modelId, systemPrompt)) {
            finalInput = injectKnowledge(ragQuery, fullUserPayload);
        }
        return getActiveStrategy().sendRequest(modelId, systemPrompt, finalInput);
    }

    private boolean shouldAugmentWithKnowledge(String modelId, String systemPrompt) {
        return mentorModelId.equals(modelId)
                || systemPrompt.contains("职业规划导师")
                || mentorSystemPrompt.equals(systemPrompt)
                || systemPrompt.contains("明理");
    }

    /**
     * 明理导师专属对话入口，落库处理
     */
    public String chatWithMingliMentor(String userId, String userMessage) {
        ChatMessage userMsg = new ChatMessage();
        userMsg.setSenderId(userId);
        userMsg.setReceiverId("0");
        userMsg.setContent(userMessage);
        chatMessageRepository.save(userMsg);

        // 调用时传入导师特有的参数，内部会被 sendRequest 的拦截器增强
        String aiResponse = sendRequest(mentorModelId, mentorSystemPrompt, userMessage);

        ChatMessage aiMsg = new ChatMessage();
        aiMsg.setSenderId("0");
        aiMsg.setReceiverId(userId);
        aiMsg.setContent(aiResponse);
        chatMessageRepository.save(aiMsg);

        return aiResponse;
    }

    /**
     * 用 rawQuery 做向量检索；fullUserPayload 为实际交给模型的用户侧全文（可含系统动态指令）。
     */
    private String injectKnowledge(String rawQueryForRetrieval, String fullUserPayload) {
        List<String> context = localVectorDb.searchSimilar(rawQueryForRetrieval, 3);
        if (context.isEmpty()) {
            return fullUserPayload;
        }

        StringBuilder sb = new StringBuilder();
        sb.append("【知识库参考（事实与政策类；与当前处境无关时可忽略）】\n");
        for (String s : context) {
            sb.append("- ").append(s).append("\n");
        }
        sb.append("\n【作答要求】\n");
        sb.append("1）延续系统人设：先体察情绪、厘清问题，再给出建议；不要为了贴知识库而打断倾听与追问。\n");
        sb.append("2）当参考内容与问题高度相关时，可引用其中的政策、流程与数据，且勿编造参考中不存在的条文。\n");
        sb.append("3）当参考内容不相关时，完全依你的专业经验作答，不必强行引用。\n");
        sb.append("\n【学生侧完整输入（含系统动态指令）】\n").append(fullUserPayload);
        return sb.toString();
    }
}