import { createApp } from 'vue'
import App from './App.vue'
import router from './router' // 引入你的路由

// 引入 Element Plus 和样式
import ElementPlus from 'element-plus'
import 'element-plus/dist/index.css'

// 引入你原有的全局样式（如果有的话）
import './style.css' 

const app = createApp(App)

// 注册插件
app.use(router)
app.use(ElementPlus)

// 挂载到页面（确保整个文件只有这一行 mount！）
app.mount('#app')