<template>
  <el-dialog
    :model-value="visible"
    @update:model-value="$emit('update:visible', $event)"
    title="请选择感兴趣的社区"
    width="600px"
    center
  >
    <div class="industry-grid">
      <div 
        v-for="item in industryList" 
        :key="item.id" 
        class="industry-card"
        @click="handleEnter(item.id)"
      >
        <h3>{{ item.name }}</h3>
        <p>{{ item.description || '暂无简介' }}</p>
      </div>
    </div>
  </el-dialog>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import request from '../utils/request' // 使用你封装的 request

const props = defineProps(['visible'])
const emit = defineEmits(['update:visible'])
const router = useRouter()

const industryList = ref([])

// 假设当前用户ID是1（后续你可以从 localStorage 或 Pinia 中获取真实ID）
const currentUserId = 1 

onMounted(async () => {
  try {
    // 调用后端的获取列表接口
    const res = await request.get('/api/community/list')
    industryList.value = res.data || res // 根据你 request.js 的拦截器配置，这里可能不需要 .data
  } catch (error) {
    ElMessage.error('获取社区列表失败')
  }
})

const handleEnter = async (communityId) => {
  try {
    // 调用后端的加入社区接口
    const res = await request.post(`/api/community/join?userId=${currentUserId}&communityId=${communityId}`)
    ElMessage.success(res.data || res) // 提示“成功加入”或“您已加入”
    
    // 关闭弹窗
    emit('update:visible', false)
    
    // 跳转到社区内部页面
    router.push(`/community/${communityId}`)
  } catch (error) {
    ElMessage.error('进入社区失败')
  }
}
</script>

<style scoped>
.industry-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 15px;
}
.industry-card {
  padding: 15px;
  border: 1px solid #e4e7ed;
  border-radius: 8px;
  cursor: pointer;
  transition: all 0.3s;
}
.industry-card:hover {
  border-color: #409eff;
  background-color: #f0f9eb;
}
.industry-card h3 { margin: 0 0 10px 0; color: #303133; }
.industry-card p { margin: 0; font-size: 13px; color: #909399; }
</style>