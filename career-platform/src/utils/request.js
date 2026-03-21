import axios from 'axios'

// 1. 创建 axios 实例
const request = axios.create({
  baseURL: 'http://localhost:8080', 
  timeout: 60000 
})

// 2. 请求拦截器 (Request Interceptor)
request.interceptors.request.use(
  config => {
    const token = localStorage.getItem('token')
    
    // ✅ 修复点 1：严格判断，坚决不把字符串 'undefined' 或 'null' 传给后端
    if (token && token !== 'undefined' && token !== 'null' && token !== '') {
      config.headers['Authorization'] = `Bearer ${token}`
    }
    
    return config
  },
  error => {
    return Promise.reject(error)
  }
)

// 3. 响应拦截器 (Response Interceptor)
request.interceptors.response.use(
  response => {
    // 正常请求直接返回数据
    return response.data
  },
  error => {
    // ✅ 修复点 2：捕捉后端的 401 报错，直接给用户弹窗提示
    if (error.response && error.response.status === 401) {
      alert('登录失败：您输入的账号未注册，或密码错误！')
      // 顺便清理掉浏览器里可能残留的废弃 Token
      localStorage.removeItem('token')
    } else if (error.response && error.response.status === 429) {
      alert('操作太频繁啦，请稍等一分钟后再试！')
    } else {
      console.error('网络请求报错:', error.response || error)
    }
    
    return Promise.reject(error)
  }
)

export default request