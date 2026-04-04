<template>
  <div class="data-screen bg-gray-900 p-6 text-white min-h-screen">
    <h1 class="text-3xl font-bold text-center mb-8 text-blue-400">🔥 全国高校毕业生就业洞察大屏</h1>
    <div class="grid grid-cols-2 gap-8">
      <div class="bg-gray-800 p-4 rounded-xl shadow-lg">
        <h3 class="text-xl mb-4 text-center">热门城市岗位需求Top10</h3>
        <div ref="cityChart" style="width: 100%; height: 400px;"></div>
      </div>
      <div class="bg-gray-800 p-4 rounded-xl shadow-lg">
        <h3 class="text-xl mb-4 text-center">高薪岗位技能词云</h3>
        <div ref="wordCloudChart" style="width: 100%; height: 400px;"></div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue';
import * as echarts from 'echarts';
// 如果要用词云，需要安装: npm install echarts-wordcloud
import 'echarts-wordcloud';

const cityChart = ref(null);
const wordCloudChart = ref(null);

onMounted(() => {
  // 1. 渲染城市柱状图 (对接你刚写的后端聚合接口)
  const myCityChart = echarts.init(cityChart.value, 'dark');
  myCityChart.setOption({
    backgroundColor: 'transparent',
    tooltip: {},
    xAxis: { data: ['北京', '上海', '深圳', '杭州', '广州', '成都', '武汉'] },
    yAxis: {},
    series: [{
      name: '岗位数',
      type: 'bar',
      data: [1200, 1150, 980, 850, 700, 450, 300],
      itemStyle: {
        color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
          { offset: 0, color: '#83bff6' },
          { offset: 0.5, color: '#188df0' },
          { offset: 1, color: '#188df0' }
        ])
      }
    }]
  });

  // 2. 渲染技能词云
  const myWordCloud = echarts.init(wordCloudChart.value);
  myWordCloud.setOption({
    series: [{
      type: 'wordCloud',
      shape: 'circle',
      keepAspect: false,
      sizeRange: [14, 60],
      rotationRange: [-90, 90],
      textStyle: {
        color: function () {
          return 'rgb(' + [
            Math.round(Math.random() * 160 + 90),
            Math.round(Math.random() * 160 + 90),
            Math.round(Math.random() * 160 + 90)
          ].join(',') + ')';
        }
      },
      data: [
        { name: '微服务', value: 1000 }, { name: 'Python', value: 800 },
        { name: '大模型微调', value: 1500 }, { name: 'Vue3', value: 700 },
        { name: '高并发', value: 900 }, { name: 'Elasticsearch', value: 600 }
      ]
    }]
  });
});
</script>