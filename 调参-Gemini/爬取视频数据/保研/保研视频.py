import os
import asyncio
from bilibili_api import search, sync
import yt_dlp

# --- 配置区 ---
KEYWORD = "大学生保研"
MAX_COUNT = 100
MIN_DURATION = 180  # 3分钟
MAX_DURATION = 480  # 8分钟
SAVE_DIR = r"F:\计设--数据\视频\保研"


# -------------

def parse_duration(dur_str):
    """
    更健壮的时长解析函数
    支持 "MM:SS" 或 "HH:MM:SS" 格式，并处理异常情况
    """
    if not dur_str or not isinstance(dur_str, str):
        return 0
    try:
        parts = dur_str.split(':')
        # 过滤掉空字符串并转为整数
        parts = [int(p) for p in parts if p.strip().isdigit()]

        if len(parts) == 2:  # MM:SS
            return parts[0] * 60 + parts[1]
        elif len(parts) == 3:  # HH:MM:SS
            return parts[0] * 3600 + parts[1] * 60 + parts[2]
        return 0
    except (ValueError, IndexError):
        return 0


async def get_video_urls():
    print(f"正在搜索关键词: {KEYWORD}...")
    urls = []
    page = 1

    while len(urls) < MAX_COUNT:
        # 增加异常捕获，防止网络抖动导致搜索中断
        try:
            res = await search.search_by_type(
                keyword=KEYWORD,
                search_type=search.SearchObjectType.VIDEO,
                page=page
            )
        except Exception as e:
            print(f"搜索第 {page} 页失败: {e}")
            break

        v_list = res.get('result', [])
        if not v_list:
            print("没有更多搜索结果了。")
            break

        for v in v_list:
            # 使用更安全的解析方法
            total_sec = parse_duration(v.get('duration', ''))

            if MIN_DURATION <= total_sec <= MAX_DURATION:
                # 获取 BVID
                bvid = v.get('bvid')
                if bvid:
                    video_url = f"https://www.bilibili.com/video/{bvid}"
                    if video_url not in urls:
                        urls.append(video_url)

                if len(urls) >= MAX_COUNT:
                    break

        print(f"进度: 已筛选出 {len(urls)}/{MAX_COUNT} 个视频")
        page += 1
        await asyncio.sleep(1.5)  # 稍微加长延迟，更安全

    return urls


def download_videos(url_list):
    if not os.path.exists(SAVE_DIR):
        os.makedirs(SAVE_DIR)

    ydl_opts = {
        # 🌟 核心修改：强制过滤并下载 H.264 (avc) 编码格式，确保网页端 HTML5 完美播放
        'format': 'bestvideo[vcodec^=avc]+bestaudio/best[vcodec^=avc]/best',

        'outtmpl': os.path.join(SAVE_DIR, '%(title)s.%(ext)s'),
        'cookiefile': 'bili_cookies.txt',  # 确保文件名和刚才导出的一致
        'referer': 'https://www.bilibili.com/',
        'merge_output_format': 'mp4',
        'ignoreerrors': True,
        'noprogress': False,
    }

    with yt_dlp.YoutubeDL(ydl_opts) as ydl:
        ydl.download(url_list)


if __name__ == "__main__":
    # 使用 sync 运行异步任务
    try:
        target_urls = sync(get_video_urls())
        if target_urls:
            print(f"\n--- 筛选完成，准备下载 {len(target_urls)} 个视频 ---")
            download_videos(target_urls)
        else:
            print("未找到符合条件的视频。")
    except Exception as e:
        print(f"程序运行出错: {e}")