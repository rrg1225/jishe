import json
import os

# ================= 配置区 =================
# 你的 JSON 文件完整路径
FILE_PATH = r'F:\计设--数据\Boss.json'


# ==========================================

def is_garbage_area(area_text):
    """判断地区文本是否为垃圾数据"""
    if not isinstance(area_text, str):
        return True

    # 1. 精确匹配“未知地区”
    if area_text == '未知地区':
        return True

    # 2. 包含换行符（说明抓到了包含经验、学历的多行文本）
    if '\n' in area_text:
        return True

    # 3. 包含乱码特征或薪资单位（比如截图中的 ă, 薪, K）
    invalid_chars = ['ă', '薪', 'K', 'k', '月', '年']
    for char in invalid_chars:
        if char in area_text:
            return True

    # 4. 长度异常（正常的地区如“南京·鼓楼区·万达广场”很少超过15-20个字符）
    if len(area_text) > 20:
        return True

    return False


def clean_data():
    if not os.path.exists(FILE_PATH):
        print(f"❌ 找不到文件: {FILE_PATH}，请检查路径！")
        return

    print("⏳ 正在读取数据文件...")

    with open(FILE_PATH, 'r', encoding='utf-8') as f:
        try:
            data = json.load(f)
        except Exception as e:
            print(f"❌ JSON 解析失败，文件可能已损坏: {e}")
            return

    cleaned_count = 0
    total_count = len(data)

    # 遍历数据，执行清洗
    for job in data:
        # 如果存在'地区'字段，且被判定为垃圾数据，则删除该字段
        if '地区' in job and is_garbage_area(job['地区']):
            del job['地区']
            cleaned_count += 1

    print("💾 正在覆盖原文件...")
    with open(FILE_PATH, 'w', encoding='utf-8') as f:
        json.dump(data, f, ensure_ascii=False, indent=4)

    print(f"✅ 清洗完成！")
    print(f"📊 总数据量: {total_count} 条")
    print(f"🧹 共剔除了 {cleaned_count} 条无效或乱码的地区信息。")
    print(f"📁 文件已原地更新: {FILE_PATH}")


if __name__ == '__main__':
    clean_data()