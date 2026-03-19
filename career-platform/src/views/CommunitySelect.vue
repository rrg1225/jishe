<template>
  <div class="community-select-page">
    <header class="page-header">
      <div class="header-content">
        <h2>发现你感兴趣的圈子</h2>
        <p>加入专属行业/学业社区，与同行者一起交流成长</p>
      </div>
      <button class="back-btn" @click="router.back()">← 返回首页</button>
    </header>

    <div class="main-layout">
      <aside class="category-menu">
        <div 
          v-for="(category, index) in categoryData" 
          :key="index"
          class="menu-item"
          :class="{ active: activeCategoryIndex === index }"
          @click="activeCategoryIndex = index"
        >
          <span class="icon">{{ category.icon }}</span>
          <span>{{ category.mainName }}</span>
        </div>
      </aside>

      <main class="community-grid">
        <div 
          v-for="community in currentCommunities" 
          :key="community.id" 
          class="community-card"
          @click="goToCommunity(community.id)"
          :style="{ '--card-color': community.color }" 
        >
          <div class="card-top">
            <div class="avatar" :style="{ background: community.color }">{{ community.name.substring(0,1) }}</div>
            <div class="info">
              <h3>{{ community.name }}</h3>
              <span class="member-count">👥 {{ community.members }} 人已加入</span>
            </div>
          </div>
          <p class="desc">{{ community.desc }}</p>
          <button class="enter-btn">进入社区</button>
        </div>
      </main>
    </div>
  </div>
</template>

<script setup>
import { ref, computed } from 'vue'
import { useRouter } from 'vue-router'

const router = useRouter()
const activeCategoryIndex = ref(0)

