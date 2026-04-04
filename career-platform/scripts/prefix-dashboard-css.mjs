import fs from 'fs'
import path from 'path'
import { fileURLToPath } from 'url'
import postcss from 'postcss'
import prefixSelector from 'postcss-prefix-selector'

const __dirname = path.dirname(fileURLToPath(import.meta.url))
const root = path.join(__dirname, '..')
const input = path.join(root, 'src/views/Dashboard/dashboard.raw.css')
const output = path.join(root, 'src/views/Dashboard/dashboard.prefixed.css')

const css = fs.readFileSync(input, 'utf8')

const result = postcss([
  prefixSelector({
    prefix: '.dashboard-page',
    ignore: [/^@keyframes/, /^@media/, /^from$/, /^to$/, /^\d+%$/],
    transform: (prefix, selector, prefixed) => {
      if (selector.startsWith('@keyframes')) return selector
      if (selector.startsWith('@media')) return selector
      return prefixed
    },
  }),
])
  .process(css, { from: input, to: output })
  .css

fs.writeFileSync(output, result, 'utf8')
console.log('Wrote', output)
