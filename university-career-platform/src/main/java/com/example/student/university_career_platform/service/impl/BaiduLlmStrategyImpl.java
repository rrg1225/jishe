package com.example.student.university_career_platform.service.impl;

import com.alibaba.dashscope.aigc.generation.GenerationResult;
import com.example.student.university_career_platform.service.strategy.LlmStrategy;
import io.reactivex.Flowable;
import org.springframework.stereotype.Service;

// 🌟 第二个策略实现
@Service("baiduLlmStrategy")
public class BaiduLlmStrategyImpl implements LlmStrategy {

    @Override
    public String sendRequest(String modelId, String systemPrompt, String userInput) {
        // 模拟调用百度文心一言 API 的耗时和返回
        return "【百度文心一言引擎 接管成功】\n由于当前未配置百度 API Key，这是策略模式路由返回的测试数据。\n您的输入是：" + userInput;
    }

    @Override
    public Flowable<GenerationResult> streamChat(String modelId, String systemPrompt, String userInput) {
        throw new UnsupportedOperationException("【百度引擎】暂未开启流式输出支持");
    }
}