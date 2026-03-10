package com.example.student.university_career_platform.service;

import com.zhipu.oapi.ClientV4;
import com.zhipu.oapi.Constants;
import com.zhipu.oapi.service.v4.model.ChatCompletionRequest;
import com.zhipu.oapi.service.v4.model.ChatMessage;
import com.zhipu.oapi.service.v4.model.ChatMessageRole;
import com.zhipu.oapi.service.v4.model.ModelApiResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class ZhipuResumeService {

    @Value("${ai.zhipu.api-key:}") // 加个冒号防止没配直接启动报错
    private String zhipuApiKey;

    public String generateResume(String userRequirements) throws Exception {
        System.out.println("========== 正在调用智谱大模型 ==========");
        System.out.println("当前读取到的 API Key 是: " + zhipuApiKey);

        if (zhipuApiKey == null || zhipuApiKey.trim().isEmpty()) {
            throw new Exception("后端未读取到智谱的 API Key，请检查 application 配置文件！");
        }

        try {
            ClientV4 client = new ClientV4.Builder(zhipuApiKey).build();
            List<ChatMessage> messages = new ArrayList<>();

            String systemPrompt = "你是一位资深HR，请根据用户提供的信息撰写专属简历，必须使用Markdown排版，使用STAR法则。";
            messages.add(new ChatMessage(ChatMessageRole.SYSTEM.value(), systemPrompt));
            messages.add(new ChatMessage(ChatMessageRole.USER.value(), userRequirements));

            ChatCompletionRequest chatCompletionRequest = ChatCompletionRequest.builder()
                    .model("glm-4") // 强制硬编码模型名称，防止 Constants 版本不匹配
                    .stream(false)
                    .invokeMethod(Constants.invokeMethod)
                    .messages(messages)
                    .build();

            // 发起请求
            ModelApiResponse response = client.invokeModelApi(chatCompletionRequest);

            System.out.println("智谱API返回的 HTTP 状态码: " + response.getCode());

            // 拦截智谱返回的非正常结果
            if (response.getCode() != 200) {
                System.err.println("智谱API调用失败：" + response.getMsg());
                throw new Exception("智谱接口返回错误: " + response.getMsg());
            }

            // 安全解析结果
            if (response.getData() != null && response.getData().getChoices() != null && !response.getData().getChoices().isEmpty()) {
                System.out.println("========== 简历生成成功！==========");
                return response.getData().getChoices().get(0).getMessage().getContent().toString();
            } else {
                throw new Exception("智谱接口返回的数据为空！可能是因为触发了安全审核机制。");
            }

        } catch (Exception e) {
            e.printStackTrace(); // 强制在控制台打印红色堆栈
            throw new Exception("Java后端执行出错: " + e.getMessage());
        }
    }
}