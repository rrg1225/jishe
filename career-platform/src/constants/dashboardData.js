/** Dashboard 静态配置与初始占位数据（由 Dashboard.vue 迁移） */

export const CATEGORY_MAP = {
  KAOYAN: '考研动态',
  BAOYAN: '保研推免',
  LIUXUE: '海外留学',
  CONTEST: '竞赛信息大厅',
}

export function createHomeData() {
  return [
    {
      title: '🎓 学业深造推荐',
      target: 'matrix',
      items: [],
    },
    {
      title: '💼 校企岗位热榜',
      target: 'job',
      items: [
        {
          title: 'Java/Spring Boot 后端开发高频面试题解析',
          type: 'video',
          typeText: '视频',
          author: '代码大牛',
          color: 'linear-gradient(135deg, #e0c3fc 0%, #8ec5fc 100%)',
        },
        {
          title: '从零到一教你如何打造脱颖而出的前端作品集',
          type: 'article',
          typeText: '文章',
          author: '前端进阶指南',
          color: 'linear-gradient(135deg, #ff9a9e 0%, #fecfef 99%, #fecfef 100%)',
        },
        {
          title: '字节跳动数据分析实习生日常揭秘',
          type: 'video',
          typeText: '视频',
          author: '职场Vlog',
          color: 'linear-gradient(135deg, #fbc2eb 0%, #a6c1ee 100%)',
        },
        {
          title: '产品经理必备：大厂真实需求文档(PRD)长啥样？',
          type: 'article',
          typeText: '文章',
          author: 'PM干货铺',
          color: 'linear-gradient(135deg, #fdcbf1 0%, #fdcbf1 1%, #e6dee9 100%)',
        },
      ],
    },
    {
      title: '🏆 精选竞赛情报',
      target: 'contest',
      items: [],
    },
  ]
}

export function createMatrixSections() {
  return [
    {
      title: '📚 考研最新政策与动态',
      dbCategory: CATEGORY_MAP.KAOYAN,
      items: [],
      page: 0,
      isLast: false,
      loading: false,
      filterType: 'all',
    },
    {
      title: '🏆 保研推免全通关攻略',
      dbCategory: CATEGORY_MAP.BAOYAN,
      items: [],
      page: 0,
      isLast: false,
      loading: false,
      filterType: 'all',
    },
    {
      title: '✈️ 海外留学情报局',
      dbCategory: CATEGORY_MAP.LIUXUE,
      items: [],
      page: 0,
      isLast: false,
      loading: false,
      filterType: 'all',
    },
  ]
}

export const CAROUSEL_SLIDES_INITIAL = [
  {
    image: '/images11/photo-1522071820081-009f0129c71c.jpg',
    title: '2026春季大厂校园招聘全面开启',
    description: '提前批内推 / 简历辅导 / 面试真题全解析，助你拿下心仪 Offer',
    link: '/recruitment',
  },
  {
    image: '/images11/photo-1517245386807-bb43f82c33c4.jpg',
    title: '考研倒计时：冲刺阶段复习规划',
    description: '名师押题 / 真题精讲 / 模拟考试，最后三个月如何高效提分',
    link: '/graduate-exam',
  },
  {
    image: '/images11/photo-1434030216411-0b793f4b4173.jpg',
    title: '留学申请季：全球TOP50院校指南',
    description: '文书写作 / 推荐信 / 面试辅导，一站式留学服务',
    link: '/abroad',
  },
  {
    image: '/images11/photo-1524178232363-1fb2b075b655.jpg',
    title: '互联网大厂实习内推专场',
    description: '字节 / 腾讯 / 阿里 / 美团，50+岗位等你来投',
    link: '/internship',
  },
  {
    image: '/images11/photo-1552664730-d307ca884978.jpg',
    title: '竞赛集训营：冲击国奖',
    description: '互联网+ / 挑战杯 / 数学建模，金牌导师带队',
    link: '/competition',
  },
]

export const HOT_KAOYAN_INITIAL = [
  {
    name: '北京大学',
    tags: ['985', '双一流', '综合类'],
    scoreLine: '356 -> 365 -> 372',
    reExamRatio: '1.2:1',
    applyRatio: '12:1',
    intensityLevel: '高',
  },
  {
    name: '清华大学',
    tags: ['985', '双一流', '理工类'],
    scoreLine: '340 -> 355 -> 360',
    reExamRatio: '1.3:1',
    applyRatio: '9:1',
    intensityLevel: '中等',
  },
  {
    name: '复旦大学',
    tags: ['985', '双一流', '综合类'],
    scoreLine: '335 -> 348 -> 355',
    reExamRatio: '1.2:1',
    applyRatio: '8.5:1',
    intensityLevel: '中等',
  },
  {
    name: '浙江大学',
    tags: ['985', '双一流', '综合类'],
    scoreLine: '360 -> 375 -> 382',
    reExamRatio: '1.5:1',
    applyRatio: '18:1',
    intensityLevel: '极高',
  },
]

export const HOT_BAOYAN_INITIAL = [
  {
    name: '清华大学',
    tags: ['985', '双一流', '理工类'],
    major: '计算机科学与技术',
    timeline: '夏令营:6月上旬 | 预推免:8月底',
    rankReq: '专业排名前3%',
    englishReq: 'CET6 520+',
    officialUrl: 'https://yz.tsinghua.edu.cn/',
  },
  {
    name: '北京航空航天大学',
    tags: ['985', '双一流', '理工类'],
    major: '计算机科学与技术',
    timeline: '夏令营:6月中旬 | 预推免:9月初',
    rankReq: '专业排名前5%',
    englishReq: 'CET6 480+',
    officialUrl: 'http://yzb.buaa.edu.cn/',
  },
  {
    name: '浙江大学',
    tags: ['985', '双一流', '综合类'],
    major: '人工智能',
    timeline: '夏令营:6月中旬 | 预推免:8月下旬',
    rankReq: '专业排名前5%',
    englishReq: 'CET6 500+',
    officialUrl: 'http://grs.zju.edu.cn/yjszs/',
  },
  {
    name: '上海交通大学',
    tags: ['985', '双一流', '理工类'],
    major: '软件工程',
    timeline: '夏令营:7月上旬 | 预推免:9月上旬',
    rankReq: '专业排名前10%',
    englishReq: 'CET6 460+',
    officialUrl: 'https://yzb.sjtu.edu.cn/',
  },
  {
    name: '南京大学',
    tags: ['985', '双一流', '综合类'],
    major: '人工智能',
    timeline: '夏令营:6月底 | 预推免:9月中旬',
    rankReq: '专业排名前10%',
    englishReq: 'CET6 460+',
    officialUrl: 'https://gsec.nju.edu.cn/',
  },
  {
    name: '同济大学',
    tags: ['985', '双一流', '理工类'],
    major: '电子信息',
    timeline: '夏令营:7月中旬 | 预推免:9月初',
    rankReq: '专业排名前8%',
    englishReq: 'CET6 425+',
    officialUrl: 'https://yz.tongji.edu.cn/',
  },
]
