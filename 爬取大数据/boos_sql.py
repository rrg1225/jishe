import json
import os

# ================= 配置区 =================
INPUT_JSON_FILE = r'F:\计设--数据\Boss.json'
OUTPUT_SQL_FILE = r'F:\计设--数据\boss_jobs.sql'


# ==========================================

def escape_sql_string(value):
    """处理 SQL 字符串转义，防止特殊字符导致 SQL 语法错误"""
    if value is None or str(value).strip() == "":
        return "NULL"
    safe_value = str(value).replace('\\', '\\\\').replace("'", "''")
    return f"'{safe_value}'"


def generate_sql_file():
    if not os.path.exists(INPUT_JSON_FILE):
        print(f"❌ 找不到 JSON 文件: {INPUT_JSON_FILE}")
        return

    print("⏳ 正在读取 JSON 数据...")
    with open(INPUT_JSON_FILE, 'r', encoding='utf-8') as f:
        jobs_data = json.load(f)

    print(f"📝 开始生成 SQL 语句，共 {len(jobs_data)} 条数据...")

    with open(OUTPUT_SQL_FILE, 'w', encoding='utf-8') as sql_file:
        # 1. 写入最新的建表语句 (加入了 gmt_created 和 gmt_modified)
        create_table_sql = """-- ----------------------------
-- 自动生成的 Boss 直聘数据 SQL 脚本 (符合企业规范版)
-- ----------------------------
        DROP TABLE IF EXISTS `boss_jobs`;
        CREATE TABLE `boss_jobs` \
        ( \
            `id`              int(11) NOT NULL AUTO_INCREMENT COMMENT '主键ID', \
            `tech_stack`      varchar(50)  NOT NULL COMMENT '所属技术栈', \
            `job_name`        varchar(100) NOT NULL COMMENT '岗位名称', \
            `salary`          varchar(50)  NOT NULL COMMENT '薪资', \
            `requirements`    varchar(100) DEFAULT NULL COMMENT '招聘要求', \
            `company_name`    varchar(100) NOT NULL COMMENT '公司名称', \
            `industry`        varchar(100) DEFAULT NULL COMMENT '所属行业', \
            `financing_stage` varchar(50)  DEFAULT NULL COMMENT '融资阶段', \
            `area`            varchar(100) DEFAULT NULL COMMENT '地区', \
            `job_description` longtext COMMENT '岗位具体信息', \
            `gmt_created`     datetime     DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间', \
            `gmt_modified`    datetime     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间', \
            PRIMARY KEY (`id`)
        ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='Boss直聘岗位数据表';

        -- ----------------------------
-- 开始插入数据
-- ---------------------------- \
                           """
        sql_file.write(create_table_sql)

        # 2. 遍历 JSON 数据，生成 INSERT 语句
        for job in jobs_data:
            tech_stack = escape_sql_string(job.get('所属技术栈'))
            job_name = escape_sql_string(job.get('岗位名称'))
            salary = escape_sql_string(job.get('薪资'))
            requirements = escape_sql_string(job.get('招聘要求'))
            company_name = escape_sql_string(job.get('公司名称'))
            industry = escape_sql_string(job.get('所属行业'))
            financing_stage = escape_sql_string(job.get('融资阶段'))
            area = escape_sql_string(job.get('地区'))
            job_description = escape_sql_string(job.get('岗位具体信息'))

            # 💡 注意这里：INSERT 语句里我并没有写 gmt_created 和 gmt_modified
            # 因为我们在建表时设置了 DEFAULT CURRENT_TIMESTAMP，MySQL 插入时会自动填充当前时间！
            insert_sql = (
                f"INSERT INTO `boss_jobs` "
                f"(`tech_stack`, `job_name`, `salary`, `requirements`, `company_name`, `industry`, `financing_stage`, `area`, `job_description`) "
                f"VALUES "
                f"({tech_stack}, {job_name}, {salary}, {requirements}, {company_name}, {industry}, {financing_stage}, {area}, {job_description});\n"
            )

            sql_file.write(insert_sql)

    print(f"✅ 生成完毕！符合规范的 SQL 文件已保存至: {OUTPUT_SQL_FILE}")


if __name__ == '__main__':
    generate_sql_file()