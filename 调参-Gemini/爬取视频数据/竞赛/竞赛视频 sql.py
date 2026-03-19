import os
import oss2
import urllib.parse

# ================= 🌟 1. 阿里云 OSS 核心配置 =================
# 请填写你自己的 AK 和 SK (在阿里云 RAM 访问控制里获取)
ACCESS_KEY_ID = 'LTAI5tFnUGfgjnvD8ndJuruL'
ACCESS_KEY_SECRET = 'agKq071RfPbsZxOB1OYhtGLEKasEnt'

# 你的 Bucket 所在的地域节点 (从你之前的截图推测是杭州)
ENDPOINT = 'oss-cn-hangzhou.aliyuncs.com'
# 你的 Bucket 名称
BUCKET_NAME = 'zhiyuexueye'

# ================= 🌟 2. 映射关系配置 =================
# 定义你要扫描的 OSS 文件夹路径，以及它们对应的数据库分类
PREFIX_MAP = {
    'jishi_project/竞赛/': '竞赛信息大厅',
    'jishi_project/考研/': '考研动态'
}

# SQL 保存路径 (存到你的 F 盘)
OUTPUT_SQL_PATH = r"F:\计设--数据\oss_videos_sync.sql"


def generate_sql_from_oss():
    print("🚀 正在连接阿里云 OSS 服务器...")
    try:
        auth = oss2.Auth(ACCESS_KEY_ID, ACCESS_KEY_SECRET)
        bucket = oss2.Bucket(auth, ENDPOINT, BUCKET_NAME)
    except Exception as e:
        print(f"❌ 连接 OSS 失败，请检查 AK、SK 和 Endpoint！\n错误信息: {e}")
        return

    sql_values = []

    for prefix, category in PREFIX_MAP.items():
        print(f"\n==============================================")
        print(f"🔍 正在扫描 OSS 目录: {prefix} (目标分类: {category})")

        count = 0
        # 遍历该前缀下的所有文件
        for obj in oss2.ObjectIterator(bucket, prefix=prefix):
            key = obj.key

            # 过滤掉文件夹本身（OSS里的文件夹本质上是以 '/' 结尾的文件）
            # 这里我们假设你要读取的是 .mp4 视频，如果还有其他格式，可以去掉 .endswith 的限制
            if key.endswith('/') or not key.endswith('.mp4'):
                continue

            # 从路径中提取文件名作为标题 (比如 'jishi_project/考研/张雪峰线代.mp4' -> '张雪峰线代')
            filename = key.split('/')[-1]
            title = filename.replace('.mp4', '').replace("'", "''")

            # 🌟 拼接阿里云 OSS 完整链接
            # 必须使用 urllib.parse.quote 对中文路径进行编码，否则前端访问会变成乱码 404！
            encoded_key = urllib.parse.quote(key)
            video_url = f"https://{BUCKET_NAME}.{ENDPOINT}/{encoded_key}"

            # 组装 SQL 语句，注意 type 设为 'video'
            sql_values.append(f"('{title}', 'video', '{category}', '官方收录', '{video_url}', NOW())")
            count += 1
            print(f"   🎬 找到视频: {title}")

        print(f"✨ 该目录下共盘点到 {count} 个视频。")

    # ================= 🌟 3. 生成最终的 SQL 文件 =================
    if sql_values:
        final_sql = "INSERT INTO contents (title, type, category, author_name, video_url, gmt_created) VALUES\n"
        final_sql += ",\n".join(sql_values) + ";"

        # 确保 F 盘的文件夹存在
        os.makedirs(os.path.dirname(OUTPUT_SQL_PATH), exist_ok=True)

        with open(OUTPUT_SQL_PATH, "w", encoding="utf-8") as f:
            f.write(final_sql)

        print(f"\n🎉 大功告成！全网同步完毕！")
        print(f"✅ 共生成了 {len(sql_values)} 条数据库记录。")
        print(f"📁 SQL 蓝图已保存在: {OUTPUT_SQL_PATH}")
        print("👉 下一步：直接把这个 SQL 文件拖进 Navicat 运行即可！")
    else:
        print("\n😭 在指定的 OSS 目录下没有找到任何 .mp4 视频文件，请检查目录路径是否正确。")


if __name__ == '__main__':
    generate_sql_from_oss()