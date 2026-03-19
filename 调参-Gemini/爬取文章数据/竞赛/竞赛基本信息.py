import requests
from bs4 import BeautifulSoup
import os
import re
import time
import pandas as pd  # 需要安装 pandas: pip install pandas openpyxl


# ==========================================
# 1. 网页内容提取 (需根据不同网站定制)
# ==========================================
def scrape_official_website(url):
    """
    访问官网并提取基础信息。
    ⚠️ 难点提醒：由于各个官网结构不同，这里的提取规则只能尽量通用，
    如果是非常复杂的网站，可能需要为特定网址写专门的解析函数。
    """
    headers = {
        'User-Agent': 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/115.0.0.0 Safari/537.36'
    }

    try:
        print(f"🔄 正在访问: {url}")
        response = requests.get(url, headers=headers, timeout=15)
        response.encoding = response.apparent_encoding
        response.raise_for_status()

        soup = BeautifulSoup(response.text, 'html.parser')

        # 【通用尝试方案】
        # 尝试获取网页的 title 作为比赛名称
        title_text = soup.title.string.strip() if soup.title else "未知赛事名称"

        # 尝试提取网页正文的所有文字（去除脚本和样式）
        for script in soup(["script", "style"]):
            script.extract()
        text_content = soup.get_text(separator='\n', strip=True)

        # 截取前 300 个字符作为简介（因为没有特定 class，只能暴力提取文字）
        desc_text = text_content[:300].replace('\n', ' ') + "..." if text_content else "暂无简介信息。"

        return {
            "name": title_text,
            "organizer": "请参考官网信息",  # 通用爬虫很难精准定位主办方
            "status": "请前往官网查看最新状态",
            "tags": ["综合赛事", "能力提升"],
            "description": desc_text,
            "source_url": url
        }

    except Exception as e:
        print(f"❌ 抓取失败 ({url}): {e}")
        return None


# ==========================================
# 2. 文章生成与保存 (复用之前的逻辑)
# ==========================================
def generate_and_save(comp_data, base_dir):
    if not comp_data:
        return

    tags_str = "、".join(comp_data['tags'])
    article = f"""
【赛事推荐】你想丰富简历、提升专业技能吗？千万别错过{comp_data['name']}！

🎯 赛事概览：
主办方：{comp_data['organizer']}
当前状态：{comp_data['status']}
赛事标签：{tags_str}

💡 赛事简介：
"{comp_data['description']}"

🔗 了解更多详情或直接报名，请访问比赛官方主页：{comp_data['source_url']}
"""

    # 自动创建目录
    if not os.path.exists(base_dir):
        os.makedirs(base_dir)

    # 清理非法字符并保存
    safe_filename = re.sub(r'[\\/:*?"<>|]', '_', comp_data['name'])
    file_path = os.path.join(base_dir, f"{safe_filename}_基础信息.txt")

    try:
        with open(file_path, 'w', encoding='utf-8') as file:
            file.write(article.strip())
        print(f"✅ 保存成功: {file_path}")
    except Exception as e:
        print(f"❌ 保存失败: {e}")


# ==========================================
# 3. 批量执行主程序
# ==========================================
def batch_process_competitions(excel_path, target_dir):
    """
    读取 Excel 文件中的链接并批量执行爬取
    """
    if not os.path.exists(excel_path):
        print(f"❌ 找不到 Excel 文件: {excel_path}")
        return

    print("📄 正在读取 Excel 链接列表...")
    try:
        # 假设 Excel 中有一列叫 '官网链接'
        df = pd.read_excel(excel_path)
        urls = df['官网链接'].dropna().tolist()
    except Exception as e:
        print(f"读取 Excel 出错: {e}")
        return

    print(f"🎯 共找到 {len(urls)} 个比赛链接，准备开始抓取...\n")
    print("-" * 40)

    for i, url in enumerate(urls, 1):
        print(f"\n[{i}/{len(urls)}] 处理中...")
        data = scrape_official_website(url)
        if data:
            generate_and_save(data, target_dir)

        # 礼貌爬取，每次请求后暂停 2 秒，防止被官网封锁 IP
        time.sleep(2)


if __name__ == "__main__":
    # 1. 你的目标保存文件夹
    save_directory = r"F:\计设--数据\文章\竞赛基本信息"

    # 2. 你的 Excel 文件路径 (你需要先建一个这样的 Excel 表，把网址放进去)
    # 比如在 F 盘建一个 target_urls.xlsx，里面有一列名为 "官网链接"
    excel_file = r"F:\计设--数据\target_urls.xlsx"

    # 开始批量处理
    batch_process_competitions(excel_file, save_directory)