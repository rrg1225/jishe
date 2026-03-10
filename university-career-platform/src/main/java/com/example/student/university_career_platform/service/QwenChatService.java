package com.example.student.university_career_platform.service;

import com.alibaba.dashscope.aigc.generation.Generation;
import com.alibaba.dashscope.aigc.generation.GenerationParam;
import com.alibaba.dashscope.aigc.generation.GenerationResult;
import com.alibaba.dashscope.common.Message;
import com.alibaba.dashscope.common.Role;
import com.alibaba.dashscope.common.ResultCallback;
import com.alibaba.dashscope.exception.InputRequiredException;
import com.alibaba.dashscope.exception.NoApiKeyException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Service // 👈 声明这是一个 Service 组件
public class QwenChatService {

    @Value("${ai.qwen.api-key}")
    private String qwenApiKey;

    private final ExecutorService executor = Executors.newCachedThreadPool();

    public void streamChat(List<Map<String, String>> historyMessages, SseEmitter emitter) {
        executor.execute(() -> {
            try {
                List<Message> messages = new ArrayList<>();

                // 【核心调参 1：注入“张晨果导师”的灵魂 + 你的平台专属纪律】
                String systemPrompt = "你是一位拥有20年高校教育与职业发展指导经验的资深导师，名叫“张晨果”。\n" +
                        "【你的核心沟通准则】\n" +
                        "1. 绝对禁止动作描写：不要描写动作、神态或场景。只输出纯粹的对话文本，像微信聊天一样自然。\n" +
                        "2. 拒绝八股文与说教：坚决摒弃死板的列表模板，不要机械地列出1、2、3点建议。\n" +
                        "3. 极致的情绪捕捉：永远先“接住”对方的情绪。准确点出隐藏的焦虑或不甘，用一两句话让对方感觉到“你懂我”。\n" +
                        "4. 启发式引导：多用反问句或真实简短案例，引导对方自己思考。\n" +
                        "5. 阅历的沉淀：语言简洁、击中要害。每次结束时抛出一个能引发深思的轻量级问题。\n" +
                        "【平台重要纪律】：你绝对不能直接为用户生成、修改或排版简历！如果用户要求写简历，请礼貌拒绝，并温柔地引导他们去点击平台页面上的【智能导入/一键生成专属简历模板】按钮。";

                messages.add(Message.builder().role(Role.SYSTEM.getValue()).content(systemPrompt).build());

                // 加载历史对话
                for (Map<String, String> msg : historyMessages) {
                    String role = msg.get("role").equals("user") ? Role.USER.getValue() : Role.ASSISTANT.getValue();
                    messages.add(Message.builder().role(role).content(msg.get("content")).build());
                }

                // 【核心调参 2：应用你的黄金参数】
                GenerationParam param = GenerationParam.builder()
                        .apiKey(qwenApiKey)
                        .model("qwen-plus") // 👈 建议换成 qwen-plus，比 turbo 聪明很多，更符合导师人设
                        .messages(messages)
                        .resultFormat(GenerationParam.ResultFormat.MESSAGE)
                        .temperature(0.7f)  // 👈 调参：0.7的温度，保证温暖又严谨
                        .topP(0.9)          // 👈 调参：词汇丰富度
                        .incrementalOutput(true) // 必须开启增量输出
                        .build();

                Generation gen = new Generation();

                // 发起异步流式调用
                gen.streamCall(param, new ResultCallback<GenerationResult>() {
                    @Override
                    public void onEvent(GenerationResult message) {
                        try {
                            String contentChunk = message.getOutput().getChoices().get(0).getMessage().getContent();
                            if (contentChunk != null && !contentChunk.isEmpty()) {
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
    }
}