// 模拟分类和社区数据（全学科覆盖，每个类别至少6个卡片）
const categoryData = ref([
  {
    mainName: '校园专属', icon: '🎓',
    list: [
      { id: 101, name: '考研上岸交流圈', desc: '院校选择、专业课复习、政治英语打卡。', members: '12.5k', color: '#f59e0b' },
      { id: 102, name: '保研推免情报局', desc: '夏令营、九推、联系导师经验分享。', members: '8.2k', color: '#10b981' },
      { id: 103, name: '大厂实习内推', desc: '日常实习、暑期实习面经与内推码。', members: '24k', color: '#3b82f6' },
      { id: 104, name: '竞赛科研组队', desc: '互联网+、挑战杯、大创项目找队友。', members: '6.8k', color: '#8b5cf6' },
      { id: 105, name: '选调生/考公', desc: '选调生备考、公务员考试经验分享。', members: '7.3k', color: '#f97316' },
      { id: 106, name: '留学申请', desc: '文书写作、选校定位、语言考试。', members: '5.6k', color: '#ec4899' }
    ]
  },
  {
    mainName: 'IT / 互联网', icon: '💻',
    list: [
      { id: 201, name: '前端开发进阶', desc: 'Vue, React, JS/TS 技术交流与八股文', members: '15k', color: '#0ea5e9' },
      { id: 202, name: '后端架构师', desc: 'Java, Go, 微服务，高并发解决方案。', members: '18k', color: '#6366f1' },
      { id: 203, name: 'AI / 算法', desc: '大模型、深度学习、PyTorch前沿追踪。', members: '9k', color: '#8b5cf6' },
      { id: 204, name: '产品经理 PM', desc: '需求分析、原型设计、交互体验探讨。', members: '11k', color: '#ec4899' },
      { id: 205, name: '运维/测试', desc: 'CI/CD、自动化测试、K8s 实战经验', members: '7.3k', color: '#14b8a6' },
      { id: 206, name: '数据科学', desc: 'SQL、Python、数据分析、机器学习', members: '12k', color: '#f97316' }
    ]
  },
  {
    mainName: '金融 / 经济', icon: '💰',
    list: [
      { id: 301, name: '投行/券商交流', desc: 'CPA, CFA备考，投行研究学习。', members: '6.5k', color: '#ef4444' },
      { id: 302, name: '银行春招秋招', desc: '四大行及股份制银行笔试面试解析。', members: '9.2k', color: '#f97316' },
      { id: 303, name: '量化投资', desc: '量化策略、回测框架、高频交易', members: '4.8k', color: '#8b5cf6' },
      { id: 304, name: '保险精算', desc: '精算师考试、保险产品设计', members: '3.2k', color: '#0ea5e9' },
      { id: 305, name: '会计/审计', desc: 'CPA备考、四大工作经验分享', members: '8.7k', color: '#10b981' },
      { id: 306, name: '经济学研究', desc: '宏观微观经济学、学术论文交流', members: '4.1k', color: '#6366f1' }
    ]
  },
  {
    mainName: '教育 / 传媒', icon: '📚',
    list: [
      { id: 401, name: '教师编/教资', desc: '教师资格证、各地教师编制考试。', members: '14k', color: '#14b8a6' },
      { id: 402, name: '新媒体运营', desc: '小红书、抖音账号起号与内容变现。', members: '7.8k', color: '#f97316' },
      { id: 403, name: '新闻/传播', desc: '新闻实务、传播理论、媒体实习', members: '5.6k', color: '#ec4899' },
      { id: 404, name: '出版/编辑', desc: '图书出版、编辑校对、版权运营', members: '3.1k', color: '#6366f1' },
      { id: 405, name: '教育技术', desc: '在线教育、教育产品设计', members: '4.2k', color: '#0ea5e9' },
      { id: 406, name: '播音/主持', desc: '播音艺考、主持人技巧交流', members: '3.8k', color: '#8b5cf6' }
    ]
  },
  {
    mainName: '土木 / 建筑', icon: '🏗️',
    list: [
      { id: 501, name: '结构设计', desc: 'PKPM、YJK、结构力学学习交流', members: '5.2k', color: '#f97316' },
      { id: 502, name: 'BIM 技术', desc: 'Revit、Navisworks、BIM 等级考试', members: '4.3k', color: '#0ea5e9' },
      { id: 503, name: '工程造价', desc: '广联达、定额计价、成本控制', members: '6.1k', color: '#10b981' },
      { id: 504, name: '施工技术', desc: '现场施工、安全管理、施工组织', members: '3.8k', color: '#8b5cf6' },
      { id: 505, name: '建筑学', desc: '建筑设计、建筑历史、居住区规划', members: '4.7k', color: '#ec4899' },
      { id: 506, name: '城市规划', desc: '城乡规划、城市设计、交通规划', members: '3.2k', color: '#6366f1' }
    ]
  },
  {
    mainName: '环境 / 化工', icon: '🌱',
    list: [
      { id: 601, name: '环境工程', desc: '水处理、大气治理、固废资源化', members: '4.1k', color: '#10b981' },
      { id: 602, name: '化学工程', desc: '化工原理、工艺流程、Aspen Plus', members: '3.5k', color: '#f97316' },
      { id: 603, name: '材料科学', desc: '金属/高分子/复合材料研究', members: '4.8k', color: '#8b5cf6' },
      { id: 604, name: '新能源', desc: '锂电池、光伏、氢能技术交流', members: '5.3k', color: '#0ea5e9' },
      { id: 605, name: '环境科学', desc: '生态学、环境监测、环评', members: '3.2k', color: '#6366f1' },
      { id: 606, name: '食品科学', desc: '食品安全、食品研发、检验检测', members: '3.9k', color: '#ec4899' }
    ]
  },
  {
    mainName: '艺术 / 设计', icon: '🎨',
    list: [
      { id: 701, name: 'UI/UX 设计', desc: 'Figma、Sketch、交互设计作品集', members: '8.2k', color: '#ec4899' },
      { id: 702, name: '平面设计', desc: 'PS、AI、品牌设计、海报制作', members: '9.1k', color: '#f97316' },
      { id: 703, name: '服装设计', desc: '时装画、打版、服装工艺', members: '3.9k', color: '#8b5cf6' },
      { id: 704, name: '动画/游戏美术', desc: '原画、建模、Unity/UE 美术', members: '5.7k', color: '#0ea5e9' },
      { id: 705, name: '室内设计', desc: '家装工装、软装搭配、施工图', members: '4.5k', color: '#10b981' },
      { id: 706, name: '摄影/后期', desc: '摄影技巧、后期修图、设备交流', members: '4.2k', color: '#6366f1' }
    ]
  },
  {
    mainName: '机械 / 车辆', icon: '⚙️',
    list: [
      { id: 801, name: '机械设计', desc: 'SolidWorks、CAD、有限元分析', members: '6.4k', color: '#6366f1' },
      { id: 802, name: '车辆工程', desc: '新能源汽车、底盘设计、智能网联', members: '4.7k', color: '#10b981' },
      { id: 803, name: '机器人', desc: 'ROS、运动控制、机器视觉', members: '5.2k', color: '#8b5cf6' },
      { id: 804, name: '自动化', desc: 'PLC、工业控制、嵌入式', members: '6.8k', color: '#f97316' },
      { id: 805, name: '航空航天', desc: '飞行器设计、航空发动机、无人机', members: '3.1k', color: '#0ea5e9' },
      { id: 806, name: '船舶工程', desc: '船舶设计、轮机工程、海洋平台', members: '2.4k', color: '#ec4899' }
    ]
  },
  {
    mainName: '销售 / 商务', icon: '📈',
    list: [
      { id: 901, name: '大客户销售', desc: 'B2B销售技巧、客户关系管理', members: '5.6k', color: '#f97316' },
      { id: 902, name: '跨境电商', desc: '亚马逊、TikTok Shop、独立站', members: '7.2k', color: '#0ea5e9' },
      { id: 903, name: '市场营销', desc: '品牌策划、市场调研、营销策略', members: '8.4k', color: '#ec4899' },
      { id: 904, name: '商务拓展', desc: '渠道合作、商务谈判、资源整合', members: '4.1k', color: '#8b5cf6' },
      { id: 905, name: '品牌管理', desc: '品牌建设、公关传播、危机管理', members: '3.5k', color: '#10b981' },
      { id: 906, name: '直播带货', desc: '主播技巧、直播运营、选品经验', members: '5.9k', color: '#6366f1' }
    ]
  },
  {
    mainName: '法律 / 文学 / 历史', icon: '⚖️',
    list: [
      { id: 1001, name: '法学交流', desc: '法考、法硕、法律实务探讨', members: '6.3k', color: '#8b5cf6' },
      { id: 1002, name: '文学创作', desc: '小说写作、诗歌散文、投稿经验', members: '4.5k', color: '#ec4899' },
      { id: 1003, name: '历史研究', desc: '中国史、世界史、考古文博', members: '3.2k', color: '#f97316' },
      { id: 1004, name: '哲学思辨', desc: '中西方哲学、逻辑学、伦理学', members: '2.8k', color: '#0ea5e9' },
      { id: 1005, name: '汉语言文学', desc: '古代文学、现当代文学、语言学', members: '4.1k', color: '#10b981' },
      { id: 1006, name: '外语学习', desc: '英语、日语、韩语、小语种学习', members: '7.2k', color: '#6366f1' }
    ]
  },
  {
    mainName: '农学 / 基础学科', icon: '🌾',
    list: [
      { id: 1101, name: '农学/园艺', desc: '作物栽培、植物保护、园林设计', members: '3.7k', color: '#10b981' },
      { id: 1102, name: '数学物理', desc: '数模竞赛、物理竞赛、科研交流', members: '5.1k', color: '#6366f1' },
      { id: 1103, name: '化学/生物', desc: '实验技术、考研、科研心得', members: '4.4k', color: '#f97316' },
      { id: 1104, name: '地理/地质', desc: 'GIS、遥感、地质勘探', members: '2.9k', color: '#8b5cf6' },
      { id: 1105, name: '林学/生态', desc: '森林培育、生态保护、园林', members: '2.5k', color: '#0ea5e9' },
      { id: 1106, name: '统计学', desc: '数据分析、统计建模、SPSS/R', members: '4.8k', color: '#ec4899' }
    ]
  },
  {
    mainName: '医学 / 药学', icon: '⚕️',
    list: [
      { id: 1201, name: '临床医学', desc: '执医考试、考研、临床经验', members: '8.5k', color: '#0ea5e9' },
      { id: 1202, name: '护理学', desc: '护士资格证、护理考研、工作交流', members: '7.2k', color: '#ec4899' },
      { id: 1203, name: '药学/中药学', desc: '药理学、药剂学、执业药师', members: '4.8k', color: '#10b981' },
      { id: 1204, name: '公共卫生', desc: '流行病学、卫生统计、疾控', members: '3.3k', color: '#f97316' },
      { id: 1205, name: '口腔医学', desc: '口腔考研、临床技术、材料', members: '3.9k', color: '#8b5cf6' },
      { id: 1206, name: '医学影像', desc: '影像诊断、超声、放射技术', members: '2.8k', color: '#6366f1' }
    ]
  },
  {
    mainName: '体育 / 艺术体育', icon: '⚽',
    list: [
      { id: 1301, name: '体育教育', desc: '教资、考研、运动训练', members: '4.2k', color: '#f97316' },
      { id: 1302, name: '运动康复', desc: '康复治疗、运动损伤防护', members: '2.9k', color: '#10b981' },
      { id: 1303, name: '舞蹈/表演', desc: '艺考、专业训练、演出机会', members: '3.5k', color: '#ec4899' },
      { id: 1304, name: '运动训练', desc: '体能训练、竞赛指导、教练员', members: '2.8k', color: '#8b5cf6' },
      { id: 1305, name: '体育管理', desc: '赛事运营、场馆管理、体育经济', members: '2.1k', color: '#0ea5e9' },
      { id: 1306, name: '电竞/游戏', desc: '电竞选手、赛事解说、游戏运营', members: '5.6k', color: '#6366f1' }
    ]
  }
])

