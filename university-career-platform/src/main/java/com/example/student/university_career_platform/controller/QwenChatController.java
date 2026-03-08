package com.example.student.university_career_platform.controller;

import com.alibaba.dashscope.aigc.generation.Generation;
import com.alibaba.dashscope.aigc.generation.GenerationParam;
import com.alibaba.dashscope.aigc.generation.GenerationResult;
import com.alibaba.dashscope.common.Message;
import com.alibaba.dashscope.common.Role;
import com.alibaba.dashscope.common.ResultCallback;
import com.alibaba.dashscope.exception.InputRequiredException;
import com.alibaba.dashscope.exception.NoApiKeyException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@RestController
@RequestMapping("/api/chat")
@CrossOrigin(origins = "*") // 👈 新增：允许任何前端地址来调用这个接口
public class QwenChatController {

    @Value("${ai.qwen.api-key}")
    private String qwenApiKey;

    private final ExecutorService executor = Executors.newCachedThreadPool();

    @PostMapping("/qwen/stream")
    public SseEmitter chatStream(@RequestBody Map<String, List<Map<String, String>>> requestBody) {
        List<Map<String, String>> historyMessages = requestBody.get("messages");
        SseEmitter emitter = new SseEmitter(0L); // 永不超时

        executor.execute(() -> {
            try {
                // 1. 构造千问的消息列表
                List<Message> messages = new ArrayList<>();

                // 【核心：角色设定】
                String systemPrompt = "你是一个名为“知心学长”的伴生型AI导师，服务于大学生的学业与职业规划平台。" +
                        "你擅长情绪疏导、专业选择建议、考研/就业方向指导。" +
                        "【重要纪律】：你绝对不能直接为用户生成、修改或排版简历！如果用户要求你写简历，请礼貌地拒绝，并温柔地引导他们去点击平台页面上的【一键生成专属简历模板】按钮。";

                Message systemMsg = Message.builder().role(Role.SYSTEM.getValue()).content(systemPrompt).build();
                messages.add(systemMsg);

                // 2. 将前端传来的历史对话转换为千问的 Message 格式
                for (Map<String, String> msg : historyMessages) {
                    String role = msg.get("role").equals("user") ? Role.USER.getValue() : Role.ASSISTANT.getValue();
                    messages.add(Message.builder().role(role).content(msg.get("content")).build());
                }

                // 3. 配置调用参数 (开启 incrementalOutput 实现真正的逐字流式输出)
                GenerationParam param = GenerationParam.builder()
                        .apiKey(qwenApiKey)
                        .model("qwen-turbo") // 如果你想用更强的，可以换成 qwen-max
                        .messages(messages)
                        .resultFormat(GenerationParam.ResultFormat.MESSAGE)
                        .incrementalOutput(true) // 必须为 true，否则是一次性返回
                        .build();

                Generation gen = new Generation();

                // 4. 发起异步流式调用
                gen.streamCall(param, new ResultCallback<GenerationResult>() {
                    @Override
                    public void onEvent(GenerationResult message) {
                        try {
                            // 提取流式返回的文字片段
                            String contentChunk = message.getOutput().getChoices().get(0).getMessage().getContent();
                            if (contentChunk != null && !contentChunk.isEmpty()) {
                                // 推送给前端 Vue
                                emitter.send(SseEmitter.event().data(contentChunk));
                            }
                        } catch (Exception e) {
                            emitter.completeWithError(e);
                        }
                    }

                    @Override
                    public void onError(Exception e) {
                        try {
                            emitter.send(SseEmitter.event().data(" [抱歉，导师大脑暂时短路了，请稍后再试] "));
                            emitter.completeWithError(e);
                        } catch (Exception ex) {
                            emitter.completeWithError(ex);
                        }
                    }

                    @Override
                    public void onComplete() {
                        try {
                            // 发送结束标识符，通知前端关闭流
                            emitter.send(SseEmitter.event().data("[DONE]"));
                            emitter.complete();
                        } catch (Exception e) {
                            emitter.completeWithError(e);
                        }
                    }
                });

            } catch (NoApiKeyException | InputRequiredException e) {
                emitter.completeWithError(e);
            }
        });

        return emitter;
    }
}