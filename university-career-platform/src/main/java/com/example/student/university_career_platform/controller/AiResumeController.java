package com.example.student.university_career_platform.controller;

import com.example.student.university_career_platform.service.LlmClientService;
import com.example.student.university_career_platform.service.ResumeGenService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/resume")
public class AiResumeController {

    @Autowired
    private ResumeGenService resumeGenService;

    @Autowired
    private LlmClientService llmClientService;

    @Value("${ai.model.resume-draft}")
    private String modelR1;

    /**
     * AI 一键生成/润色简历接口 (极简版)
     */
    @PostMapping("/ai-generate")
    public ResponseEntity<?> aiGenerateResume(@RequestBody Map<String, Object> rawResumeData) {
        // 1. 获取参数
        String userId = (String) rawResumeData.getOrDefault("userId", "default");

        // 2. 异步调用 (报错会被 AOP 全局接管)
        resumeGenService.optimizeResumeAsync(rawResumeData, userId);

        // 3. 永远只负责返回成功
        Map<String, Object> responseMap = new HashMap<>();
        responseMap.put("code", 200);
        responseMap.put("message", "任务已提交至AI队列，请留意系统通知！");
        return ResponseEntity.ok(responseMap);
    }

    /**
     * 纯文本划词润色接口 (清除了冗余的 try-catch)
     */
    @PostMapping("/text-polish")
    public ResponseEntity<?> polishText(@RequestBody Map<String, String> request) {
        String requirements = request.getOrDefault("requirements", "请优化以下文本");
        String originalText = request.getOrDefault("originalText", "");

        String systemPrompt = "你是一个资深的HR和文本优化专家。请严格按照用户的要求处理文本。不要包含 <think> 标签的内容，只输出最终的正式结果。";
        String userInput = "【用户要求】：\n" + requirements + "\n\n【原始文本】：\n" + originalText;

        // 如果调用大模型超时或失败，GlobalExceptionHandler 会自动拦截并返回友好的 JSON 给前端
        String aiResult = llmClientService.sendRequest(modelR1, systemPrompt, userInput);
        aiResult = aiResult.replaceAll("<think>[\\s\\S]*?</think>", "").trim();

        return ResponseEntity.ok(aiResult);
    }
}