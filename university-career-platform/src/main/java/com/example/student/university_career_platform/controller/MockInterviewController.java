package com.example.student.university_career_platform.controller;

import com.example.student.university_career_platform.model.InterviewSession;
import com.example.student.university_career_platform.repository.InterviewSessionRepository;
import com.example.student.university_career_platform.service.LlmClientService;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/interview")
public class MockInterviewController {

    @Autowired
    private LlmClientService llmClientService;

    @Autowired
    private InterviewSessionRepository sessionRepository;

    private final ObjectMapper objectMapper = new ObjectMapper();

    private final String MODEL_ID = "qwen-max";

    /**
     * 1. 开始面试（生成第一题）
     */
    @PostMapping("/start")
    public ResponseEntity<?> startInterview(@RequestBody Map<String, Object> params) {
        String userId = (String) params.getOrDefault("userId", "default");
        String jobTitle = (String) params.getOrDefault("jobTitle", "Java开发工程师");

        // 初始化会话 (去掉了手动 setCreateTime，由实体类 @PrePersist 自动处理)
        InterviewSession session = new InterviewSession();
        session.setUserId(userId);
        session.setJobTitle(jobTitle);
        session.setCurrentRound(1);
        session.setStatus("ONGOING");
        session.setChatHistory("[]");

        // 让大模型生成第一题
        String systemPrompt = "你是一个专业的技术面试官。请根据候选人应聘的【" + jobTitle + "】岗位，提出第一个面试问题。要求：单刀直入，只问一个问题，不要有多余的寒暄。";
        String firstQuestion = llmClientService.sendRequest(MODEL_ID, systemPrompt, "请出题");

        // 保存历史
        ArrayNode historyArray = objectMapper.createArrayNode();
        ObjectNode turn = objectMapper.createObjectNode();
        turn.put("round", 1);
        turn.put("question", firstQuestion);
        historyArray.add(turn);

        session.setChatHistory(historyArray.toString());
        sessionRepository.save(session);

        Map<String, Object> response = new HashMap<>();
        response.put("sessionId", session.getId());
        response.put("question", firstQuestion);

        return ResponseEntity.ok(response);
    }

    /**
     * 2. 提交回答，获取下一题/追问
     */
    @PostMapping("/chat")
    public ResponseEntity<?> submitAnswer(@RequestBody Map<String, Object> params) {
        try {
            Long sessionId = Long.valueOf(params.get("sessionId").toString());
            String answer = (String) params.get("answer");
            String emotion = (String) params.getOrDefault("emotion", "平静");

            Optional<InterviewSession> optional = sessionRepository.findById(sessionId);
            if (optional.isEmpty()) return ResponseEntity.badRequest().body("Session not found");
            InterviewSession session = optional.get();

            ArrayNode historyArray = (ArrayNode) objectMapper.readTree(session.getChatHistory());

            String systemPrompt = """
                    你是严格的技术/行为面试官。结合候选人面部情绪【%s】、应聘岗位【%s】与历史问答，评估本轮回答。
                    只输出一段合法 JSON（不要 markdown 代码块、不要其它文字），结构严格为：
                    {"feedback":"对本轮回答的简短点评（50字内）","correctness":"较好|一般|不足","completenessScore":75,"nextQuestion":"下一道追问或新题（单段问句）"}
                    其中 completenessScore 必须为 0～100 的整数。
                    规则：completenessScore 依据是否切题、结构是否完整（如 STAR）、关键要点是否覆盖；correctness 用较好/一般/不足三档；nextQuestion 必须是一个清晰的问题。
                    """.formatted(emotion, session.getJobTitle());

            String userInput = "【历史对话】：\n" + historyArray.toString() + "\n\n【候选人本次回答】：\n" + answer;

            String raw = llmClientService.sendRequest(MODEL_ID, systemPrompt, userInput);
            Map<String, Object> eval = parseInterviewChatJson(raw);

            String nextQuestion = (String) eval.get("nextQuestion");

            ObjectNode currentTurn = (ObjectNode) historyArray.get(historyArray.size() - 1);
            currentTurn.put("answer", answer);
            currentTurn.put("emotion", emotion);
            currentTurn.put("feedback", String.valueOf(eval.get("feedback")));
            currentTurn.put("correctness", String.valueOf(eval.get("correctness")));
            Object csObj = eval.get("completenessScore");
            int csVal = csObj instanceof Number ? ((Number) csObj).intValue() : 0;
            currentTurn.put("completenessScore", csVal);

            ObjectNode newTurn = objectMapper.createObjectNode();
            newTurn.put("round", session.getCurrentRound() + 1);
            newTurn.put("question", nextQuestion);
            historyArray.add(newTurn);

            session.setCurrentRound(session.getCurrentRound() + 1);
            session.setChatHistory(historyArray.toString());

            // 实体类的 @PreUpdate 会自动更新 gmtModified
            sessionRepository.save(session);

            Map<String, Object> response = new HashMap<>();
            response.put("nextQuestion", nextQuestion);
            response.put("round", session.getCurrentRound());
            response.put("feedback", eval.get("feedback"));
            response.put("correctness", eval.get("correctness"));
            response.put("completenessScore", eval.get("completenessScore"));
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body("Error processing chat: " + e.getMessage());
        }
    }

