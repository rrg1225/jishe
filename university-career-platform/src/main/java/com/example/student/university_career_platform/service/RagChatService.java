package com.example.student.university_career_platform.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class RagChatService {

    @Autowired
    private LlmClientService llmClientService;

    @Autowired
    private LocalVectorDb localVectorDb;

    public String chatWithKnowledgeBase(String userQuery) {
        // 1. 去本地知识库检索最相关的 Top-2 文本片段 (直接传入用户的字符串提问)
        List<String> retrievedContexts = localVectorDb.searchSimilar(userQuery, 2);

        // 2. 拼装 RAG 专属的用户输入 Prompt
        StringBuilder promptBuilder = new StringBuilder();
        promptBuilder.append("请严格根据以下【参考内部政策】来回答用户的问题。如果参考政策中没有提到，请回答“我不清楚该政策”，绝不要瞎编。\n\n");
        promptBuilder.append("【参考内部政策】：\n");
        for (int i = 0; i < retrievedContexts.size(); i++) {
            promptBuilder.append(i + 1).append(". ").append(retrievedContexts.get(i)).append("\n");
        }
        promptBuilder.append("\n【学生提问】：").append(userQuery);

        // 3. 定义大模型的 System Prompt
        String systemPrompt = "你是一个专业的校园就业指导AI导师。";

        // 4. 🚀 完美适配你项目中真实的 sendRequest 方法，严格传入 (modelId, systemPrompt, userInput) 三个参数
        // 这里默认传 "qwen-max"，你也可以传你属性配置里的变量
        return llmClientService.sendRequest("qwen-max", systemPrompt, promptBuilder.toString());
    }
}