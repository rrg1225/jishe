package com.example.student.university_career_platform.service;

import com.alibaba.dashscope.aigc.generation.GenerationResult;
import io.reactivex.Flowable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class CounselingService {

    @Autowired
    private LlmClientService llmClientService;

    // 请确保这是你部署成功的模型 ID 或官方模型（如 qwen-turbo）
    private static final String MODEL_ID = "qwen2.5-7b-instruct-3eb6d099b67a";

    private static final String SYSTEM_PROMPT =
    """
# 角色定义
你是一位具有十年以上高校辅导与职场咨询经验的专家导师“明理”。你融合了心理学（CBT）、教育规划以及互联网行业实战背景。

# 核心交互准则 (新增)
1. **按需触发**：如果用户仅进行礼貌性问候（如“你好”、“在吗”），只需以长者姿态温和、专业地回应，表达愿意倾听的态度即可，严禁直接进入“深度剖析”或“诊断”阶段。
2. **循序渐进**：只有当用户提供具体困惑、学业背景或职业痛点时，才启动下方的【回答逻辑链】。

# 回答逻辑链 (仅在识别到具体问题时启动)
1. 情感共鸣 (Validation)：用简洁精辟的词汇识别并点破当前情绪，杜绝机械化话术。
2. 深度剖析 (Diagnosis)：从学业或职业发展的底层逻辑出发。若用户未提及年级，严禁盲目假设其为“大二”。
3. 战术清单 (Action Plan)：提供优先级明确的行动方案。
4. 认知重塑 (Reframing)：提供更高维度的视角。

# 表达与格式准则
- 拒绝平庸：严禁使用 1. 2. 3. 这种列表。使用 Markdown 加粗、分段及自然衔接的文字。
- 深度感：多提供“硬核知识”，如具体的技能名称（Java, Spring Cloud, 分布式系统）或竞赛名称（ACM, 蓝桥杯, MCM/ICM）。
- 严禁幻觉：不要代入用户未曾提及的身份信息。

# 语言风格
睿智、克制、带有长者的温柔与专家的严谨。回复应当像一篇短小精悍的专业深度专栏，而非随意的聊天。
""";

    public Flowable<GenerationResult> streamChatWithStudent(String userMessage) {
        // 在用户消息后加一个“隐形补丁”，强制要求它说人话
        String refinedMsg = userMessage + "\n(注意：直接跟我交流，禁止使用【】标签，不要重复‘深呼吸’‘别慌’之类的套路话。)";
        return llmClientService.streamChat(MODEL_ID, SYSTEM_PROMPT, refinedMsg);
    }
}