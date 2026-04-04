<script setup>
import { reactive } from 'vue'
import { useDashboard } from '../composables/dashboard/useDashboard.js'
import LayoutHeader from './Dashboard/components/LayoutHeader.vue'
import LayoutSidebar from './Dashboard/components/LayoutSidebar.vue'
import DashboardSearchSection from './Dashboard/components/DashboardSearchSection.vue'
import HomeSection from './Dashboard/components/HomeSection.vue'
import MatrixSection from './Dashboard/components/MatrixSection.vue'
import JobSection from './Dashboard/components/JobSection.vue'
import ContestSection from './Dashboard/components/ContestSection.vue'
import DashboardArticlePanel from './Dashboard/components/DashboardArticlePanel.vue'
import ResumeSection from './Dashboard/components/ResumeSection.vue'
import DashboardChatSection from './Dashboard/components/DashboardChatSection.vue'

// 嵌套在普通对象里的 ref 作为 props 传入子组件时不会自动解包；用 reactive 包裹后属性访问会解包 ref
const dash = reactive(useDashboard())
</script>

<template>
  <div class="dashboard-page">
    <div class="layout">
      <LayoutHeader :dash="dash" />

      <div class="main-container">
        <LayoutSidebar :dash="dash" />

        <main class="content">
          <DashboardSearchSection v-if="dash.activeMenu === 'search'" :dash="dash" />
          <HomeSection v-if="dash.activeMenu === 'home'" :dash="dash" />
          <MatrixSection v-if="dash.activeMenu === 'matrix'" :dash="dash" />
          <JobSection v-if="dash.activeMenu === 'job'" :dash="dash" />
          <ContestSection v-if="dash.activeMenu === 'contest'" :dash="dash" />
          <DashboardArticlePanel v-if="dash.activeMenu === 'articleDetail'" :dash="dash" />
          <ResumeSection v-if="dash.activeMenu === 'profile'" :dash="dash" />
          <DashboardChatSection v-if="dash.activeMenu === 'chat'" />
        </main>
      </div>
    </div>
  </div>
</template>

<style src="./Dashboard/dashboard.prefixed.css"></style>
