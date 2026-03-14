import requests
import pandas as pd
import time
import random

print("爬虫开始运行...")

# 先测试一下
jobs = []
for i in range(1, 4):
    job = {
        'title': f'前端开发工程师{i}',
        'company': '测试公司',
        'salary': '20k-30k',
        'location': '北京',
        'tags': 'Java,Python'
    }
    jobs.append(job)

df = pd.DataFrame(jobs)
df.to_csv('jobs.csv', index=False, encoding='utf-8-sig')
print("数据已保存到 jobs.csv")
print("爬虫运行结束")