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

    @Autowired
    private ObjectMapper objectMapper;

    @Value("${ai.model.resume-draft}")
    private String modelR1;

    @Value("${ai.model.resume}")
    private String modelV3;

    public Map<String, Object> optimizeResume(Map<String, Object> rawData) {
        // 1. 提取基础数据
        String fullName = (String) rawData.getOrDefault("fullName", "未提供姓名");
        String contactPhone = (String) rawData.getOrDefault("contactPhone", "未提供电话");
        String targetJob = (String) rawData.getOrDefault("targetJob", "未明确求职岗位");
        String education = (String) rawData.getOrDefault("educationInfo", "未提供");
        String project = (String) rawData.getOrDefault("projectExperience", "未提供");
        String skills = (String) rawData.getOrDefault("professionalSkills", "未提供");
        String templateName = (String) rawData.getOrDefault("templateName", "标准通用模板");

        // 2. R1 深度润色
        String r1SystemPrompt = "你是一个顶级的资深HR和简历面试辅导专家。请根据求职者的基本信息、教育经历、项目经历和技能，为其量身定制和深度润色简历内容。请使用专业的HR术语，并采用STAR法则优化项目经历，切勿自行捏造虚假姓名或经历。";
        String r1UserInput = String.format(
                "【求职者基本信息】\n姓名：%s\n联系电话：%s\n求职意向：%s\n目标简历风格：%s\n\n" +
                        "【需要优化的原始内容】\n原始教育经历：\n%s\n\n原始项目经历：\n%s\n\n原始专业技能：\n%s\n\n" +
                        "请进行深度润色，提升专业度和吸引力。",
                fullName, contactPhone, targetJob, templateName, education, project, skills
        );
        String r1DraftText = llmClientService.sendRequest(modelR1, r1SystemPrompt, r1UserInput);

        // 3. V3 格式化提取更多字段
        String v3SystemPrompt = "你是一个无情的JSON数据格式化提取器。请将文本中提到的个人信息和润色后的经历，严格按照要求的JSON格式提取出来。不要输出任何废话或 Markdown 标签。";
        String v3UserInput = "HR专家的润色结果如下：\n" + r1DraftText +
                "\n\n请提取并严格返回如下 JSON 格式：\n" +
                "{\n" +
                "  \"fullName\": \"姓名\",\n" +
                "  \"contactPhone\": \"手机号\",\n" +
                "  \"email\": \"邮箱\",\n" +
                "  \"targetCity\": \"意向城市\",\n" +
                "  \"eduStatus\": \"学历届数(如2028级)\",\n" +
                "  \"educationInfo\": \"润色后的教育经历文本\",\n" +
                "  \"projectExperience\": \"润色后的项目经历文本\",\n" +
                "  \"professionalSkills\": \"润色后的专业技能文本\"\n" +
                "}";

        String v3JsonOutput = llmClientService.sendRequest(modelV3, v3SystemPrompt, v3UserInput);

        // 4. 解析与合并
        Map<String, Object> result = new HashMap<>(rawData);
        try {
            String cleanJson = v3JsonOutput.replaceAll("```json", "").replaceAll("```", "").trim();
            Map<String, String> optimizedData = objectMapper.readValue(cleanJson, new TypeReference<Map<String, String>>() {});

            // 覆盖基础字段
            if(optimizedData.containsKey("fullName")) result.put("fullName", optimizedData.get("fullName"));
            if(optimizedData.containsKey("contactPhone")) result.put("contactPhone", optimizedData.get("contactPhone"));
            if(optimizedData.containsKey("email")) result.put("email", optimizedData.get("email"));
            if(optimizedData.containsKey("targetCity")) result.put("targetCity", optimizedData.get("targetCity"));
            if(optimizedData.containsKey("eduStatus")) result.put("eduStatus", optimizedData.get("eduStatus"));

            // 覆盖核心内容
            result.put("educationInfo", optimizedData.getOrDefault("educationInfo", education));
            result.put("projectExperience", optimizedData.getOrDefault("projectExperience", project));
            result.put("professionalSkills", optimizedData.getOrDefault("professionalSkills", skills));
        } catch (Exception e) {
            result.put("projectExperience", r1DraftText);
        }
        return result;
    }
}