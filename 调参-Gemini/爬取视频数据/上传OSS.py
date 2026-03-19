import os
import oss2

# --- 1. 基础配置 ---
# 请在阿里云控制台获取以下信息
ACCESS_KEY_ID = 'LTAI5tFnUGfgjnvD8ndJuruL'
ACCESS_KEY_SECRET = 'agKq071RfPbsZxOB1OYhtGLEKasEnt'
BUCKET_NAME = 'zhiyuexueye'
ENDPOINT = 'oss-cn-hangzhou.aliyuncs.com'  # 根据你的区域修改

# 你的 F 盘视频根目录
BASE_DIR = r"F:\计设--数据\视频"

# --- 2. 初始化 OSS ---
auth = oss2.Auth(ACCESS_KEY_ID, ACCESS_KEY_SECRET)
bucket = oss2.Bucket(auth, ENDPOINT, BUCKET_NAME)

# 拼接基础 URL (用于打印查看结果)
OSS_BASE_URL = f"https://{BUCKET_NAME}.{ENDPOINT}/"


def simple_upload():
    print("开始扫描本地文件并上传至 OSS...\n")

    # 遍历 F 盘下的“保研”、“海外留学”等文件夹
    for category in os.listdir(BASE_DIR):
        category_path = os.path.join(BASE_DIR, category)

        # 只处理文件夹
        if os.path.isdir(category_path):
            files = os.listdir(category_path)
            if not files:
                continue

            print(f"正在处理板块: 【{category}】")

            for filename in files:
                # 支持视频和常见文章格式
                if filename.lower().endswith(('.mp4', '.pdf', '.docx', '.png', '.jpg')):
                    local_file = os.path.join(category_path, filename)

                    # 关键：定义 OSS 上的路径，实现“分簇存储”
                    # 格式为：项目名/模块名/文件名
                    oss_path = f"jishi_project/{category}/{filename}"

                    try:
                        # 执行上传
                        print(f"  正在上传: {filename} ...", end="")
                        bucket.put_object_from_file(oss_path, local_file)

                        # 生成永久访问 URL
                        final_url = OSS_BASE_URL + oss_path
                        print(f" [成功]")
                        print(f"  URL: {final_url}\n")

                    except Exception as e:
                        print(f" [失败] 错误信息: {e}")


if __name__ == "__main__":
    simple_upload()
    print("所有文件处理完毕！")