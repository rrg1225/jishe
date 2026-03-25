package com.example.student.university_career_platform.controller;

import com.example.student.university_career_platform.service.LlmClientService;
import com.example.student.university_career_platform.service.ResumeGenService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/resume")
public class AiResumeController {

    @Autowired
    private ResumeGenService resumeGenService;

    //注入 llmClientService 服务
    @Autowired
    private LlmClientService llmClientService;

    //注入 application.properties 中的模型配置
    @Value("${ai.model.resume-draft}")
    private String modelR1;

    /**
     * AI 一键生成/润色简历接口
     */
    @PostMapping("/ai-generate")
    public ResponseEntity<?> aiGenerateResume(@RequestBody Map<String, Object> rawResumeData) {
        try {
            // 调用 Service 层，将用户填写的草稿交给大模型处理
            Map<String, Object> optimizedData = resumeGenService.optimizeResume(rawResumeData);

            // 返回给前端，前端拿到后直接跳转到 ResumeFinalEdit.vue 并展示
            return ResponseEntity.ok(optimizedData);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().body("AI 生成失败：" + e.getMessage());
        }
    }

    // 前端划词润色、扩写、重新生成专用的纯文本接口
    @PostMapping("/text-polish")
    public ResponseEntity<?> polishText(@RequestBody Map<String, String> request) {
        try {
            String requirements = request.getOrDefault("requirements", "请优化以下文本");
            String originalText = request.getOrDefault("originalText", "");

            String systemPrompt = "你是一个资深的HR和文本优化专家。请严格按照用户的要求处理文本。不要包含 <think> 标签的内容，只输出最终的正式结果。";
            String userInput = "【用户要求】：\n" + requirements + "\n\n【原始文本】：\n" + originalText;

            // 划词润色不需要转 JSON，直接调用 R1 模型展现强大的文字功底
            String aiResult = llmClientService.sendRequest(modelR1, systemPrompt, userInput);

            // 过滤掉可能返回的 <think> 标签，保证前端弹窗里干净
            aiResult = aiResult.replaceAll("<think>[\\s\\S]*?</think>", "").trim();

            return ResponseEntity.ok(aiResult);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().body("服务异常：" + e.getMessage());
        }
    }
}