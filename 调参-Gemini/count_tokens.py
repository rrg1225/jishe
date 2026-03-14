import os
from transformers import AutoTokenizer

# 1. 在线加载 Qwen2.5 分词器 (国内建议使用 ModelScope 镜像)
# 只要联网，它会自动下载分词器配置文件
model_id = "qwen/Qwen2.5-7B-Instruct"
tokenizer = AutoTokenizer.from_pretrained(model_id, revision="master")


def count_folder_tokens(folder_path):
    total_tokens = 0
    file_count = 0

    # 遍历当前目录下所有 txt 文件
    for file_name in os.listdir(folder_path):
        if file_name.endswith('.txt'):
            file_path = os.path.join(folder_path, file_name)
            with open(file_path, 'r', encoding='utf-8') as f:
                content = f.read()
                # 计算该文件的 token
                tokens = tokenizer.encode(content)
                total_tokens += len(tokens)
                file_count += 1
                print(f"文件: {file_name} | Tokens: {len(tokens)}")

    return total_tokens, file_count


# 假设你的文件就在当前目录下
path = "."
total, count = count_folder_tokens(path)

print("-" * 30)
print(f"项目统计:")
print(f"总文件数: {count}")
print(f"总 Token 数: {total}")

# 5090 训练效率预估 (按保守 2500 tokens/s 计算)
train_time_sec = total / 2500
print(f"RTX 5090 训练 1 轮 (1 Epoch) 预估耗时: {train_time_sec:.2f} 秒")