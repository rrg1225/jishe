import os
import urllib.parse  # 🌟 引入 URL 编码库

# ================= 配置区 =================
# 本地文章数据存放的根目录
BASE_DIR = r"F:\计设--数据\文章\竞赛"

# 你的 OSS 外链前缀 (请替换为你的真实 Bucket 和 Endpoint)
# 例如之前传图时用的文件夹前缀是 "竞赛图文库"
OSS_BASE_URL = "https://你的bucket名称.oss-cn-hangzhou.aliyuncs.com/竞赛图文库"


# ================= 功能区 =================

def generate_article_sql():
    # 准备 SQL 头部
    sql_header = "INSERT INTO article_data (topic, title, content, image_urls, create_time) VALUES\n"
    values_list = []

    print(f"📂 开始扫描数据目录: {BASE_DIR}")

    # 1. 遍历外层的主题文件夹 (如: 数学建模, 蓝桥杯)
    for topic in os.listdir(BASE_DIR):
        topic_path = os.path.join(BASE_DIR, topic)
        if not os.path.isdir(topic_path): continue

        # 2. 遍历主题下的具体文章文件夹 (如: 1_文章标题)
        for article_folder in os.listdir(topic_path):
            article_path = os.path.join(topic_path, article_folder)
            if not os.path.isdir(article_path): continue

            # 提取展示用的标题 (去掉前面的排序号 "1_")
            title = article_folder.split('_', 1)[-1] if '_' in article_folder else article_folder

            content_text = ""
            img_urls = []

            # 3. 扫描文章文件夹内的文件
            for file in os.listdir(article_path):
                file_path = os.path.join(article_path, file)

                # 读取正文
                if file == 'content.txt':
                    try:
                        with open(file_path, 'r', encoding='utf-8') as f:
                            content_text = f.read()
                    except Exception as e:
                        print(f"读取文本失败: {file_path}")

                # 提取图片并生成 OSS 链接
                elif file.lower().endswith(('.jpg', '.jpeg', '.png', '.webp', '.gif')):
                    # 🌟 核心：对多级中文目录进行 URL 编码
                    safe_topic = urllib.parse.quote(topic)
                    safe_folder = urllib.parse.quote(article_folder)
                    safe_filename = urllib.parse.quote(file)

                    # 拼凑出前端可直接访问的合法外链
                    img_url = f"{OSS_BASE_URL}/{safe_topic}/{safe_folder}/{safe_filename}"
                    img_urls.append(img_url)

            # 4. 如果正文不为空，则构造单条 SQL 记录
            if content_text:
                # 🌟 极度重要：转义长文本中的单引号和反斜杠，否则 Navicat 必报错！
                # 将 \ 替换为 \\，将 ' 替换为 ''
                safe_content = content_text.replace('\\', '\\\\').replace("'", "''")
                safe_title = title.replace('\\', '\\\\').replace("'", "''")
                safe_topic_sql = topic.replace('\\', '\\\\').replace("'", "''")

                # 图片链接多张用逗号隔开
                images_str = ",".join(img_urls)

                # 组合成一行 VALUES: ('所属竞赛', '文章标题', '文章正文', '图片链接集合', NOW())
                record = f"('{safe_topic_sql}', '{safe_title}', '{safe_content}', '{images_str}', NOW())"
                values_list.append(record)

    # 5. 将所有记录合并并写入 .sql 文件
    if values_list:
        final_sql = sql_header + ",\n".join(values_list) + ";"

        # 写入文件
        sql_filename = "insert_articles.sql"
        with open(sql_filename, "w", encoding="utf-8") as f:
            f.write(final_sql)

        print(f"\n✅ SQL 脚本生成完毕！共成功打包了 {len(values_list)} 篇文章。")
        print(f"👉 所有图片路径的 '#' 和空格均已进行了安全的 URL 编码。")
        print(f"👉 文本内容中的单引号等特殊字符已做转义处理。")
        print(f"📁 请在当前目录下找到【{sql_filename}】，然后将它拖入 Navicat 运行即可！")
    else:
        print("\n⚠️ 扫描完毕，但未找到任何有效的文章内容。")


if __name__ == "__main__":
    generate_article_sql()