    /**
     * 解析大模型返回的本轮 JSON：feedback / correctness / completenessScore / nextQuestion
     */
    private Map<String, Object> parseInterviewChatJson(String raw) {
        Map<String, Object> map = new HashMap<>();
        if (raw == null) {
            raw = "";
        }
        String cleaned = raw.replace("```json", "").replace("```", "").trim();
        int start = cleaned.indexOf('{');
        int end = cleaned.lastIndexOf('}');
        if (start >= 0 && end > start) {
            cleaned = cleaned.substring(start, end + 1);
        }
        try {
            JsonNode n = objectMapper.readTree(cleaned);
            map.put("feedback", jsonText(n, "feedback", "已记录你的回答。"));
            map.put("correctness", jsonText(n, "correctness", "一般"));
            int score = 60;
            if (n.has("completenessScore") && n.get("completenessScore").isNumber()) {
                score = n.get("completenessScore").asInt();
            }
            score = Math.max(0, Math.min(100, score));
            map.put("completenessScore", score);
            map.put("nextQuestion", jsonText(n, "nextQuestion", "请再具体说说你在项目中的职责与产出。"));
        } catch (Exception e) {
            map.put("feedback", "未能解析模型结构化输出，已将下文作为下一环节内容。");
            map.put("correctness", "未分级");
            map.put("completenessScore", 0);
            String fallback = raw.length() > 800 ? raw.substring(0, 800) + "…" : raw;
            map.put("nextQuestion", fallback.isEmpty() ? "请补充描述你的相关经历。" : fallback);
        }
        return map;
    }

    private static String jsonText(JsonNode n, String field, String def) {
        if (n != null && n.has(field) && !n.get(field).isNull()) {
            return n.get(field).asText();
        }
        return def;
    }

    /**
     * 3. 结束面试，输出 JSON 维度的评分报告
     */
    @PostMapping("/finish")
    public ResponseEntity<?> finishInterview(@RequestBody Map<String, Object> params) {
        try {
            Long sessionId = Long.valueOf(params.get("sessionId").toString());
            InterviewSession session = sessionRepository.findById(sessionId).get();

            String systemPrompt = "你是一个面试评价系统。请根据提供的面试历史记录，生成一份JSON格式的报告。绝不要输出其他文字，只输出合法的JSON格式！\n" +
                    "JSON结构必须为：{\"expressScore\": 80, \"logicScore\": 75, \"techScore\": 85, \"summary\": \"总体评价...\", \"suggestions\": [\"建议1\", \"建议2\"]}";

            String reportJson = llmClientService.sendRequest(MODEL_ID, systemPrompt, "【面试记录】：" + session.getChatHistory());

            reportJson = reportJson.replace("```json", "").replace("```", "").trim();

            session.setStatus("FINISHED");
            session.setFinalReport(reportJson);
            sessionRepository.save(session);

            JsonNode jsonNode = objectMapper.readTree(reportJson);
            return ResponseEntity.ok(jsonNode);

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body("Error finishing interview: " + e.getMessage());
        }
    }
}