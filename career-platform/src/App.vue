<template>
  <router-view></router-view>
  <AIMentor v-if="showMentor" />
</template>

<script setup>
import { computed } from 'vue'
import { useRoute } from 'vue-router'
import AIMentor from './components/AIMentor.vue' 

const route = useRoute()
const showMentor = computed(() => {
  const hiddenPaths = ['/login', '/register', '/forgot-password']
  return !hiddenPaths.includes(route.path)
})
</script>

<style>
/* 🌟 修复滚动 Bug：移除死板的 height: 100%，改用 min-height 让页面能够自然向下延伸 */
body, html { 
  margin: 0; 
  padding: 0; 
  min-height: 100vh; /* 允许内容撑开高度 */
  width: 100%; 
  font-family: 'Helvetica Neue', Arial, sans-serif; 
}
#app { 
  min-height: 100vh; 
}
</style>