import os
import oss2
from urllib.parse import quote

# ================= 1. OSS 基础配置 =================
# ⚠️ 请替换为你自己的阿里云 OSS 真实信息
ACCESS_KEY_ID = 'LTAI5tFnUGfgjnvD8ndJuruL'
ACCESS_KEY_SECRET = 'agKq071RfPbsZxOB1OYhtGLEKasEnt'
BUCKET_NAME = 'zhiyuexueye'
ENDPOINT = 'oss-cn-hangzhou.aliyuncs.com'


# 本地数据根目录
LOCAL_BASE_DIR = r"F:\计设--数据\文章\竞赛"

# ================= 2. 初始化 OSS =================
print("☁️ 正在连接阿里云 OSS...")
try:
    auth = oss2.Auth(ACCESS_KEY_ID, ACCESS_KEY_SECRET)
    # oss2 库需要带有 http:// 或 https:// 前缀的 endpoint
    bucket = oss2.Bucket(auth, f'http://{ENDPOINT}', BUCKET_NAME)
    print("✅ OSS 连接成功！")
except Exception as e:
    print(f"❌ OSS 连接失败: {e}")
    exit()


# ================= 3. 核心处理逻辑 =================

def process_and_upload():
    print(f"\n📁 开始扫描本地目录: {LOCAL_BASE_DIR}")

    # 遍历本地文件夹
    for root, dirs, files in os.walk(LOCAL_BASE_DIR):
        # 排除空文件夹和根目录本身
        if not files or root == LOCAL_BASE_DIR:
            continue

        # 1. 识别当前文件夹内的文本和图片
        txt_file = None
        img_files = []
        for file in files:
            if file == 'content.txt':
                txt_file = file
            elif file.lower().endswith(('.jpg', '.jpeg', '.png', '.webp', '.gif')):
                img_files.append(file)

        # 如果没有文本，跳过
        if not txt_file:
            continue

        # 获取文章名字 (也就是当前文件夹的名字)
        article_name = os.path.basename(root)
        print(f"\n  -> 正在处理组合: 【{article_name}】")

        # 定义 OSS 中的存储路径前缀 (例如: 竞赛图文库/数学建模 建模思路/文章标题/)
        rel_path = os.path.relpath(root, LOCAL_BASE_DIR)
        oss_folder_prefix = f"竞赛图文库/{rel_path}/".replace("\\", "/")

        # --- 步骤 A：上传图片，获取云端外链 ---
        cloud_image_urls = []
        for img in img_files:
            local_img_path = os.path.join(root, img)
            oss_img_key = oss_folder_prefix + img

            try:
                # 上传图片到 OSS
                bucket.put_object_from_file(oss_img_key, local_img_path)
                # 拼接生成该图片的公网访问 URL (使用 quote 处理中文或空格路径)
                public_url = f"https://{BUCKET_NAME}.{ENDPOINT}/{quote(oss_img_key)}"
                cloud_image_urls.append(public_url)
            except Exception as e:
                print(f"      [图片上传失败] {img}: {e}")

        # --- 步骤 B：读取文字并组合图片 ---
        local_txt_path = os.path.join(root, txt_file)
        with open(local_txt_path, 'r', encoding='utf-8') as f:
            article_text = f.read()

        # 生成 Markdown 格式的图文排版
        # 你可以根据需要在下面修改排版格式
        combined_markdown = f"# {article_name}\n\n"
        combined_markdown += f"{article_text}\n\n"
        combined_markdown += f"---\n### 相关配图\n\n"

        for url in cloud_image_urls:
            # Markdown 插入图片语法: ![图片描述](图片URL)
            combined_markdown += f"![文章配图]({url})\n\n"

        # --- 步骤 C：保存组合后的文件并上传 OSS ---
        md_filename = "完整图文版.md"
        local_md_path = os.path.join(root, md_filename)

        # 1. 保存到本地 (方便你在本地 F 盘用 Typora 等软件预览)
        with open(local_md_path, 'w', encoding='utf-8') as f:
            f.write(combined_markdown)

        # 2. 将这篇最终组合好的 Markdown 文章上传到 OSS
        oss_md_key = oss_folder_prefix + md_filename
        try:
            bucket.put_object_from_file(oss_md_key, local_md_path)
            print(f"      [OK] 图文融合完毕并成功上传 OSS！")
        except Exception as e:
            print(f"      [文件上传失败] {md_filename}: {e}")


if __name__ == '__main__':
    process_and_upload()
    print("\n🎉 所有本地文章图文融合及 OSS 同步全部完成！")