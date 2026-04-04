<template>
  <div class="skill-tree-container">
    <div ref="graphChart" style="width: 100%; height: 600px;"></div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue';
import * as echarts from 'echarts';

const graphChart = ref(null);

onMounted(() => {
  const myChart = echarts.init(graphChart.value);
  
  // 模拟从后端获取的知识图谱数据 (岗位 -> 核心领域 -> 具体技术)
  const graphData = {
    name: 'Java全栈开发工程师',
    children: [
      {
        name: '后端核心',
        children: [{ name: 'Spring Boot' }, { name: 'JVM调优' }, { name: '多线程并发' }]
      },
      {
        name: '中间件与微服务',
        children: [{ name: 'Redis缓存' }, { name: 'Kafka消息队列' }, { name: 'Spring Cloud' }]
      },
      {
        name: '前端技术',
        children: [{ name: 'Vue 3' }, { name: 'TypeScript' }, { name: 'ECharts' }]
      }
    ]
  };

  const option = {
    tooltip: { trigger: 'item', triggerOn: 'mousemove' },
    series: [
      {
        type: 'tree',
        data: [graphData],
        top: '1%', left: '7%', bottom: '1%', right: '20%',
        symbolSize: 15,
        label: {
          position: 'left',
          verticalAlign: 'middle',
          align: 'right',
          fontSize: 16,
          color: '#fff',
          backgroundColor: '#3b82f6',
          padding: [5, 10],
          borderRadius: 5
        },
        leaves: {
          label: { position: 'right', verticalAlign: 'middle', align: 'left', backgroundColor: '#10b981' }
        },
        expandAndCollapse: true,
        animationDuration: 550,
        animationDurationUpdate: 750
      }
    ]
  };
  myChart.setOption(option);
});
</script>