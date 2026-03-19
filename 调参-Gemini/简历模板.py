import os
import requests
from bs4 import BeautifulSoup
import time

# 目标保存路径
SAVE_DIR = r"F:\计设--数据\简历模板"

# 确保目录存在
if not os.path.exists(SAVE_DIR):
    os.makedirs(SAVE_DIR)
    print(f"已创建目录: {SAVE_DIR}")

# 请求头，模拟真实浏览器，防止被直接拦截
HEADERS = {
    "User-Agent": "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36",
    "Accept": "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,*/*;q=0.8",
    # 如果分析出需要登录才能看到完整列表，可以在这里加上你的 Cookie
    # "Cookie": "your_wps_cookie_here"
}


def download_file(url, filename):
    """下载文件（图片或文档）到指定目录"""
    try:
        response = requests.get(url, headers=HEADERS, timeout=10)
        response.raise_for_status()

        filepath = os.path.join(SAVE_DIR, filename)
        with open(filepath, 'wb') as f:
            f.write(response.content)
        print(f"成功下载: {filename}")
    except Exception as e:
        print(f"下载失败 {url}: {e}")


def scrape_wps_templates(page_url):
    """解析页面并提取模板信息"""
    try:
        response = requests.get(page_url, headers=HEADERS, timeout=10)
        response.raise_for_status()

        soup = BeautifulSoup(response.text, 'html.parser')

        # ⚠️ 注意：这里的 class name ('template-item', 'img-cover' 等) 是假设的。
        # 你需要在浏览器中按 F12 (开发者工具) 检查 WPS 页面的真实 DOM 结构并替换它们。
        items = soup.find_all('div', class_='template-item')

        for index, item in enumerate(items):
            # 提取模板名称
            title_tag = item.find('a', class_='template-title')
            title = title_tag.text.strip() if title_tag else f"模板_{index}"

            # 提取预览图 URL
            img_tag = item.find('img')
            if img_tag and img_tag.get('src'):
                img_url = img_tag.get('src')
                # 处理可能省略协议头的 URL (如 //example.com/img.jpg)
                if img_url.startswith('//'):
                    img_url = 'https:' + img_url

                # 下载预览图
                file_extension = img_url.split('.')[-1].split('?')[0]  # 简单获取后缀，去除参数
                if len(file_extension) > 4:
                    file_extension = 'jpg'
                filename = f"{title}.{file_extension}"

                download_file(img_url, filename)
                time.sleep(1)  # 礼貌爬取，防止请求过快被封 IP

    except Exception as e:
        print(f"解析页面失败: {e}")


if __name__ == "__main__":
    # 替换为你要抓取的 WPS 简历列表页 URL
    # 例如：https://moban.wps.cn/resume/
    target_url = "https://moban.wps.cn/resume/"
    print("开始执行爬虫任务...")
    scrape_wps_templates(target_url)
    print("任务执行完毕，请检查文件夹。")