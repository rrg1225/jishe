import os
import oss2
import pymysql

# ================= 1. 配置阿里云 OSS (注意使用外网 Endpoint) =================
ACCESS_KEY_ID = 'LTAI5tFnUGfgjnvD8ndJuruL'
ACCESS_KEY_SECRET = 'agKq071RfPbsZxOB1OYhtGLEKasEnt'
BUCKET_NAME = 'renrugu'
ENDPOINT = 'oss-cn-hangzhou.aliyuncs.com'  # 本地运行务必用外网 Endpoint

auth = oss2.Auth(ACCESS_KEY_ID, ACCESS_KEY_SECRET)
bucket = oss2.Bucket(auth, ENDPOINT, BUCKET_NAME)

# ================= 2. 配置云端 MySQL 数据库 =================
# 之前在 Navicat 里怎么连的，这里就怎么填
DB_HOST = '120.27.205.204'  # 你的 ECS 公网 IP
DB_USER = 'jishe'  # 或者是你单独建的数据库账号
DB_PASS = '3e6tr3XkahMmiTDC'
DB_NAME = 'jishe'  # 你的数据库名


def get_db_connection():
    return pymysql.connect(host=DB_HOST, user=DB_USER, password=DB_PASS, database=DB_NAME,
                           cursorclass=pymysql.cursors.DictCursor)


# ================= 3. 核心业务：上传视频并写入数据库 =================
def process_and_upload(local_file_path, title, module_category):
    """
    处理本地视频，分类上传至 OSS，并将信息同步到云端数据库
    :param local_file_path: 本地视频的物理路径，比如 'D:/temp_videos/funny_cat.mp4'
    :param title: 视频标题
    :param module_category: 前端模块分类（如 'tech', 'entertainment'）
    """
    file_name = os.path.basename(local_file_path)
    # 在 OSS 里按照分类建文件夹，保持整洁
    oss_object_name = f"videos/{module_category}/{file_name}"

    print(f"正在上传 [{title}] 到 OSS 分类: {module_category}...")

    try:
        # 上传到 OSS
        bucket.put_object_from_file(oss_object_name, local_file_path)

        # 拼接外网播放链接 (前端直接拿这个链接放进 <video> 标签)
        video_url = f"https://{BUCKET_NAME}.{ENDPOINT}/{oss_object_name}"
        print(f"✅ OSS 上传成功！播放链接: {video_url}")

        # 将数据写入云端 MySQL 的 contents 表 (假设表结构有 title, category, video_url 等)
        conn = get_db_connection()
        try:
            with conn.cursor() as cursor:
                sql = """
                      INSERT INTO contents (title, content_type, module_category, oss_url)
                      VALUES (%s, 'video', %s, %s) \
                      """
                cursor.execute(sql, (title, module_category, video_url))
            conn.commit()
            print("✅ 数据库写入成功！前端可以读取到这条数据了。")
        finally:
            conn.close()

        # (可选) 上传完毕后，删除本地占硬盘的视频文件
        # os.remove(local_file_path)
        # print("🧹 本地临时文件已清理")

    except Exception as e:
        print(f"❌ 处理失败: {e}")


# ================= 4. 模拟爬虫抓取主程序 =================
if __name__ == '__main__':
    # 假设你的爬虫刚才把两个视频下载到了本地的 D 盘

    # 模拟抓取到的数据 1 (科技类)
    mock_data_1 = {
        "local_path": "test_java_tutorial.mp4",  # 填入你本地真实存在的一个测试 mp4 文件路径
        "title": "Java SpringBoot 从入门到精通",
        "category": "tech"  # 对应前端的科技学习模块
    }

    # 模拟抓取到的数据 2 (生活搞笑类)
    mock_data_2 = {
        "local_path": "test_funny_cat.mp4",
        "title": "今天遇到一只超好笑的猫",
        "category": "entertainment"  # 对应前端的娱乐模块
    }

    # 执行流水线
    # process_and_upload(mock_data_1['local_path'], mock_data_1['title'], mock_data_1['category'])