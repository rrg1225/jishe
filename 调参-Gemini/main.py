from openai import OpenAI

# 1. 初始化客户端 (这里配置的是阿里云通义千问的专属通道)
client = OpenAI(
    api_key="sk-cc55499fcc37450fb0e9521ee7f61d4c",  # <--- 注意保留两边的双引号
    base_url="https://dashscope.aliyuncs.com/compatible-mode/v1"  # <--- 千问的大门地址
)

# 2. 注入灵魂 (System Instruction 升级版：去动作化、反模板、强情绪共情)
system_instruction = """
你是一位拥有20年高校教育与职业发展指导经验的资深导师，名叫“张晨果”。

【你的核心沟通准则】
1. 绝对禁止动作描写：不要使用括号或星号来描写动作、神态或场景（例如绝对不能出现“喝了一口茶”、“微笑着说”、“拍拍肩膀”等）。只输出纯粹的对话文本，就像在微信上打字聊天一样自然。
2. 拒绝八股文与说教：坚决摒弃“短期规划/长期规划”这种死板的列表模板。不要每次都机械地列出 1、2、3 点建议。
3. 极致的情绪捕捉：你的第一反应永远是“接住”对方的情绪。准确点出对方话语背后隐藏的焦虑、不甘、疲惫或渴望。用一两句极其精准、有温度的话，让对方立刻感觉到“你懂我”。
4. 启发式引导，而非直接给答案：不要长篇大论地灌输道理。多用反问句，或者分享一个你曾经带过的学生的简短真实案例（一两句话带过），引导对方自己去思考。
5. 阅历的沉淀：你的语言简洁、有力量、击中要害。不要过于热情和啰嗦，要有资深长者的从容感。每次对话结束时，抛出一个能引发对方深思的轻量级问题，引导对话继续。
"""

# 3. 手动管理聊天记录
messages = [
    {"role": "system", "content": system_instruction}
]

print("=====================================")
print("张晨果导师（知无不答）已上线。输入 '退出' 结束对话。")
print("=====================================")

# 4. 聊天循环
while True:
    user_input = input("\n你: ")

    if user_input.strip() == "退出":
        print("张晨果导师: 祝你前程似锦，再见！")
        break

    if not user_input.strip():
        continue

    # 把用户的话记录下来
    messages.append({"role": "user", "content": user_input})

    try:
        print("张晨果导师思考中...")

        # 5. 核心调用：使用千问模型并应用你的参数
        response = client.chat.completions.create(
            model="qwen-plus",  # qwen-plus 是千问的主力模型，智商高且速度极快
            messages=messages,
            temperature=0.7,  # 你的调参：保留导师的温度与人情味
            top_p=0.9  # 你的调参：保留词汇的丰富度
        )

        # 提取回复并打印
        ai_reply = response.choices[0].message.content
        print(f"\n张晨果导师:\n{ai_reply}")

        # 把 AI 的回复存入记录，这样导师就有记忆了
        messages.append({"role": "assistant", "content": ai_reply})

    except Exception as e:
        print(f"\n抱歉，API 调用出错了: {e}")