const currentCommunities = computed(() => {
  return categoryData.value[activeCategoryIndex.value].list
})

const goToCommunity = (id) => {
  router.push(`/community/${id}`)
}
</script>

<style scoped>
/* ================= 页面整体与背景 ================= */
.community-select-page {
  min-height: 100vh;
  position: relative;
  padding: 30px;
  background: #f8fafc;  /* 底部纯色背景 */
}

/* 上半部分背景图片层 */
.community-select-page::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 60%;  /* 只占上半部分60%高度 */
  background: url('/images11/premium_vector-1738825719228-b6495015fdb1.avif') no-repeat center top;
  background-size: cover;
  mask: linear-gradient(to bottom, black 0%, black 70%, transparent 100%);
  -webkit-mask: linear-gradient(to bottom, black 0%, black 70%, transparent 100%);
  opacity: 0.2;
  z-index: 0;
  pointer-events: none;
}

/* 降低干扰的半透明遮罩层 */
.community-select-page::after {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 60%;
  background: rgba(255, 255, 255, 0.2); 
  z-index: 0;
  pointer-events: none;
}

/* 内容层提升权重与文字阴影 */
.community-select-page > * { position: relative; z-index: 2; }
.page-header h2, .page-header p, .category-menu, .community-card {
  text-shadow: 0 1px 2px rgba(255, 255, 255, 0.8);
}

