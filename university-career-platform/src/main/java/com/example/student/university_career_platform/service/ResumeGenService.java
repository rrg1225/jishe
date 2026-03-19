package com.example.student.university_career_platform.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
public class ResumeGenService {

    @Autowired
    private LlmClientService llmClientService;

    // 🌟 在这里补上缺失的模型 ID 常量配置[cite: 4]
    private static final String MODEL_ID = "qwen2.5-7b-instruct_5fd34c80";

    public Map<String, Object> optimizeResume(Map<String, Object> rawData) {
        // 1. 获取用户填写的基础信息
        String education = (String) rawData.getOrDefault("educationInfo", "");
        String project = (String) rawData.getOrDefault("projectExperience", "");
        String templateName = (String) rawData.getOrDefault("templateName", "标准通用模板");

        // 2. 构建 System Prompt (系统角色设定) 和 User Input (用户数据)
        String systemPrompt = "你是一个资深的 HR 和简历优化专家。现在有一个大学生正在使用【" + templateName + "】求职。\n" +
                "请将以下草稿内容进行专业润色，要求使用 STAR 法则，突出量化数据，语言精炼专业。\n" +
                "请直接返回优化后的内容，不要说废话。";

        String userInput = "草稿内容：\n" +
                "教育经历：" + education + "\n" +
                "项目经历：" + project + "\n";

        // 3. 调用项目中真实存在的大模型 API[cite: 4]
        String aiOptimizedText = llmClientService.sendRequest(MODEL_ID, systemPrompt, userInput);

        // 4. 将 AI 优化的结果重新封装
        Map<String, Object> result = new HashMap<>(rawData);
        // 假设我们将 AI 整体优化的一大段话塞回 projectExperience 给前端展示
        result.put("projectExperience", aiOptimizedText);

        return result;
    }
}