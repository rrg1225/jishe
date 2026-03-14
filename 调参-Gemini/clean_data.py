import json
import os

# 你的输入文件和期望的输出文件
input_file = '训练.json'
output_file = '训练-修正.json'  # 我们给新文件起个名字，防止覆盖原文件

print(f"🚀 开始读取并转换数据集: {input_file} ...\n")

success_count = 0
error_count = 0

# 1. 智能读取原始数据 (兼容 JSON 数组 或 错写的 JSONL)
try:
    with open(input_file, 'r', encoding='utf-8') as f:
        try:
            # 尝试按标准的 .json 数组格式读取
            raw_data = json.load(f)
        except json.JSONDecodeError:
            # 如果报错，说明它其实是个伪装成 .json 的 .jsonl，我们按行强读
            f.seek(0)
            raw_data = [json.loads(line) for line in f if line.strip()]
except Exception as e:
    print(f"❌ 读取文件失败，请检查文件编码是否为 UTF-8: {e}")
    exit()

# 2. 开始逐条清洗并转换为标准格式
with open(output_file, 'w', encoding='utf-8') as out_f:
    for index, item in enumerate(raw_data):
        # 百炼标准的容器格式
        formatted_item = {"messages": []}
        is_valid = True

        # 情况 A：如果你的原数据是 {"instruction": "...", "output": "..."} 格式
        if "instruction" in item or "output" in item:
            user_content = str(item.get("instruction", "")) + "\n" + str(item.get("input", ""))
            user_content = user_content.strip()
            assistant_content = str(item.get("output", "")).strip()

            if not user_content or not assistant_content:
                error_count += 1
                continue

            formatted_item["messages"].append({"role": "user", "content": user_content})
            formatted_item["messages"].append({"role": "assistant", "content": assistant_content})

        # 情况 B：如果你的原数据已经是 {"messages": [...]} 格式，只做清洗
        elif "messages" in item:
            for msg in item["messages"]:
                role = msg.get("role", "")
                content = str(msg.get("content", ""))

                # 严格校验角色名称和内容是否为空
                if role not in ["system", "user", "assistant"] or not content.strip():
                    is_valid = False
                    break

                formatted_item["messages"].append({"role": role, "content": content.strip()})

            # 如果校验失败，或者一轮对话连“问”和“答”两句话都凑不齐，直接丢弃
            if not is_valid or len(formatted_item["messages"]) < 2:
                error_count += 1
                continue
        else:
            # 根本认不出来的奇葩格式，直接丢弃
            error_count += 1
            continue

        # 3. 写入标准的 JSONL 格式 (核心！字典直接 dump，末尾加换行符)
        out_f.write(json.dumps(formatted_item, ensure_ascii=False) + '\n')
        success_count += 1

print("=" * 40)
print(f"✅ 格式转换与清洗彻底完成！")
print(f"🎉 成功生成标准数据: {success_count} 条")
print(f"🗑️ 剔除劣质/错误数据: {error_count} 条")
print(f"📁 请拿着 【{output_file}】 直接去阿里云百炼上传！")
print("=" * 40)