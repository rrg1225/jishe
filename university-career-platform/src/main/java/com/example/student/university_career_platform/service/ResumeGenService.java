package com.example.student.university_career_platform.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
public class ResumeGenService {

    @Autowired
    private LlmClientService llmClientService;

    @Autowired
    private ObjectMapper objectMapper;

    // 注入你项目里的 WebSocketServer
    @Autowired
    private WebSocketServer webSocketServer;

    @Value("${ai.model.resume-draft}")
    private String modelR1;

    @Value("${ai.model.resume}")
    private String modelV3;

    @Async
    public void optimizeResumeAsync(Map<String, Object> rawData, String userId) {
        Map<String, Object> result = new HashMap<>(rawData);

        try {
            String fullName = (String) rawData.getOrDefault("fullName", "未提供姓名");
            String contactPhone = (String) rawData.getOrDefault("contactPhone", "未提供电话");
            String targetJob = (String) rawData.getOrDefault("targetJob", "未明确求职岗位");
            String education = (String) rawData.getOrDefault("educationInfo", "未提供");
            String project = (String) rawData.getOrDefault("projectExperience", "未提供");
            String skills = (String) rawData.getOrDefault("professionalSkills", "未提供");
            String templateName = (String) rawData.getOrDefault("templateName", "标准通用模板");

            String r1SystemPrompt = "你是一个顶级的资深HR和简历面试辅导专家。请根据求职者的基本信息、教育经历、项目经历和技能，为其量身定制和深度润色简历内容。请使用专业的HR术语，并采用STAR法则优化项目经历，切勿自行捏造虚假姓名或经历。";
            String r1UserInput = String.format(
                    """
                            【求职者基本信息】
                            姓名：%s
                            联系电话：%s
                            求职意向：%s
                            目标简历风格：%s
                            
                            【需要优化的原始内容】
                            原始教育经历：
                            %s
                            
                            原始项目经历：
                            %s
                            
                            原始专业技能：
                            %s
                            
                            请进行深度润色，提升专业度和吸引力。""",
                    fullName, contactPhone, targetJob, templateName, education, project, skills
            );
            String r1DraftText = llmClientService.sendRequest(modelR1, r1SystemPrompt, r1UserInput);

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

            String cleanJson = v3JsonOutput.replaceAll("```json", "").replaceAll("```", "").trim();
            Map<String, String> optimizedData = objectMapper.readValue(cleanJson, new TypeReference<Map<String, String>>() {});

            if(optimizedData.containsKey("fullName")) result.put("fullName", optimizedData.get("fullName"));
            if(optimizedData.containsKey("contactPhone")) result.put("contactPhone", optimizedData.get("contactPhone"));
            if(optimizedData.containsKey("email")) result.put("email", optimizedData.get("email"));
            if(optimizedData.containsKey("targetCity")) result.put("targetCity", optimizedData.get("targetCity"));
            if(optimizedData.containsKey("eduStatus")) result.put("eduStatus", optimizedData.get("eduStatus"));
            result.put("educationInfo", optimizedData.getOrDefault("educationInfo", education));
            result.put("projectExperience", optimizedData.getOrDefault("projectExperience", project));
            result.put("professionalSkills", optimizedData.getOrDefault("professionalSkills", skills));

// 🌟 5. 成功：构建 WebSocket 消息并推送给前端的全局通知通道
            Map<String, Object> wsMessage = new HashMap<>();
            wsMessage.put("type", "RESUME_READY");
            wsMessage.put("resumeData", result);

            String jsonMessage = objectMapper.writeValueAsString(wsMessage);

            // 【重点修复】：调用真实存在的方法，并拼接 _notify 防止和私信冲突
            webSocketServer.sendMessageToUser(userId + "_notify", jsonMessage);

        } catch (Exception e) {
            e.printStackTrace();
            // 🌟 6. 失败：推送失败通知给前端
            try {
                result.put("projectExperience", "生成异常：" + e.getMessage());
                Map<String, Object> errorMap = new HashMap<>();
                errorMap.put("type", "RESUME_FAILED");
                String errorMsg = objectMapper.writeValueAsString(errorMap);

                // 同理，推送给 _notify 通道
                webSocketServer.sendMessageToUser(userId + "_notify", errorMsg);
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
        }
}