<template>
  <router-view></router-view>
  <AIMentor v-if="showMentor" />
</template>

<script setup>
import { computed } from 'vue'
import { useRoute } from 'vue-router'
// 请确保路径匹配你刚创建文件的位置
import AIMentor from './components/AIMentor.vue' 

const route = useRoute()
// 当所在路由不是这些基础页面时，显示 AI 导师
const showMentor = computed(() => {
  const hiddenPaths = ['/login', '/register', '/forgot-password']
  return !hiddenPaths.includes(route.path)
})
</script>

<style>
/* 移除默认的 body 边距，确保全屏铺满 */
body, html { margin: 0; padding: 0; height: 100%; width: 100%; font-family: 'Helvetica Neue', Arial, sans-serif; }
#app { height: 100%; }
</style>