/* ================= 顶部导航 ================= */
.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  max-width: 1200px;
  margin: 0 auto 30px;
  position: relative;
  z-index: 2;
}

.header-content h2 {
  font-size: 32px;
  font-weight: 700;
  margin: 0 0 8px 0;
  background: linear-gradient(135deg, #1e4f6b, #2d8b6f);
  -webkit-background-clip: text;
  background-clip: text;
  color: transparent;
  display: inline-block;
}

.header-content p { color: #475569; margin: 0; font-size: 16px; }

.back-btn {
  padding: 8px 24px;
  background: rgba(255, 255, 255, 0.7);
  backdrop-filter: blur(8px);
  border: 1px solid rgba(74, 163, 209, 0.2);
  border-radius: 40px;
  cursor: pointer;
  transition: all 0.3s;
  color: #1e293b;
  font-weight: 500;
}
.back-btn:hover {
  background: white;
  border-color: #4aa3d1;
  transform: translateY(-2px);
  box-shadow: 0 6px 12px rgba(74, 163, 209, 0.15);
}

/* ================= 主体布局 ================= */
.main-layout {
  display: flex;
  gap: 30px;
  max-width: 1200px;
  margin: 0 auto;
  position: relative;
  z-index: 2;
}

/* ================= 左侧菜单 ================= */
.category-menu {
  width: 220px;
  background: rgba(255, 255, 255, 0.6);
  backdrop-filter: blur(12px);
  border-radius: 24px;
  padding: 20px 0;
  box-shadow: 0 8px 20px rgba(0, 0, 0, 0.03);
  border: 1px solid rgba(255, 255, 255, 0.6);
  height: fit-content;
  position: relative;
  z-index: 2;
}

.menu-item {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 10px 16px;
  cursor: pointer;
  transition: all 0.2s;
  position: relative;
}
.menu-item::before {
  content: '';
  width: 4px;
  height: 20px;
  background: transparent;
  border-radius: 4px;
  transition: all 0.2s;
  margin-right: 4px;
}
.menu-item:hover { background: rgba(74, 163, 209, 0.05); }
.menu-item:hover::before { background: #4aa3d1; opacity: 0.3; }
.menu-item.active::before {
  background: linear-gradient(135deg, #4aa3d1, #2d8b6f);
  opacity: 1;
  box-shadow: 0 2px 8px rgba(74, 163, 209, 0.3);
}

.menu-item .icon {
  font-size: 18px;
  width: 20px;
  text-align: center;
  opacity: 0.7;
}
.menu-item.active .icon { opacity: 1; color: #0369a1; }
.menu-item span:last-child {
  flex: 0 1 auto;
  font-size: 14px;
  color: #334155;
  white-space: nowrap;
}
.menu-item.active span:last-child { color: #0369a1; font-weight: 500; }

/* ================= 社区卡片网格 ================= */
.community-grid {
  flex: 1;
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 20px;
  position: relative;
  z-index: 2;
  align-content: start;
}

.community-card {
  background: rgba(255, 255, 255, 0.7);
  backdrop-filter: blur(8px);
  border-radius: 20px;
  padding: 20px;
  transition: all 0.3s ease;
  cursor: pointer;
  border: 1px solid rgba(74, 163, 209, 0.2);
  display: flex;
  flex-direction: column;
  width: 100%;
  box-sizing: border-box;
  position: relative;
  overflow: hidden;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.02);
}
.community-card:hover {
  transform: translateY(-4px);
  border-color: var(--card-color);
  box-shadow: 0 12px 24px rgba(74, 163, 209, 0.1);
  background: rgba(255, 255, 255, 0.85);
}

/* 动态颜色背景块 */
.community-card::before {
  content: '';
  position: absolute;
  top: 0; left: 0; right: 0; bottom: 0;
  background: linear-gradient(135deg, var(--card-color), transparent);
  opacity: 0.08;
  z-index: 0;
  pointer-events: none;
}

.card-top { display: flex; align-items: center; gap: 12px; margin-bottom: 12px; }
.avatar {
  width: 48px;
  height: 48px;
  border-radius: 16px;
  display: flex; align-items: center; justify-content: center;
  color: white; font-size: 20px; font-weight: 600;
  box-shadow: 0 6px 12px rgba(0, 0, 0, 0.1);
  flex-shrink: 0;
  background: var(--card-color);
  transition: all 0.3s;
}
.community-card:hover .avatar {
  transform: scale(1.05);
  box-shadow: 0 8px 16px rgba(0, 0, 0, 0.15);
}

.info { flex: 1; min-width: 0; }
.info h3 {
  margin: 0 0 6px 0; font-size: 18px; font-weight: 600; color: #1e293b;
  white-space: nowrap; overflow: hidden; text-overflow: ellipsis;
}
.member-count {
  font-size: 12px; color: #475569; background: rgba(255, 255, 255, 0.6);
  padding: 4px 10px; border-radius: 30px; display: inline-block;
  border: 1px solid rgba(0, 0, 0, 0.03);
}

.desc {
  color: #475569; font-size: 14px; line-height: 1.5; margin: 0 0 18px 0;
  display: -webkit-box; -webkit-line-clamp: 2; -webkit-box-orient: vertical;
  overflow: hidden; min-height: 42px;
}

.enter-btn {
  width: 100%; padding: 10px 0;
  background: rgba(255, 255, 255, 0.5); backdrop-filter: blur(4px);
  border: 1px solid rgba(74, 163, 209, 0.2); border-radius: 40px;
  font-size: 14px; color: #1e293b; cursor: pointer; transition: all 0.2s; margin-top: auto;
}
.community-card:hover .enter-btn {
  background: var(--card-color); color: white; border-color: transparent;
  transform: translateY(-2px); box-shadow: 0 6px 12px rgba(74, 163, 209, 0.2);
}
</style>