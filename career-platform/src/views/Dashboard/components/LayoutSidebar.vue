<script setup>
defineProps({
  dash: { type: Object, required: true },
})
</script>

<template>
  <aside class="sidebar">
    <ul>
      <li :class="{ active: dash.activeMenu === 'home' }" @click="dash.activeMenu = 'home'">首页 (推荐)</li>
      <li :class="{ active: dash.activeMenu === 'matrix' }" @click="dash.activeMenu = 'matrix'">学业深造矩阵</li>
      <li :class="{ active: dash.activeMenu === 'job' }" @click="dash.activeMenu = 'job'">校企岗位精选</li>
      <li :class="{ active: dash.activeMenu === 'contest' }" @click="dash.activeMenu = 'contest'">竞赛信息大厅</li>
      <li :class="{ active: dash.activeMenu === 'profile' }" @click="dash.activeMenu = 'profile'">智能简历定制</li>
      <li @click="dash.router.push('/mock-interview')">🎤 AI 模拟面试</li>
      <li :class="{ active: dash.activeMenu === 'chat' }" @click="dash.activeMenu = 'chat'">💬 私信交流</li>
    </ul>
    <div class="community-entry">
      <button class="industry-glow-btn" @click="dash.router.push('/community-select')">🌐 进入社区</button>
    </div>
  </aside>
</template>
