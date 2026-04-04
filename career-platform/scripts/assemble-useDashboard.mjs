import fs from 'fs'
import path from 'path'
import { fileURLToPath } from 'url'

const __dirname = path.dirname(fileURLToPath(import.meta.url))
const bodyPath = path.join(__dirname, '../src/composables/dashboard/_useDashboard.body.txt')
let body = fs.readFileSync(bodyPath, 'utf8')

// Imports
body = body.replace(
  /^import \{ ref, reactive, onMounted, onUnmounted, watch, computed \} from 'vue'\nimport \{ useRouter, useRoute \} from 'vue-router'\nimport request from '\.\.\/utils\/request'\nimport Chat from '\.\/Chat\.vue' \nimport \{ ElMessage \} from 'element-plus'\n/m,
  `import { ref, onMounted, onUnmounted, watch, computed } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import request from '../../utils/request'
import { ElMessage } from 'element-plus'
import {
  CATEGORY_MAP,
  createHomeData,
  createMatrixSections,
  CAROUSEL_SLIDES_INITIAL,
  HOT_KAOYAN_INITIAL,
  HOT_BAOYAN_INITIAL,
} from '../../constants/dashboardData.js'
import { JOB_CATEGORIES_INITIAL } from '../../constants/jobCategories.js'
import { MAJOR_CATEGORIES_INITIAL } from '../../constants/majorCategories.js'
`
)

body = body.replace(/\nconst isChatSidebarOpen = ref\(false\)\n\nconst goToPage = \(path\) => \{[^}]+\}\n/m, '\n')

// Replace CATEGORY_MAP + homeData + matrixSections
body = body.replace(
  /\/\/ ================= ★ 核心：后端数据分类映射 ★ =================\nconst CATEGORY_MAP = \{[\s\S]*?\}\n\nconst homeData = ref\(\[[\s\S]*?\]\)\n\nconst matrixSections = ref\(\[[\s\S]*?\]\)\n/m,
  `const homeData = ref(createHomeData())
const matrixSections = ref(createMatrixSections())
`
)

// Remove misplaced onMounted (first)
body = body.replace(
  /\nonMounted\(\(\) => \{\n  fetchHeaderUserInfo\(\)\n  startAutoPlay\(\)\n  initRealData\(\)\n  fetchContestArticles\(\)\n  fetchInitialBoardData\(\)\n  initWebSocket\(\)\n  fetchJobData\(\)\n  fetchTemplatesData\(\)\n\n  \/\/ 🌟 新增：挂载时拉取热搜\n  fetchRealHotSearch\(\)\n\}\)\n\n/m,
  '\n'
)

// Remove first onUnmounted (before carousel)
body = body.replace(
  /\nonUnmounted\(\(\) => \{\n  if \(blurTimer\) clearTimeout\(blurTimer\)\n  if \(slideInterval\) clearInterval\(slideInterval\)\n  if \(ws\) \{\n    ws\.close\(\)\n  \}\n\}\)\n\n/m,
  '\n'
)

// carouselSlides
body = body.replace(
  /const carouselSlides = ref\(\[[\s\S]*?\]\)\n/m,
  'const carouselSlides = ref([...CAROUSEL_SLIDES_INITIAL])\n'
)

// hotKaoyan / hotBaoyan
body = body.replace(
  /const hotKaoyanData = ref\(\[[\s\S]*?\]\)\n\nconst hotBaoyanData = ref\(\[[\s\S]*?\]\)\n/m,
  'const hotKaoyanData = ref([...HOT_KAOYAN_INITIAL])\n\nconst hotBaoyanData = ref([...HOT_BAOYAN_INITIAL])\n'
)

// jobCategories — replace giant line
body = body.replace(
  /\/\/ ================= ★ 产教融合真实数据获取与过滤逻辑 ★ =================\nconst jobCategories = ref\(\[[\s\S]*?\]\)\n\nconst jobCards/m,
  '// ================= ★ 产教融合真实数据获取与过滤逻辑 ★ =================\nconst jobCategories = ref(JOB_CATEGORIES_INITIAL)\n\nconst jobCards'
)

// majorCategories block through closing ])\n\n// 🌟 核心修改 1
body = body.replace(
  /const majorCategories = ref\(\[[\s\S]*?\]\)\n\n\/\/ 🌟 核心修改 1/m,
  'const majorCategories = ref(MAJOR_CATEGORIES_INITIAL)\n\n// 🌟 核心修改 1'
)

// Remove contest onMounted/onUnmounted pair
body = body.replace(
  /\/\/ 🌟 核心修改 3：页面加载时启动自动刷新\nonMounted\(\(\) => \{\n  fetchRealContests\(\)\n  \n  \/\/ 每 8 秒从后端内存拉取一次最新数据（和 Python 频率保持一致）\n  contestPollTimer = setInterval\(\(\) => \{\n    fetchRealContests\(\)\n  \}, 8000\)\n\}\)\n\n\/\/ 🌟 离开页面时记得销毁定时器，节省性能\nonUnmounted\(\(\) => \{\n  if \(contestPollTimer\) \{\n    clearInterval\(contestPollTimer\)\n  \}\n\}\)\n\n/m,
  ''
)

// Wrap
const header = `/**
 * Dashboard 页面状态与业务逻辑（由原 Dashboard.vue script 完整迁移）
 */
export function useDashboard() {
`

const lifecycle = `
  onMounted(() => {
    fetchHeaderUserInfo()
    startAutoPlay()
    initRealData()
    fetchContestArticles()
    fetchInitialBoardData()
    initWebSocket()
    fetchJobData()
    fetchTemplatesData()
    fetchRealHotSearch()
    fetchRealContests()
    contestPollTimer = setInterval(() => {
      fetchRealContests()
    }, 8000)
  })

  onUnmounted(() => {
    if (blurTimer) clearTimeout(blurTimer)
    if (slideInterval) clearInterval(slideInterval)
    if (contestPollTimer) clearInterval(contestPollTimer)
    if (ws) ws.close()
  })

`

// Collect return names: const x = / let x = / function x
const decls = []
for (const m of body.matchAll(/\nconst ([a-zA-Z_$][\w$]*)\s*=/g)) decls.push(m[1])
for (const m of body.matchAll(/\nlet ([a-zA-Z_$][\w$]*)\s*=/g)) decls.push(m[1])
for (const m of body.matchAll(/\nfunction ([a-zA-Z_$][\w$]*)\s*\(/g)) decls.push(m[1])

const skip = new Set(['blurTimer', 'slideInterval', 'ws', 'contestPollTimer'])
const seen = new Set()
const ret = []
for (const n of decls) {
  if (skip.has(n) || seen.has(n)) continue
  seen.add(n)
  ret.push(n)
}

const footer =
  lifecycle +
  `  return {\n    CATEGORY_MAP,\n    ` +
  ret.join(',\n    ') +
  ',\n  }\n}\n'

const out = header + body + footer
const outPath = path.join(__dirname, '../src/composables/dashboard/useDashboard.js')
fs.writeFileSync(outPath, out, 'utf8')
console.log('Wrote', outPath, 'return count', ret.length)
