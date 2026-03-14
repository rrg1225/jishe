import json
import os
import time
from openai import OpenAI

# ==========================================
# 1. 配置通义千问 API 客户端
# ==========================================
client = OpenAI(
    api_key="sk-cc55499fcc37450fb0e9521ee7f61d4c",  # 注意：这里一定要换成你刚才在百炼控制台复制的真实 Key
    base_url="https://dashscope.aliyuncs.com/compatible-mode/v1"
)

MODEL_NAME = "qwen-plus"

# ==========================================
# 2. 定义生成参数与精细化主题
# ==========================================
CATEGORIES = [
    "保研推免：侧重跨专业申请规划、夏令营准备、各个学校的保研要求、想要去的学校的保研要求、保研关于复试和申请学校的选择",
    "考研统考：侧重专业课复习规划、如何平衡初试备考与日常琐事、如何去调节考研压力、如何去规划考研、考研学校的选择",
    "学科竞赛：侧重美赛（MCM/ICM）数据预处理与论文写作、中国高校计算机大赛项目实战、以及在大学生职业规划平台、以及英语类竞赛、商业类竞赛、大学生创新创业类竞赛、计算机类竞赛（应用，算法）、人工智能类竞赛（人工智能、挑战赛）、数模类竞赛、物理化学生物这些基础学科竞赛"
]

TARGET_COUNT_PER_CATEGORY = 60

# 【重点修改】将每次请求数量调小到 5，避免大模型生成时间过长导致假死或超时
BATCH_SIZE = 10

OUTPUT_FILE = "专项.json"


def generate_data_batch(category, batch_size):
    """调用大模型生成指定数量的数据"""

    system_prompt = "你是一个专门协助构建高质量 AI 训练数据集的算法工程师。请严格按照要求输出结构化数据。"

    user_prompt = f"""
    请为我生成 {batch_size} 组关于“{category}”的问答数据，用于微调大模型。

    要求：
    1. 场景要具体、真实、硬核。
    2. 包含单轮对话和多轮对话，回答要有深度、专业且具有实际指导意义。
    3. **必须严格输出合法的 JSON 格式**，每行一个完整独立的 JSON 对象。不要输出多余的括号。
    4. 绝对不要包含任何额外的 Markdown 标记（如 ```json 等），直接输出纯文本。

    格式严格遵守如下示例：
    {{"messages": [{{"role": "system", "content": "You are a helpful assistant"}}, {{"role": "user", "content": "问题"}}, {{"role": "assistant", "content": "回答"}}]}}
    """

    try:
        response = client.chat.completions.create(
            model=MODEL_NAME,
            messages=[
                {"role": "system", "content": system_prompt},
                {"role": "user", "content": user_prompt}
            ],
            temperature=0.7,
        )

        raw_text = response.choices[0].message.content.strip()

        # 鲁棒性处理：清理模型偶尔误加的 markdown 标记
        if raw_text.startswith("```jsonl") or raw_text.startswith("```json"):
            raw_text = raw_text.split('\n', 1)[1] if '\n' in raw_text else raw_text
        if raw_text.endswith("```"):
            raw_text = raw_text[:-3]

        return raw_text.strip().split('\n')

    except Exception as e:
        print(f"API 调用异常: {e}")
        return []


def main():
    # 【重点修改】读取或初始化一个全局的列表，用来存放所有数据
    all_data = []
    if os.path.exists(OUTPUT_FILE):
        try:
            with open(OUTPUT_FILE, 'r', encoding='utf-8') as f:
                all_data = json.load(f)
        except json.JSONDecodeError:
            print("现有文件格式不合法，将重新创建覆盖。")
            all_data = []

    total_generated = len(all_data)

    for category in CATEGORIES:
        print(f"\n=============================================")
        print(f"开始生成主题数据：\n【{category}】")
        print(f"=============================================")
        category_count = 0

        while category_count < TARGET_COUNT_PER_CATEGORY:
            print(f"  正在请求生成第 {category_count + 1} 到 {category_count + BATCH_SIZE} 条...")
            lines = generate_data_batch(category, BATCH_SIZE)

            valid_objects = []
            for line in lines:
                line = line.strip()
                # 过滤掉逗号或多余的方括号（以防大模型自作主张加了格式）
                if line.endswith(','):
                    line = line[:-1]
                if line == '[' or line == ']':
                    continue
                if not line:
                    continue

                # 严格校验并转换为 Python 字典
                try:
                    obj = json.loads(line)
                    valid_objects.append(obj)
                except json.JSONDecodeError:
                    continue

            # 【重点修改】将新验证通过的数据加入总列表，并以标准 JSON 数组格式写入文件
            if valid_objects:
                all_data.extend(valid_objects)

                # ensure_ascii=False 保证中文正常显示，indent=4 保证代码带有漂亮的缩进和换行
                with open(OUTPUT_FILE, "w", encoding="utf-8") as f:
                    json.dump(all_data, f, ensure_ascii=False, indent=4)

                category_count += len(valid_objects)
                total_generated += len(valid_objects)
                print(
                    f"  --> 成功校验并追加 {len(valid_objects)} 条数据。本主题进度: {category_count}/{TARGET_COUNT_PER_CATEGORY}")
            else:
                print("  --> 本轮未获取到格式合法的数据，自动进行重试...")

            time.sleep(1)

    print(f"\n🎉 任务圆满完成！共成功生成了 {total_generated} 条高质量数据。")


if __name__ == "__main__":
    main()