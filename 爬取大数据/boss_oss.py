import oss2
import os

# ================= 配置区 =================
# 1. 阿里云 RAM 用户的 AccessKey (千万不要泄露给别人)
ACCESS_KEY_ID = 'LTAI5tFnUGfgjnvD8ndJuruL'
ACCESS_KEY_SECRET = 'agKq071RfPbsZxOB1OYhtGLEKasEnt'

# 2. OSS 对应的 Endpoint (比如华东1杭州节点：oss-cn-hangzhou.aliyuncs.com)
# 注意：不要带 http:// 或 https:// 前缀
ENDPOINT = 'oss-cn-hangzhou.aliyuncs.com'

# 3. 你的 Bucket 名称
BUCKET_NAME = 'zhiyuexueye'

# 4. 本地要上传的文件路径 (就是咱们刚才生成的那个文件)
LOCAL_FILE_PATH = r'F:\计设--数据\Boss.json'

# 5. 在 OSS 中保存的文件路径和名字 (比如放在 dataset 文件夹下)
OSS_OBJECT_NAME = 'dataset/Boss.json'


# ==========================================

def upload_data_to_oss():
    # 检查本地文件到底存不存在，防止空气上传
    if not os.path.exists(LOCAL_FILE_PATH):
        print(f"❌ 找不到本地文件: {LOCAL_FILE_PATH}，请确认爬虫已经跑完并生成了数据！")
        return

    print(f"🚀 准备将 {LOCAL_FILE_PATH} 上传至 OSS...")

    try:
        # 验证身份并连接 Bucket
        auth = oss2.Auth(ACCESS_KEY_ID, ACCESS_KEY_SECRET)
        bucket = oss2.Bucket(auth, ENDPOINT, BUCKET_NAME)

        # 执行上传操作 (参数1: 云端文件名, 参数2: 本地文件路径)
        result = bucket.put_object_from_file(OSS_OBJECT_NAME, LOCAL_FILE_PATH)

        # 检查 HTTP 返回状态码，200 表示成功
        if result.status == 200:
            print("✅ 恭喜！文件成功上传到 OSS。")

            # 顺便帮你拼接一下文件的下载/访问链接
            file_url = f"https://{BUCKET_NAME}.{ENDPOINT}/{OSS_OBJECT_NAME}"
            print(f"🔗 文件外网链接 (若Bucket权限为公共读即可直接访问): \n{file_url}")
        else:
            print(f"⚠️ 上传结束，但状态码异常: {result.status}")

    except oss2.exceptions.OssError as e:
        print(f"❌ OSS 原生错误: {e}")
    except Exception as e:
        print(f"❌ 发生未知异常: {e}")


if __name__ == '__main__':
    upload_data_to_oss()