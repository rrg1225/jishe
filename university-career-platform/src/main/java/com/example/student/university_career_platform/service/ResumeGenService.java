package com.example.student.university_career_platform.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
public class ResumeGenService {

    @Autowired
    private LlmClientService llmClientService;

    // 引入 Spring Boot 自带的 JSON 解析器
    @Autowired
    private ObjectMapper objectMapper;

    @Value("${ai.model.resume-draft}")
    private String modelR1;

    @Value("${ai.model.resume}")
    private String modelV3;

    public Map<String, Object> optimizeResume(Map<String, Object> rawData) {
        String education = (String) rawData.getOrDefault("educationInfo", "");
        String project = (String) rawData.getOrDefault("projectExperience", "");
        String templateName = (String) rawData.getOrDefault("templateName", "标准通用模板");

        // 1. R1 负责深度润色
        String r1SystemPrompt = "你是一个顶级的资深HR和简历面试辅导专家..."; // (同上文)
        String r1UserInput = "原始教育经历：\n" + education + "\n\n原始项目经历：\n" + project;

        // 🌟 使用注入的 modelR1 变量
        String r1DraftText = llmClientService.sendRequest(modelR1, r1SystemPrompt, r1UserInput);

        // 2. V3 负责格式化提取
        String v3SystemPrompt = "你是一个无情的JSON数据格式化提取器..."; // (同上文)
        String v3UserInput = "HR专家的润色结果如下：\n" + r1DraftText;

        // 🌟 使用注入的 modelV3 变量
        String v3JsonOutput = llmClientService.sendRequest(modelV3, v3SystemPrompt, v3UserInput);

        // 3. 解析与合并 (同上文)
        Map<String, Object> result = new HashMap<>(rawData);
        try {
            String cleanJson = v3JsonOutput.replaceAll("```json", "").replaceAll("```", "").trim();
            Map<String, String> optimizedData = objectMapper.readValue(cleanJson, new TypeReference<Map<String, String>>() {});
            result.put("educationInfo", optimizedData.getOrDefault("optimizedEducation", (String)rawData.get("educationInfo")));
            result.put("projectExperience", optimizedData.getOrDefault("optimizedProject", (String)rawData.get("projectExperience")));
            result.put("professionalSkills", optimizedData.getOrDefault("optimizedSkills", (String)rawData.get("professionalSkills")));
        } catch (Exception e) {
            result.put("projectExperience", r1DraftText);
        }

        return result;
    }
}