from DrissionPage import ChromiumPage, ChromiumOptions
import json
import time
import random
import re
import os

# ================= 配置区 =================
KEYWORDS = [
    "Java开发工程师", "Java架构师", "Python后端开发",
    "Go开发工程师", "C++后端开发", "Node.js开发", "全栈工程师",
    "嵌入式","硬件工程师","量化研究员","量化开发","产品经理","产品总监",
    "市场营销","人事","生产经理","店长","导购","艺术教师","语数英教师",
    "施工员","监理工程师","物流专员","物流经理","调度","内科医生","外科医生",
    "牙医","汽车设计","动力工程师","采购专员","采购经理","主播","带货主播","前台接待",
    "酒店管理","平面设计","包装设计","品牌设计","新能源","风能","核能","光电","律师",
    "咨询顾问","人力咨询","CEO","总经理","COO"
]

PAGES_PER_KEYWORD = 1
OUTPUT_FILE = r'F:\计设--数据\Boss.json'
BROWSER_PATH = r'C:\Program Files\Google\Chrome\Application\chrome.exe'


# ==========================================

def decode_boss_font(text):
    if not text: return text
    mapping = {
        '\ue031': '0', '\ue032': '1', '\ue033': '2', '\ue034': '3', '\ue035': '4',
        '\ue036': '5', '\ue037': '6', '\ue038': '7', '\ue039': '8', '\ue03a': '9'
    }
    for k, v in mapping.items():
        text = text.replace(k, v)
    return text


def clean_detail_text(text):
    if not text or text == "提取详情失败": return text
    text = decode_boss_font(text)
    stopwords = ["收藏立即沟通", "举报", "微信扫码分享", "去App", "与BOSS随时沟通", "前往App", "点击查看地图",
                 "查看更多信息", "来自BOSS直聘", "BOSS直聘", "直聘", "kanzhun", "boss"]
    for word in stopwords:
        text = re.sub(word, '', text, flags=re.IGNORECASE)
    text = re.sub(r'\n+', '\n', text).strip()
    if "职位描述" in text:
        text = "职位描述" + text.split("职位描述")[-1]
    return text


def save_data(data):
    with open(OUTPUT_FILE, 'w', encoding='utf-8') as f:
        json.dump(data, f, ensure_ascii=False, indent=4)


def main():
    target_dir = os.path.dirname(OUTPUT_FILE)
    if not os.path.exists(target_dir):
        os.makedirs(target_dir)

    print("🚀 启动浏览器...")
    co = ChromiumOptions()
    co.set_browser_path(BROWSER_PATH)
    page = ChromiumPage(co)
    all_jobs = []

    print("\n⚡ 全自动搜索全国模式已启动...")

    for kw in KEYWORDS:
        print(f"\n\n{'=' * 20} 🎯 正在全国范围搜索: 【{kw}】 {'=' * 20}")

        try:
            target_url = f"https://www.zhipin.com/web/geek/job?query={kw}&city=100010000"
            page.get(target_url)
            time.sleep(random.uniform(4, 6))
            target_tab = page.latest_tab
        except Exception as e:
            print(f"❌ 搜索操作执行异常，跳过 {kw}。原因: {e}")
            continue

        for page_num in range(1, PAGES_PER_KEYWORD + 1):
            print(f"\n--- 正在抓取 【{kw}】 的第 {page_num} 页 ---")

            if not target_tab.wait.eles_loaded('.job-name', timeout=8):
                print("⚠️ 找不到职位名称，可能触发验证或无数据，跳过当前页。")
                break

            job_name_eles = target_tab.eles('.job-name')

            for index, name_ele in enumerate(job_name_eles):
                try:
                    job_name = name_ele.text
                    if not job_name: continue

                    # 锁定整个岗位卡片
                    card = name_ele.parent('css:[class*="job-card"]') or name_ele.parent('tag:li')
                    if not card: continue

                    # 1. 精准抓取薪资
                    salary_ele = card.ele('css:[class*="salary"]', timeout=0.2)
                    salary = decode_boss_font(salary_ele.text if salary_ele else "未知")

                    # 2. 精准抓取地区
                    area_ele = card.ele('css:[class*="job-area"]', timeout=0.2)
                    area = area_ele.text if area_ele else "未知地区"

                    # 3. ⭐️ 新增：精准抓取招聘要求（经验 + 学历）
                    requirements = "未知"
                    # Boss直聘的经验和学历通常放在 ul.tag-list 里
                    tag_list = card.ele('css:[class*="tag-list"]', timeout=0.2)
                    if tag_list:
                        tags = tag_list.texts()
                        # 过滤掉数组里的空文本、薪资、以及地区，剩下的就是经验和学历
                        valid_tags = [t for t in tags if t and 'K' not in t and '薪' not in t and t != area]
                        if valid_tags:
                            requirements = " | ".join(valid_tags)  # 拼接成比如 "3-5年 | 本科"

                    # 4. 精准抓取公司名称
                    company_name = "未知"
                    comp_ele = card.ele('css:[class*="company-name"]', timeout=0.2)
                    if comp_ele:
                        company_name = comp_ele.text
                    else:
                        # 兜底：公司名几乎一定在 h3 标签里
                        h3_ele = card.ele('tag:h3', timeout=0.2)
                        if h3_ele:
                            company_name = h3_ele.text

                    print(
                        f"✅ 提取: 【{job_name}】 | 薪资: {salary} | 地区: {area} | 要求: {requirements} | 公司: {company_name}")

                    # 5. 点击进入详情页抓取岗位描述
                    try:
                        name_ele.click()
                    except:
                        name_ele.click(by_js=True)
                    time.sleep(random.uniform(2.5, 4.0))

                    job_description = "提取详情失败"
                    selectors = ['.job-detail-section', '.job-sec-text', 'css:[class*="job-detail"]']
                    for sel in selectors:
                        desc_ele = target_tab.ele(sel, timeout=1)
                        if desc_ele and desc_ele.text and len(desc_ele.text) > 20:
                            job_description = desc_ele.text
                            break

                    clean_description = clean_detail_text(job_description)

                    # 组装最终数据
                    all_jobs.append({
                        '所属技术栈': kw,
                        '岗位名称': job_name,
                        '薪资': salary,
                        '地区': area,
                        '招聘要求': requirements,  # 新增字段存入 JSON
                        '公司名称': company_name,
                        '岗位具体信息': clean_description
                    })

                    save_data(all_jobs)

                    time.sleep(random.uniform(1.0, 2.0))
                except Exception as e:
                    continue

            if page_num < PAGES_PER_KEYWORD:
                try:
                    next_btn = target_tab.ele('text=下一页', timeout=3)
                    if next_btn and not ('disabled' in next_btn.attr('class') or next_btn.attr('disabled')):
                        next_btn.click(by_js=True)
                        time.sleep(random.uniform(5, 8))
                    else:
                        break
                except Exception:
                    break

    print(f"\n🎉 完美抓取完成！数据已包含招聘要求，保存至 {OUTPUT_FILE}")


if __name__ == '__main__':
    main()