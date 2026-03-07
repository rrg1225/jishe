import axios from 'axios'

// 1. 创建 axios 实例
const request = axios.create({
  baseURL: 'http://localhost:8080', // 确保指向你的 Java 后端地址
  timeout: 10000 // 请求超时时间
})

// 2. 🌟 核心魔法：请求拦截器 (Request Interceptor)
// 作用：每次前端发请求前，都会先执行这里。我们在这里把 Token 塞进请求头！
request.interceptors.request.use(
  config => {
    // 从浏览器的本地存储中拿出 Token
    const token = localStorage.getItem('token')
    
    // 如果有 Token，就把它放进 Headers 的 Authorization 字段里
    if (token) {
      // 注意：'Bearer ' 后面必须有一个空格，这是 JWT 的国际通用规范！
      config.headers['Authorization'] = `Bearer ${token}`
    }
    
    return config
  },
  error => {
    return Promise.reject(error)
  }
)

// 3. 响应拦截器 (Response Interceptor)
// 作用：剥离后端返回的冗余外壳，直接把核心数据交给页面
request.interceptors.response.use(
  response => {
    // Axios 默认会包一层 data，我们直接剥开它，这样 Vue 页面里就不用写 res.data.token 了
    return response.data
  },
  error => {
    // 如果后端抛出错误，在这里统一打印或处理
    console.error('网络请求报错:', error.response || error)
    return Promise.reject(error)
  }
)

export default request