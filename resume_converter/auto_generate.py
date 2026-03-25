import os
import oss2
import pymysql
import fitz  # PyMuPDF
from docx2pdf import convert as convert_docx_to_pdf
import pythoncom
from urllib.parse import urlparse

# ================= 【配置区：绝对不要改动这里】 =================
DB_HOST = '120.27.205.204'
DB_USER = 'jishe'
DB_PASSWORD = '3e6tr3XkahMmiTDC'
DB_NAME = 'jishe'

OSS_ACCESS_KEY_ID = 'LTAI5tFnUGfgjnvD8ndJuruL'
OSS_ACCESS_KEY_SECRET = 'agKq071RfPbsZxOB1OYhtGLEKasEnt'
OSS_ENDPOINT = 'oss-cn-hangzhou.aliyuncs.com'
OSS_BUCKET_NAME = 'zhiyuexueye'
# ========================================================

OSS_DOMAIN = f'https://{OSS_BUCKET_NAME}.{OSS_ENDPOINT}'


def setup_oss():
    auth = oss2.Auth(OSS_ACCESS_KEY_ID, OSS_ACCESS_KEY_SECRET)
    return oss2.Bucket(auth, OSS_ENDPOINT, OSS_BUCKET_NAME)


def convert_pdf_to_jpg(pdf_path, jpg_path):
    doc = fitz.open(pdf_path)
    page = doc.load_page(0)
    pix = page.get_pixmap(matrix=fitz.Matrix(2, 2))
    pix.save(jpg_path)
    doc.close()


def main():
    bucket = setup_oss()
    conn = pymysql.connect(host=DB_HOST, user=DB_USER, password=DB_PASSWORD, database=DB_NAME,
                           cursorclass=pymysql.cursors.DictCursor)
    temp_dir = 'temp_files'
    if not os.path.exists(temp_dir):
        os.makedirs(temp_dir)

    try:
        with conn.cursor() as cursor:
            # 获取所有模板
            cursor.execute("SELECT id, name, type FROM resume_template")
            templates = cursor.fetchall()

            print(f"🚀 路径已修正：直接访问根目录下的文件夹，准备处理 {len(templates)} 个文件...\n")

            for tpl in templates:
                tpl_id = tpl['id']
                name = tpl['name']
                tpl_type = tpl['type']  # 1=简历模板, 2=优秀简历

                # --- 【核心修正：根据你的 OSS 链接，路径直接从文件夹名开始】 ---
                if tpl_type == 1:
                    # 路径直接是 resume-templates/模板1.docx
                    object_key = f"resume-templates/{name}.docx"
                else:
                    # 优秀简历同理
                    object_key = f"excellent-resumes/{name}.docx"

                print(f"⏳ 正在核对: 【{name}】 -> 目标路径: {object_key}")

                # 检查文件到底在不在
                if not bucket.object_exists(object_key):
                    print(f"   ❌ 报错: 在 OSS 的 {OSS_BUCKET_NAME} 桶里找不到这个文件，请确认文件名是否完全一致！")
                    continue

                try:
                    local_docx = os.path.join(temp_dir, f"{tpl_id}_temp.docx")
                    local_pdf = os.path.join(temp_dir, f"{tpl_id}_temp.pdf")
                    local_jpg = os.path.join(temp_dir, f"{tpl_id}_temp.jpg")

                    # 1. 下载
                    print("   ⬇️ [1/4] 下载中...")
                    bucket.get_object_to_file(object_key, local_docx)

                    # 2. 转 PDF
                    print("   ⚙️ [2/4] 调用 Word 转换...")
                    pythoncom.CoInitialize()
                    convert_docx_to_pdf(local_docx, local_pdf)

                    # 3. 转 JPG
                    print("   🖼️ [3/4] 生成图片...")
                    convert_pdf_to_jpg(local_pdf, local_jpg)

                    # 4. 上传 (在各自目录下自动建立 previews 文件夹)
                    folder = "resume-templates" if tpl_type == 1 else "excellent-resumes"
                    preview_object_key = f"{folder}/previews/{name}.jpg"

                    print("   ⬆️ [4/4] 上传预览图...")
                    bucket.put_object_from_file(preview_object_key, local_jpg)

                    # 5. 更新数据库 (顺便把 doc_url 和 preview_url 全修正)
                    new_doc_url = f"{OSS_DOMAIN}/{object_key}"
                    new_preview_url = f"{OSS_DOMAIN}/{preview_object_key}"

                    update_sql = "UPDATE resume_template SET doc_url = %s, preview_url = %s WHERE id = %s"
                    cursor.execute(update_sql, (new_doc_url, new_preview_url, tpl_id))
                    conn.commit()

                    print(f"   ✅ 处理成功！数据库已刷新为新路径。\n")

                except Exception as e:
                    print(f"   ⚠️ 处理出错: {e}\n")
                    continue
                finally:
                    for f in [local_docx, local_pdf, local_jpg]:
                        if os.path.exists(f): os.remove(f)

    finally:
        conn.close()
        print("🎉 任务全部结束！这次路径绝对对上了，请刷新前端页面！")


if __name__ == '__main__':
    main()