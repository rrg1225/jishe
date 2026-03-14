package com.example.student.university_career_platform.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class LlmClientService {

    // 用 @Value 自动读取配置文件里的值
    @Value("${ai.aliyun.api-key}")
    private String apiKey;

    @Value("${ai.aliyun.api-url}")
    private String apiUrl;

    // Spring Boot 内置的 JSON 处理工具
    private final ObjectMapper objectMapper = new ObjectMapper();
    // 使用 Spring 原生的 RestTemplate 发送网络请求，告别版本冲突
    private final RestTemplate restTemplate = new RestTemplate();

    public String sendRequest(String modelId, String systemPrompt, String userInput) {
        try {
            // 1. 构建 JSON 请求体
            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("model", modelId);
            requestBody.put("messages", List.of(
                    Map.of("role", "system", "content", systemPrompt),
                    Map.of("role", "user", "content", userInput)
            ));
            requestBody.put("temperature", 0.7); // 修正了拼写错误

            String jsonString = objectMapper.writeValueAsString(requestBody);

            // 2. 设置 HTTP 请求头
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setBearerAuth(apiKey); // 自动拼接 "Bearer " 前缀

            // 3. 组装完整请求 (请求体 + 请求头)
            HttpEntity<String> request = new HttpEntity<>(jsonString, headers);

            // 4. 发送 POST 请求并接收响应
            ResponseEntity<String> response = restTemplate.postForEntity(apiUrl, request, String.class);

            // 5. 解析返回的 JSON 结果
            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                JsonNode rootNode = objectMapper.readTree(response.getBody());
                // 按照 API 格式，精准抠出 AI 回复的那句话
                return rootNode.path("choices").get(0).path("message").path("content").asText();
            } else {
                return "API 调用失败，状态码：" + response.getStatusCode() + "，错误信息：" + response.getBody();
            }

        } catch (Exception e) {
            e.printStackTrace();
            return "服务暂时开小差了，请稍后再试。";
        }
    }
}