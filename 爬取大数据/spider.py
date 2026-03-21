import requests
import time
import random

# 注意：由于你的 Java (IDEA) 是跑在本地电脑上的，所以这里用 localhost:8080 即可
BACKEND_API_URL = "http://localhost:8080/api/university/updateData"

# 我们前端下拉框里设定的四大热门专业
MAJORS = ["计算机科学与技术", "软件工程", "人工智能", "电子信息"]


# ================= 核心：500所高校档案生成器 =================
def init_500_schools():
    database = []
    # 1. 预设中国最顶尖的 30+ 所名校
    real_names = [
        "北京大学", "清华大学", "浙江大学", "复旦大学", "上海交通大学", "南京大学",
        "中国科学技术大学", "哈尔滨工业大学", "西安交通大学", "华中科技大学", "武汉大学",
        "中山大学", "厦门大学", "四川大学", "电子科技大学", "北京邮电大学", "北京航空航天大学",
        "同济大学", "东南大学", "华南理工大学", "国防科技大学", "中国人民大学", "北京师范大学",
        "南开大学", "天津大学", "大连理工大学", "吉林大学", "山东大学", "中国海洋大学",
        "中南大学", "湖南大学", "重庆大学", "西北工业大学", "兰州大学", "东北大学"
    ]

    # 🌟 核心修改点：让这些顶尖名校，每个学校都拥有全部 4 个热门专业！
    for name in real_names:
        is_science = "理工" in name or "科技" in name or "工业" in name or "航空" in name
        for major in MAJORS:  # 遍历 4 个专业，全部添加进数据库
            database.append({
                "name": name,
                "tags": ["985", "双一流", "理工类" if is_science else "综合类"],
                "major": major,
                "base_score": random.randint(340, 380),
                "url": f"https://yz.chsi.com.cn/sch/{name}"
            })

    # 2. 词库引擎：组合生成剩下的 460+ 所大学
    provinces = ["北京", "上海", "天津", "重庆", "河北", "山西", "辽宁", "吉林", "黑龙江",
                 "江苏", "浙江", "安徽", "福建", "江西", "山东", "河南", "湖北", "湖南",
                 "广东", "海南", "四川", "贵州", "云南", "陕西", "甘肃", "青海", "内蒙古", "广西", "宁夏", "新疆"]
    types = ["综合", "理工", "科技", "工业", "工程", "师范", "农业", "林业", "医药",
             "中医药", "财经", "政法", "交通", "建筑", "电子", "邮电", "航空", "海洋", "地质", "外国语"]

    print("⏳ 正在由 AI 引擎生成 500 所全国高校数字档案...")
    while len(database) < 500:
        prov = random.choice(provinces)
        t = random.choice(types)
        suffix = random.choice(["大学", "学院"])
        name = f"{prov}{t}{suffix}"

        # 确保大学名字不重复
        if not any(s['name'] == name for s in database):
            tags = []
            if random.random() < 0.15: tags.append("211")
            if random.random() < 0.10: tags.append("双一流")
            tags.append(f"{t}类")

            database.append({
                "name": name,
                "tags": tags,
                "major": random.choice(MAJORS),  # 🌟 给普通院校也固定好专业
                "base_score": random.randint(270, 330),  # 普通院校基准分数稍低
                "url": f"https://yz.chsi.com.cn/sch/{name}"
            })

    return database


# 脚本启动时，立刻生成这 500 所大学（全局缓存），确保每次刷新只有分数动，名字和专业不动
SCHOOL_DB = init_500_schools()


# ================= 实时流：计算大盘波动 =================
def generate_massive_data():
    scraped_data = []
    # 基础时间波动率
    time_fluctuation = int(time.time() / 5) % 8

    for school in SCHOOL_DB:
        base = school["base_score"]
        # 给每所学校加入独立的随机波动，这样大盘看起来才是千变万化的
        current_score = base + time_fluctuation + random.randint(-3, 3)

        year1 = base - random.randint(3, 8)
        year2 = base + random.randint(1, 5)

        # 动态计算报录比和激烈程度
        is_top = "985" in school["tags"] or "211" in school["tags"]
        apply_ratio_num = random.randint(6, 20) if is_top else random.randint(2, 8)

        if apply_ratio_num >= 12:
            intensity = "极高"
        elif apply_ratio_num >= 7:
            intensity = "高"
        else:
            intensity = "中等"

        scraped_data.append({
            "name": school["name"],
            "tags": school["tags"],
            "major": school["major"],  # 🌟 将固定好的专业传给前端
            "scoreLine": f"{year1} -> {year2} -> {current_score}",
            "reExamRatio": f"1.{random.randint(1, 5)}:1",
            "applyRatio": f"{apply_ratio_num}:1",
            "intensityLevel": intensity,
            "officialUrl": school["url"]
        })

    return scraped_data


def push_to_backend(data):
    if not data:
        return
    try:
        # 发送包含 500 个对象的巨大 JSON 数组
        response = requests.post(BACKEND_API_URL, json=data, timeout=8)
        if response.status_code == 200:
            print(f"[{time.strftime('%H:%M:%S')}] 🚀 成功！500所带【专业分类】的高校动态数据已推送到看板！")
        else:
            print(f"推送失败，被后端拒绝，状态码：{response.status_code}")
    except Exception as e:
        print(f"推送后端失败 (请检查 Java 是否启动): {e}")


if __name__ == "__main__":
    print("==================================================")
    print("   大数据实时演算引擎已启动 (目标：500所高校+专业划分)")
    print("==================================================")
    while True:
        # 1. 瞬间演算 500 所大学的新数据
        data = generate_massive_data()
        # 2. 推送给 Java 和 Redis
        push_to_backend(data)
        # 3. 每隔 6 秒全网刷新一次分数波动
        time.sleep(6)