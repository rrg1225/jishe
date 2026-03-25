import { createRouter, createWebHistory } from 'vue-router'
import Register from '../views/Register.vue'
import Login from '../views/Login.vue'
import Dashboard from '../views/Dashboard.vue'
import ForgotPassword from '../views/ForgotPassword.vue'
import ProfileSetup from '../views/ProfileSetup.vue' 
import UserProfile from '../views/UserProfile.vue' 
import Chat from '../views/Chat.vue'
import ResumeFinalEdit from '../views/ResumeFinalEdit.vue'

const routes = [
  { path: '/', redirect: '/login' },
  { 
    path: '/register', 
    component: Register,
    meta: { hideMentor: true } // 【新增】：通过 meta 标记此页面不显示 AI 助手
  },
  { 
    path: '/login', 
    component: Login,
    meta: { hideMentor: true } // 【新增】：通过 meta 标记此页面不显示 AI 助手
  },
  { path: '/dashboard', component: Dashboard },
  { 
    path: '/forgot-password', 
    component: ForgotPassword,
    meta: { hideMentor: true } // 【新增】：通过 meta 标记此页面不显示 AI 助手
  },
  { path: '/profile-setup', component: ProfileSetup }, 
  { path: '/user-profile', component: UserProfile },
  {
    path: '/video',
    name: 'VideoDetail',
    component: () => import('../views/VideoDetail.vue') 
  },
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
  },
  {
    path: '/chat',
    name: 'chat',
    component: Chat
  },
  {
    path: '/community/:id',
    name: 'CommunityDetail',
    component: () => import('../views/CommunityDetail.vue') 
  },
  {
    path: '/community-select',
    name: 'CommunitySelect',
    component: () => import('../views/CommunitySelect.vue')
  },
  {
    path: '/video-list',
    name: 'VideoList',
    component: () => import('../views/VideoList.vue')
  },
  {
    path: '/resume-final-edit',
    name: 'ResumeFinalEdit',
    component: ResumeFinalEdit,
    meta: { requiresAuth: true } // 如果你有登录拦截的话
  },
  {
    path: '/job-detail',
    name: 'JobDetail',
    component: () => import('../views/JobDetail.vue')
  },
  {
    path: '/template-detail/:id',
    name: 'TemplateDetail',
    component: () => import('../views/TemplateDetail.vue')
  },
  {
    path: '/template-detail/:id',
    name: 'TemplateDetail',
    component: () => import('../views/TemplateDetail.vue')
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

export default router