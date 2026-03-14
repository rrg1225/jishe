import json
import openai  # pip install openai
import concurrent.futures
from tqdm import tqdm
import os

# 配置 API (可以使用阿里云百炼、DeepSeek 等兼容 OpenAI 格式的接口)
API_KEY = "sk-cc55499fcc37450fb0e9521ee7f61d4c"
BASE_URL = "https://dashscope.aliyuncs.com/compatible-mode/v1"
MODEL_NAME = "qwen-turbo"  # 例如 qwen-max

client = openai.OpenAI(api_key=API_KEY, base_url=BASE_URL)


def extract_chunks(txt_path, chunk_size=350, overlap=50):
    """
    细粒度切块：读取 TXT 文件，每块 350 字，以确保能生成 500-1000 块文本
    加入 50 字的重叠区，防止上下文断裂
    """
    # 核心修改点：加入编码异常处理，彻底解决乱码问题
    try:
        # 优先尝试用 utf-8 读取
        with open(txt_path, "r", encoding="utf-8") as f:
            text = f.read()
    except UnicodeDecodeError:
        # 如果报错，说明文件是 Windows 默认的 GBK 编码，自动回退到 gbk
        with open(txt_path, "r", encoding="gbk") as f:
            text = f.read()

    # 简单清理空白符和换行符
    text = " ".join(text.split())

    chunks = []
    for i in range(0, len(text), chunk_size - overlap):
        chunks.append(text[i:i + chunk_size])
    return chunks


def generate_qa_from_chunk(chunk_text):
    """为单一文本块生成 1-2 条问答对"""
    prompt = f"""
    作为数据标注专家，请阅读以下大学教育或职业规划书籍的片段。
    请提取核心知识点，生成 1 到 2 个高质量的 Q&A 问答对。
    问题必须以大学生的第一人称提问（例如：迷茫、求教的具体问题）。
    回答必须专业、有理有据。

    必须严格输出纯 JSON 格式：
    [
      {{
        "messages": [
          {{"role": "system", "content": "你是一个专业的大学生学业与职业规划AI助手。"}},
          {{"role": "user", "content": "学生的提问"}},
          {{"role": "assistant", "content": "你的回答"}}
        ]
      }}
    ]

    书籍片段：
    {chunk_text}
    """
    try:
        response = client.chat.completions.create(
            model=MODEL_NAME,
            messages=[{"role": "user", "content": prompt}],
            response_format={"type": "json_object"},
            temperature=0.7  # 稍微提高温度，增加问答的多样性
        )
        data = json.loads(response.choices[0].message.content)
        # 如果模型返回的JSON是一个字典包含了列表，进行提取
        if isinstance(data, dict):
            for val in data.values():
                if isinstance(val, list):
                    return val
            return [data]
        return data if isinstance(data, list) else []
    except Exception as e:
        return []


def process_book(book_path):
    print(f"\n开始处理: {book_path}")
    chunks = extract_chunks(book_path)
    print(f"《{book_path}》共切分为 {len(chunks)} 个文本块。")

    book_dataset = []

    # 开启线程池进行高并发请求，极大缩短提取时间
    with concurrent.futures.ThreadPoolExecutor(max_workers=10) as executor:
        # 使用 tqdm 显示进度条
        futures = {executor.submit(generate_qa_from_chunk, chunk): chunk for chunk in chunks}
        for future in tqdm(concurrent.futures.as_completed(futures), total=len(chunks), desc=f"生成中"):
            result = future.result()
            if result:
                book_dataset.extend(result)

    return book_dataset


if __name__ == "__main__":
    # 核心修改点：将这里的文件后缀全部改成了 .txt
    books = [
        "你的降落伞是什么颜色.txt",
        "大学生职业生涯规划与就业指导.txt",
        "少有人走的路.txt",
        "影响力.txt",
        "微习惯 简单到不可能失败的自我管理法则.txt",
        "把时间当作朋友.txt",
        "拆掉思维里的墙.txt",
        "斯坦福大学人生设计课 .txt",
        "社会性动物.txt",
        "自卑与超越.txt",
        "自控力 .txt",
        "蛤蟆先生去看心理医生.txt",
        "认知觉醒.txt",
        "读大学，究竟读什么.txt",
        "远见：如何规划职业生涯 3 大阶段.txt"
    ]

    final_dataset = []

    for book in books:
        if os.path.exists(book):
            dataset = process_book(book)
            final_dataset.extend(dataset)
            print(f"《{book}》成功提取了 {len(dataset)} 条数据。")
        else:
            print(f"未找到文件: {book}，请确保 TXT 文件在同级目录下。")

    # 将最终的几千条数据保存为完整的训练集
    output_file = "训练.json"
    with open(output_file, "w", encoding="utf-8") as f:
        json.dump(final_dataset, f, ensure_ascii=False, indent=2)

    print(f"\n全部处理完成！共生成 {len(final_dataset)} 条训练数据，已保存至 {output_file}。")