import { createApp } from 'vue'
import App from './App.vue'
import router from './router'
import './style.css' // 默认样式

const app = createApp(App)
app.use(router)
app.mount('#app')