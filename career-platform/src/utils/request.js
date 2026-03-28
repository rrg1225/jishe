import axios from 'axios'

// 1. 创建 axios 实例
const request = axios.create({
  baseURL: import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080', 
  // 🌟 修改点：从 60000 改为 120000 (120秒)，适应一般的大模型请求
  timeout: 120000 
})

// 2. 请求拦截器 (Request Interceptor)
request.interceptors.request.use(
  config => {
    const token = localStorage.getItem('token')
    
    // 严格判断，坚决不把字符串 'undefined' 或 'null' 传给后端
    if (token && token !== 'undefined' && token !== 'null' && token !== '') {
      config.headers['Authorization'] = `Bearer ${token}`
    }
    
    return config
  },
  error => {
    return Promise.reject(error)
  }
)

// 3. 响应拦截器
request.interceptors.response.use(
  response => {
    // 配合后端的统一 Result 格式，如果 code 不是 200，可以直接抛出异常
    const res = response.data;
    if (res.code && res.code !== 200) {
        alert(res.msg || '请求失败');
        return Promise.reject(new Error(res.msg || 'Error'));
    }
    return res;
  },
  error => {
    if (error.response && error.response.status === 401) {
      alert('登录已失效，请重新登录！')
      localStorage.removeItem('token')
      localStorage.removeItem('userId')
      // 🌟 强制跳转到登录页
      window.location.href = '/login'
    } else if (error.response && error.response.status === 429) {
      alert('操作太频繁啦，请稍等一分钟后再试！')
    } else {
      console.error('网络请求报错:', error.response || error)
    }
    return Promise.reject(error)
  }
)

export default request