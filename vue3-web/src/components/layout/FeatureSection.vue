<template>
  <section class="features-section">
    <div class="features-container">
      <div class="section-header" v-if="title">
        <h2 class="section-title">{{ title }}</h2>
        <p v-if="subtitle" class="section-subtitle">{{ subtitle }}</p>
      </div>
      
      <div class="features-grid">
        <div 
          v-for="(feature, index) in features" 
          :key="index"
          class="feature-card"
        >
          <div class="icon-wrapper">
            <span class="feature-icon">{{ feature.icon }}</span>
          </div>
          <h3 class="feature-title">{{ feature.title }}</h3>
          <p class="feature-description">{{ feature.description }}</p>
          
          <div class="card-bg-decoration"></div>
        </div>
      </div>
    </div>
  </section>
</template>

<script setup>
defineProps({
  title: {
    type: String,
    default: ''
  },
  subtitle: {
    type: String,
    default: ''
  }
})

const features = [
  {
    icon: '🎯',
    title: '一站式竞赛报名',
    description: '支持多类型竞赛在线报名，流程清晰，操作便捷，让您轻松参与各类竞赛活动'
  },
  {
    icon: '📄',
    title: '项目方案提交',
    description: '在线提交项目文档，支持多格式文件上传，安全可靠的云端存储保障'
  },
  {
    icon: '👥',
    title: '团队协作管理',
    description: '团队成员管理、分工协作、消息通知一应俱全，提升团队协作效率'
  },
  {
    icon: '🤖',
    title: 'AI智能助手',
    description: '基于大模型的AI助手，提供个性化竞赛推荐、技能分析和学习路径规划'
  },
  {
    icon: '📊',
    title: '数据分析统计',
    description: '详细的参赛数据统计和分析，帮助您了解竞赛表现和提升方向'
  },
  {
    icon: '🏆',
    title: '成果展示平台',
    description: '展示您的竞赛成果和获奖经历，建立个人竞赛档案和能力画像'
  }
]
</script>

<style scoped>
@import '../../styles/views-home.css';
</style>