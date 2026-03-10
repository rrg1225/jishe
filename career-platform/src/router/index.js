import { createRouter, createWebHistory } from 'vue-router'
import Register from '../views/Register.vue'
import Login from '../views/Login.vue'
import Dashboard from '../views/Dashboard.vue'
import ForgotPassword from '../views/ForgotPassword.vue'
import ProfileSetup from '../views/ProfileSetup.vue' // 【新增】：引入完善信息页面
import UserProfile from '../views/UserProfile.vue' // 引入刚建的组件

const routes = [
  { path: '/', redirect: '/login' },
  { path: '/register', component: Register },
  { path: '/login', component: Login },
  { path: '/dashboard', component: Dashboard },
  { path: '/forgot-password', component: ForgotPassword },
  { path: '/profile-setup', component: ProfileSetup }, // 【新增】：配置路由路径
  { path: '/user-profile', component: UserProfile },
  {path: '/video',name:'VideoDetail',component: () => import('../views/VideoDetail.vue') },
  {
  path: '/article',
  name: 'ArticleDetail',
  component: () => import('../views/ArticleDetail.vue') 
},
 {
  path: '/publish',
  name: 'Publish',
  component: () => import('../views/Publish.vue') 
},
{
    path: '/resume-template',
    name: 'ResumeTemplate',
    component: () => import('../views/ResumeTemplate.vue')
  },
  {
  path: '/resume-preview',
  name: 'ResumePreview',
  component: () => import('../views/ResumePreview.vue')
}
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

export default router