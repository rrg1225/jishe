<template>
  <div class="min-h-screen bg-gray-50 py-10">
    <div class="max-w-4xl mx-auto bg-white rounded-2xl shadow-sm p-10 border border-gray-100" v-if="detail">
      
      <button @click="router.back()" class="mb-6 text-gray-500 hover:text-blue-500 flex items-center gap-2 transition-colors">
        <span>←</span> 返回模板库
      </button>

      <div class="flex flex-col md:flex-row gap-10">
<div class="w-full md:w-1/2 border rounded-xl overflow-hidden shadow-sm bg-gray-50 flex items-center justify-center">
  <img 
    :src="getSafeImageUrl(detail.previewUrl)" 
    class="w-full h-auto object-cover" 
    alt="简历预览" 
    v-if="detail.previewUrl"
  />
  <div v-else class="h-96 flex flex-col items-center justify-center text-gray-400">
    <span class="text-4xl mb-2">📄</span>
    <span>暂无封面预览图</span>
  </div>
</div>

<div class="w-full md:w-1/2 flex flex-col justify-center gap-6">
  <div class="flex flex-col gap-3 mt-4">
    <button @click="downloadDoc" class="w-full py-3.5 bg-blue-500 text-white font-medium rounded-xl hover:bg-blue-600 shadow-md hover:shadow-lg transition-all flex items-center justify-center gap-2">
      <span class="text-lg">📥</span> 下载 Word 原件
    </button>
    
    <button @click="previewDocOnline" class="w-full py-3.5 bg-indigo-500 text-white font-medium rounded-xl hover:bg-indigo-600 shadow-md hover:shadow-lg transition-all flex items-center justify-center gap-2">
      <span class="text-lg">👁️</span> 在线预览真实原件
    </button>
    
    <button 
      v-if="detail.type === 1" 
      @click="editOnline" 
      class="w-full py-3.5 bg-green-500 text-white font-medium rounded-xl hover:bg-green-600 shadow-md hover:shadow-lg transition-all flex items-center justify-center gap-2"
    >
      <span class="text-lg">✏️</span> 去填写内容
    </button>
  </div>
</div>
      </div>
    </div>

    <div v-else class="flex flex-col items-center justify-center h-96 text-gray-500 gap-4">
      <div class="animate-spin rounded-full h-10 w-10 border-4 border-gray-200 border-t-blue-500"></div>
      <p>正在努力加载模板详情...</p>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import request from '../utils/request';

const route = useRoute();
const router = useRouter();
const detail = ref(null);

onMounted(async () => {
  try {
    const res = await request.get(`/api/templates/${route.params.id}`);
    // 【修改点 1】：后端返回的是 Result 结构，真正的模板数据在 res.data 中
    // 兼容写法：如果 res.data 存在就用 res.data，否则用 res
    detail.value = res.data || res;
  } catch (error) {
    console.error("获取详情失败", error);
  }
});

// 下载 Word 的方法
const downloadDoc = () => {
  if (detail.value && detail.value.docUrl) {
    window.open(detail.value.docUrl, '_blank');
  } else {
    alert('暂无下载链接');
  }
};

// 跳转到编辑页的方法
const editOnline = () => {
  // 跳转到现有的编辑页，并把 templateId 带过去
  router.push({ path: '/resume-final-edit', query: { templateId: detail.value.id } });
};

// 安全图片渲染，防止 docx 链接直接赋给 img 标签导致裂图
const getSafeImageUrl = (url) => {
  if (!url) return '';
  const lowerUrl = String(url).toLowerCase();
  // 如果后端存的封面其实是 doc/docx 链接，给一张好看的默认骨架屏/占位图
  if (lowerUrl.includes('.doc') || lowerUrl.includes('.docx')) {
    return 'https://images.unsplash.com/photo-1612441804231-77a36b284856?w=600';
  }
  return url;
};

// 调用微软官方引擎在线预览真实的 DOCX 原件
const previewDocOnline = () => {
  if (detail.value && detail.value.docUrl) {
    const officeUrl = `https://view.officeapps.live.com/op/view.aspx?src=${encodeURIComponent(detail.value.docUrl)}`;
    window.open(officeUrl, '_blank');
  } else {
    alert('暂无源文件，无法在线预览');
  }
};

// 时间格式化工具
const formatDate = (dateStr) => {
  if (!dateStr) return '';
  const date = new Date(dateStr);
  return `${date.getFullYear()}-${String(date.getMonth() + 1).padStart(2, '0')}-${String(date.getDate()).padStart(2, '0')}`;
};
</script>