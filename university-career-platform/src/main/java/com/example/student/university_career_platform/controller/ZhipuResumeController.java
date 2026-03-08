package com.example.student.university_career_platform.controller;

import com.zhipu.oapi.ClientV4;
import com.zhipu.oapi.Constants;
import com.zhipu.oapi.service.v4.model.ChatCompletionRequest;
import com.zhipu.oapi.service.v4.model.ChatMessage;
import com.zhipu.oapi.service.v4.model.ChatMessageRole;
import com.zhipu.oapi.service.v4.model.ModelApiResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/resume")
public class ZhipuResumeController {

    @Value("${ai.zhipu.api-key}")
    private String zhipuApiKey;

    @PostMapping("/zhipu/generate")
    public ResponseEntity<?> generateResume(@RequestBody Map<String, String> request) {
        String userRequirements = request.get("requirements");

        if (userRequirements == null || userRequirements.trim().isEmpty()) {
            return ResponseEntity.badRequest().body("请提供简历要求或个人经历");
        }

        try {
            // 1. 初始化智谱客户端
            ClientV4 client = new ClientV4.Builder(zhipuApiKey).build();

            // 2. 构建对话列表
            List<ChatMessage> messages = new ArrayList<>();

            // 【超级提示词：资深HR视角 + Markdown 排版指令】
            String systemPrompt = "你是一位拥有10年经验的互联网大厂资深HR和高级职业规划师。" +
                    "请根据用户提供的个人信息、技能和项目经历，为他们撰写一份具有极强求职竞争力的专属简历。" +
                    "【强制要求】：\n" +
                    "1. 必须且只能使用 Markdown 格式输出。排版要清晰、美观、有层次感（使用加粗、列表、分割线等）。\n" +
                    "2. 语言要精炼、专业。项目经历必须使用STAR法则（情境、任务、行动、结果）进行描述，多用数据化结果支撑，多用动宾短语（例如：负责...、优化了...、实现了...）。\n" +
                    "3. 如果用户提供的信息过于简略，请发挥你的专业能力，合理推演和润色相关细节（例如扩写技术细节），但不要过度夸大。";

            messages.add(new ChatMessage(ChatMessageRole.SYSTEM.value(), systemPrompt));
            messages.add(new ChatMessage(ChatMessageRole.USER.value(), userRequirements));

            // 3. 构建请求参数
            ChatCompletionRequest chatCompletionRequest = ChatCompletionRequest.builder()
                    .model(Constants.ModelChatGLM4) // 使用智谱 GLM-4 模型
                    .stream(false) // 简历生成不使用流式，一次性生成完整文本
                    .invokeMethod(Constants.invokeMethod)
                    .messages(messages)
                    .build();

            // 4. 发送请求并获取结果
            ModelApiResponse invokeModelApiResp = client.invokeModelApi(chatCompletionRequest);

            // 5. 解析并返回生成的文本
            String generatedMarkdown = invokeModelApiResp.getData().getChoices().get(0).getMessage().getContent().toString();

            return ResponseEntity.ok(generatedMarkdown);

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().body("简历生成失败，请检查 API Key 或网络设置。错误信息：" + e.getMessage());
        }
    }
}