<script setup>
import { onMounted } from 'vue'
import AIAssistant from './components/ai/AIAssistant.vue'

// 全局性能优化
onMounted(() => {
  // 预连接到API服务器
  const link = document.createElement('link')
  link.rel = 'preconnect'
  link.href = 'http://localhost:8080'
  document.head.appendChild(link)
  
  // 启用被动事件监听器
  document.addEventListener('touchstart', () => {}, { passive: true })
  document.addEventListener('touchmove', () => {}, { passive: true })
})
</script>

<template>
  <router-view />
  <!-- AI助手组件 - 全局浮动 -->
  <AIAssistant />
</template>

<!-- 样式已移至模块化CSS文件中 -->
