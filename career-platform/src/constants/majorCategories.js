/** 竞赛大厅 — 学科门类静态数据（由 Dashboard 迁移） */
export const MAJOR_CATEGORIES_INITIAL = [
  {
    name: '哲学',
    icon: '📚',
    directions: [
      { name: '哲学类', skills: [ { name: '逻辑学', count: 8 }, { name: '伦理学', count: 6 }, { name: '美学', count: 5 }, { name: '宗教学', count: 4 } ] }
    ]
  },
  {
    name: '经济学',
    icon: '📈',
    directions: [
      { name: '经济学类', skills: [ { name: '微观经济学', count: 18 }, { name: '宏观经济学', count: 15 }, { name: '计量经济学', count: 12 }, { name: '产业经济学', count: 10 } ] },
      { name: '金融学类', skills: [ { name: '金融工程', count: 22 }, { name: '投资学', count: 19 }, { name: '保险学', count: 14 }, { name: '信用管理', count: 9 } ] },
      { name: '经济与贸易', skills: [ { name: '国际贸易', count: 16 }, { name: '国际商务', count: 13 }, { name: '跨境电商', count: 11 } ] }
    ]
  },
  {
    name: '法学',
    icon: '⚖️',
    directions: [
      { name: '法学类', skills: [ { name: '民商法', count: 16 }, { name: '刑法', count: 14 }, { name: '经济法', count: 12 }, { name: '国际法', count: 9 }, { name: '知识产权', count: 8 } ] },
      { name: '政治学类', skills: [ { name: '政治学理论', count: 10 }, { name: '国际政治', count: 8 }, { name: '行政管理', count: 15 } ] },
      { name: '社会学类', skills: [ { name: '社会学', count: 12 }, { name: '社会工作', count: 10 }, { name: '人类学', count: 6 } ] },
      { name: '马克思主义理论', skills: [ { name: '马克思主义原理', count: 9 }, { name: '思想政治教育', count: 14 }, { name: '中共党史', count: 7 } ] }
    ]
  },
  {
    name: '教育学',
    icon: '📖',
    directions: [
      { name: '教育学类', skills: [ { name: '教育学原理', count: 14 }, { name: '课程与教学论', count: 16 }, { name: '教育技术学', count: 12 }, { name: '学前教育', count: 11 }, { name: '特殊教育', count: 7 } ] },
      { name: '体育学类', skills: [ { name: '体育教育', count: 15 }, { name: '运动训练', count: 13 }, { name: '社会体育', count: 10 }, { name: '体育产业', count: 9 } ] }
    ]
  },
  {
    name: '文学',
    icon: '✍️',
    directions: [
      { name: '中国语言文学', skills: [ { name: '古代文学', count: 13 }, { name: '现当代文学', count: 15 }, { name: '语言学', count: 12 }, { name: '创意写作', count: 11 }, { name: '比较文学', count: 8 } ] },
      { name: '外国语言文学', skills: [ { name: '英语', count: 22 }, { name: '日语', count: 16 }, { name: '俄语', count: 9 }, { name: '法语', count: 8 }, { name: '翻译', count: 14 } ] },
      { name: '新闻传播学', skills: [ { name: '新闻学', count: 18 }, { name: '广播电视学', count: 15 }, { name: '广告学', count: 14 }, { name: '网络与新媒体', count: 21 }, { name: '传播学', count: 12 } ] }
    ]
  },
  {
    name: '历史学',
    icon: '🏛️',
    directions: [
      { name: '历史学类', skills: [ { name: '中国史', count: 12 }, { name: '世界史', count: 10 }, { name: '考古学', count: 7 }, { name: '文物与博物馆学', count: 8 }, { name: '文化遗产', count: 6 } ] }
    ]
  },
  {
    name: '理学',
    icon: '🔬',
    directions: [
      { name: '数学', skills: [ { name: '基础数学', count: 24 }, { name: '应用数学', count: 28 }, { name: '计算数学', count: 19 }, { name: '概率论与数理统计', count: 16 }, { name: '运筹学', count: 13 } ] },
      { name: '物理学', skills: [ { name: '理论物理', count: 16 }, { name: '凝聚态物理', count: 18 }, { name: '光学', count: 14 }, { name: '声学', count: 9 }, { name: '无线电物理', count: 11 } ] },
      { name: '化学', skills: [ { name: '无机化学', count: 17 }, { name: '有机化学', count: 19 }, { name: '分析化学', count: 16 }, { name: '物理化学', count: 14 }, { name: '高分子化学', count: 12 } ] },
      { name: '生物学', skills: [ { name: '植物学', count: 14 }, { name: '动物学', count: 13 }, { name: '微生物学', count: 15 }, { name: '生物化学', count: 16 }, { name: '分子生物学', count: 17 }, { name: '生态学', count: 12 } ] },
      { name: '地理学', skills: [ { name: '自然地理', count: 13 }, { name: '人文地理', count: 11 }, { name: '地理信息科学', count: 18 }, { name: '遥感科学与技术', count: 14 } ] },
      { name: '地质学', skills: [ { name: '古生物学', count: 7 }, { name: '构造地质学', count: 9 }, { name: '地球化学', count: 8 } ] }
    ]
  },
  {
    name: '工学',
    icon: '⚙️',
    directions: [
      { name: '电子信息类', skills: [ { name: '通信工程', count: 28 }, { name: '电子信息工程', count: 26 }, { name: '电子科学与技术', count: 23 }, { name: '微电子', count: 19 }, { name: '光电信息', count: 17 }, { name: '集成电路', count: 21 } ] },
      { name: '自动化类', skills: [ { name: '自动化', count: 25 }, { name: '机器人工程', count: 22 }, { name: '轨道交通信号', count: 13 }, { name: '智能科学与技术', count: 24 } ] },
      { name: '计算机类', skills: [ { name: '计算机科学与技术', count: 38 }, { name: '软件工程', count: 35 }, { name: '网络工程', count: 24 }, { name: '信息安全', count: 22 }, { name: '物联网工程', count: 26 }, { name: '数字媒体技术', count: 19 }, { name: '数据科学与大数据', count: 31 }, { name: '人工智能', count: 29 } ] },
      { name: '土木类', skills: [ { name: '土木工程', count: 27 }, { name: '建筑环境与能源', count: 16 }, { name: '给排水科学与工程', count: 14 }, { name: '道路桥梁', count: 18 }, { name: '智能建造', count: 15 } ] },
      { name: '机械类', skills: [ { name: '机械设计制造', count: 26 }, { name: '车辆工程', count: 22 }, { name: '工业设计', count: 17 }, { name: '过程装备', count: 13 }, { name: '智能制造工程', count: 21 } ] },
      { name: '化工与制药', skills: [ { name: '化学工程与工艺', count: 19 }, { name: '制药工程', count: 18 }, { name: '能源化学工程', count: 14 }, { name: '精细化工', count: 12 } ] },
      { name: '材料类', skills: [ { name: '材料科学与工程', count: 21 }, { name: '金属材料工程', count: 14 }, { name: '高分子材料', count: 17 }, { name: '复合材料', count: 11 }, { name: '新能源材料', count: 16 } ] },
      { name: '环境科学与工程', skills: [ { name: '环境工程', count: 20 }, { name: '环境科学', count: 16 }, { name: '环境生态工程', count: 13 }, { name: '资源循环科学', count: 10 } ] },
      { name: '生物医学工程', skills: [ { name: '生物医学工程', count: 15 }, { name: '医学影像技术', count: 13 }, { name: '康复工程', count: 9 } ] },
      { name: '食品科学与工程', skills: [ { name: '食品科学', count: 16 }, { name: '食品质量与安全', count: 14 }, { name: '粮食工程', count: 8 } ] },
      { name: '航空航天类', skills: [ { name: '飞行器设计', count: 12 }, { name: '航空航天工程', count: 14 }, { name: '飞行器动力', count: 11 }, { name: '无人机技术', count: 16 } ] }
    ]
  },
  {
    name: '农学',
    icon: '🌾',
    directions: [
      { name: '植物生产类', skills: [ { name: '农学', count: 15 }, { name: '园艺', count: 13 }, { name: '植物保护', count: 12 }, { name: '种子科学', count: 8 } ] },
      { name: '林学类', skills: [ { name: '林学', count: 12 }, { name: '森林保护', count: 9 }, { name: '园林', count: 16 }, { name: '水土保持', count: 7 } ] },
      { name: '动物生产类', skills: [ { name: '动物科学', count: 13 }, { name: '蚕学', count: 4 }, { name: '蜂学', count: 3 } ] },
      { name: '动物医学类', skills: [ { name: '动物医学', count: 14 }, { name: '动物药学', count: 7 }, { name: '动植物检疫', count: 8 } ] },
      { name: '水产类', skills: [ { name: '水产养殖学', count: 9 }, { name: '海洋渔业科学', count: 7 }, { name: '水族科学与技术', count: 5 } ] },
      { name: '草学类', skills: [ { name: '草业科学', count: 6 }, { name: '草坪管理', count: 4 } ] }
    ]
  },
  {
    name: '医学',
    icon: '⚕️',
    directions: [
      { name: '临床医学类', skills: [ { name: '临床医学', count: 22 }, { name: '麻醉学', count: 12 }, { name: '医学影像学', count: 15 }, { name: '眼视光医学', count: 8 }, { name: '精神医学', count: 7 }, { name: '放射医学', count: 6 } ] },
      { name: '口腔医学', skills: [ { name: '口腔医学', count: 14 }, { name: '口腔正畸', count: 8 } ] },
      { name: '公共卫生与预防医学', skills: [ { name: '预防医学', count: 16 }, { name: '流行病学', count: 12 }, { name: '卫生检验', count: 9 }, { name: '全球健康学', count: 5 } ] },
      { name: '中医学类', skills: [ { name: '中医学', count: 15 }, { name: '针灸推拿学', count: 13 }, { name: '中西医临床', count: 11 }, { name: '中药学', count: 14 } ] },
      { name: '药学类', skills: [ { name: '药学', count: 18 }, { name: '药物制剂', count: 12 }, { name: '临床药学', count: 14 }, { name: '药理学', count: 11 }, { name: '药物分析', count: 10 } ] },
      { name: '医学技术类', skills: [ { name: '医学检验技术', count: 14 }, { name: '康复治疗学', count: 13 }, { name: '听力与言语康复', count: 6 } ] },
      { name: '护理学类', skills: [ { name: '护理学', count: 19 }, { name: '助产学', count: 8 } ] }
    ]
  },
  {
    name: '管理学',
    icon: '📊',
    directions: [
      { name: '管理科学与工程', skills: [ { name: '信息管理与信息系统', count: 21 }, { name: '工程管理', count: 18 }, { name: '大数据管理与应用', count: 23 }, { name: '工程造价', count: 16 } ] },
      { name: '工商管理类', skills: [ { name: '工商管理', count: 24 }, { name: '市场营销', count: 22 }, { name: '会计学', count: 28 }, { name: '财务管理', count: 25 }, { name: '人力资源管理', count: 19 }, { name: '审计学', count: 21 }, { name: '资产评估', count: 12 } ] },
      { name: '农业经济管理', skills: [ { name: '农林经济管理', count: 11 }, { name: '农村区域发展', count: 7 } ] },
      { name: '公共管理类', skills: [ { name: '行政管理', count: 18 }, { name: '公共事业管理', count: 15 }, { name: '劳动与社会保障', count: 12 }, { name: '土地资源管理', count: 11 }, { name: '城市管理', count: 10 } ] },
      { name: '图书情报与档案', skills: [ { name: '图书馆学', count: 7 }, { name: '档案学', count: 6 }, { name: '信息资源管理', count: 9 } ] },
      { name: '物流管理与工程', skills: [ { name: '物流管理', count: 17 }, { name: '物流工程', count: 14 }, { name: '供应链管理', count: 19 } ] },
      { name: '电子商务类', skills: [ { name: '电子商务', count: 21 }, { name: '跨境电商', count: 16 } ] },
      { name: '旅游管理类', skills: [ { name: '旅游管理', count: 15 }, { name: '酒店管理', count: 12 }, { name: '会展经济与管理', count: 9 } ] }
    ]
  },
  {
    name: '艺术学',
    icon: '🎨',
    directions: [
      { name: '艺术学理论', skills: [ { name: '艺术史论', count: 8 }, { name: '艺术管理', count: 10 } ] },
      { name: '音乐与舞蹈学', skills: [ { name: '音乐表演', count: 16 }, { name: '音乐学', count: 14 }, { name: '作曲与作曲技术', count: 7 }, { name: '舞蹈表演', count: 12 }, { name: '舞蹈编导', count: 8 } ] },
      { name: '戏剧与影视学', skills: [ { name: '表演', count: 11 }, { name: '戏剧影视文学', count: 10 }, { name: '广播电视编导', count: 15 }, { name: '播音与主持', count: 14 }, { name: '动画', count: 16 }, { name: '影视摄影与制作', count: 12 } ] },
      { name: '美术学', skills: [ { name: '绘画', count: 15 }, { name: '雕塑', count: 7 }, { name: '美术学', count: 12 }, { name: '书法学', count: 9 }, { name: '中国画', count: 10 } ] },
      { name: '设计学', skills: [ { name: '视觉传达设计', count: 24 }, { name: '环境设计', count: 22 }, { name: '产品设计', count: 20 }, { name: '服装与服饰设计', count: 16 }, { name: '数字媒体艺术', count: 23 }, { name: '艺术与科技', count: 14 }, { name: '陶瓷艺术设计', count: 7 } ] }
    ]
  },
  {
    name: '交叉学科',
    icon: '🔄',
    directions: [
      { name: '集成电路科学与工程', skills: [ { name: '芯片设计', count: 14 }, { name: '工艺集成', count: 9 } ] },
      { name: '国家安全学', skills: [ { name: '国家安全战略', count: 6 }, { name: '网络安全', count: 15 } ] },
      { name: '设计学', skills: [ { name: '智能交互设计', count: 12 }, { name: '服务设计', count: 8 } ] },
      { name: '遥感科学与技术', skills: [ { name: '遥感应用', count: 11 }, { name: '地理信息工程', count: 13 } ] }
    ]
  }
]
