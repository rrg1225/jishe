import os
import time
import random
import requests
from selenium import webdriver
from selenium.webdriver.common.by import By
from selenium.webdriver.common.keys import Keys
from selenium.webdriver.chrome.options import Options
from selenium.webdriver.chrome.service import Service
from urllib.parse import urljoin, quote

# ================= 1. 配置区 =================
SAVE_PATH = r"F:\计设--数据\文章\竞赛"
CHROME_DRIVER_PATH = r"E:\chromedriver-win64\chromedriver-win64\chromedriver.exe"

# 如果某些专题已经抓取完毕，你可以从列表中删除它们，避免重复跑
TOPICS = [
    '数学建模 建模思路', '美赛 报名流程', '互联网+ 商业计划书', '挑战杯 获奖作品',
    '蓝桥杯 刷题指南', '全国大学生电子设计竞赛', 'ACM 竞赛入门', '中国大学生计算机设计大赛',
    'RoboMaster 机器人大赛', '全国大学生智能车竞赛', '大学生工程实践与创新能力大赛',
    '大广赛 获奖作品展', '米兰设计周 参赛指南', '全国大学生英语竞赛 NECCS', '职业规划大赛 演讲',
    '化工设计竞赛', '结构设计大赛', '物理实验竞赛', '周培源力学竞赛'
]
TARGET_COUNT = 30  # 每个竞赛建议爬取数量


# ================= 2. 功能函数 =================

def sanitize_filename(name):
    """清理无效的文件名字符"""
    return "".join([c for c in name if c not in r'\/:*?"<>|']).strip()


def download_img(url, folder):
    """下载图片"""
    try:
        # 增加 headers 模拟浏览器下载图片
        headers = {'User-Agent': 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36'}
        res = requests.get(url, headers=headers, timeout=8)
        if res.status_code == 200:
            ext = url.split('.')[-1].split('?')[0].lower()
            if ext not in ['jpg', 'jpeg', 'png', 'webp', 'gif']: ext = 'jpg'
            filename = f"img_{int(time.time() * 1000)}.{ext}"
            with open(os.path.join(folder, filename), 'wb') as f:
                f.write(res.content)
    except:
        pass


def init_driver():
    """初始化浏览器并增加深度伪装"""
    print("  [Step] 正在配置浏览器参数...")
    chrome_options = Options()
    # 深度伪装：防止被识别为机器人
    chrome_options.add_experimental_option('excludeSwitches', ['enable-automation'])
    chrome_options.add_argument('--disable-blink-features=AutomationControlled')

    try:
        service = Service(executable_path=CHROME_DRIVER_PATH)
        driver = webdriver.Chrome(service=service, options=chrome_options)
        # 执行脚本修改浏览器指纹
        driver.execute_cdp_cmd("Page.addScriptToEvaluateOnNewDocument", {
            "source": """
                Object.defineProperty(navigator, 'webdriver', {
                  get: () => undefined
                })
            """
        })
        print("  [OK] 浏览器启动成功！")
        return driver
    except Exception as e:
        print(f"  [Error] 浏览器启动失败！原因: {e}")
        return None


# ================= 3. 主程序逻辑 =================

def start_mission():
    print("\n" + "=" * 40)
    print("🚀 自动化爬虫任务正式启动")
    print("=" * 40)

    driver = init_driver()
    if not driver: return

    for topic in TOPICS:
        print(f"\n📂 当前专题: 【{topic}】")
        topic_folder = os.path.join(SAVE_PATH, sanitize_filename(topic))
        os.makedirs(topic_folder, exist_ok=True)

        # 直接构造搜索链接，避开首页点击，提高稳定性
        search_url = f"https://cn.bing.com/search?q={quote(topic)}"
        driver.get(search_url)
        time.sleep(random.uniform(4, 6))

        links = []
        # 收集链接
        print(f"  [Step] 正在搜集链接，目标 {TARGET_COUNT} 条...")

        while len(links) < TARGET_COUNT:
            search_results = driver.find_elements(By.CSS_SELECTOR, "h2 a")

            # 自动检测是否被拦截（验证码）
            if not search_results:
                print("  [⚠️ 警告] 未发现结果！可能是触发了验证码。")
                print("  [!] 请在浏览器窗口手动处理验证码或点击搜索。程序等待 15 秒...")
                time.sleep(15)
                search_results = driver.find_elements(By.CSS_SELECTOR, "h2 a")

            for a in search_results:
                url = a.get_attribute("href")
                if url and "http" in url and url not in links:
                    # 排除掉必应和微软自身的链接
                    if "bing.com" not in url and "microsoft.com" not in url:
                        links.append(url)
                if len(links) >= TARGET_COUNT: break

            if len(links) < TARGET_COUNT:
                try:
                    next_page = driver.find_element(By.CSS_SELECTOR, "a[title='下一页']")
                    next_page.click()
                    time.sleep(random.uniform(3, 5))
                except:
                    print("  [Info] 已无更多搜索结果。")
                    break

        print(f"  [OK] 共找到 {len(links)} 条有效链接。")

        # 逐个抓取内容
        for i, link in enumerate(links):
            try:
                print(f"    ({i + 1}/{len(links)}) 抓取中: {link[:40]}...")
                driver.get(link)
                # 模拟真人阅读，随机等待
                time.sleep(random.uniform(4, 7))

                title = sanitize_filename(driver.title)[:40]
                article_dir = os.path.join(topic_folder, f"{i + 1}_{title}")
                os.makedirs(article_dir, exist_ok=True)

                # 提取全文文本
                body_text = driver.find_element(By.TAG_NAME, "body").text
                with open(os.path.join(article_dir, "content.txt"), "w", encoding="utf-8") as f:
                    f.write(f"Source: {link}\n\n{body_text}")

                # 抓取图片
                imgs = driver.find_elements(By.TAG_NAME, "img")
                count = 0
                for img in imgs:
                    src = img.get_attribute("src")
                    if src and src.startswith("http"):
                        download_img(src, article_dir)
                        count += 1
                    if count >= 5: break  # 每篇文章限5张图

                print(f"      - 成功保存：{title}")
            except Exception as e:
                print(f"      - [跳过] 该文章抓取失败")
                continue

        # --- 关键防封点：每个大专题跑完，休息较长时间 ---
        wait_time = random.uniform(15, 25)
        print(f"\n💤 专题【{topic}】处理完毕，强制休息 {int(wait_time)} 秒以防止封禁 IP...")
        time.sleep(wait_time)

    driver.quit()
    print("\n" + "=" * 40)
    print("🎉 恭喜！所有竞赛专题文章已下载完毕。")
    print("=" * 40)


if __name__ == "__main__":
    # 确保保存路径存在
    if not os.path.exists(SAVE_PATH):
        os.makedirs(SAVE_PATH)
    start_mission()