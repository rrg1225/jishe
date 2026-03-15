package com.example.student.university_career_platform.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ResumeGenService {

    @Autowired
    private LlmClientService llmClientService;

    // 这里填你微调后的 qwen-turbo 模型 ID，没微调前可以先写 "qwen-turbo" 测试
    private static final String MODEL_ID = "qwen2.5-7b-instruct_5fd34c80";

    // 绝对无情的排版指令
    private static final String SYSTEM_PROMPT =
            "你是一个专业的简历排版生成器。请提取用户输入的杂乱信息，" +
                    "严格按照 Markdown 格式输出简历内容。标题格式统一为 `# 姓名's Resume`。" +
                    "绝对不允许输出任何问候语、解释说明或格式以外的字符。";

    public String generateAiResume(String rawUserInfo) {
        return llmClientService.sendRequest(MODEL_ID, SYSTEM_PROMPT, rawUserInfo);
    }
}