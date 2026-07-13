import os

import fitz  # PyMuPDF
import oss2
import pymysql
import pythoncom
from docx2pdf import convert as convert_docx_to_pdf


def required_env(name):
    value = os.getenv(name)
    if not value:
        raise RuntimeError(f"Missing required environment variable: {name}")
    return value


# Secrets are supplied by the runtime environment and must never be committed.
DB_HOST = os.getenv("DB_HOST", "localhost")
DB_USER = os.getenv("DB_USER", "jishe")
DB_PASSWORD = required_env("DB_PASSWORD")
DB_NAME = os.getenv("DB_NAME", "jishe")

OSS_ACCESS_KEY_ID = required_env("ALIYUN_OSS_ACCESS_KEY_ID")
OSS_ACCESS_KEY_SECRET = required_env("ALIYUN_OSS_ACCESS_KEY_SECRET")
OSS_ENDPOINT = os.getenv("ALIYUN_OSS_ENDPOINT", "oss-cn-hangzhou.aliyuncs.com")
OSS_BUCKET_NAME = os.getenv("ALIYUN_OSS_BUCKET_NAME", "zhiyuexueye")
OSS_DOMAIN = f"https://{OSS_BUCKET_NAME}.{OSS_ENDPOINT}"


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
    conn = pymysql.connect(
        host=DB_HOST,
        user=DB_USER,
        password=DB_PASSWORD,
        database=DB_NAME,
        cursorclass=pymysql.cursors.DictCursor,
    )
    temp_dir = "temp_files"
    os.makedirs(temp_dir, exist_ok=True)

    try:
        with conn.cursor() as cursor:
            cursor.execute("SELECT id, name, type FROM resume_template")
            templates = cursor.fetchall()

            print(f"准备处理 {len(templates)} 个简历模板。")

            for template in templates:
                template_id = template["id"]
                name = template["name"]
                template_type = template["type"]
                folder = (
                    "resume-templates"
                    if template_type == 1
                    else "excellent-resumes"
                )
                object_key = f"{folder}/{name}.docx"

                print(f"正在检查 {name}: {object_key}")
                if not bucket.object_exists(object_key):
                    print(f"OSS 中不存在该文件: {object_key}")
                    continue

                local_docx = os.path.join(temp_dir, f"{template_id}_temp.docx")
                local_pdf = os.path.join(temp_dir, f"{template_id}_temp.pdf")
                local_jpg = os.path.join(temp_dir, f"{template_id}_temp.jpg")

                try:
                    bucket.get_object_to_file(object_key, local_docx)

                    pythoncom.CoInitialize()
                    convert_docx_to_pdf(local_docx, local_pdf)
                    convert_pdf_to_jpg(local_pdf, local_jpg)

                    preview_object_key = f"{folder}/previews/{name}.jpg"
                    bucket.put_object_from_file(preview_object_key, local_jpg)

                    new_doc_url = f"{OSS_DOMAIN}/{object_key}"
                    new_preview_url = f"{OSS_DOMAIN}/{preview_object_key}"
                    update_sql = (
                        "UPDATE resume_template "
                        "SET doc_url = %s, preview_url = %s WHERE id = %s"
                    )
                    cursor.execute(
                        update_sql,
                        (new_doc_url, new_preview_url, template_id),
                    )
                    conn.commit()
                    print(f"处理成功: {name}")
                except Exception as error:
                    print(f"处理失败 {name}: {error}")
                finally:
                    pythoncom.CoUninitialize()
                    for path in (local_docx, local_pdf, local_jpg):
                        if os.path.exists(path):
                            os.remove(path)
    finally:
        conn.close()


if __name__ == "__main__":
    main()
