import fs from 'fs'
import path from 'path'
import { fileURLToPath } from 'url'

const __dirname = path.dirname(fileURLToPath(import.meta.url))
const dash = fs.readFileSync(path.join(__dirname, '../src/views/Dashboard.vue'), 'utf8')

function extractRefArray(varName, endMarker) {
  const needle = `const ${varName} = ref(`
  const start = dash.indexOf(needle)
  if (start < 0) throw new Error('missing ' + varName)
  const arrStart = dash.indexOf('[', start)
  let depth = 0
  let i = arrStart
  for (; i < dash.length; i++) {
    const c = dash[i]
    if (c === '[') depth++
    else if (c === ']') {
      depth--
      if (depth === 0) {
        i++
        break
      }
    }
  }
  const slice = dash.slice(arrStart, i)
  const after = dash.slice(i, i + 80)
  if (!after.trimStart().startsWith(')')) {
    throw new Error('parse error after ' + varName + ': ' + after.slice(0, 40))
  }
  return slice
}

const jobArr = extractRefArray('jobCategories')
const majorArr = extractRefArray('majorCategories')

const outDir = path.join(__dirname, '../src/constants')
fs.mkdirSync(outDir, { recursive: true })

fs.writeFileSync(
  path.join(outDir, 'jobCategories.js'),
  `/** 产教融合双选 — 行业/岗位分类静态数据（由 Dashboard 迁移） */\nexport const JOB_CATEGORIES_INITIAL = ${jobArr}\n`,
  'utf8'
)
fs.writeFileSync(
  path.join(outDir, 'majorCategories.js'),
  `/** 竞赛大厅 — 学科门类静态数据（由 Dashboard 迁移） */\nexport const MAJOR_CATEGORIES_INITIAL = ${majorArr}\n`,
  'utf8'
)
console.log('Wrote jobCategories.js, majorCategories.js')
