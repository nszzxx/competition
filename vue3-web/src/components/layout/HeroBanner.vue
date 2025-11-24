<template>
  <section class="hero-banner">
    <!-- 动态背景 -->
    <div class="hero-bg">
      <div class="blob blob-1"></div>
      <div class="blob blob-2"></div>
      <div class="blob blob-3"></div>
      <div class="grid-overlay"></div>
    </div>

    <div class="hero-content">
      <div class="text-content">
        <div class="badge-wrapper">
          <span class="new-badge">New</span>
          <span class="badge-text">智能竞赛管理系统 2.0</span>
        </div>
        
        <h1 class="hero-title">
          <span class="gradient-text">开启你的</span><br>
          <span class="highlight-text">竞赛之旅</span>
        </h1>
        
        <p class="hero-subtitle">
          {{ subtitle }}
        </p>
        
        <div class="hero-actions">
          <button class="btn-primary" @click="$emit('action')">
            <span>{{ actionText }}</span>
            <svg xmlns="http://www.w3.org/2000/svg" width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><line x1="5" y1="12" x2="19" y2="12"></line><polyline points="12 5 19 12 12 19"></polyline></svg>
          </button>
          <button class="btn-secondary">
            了解更多
          </button>
        </div>

        <div class="hero-stats">
          <div class="stat-item">
            <span class="stat-num">1000+</span>
            <span class="stat-label">活跃用户</span>
          </div>
          <div class="divider"></div>
          <div class="stat-item">
            <span class="stat-num">500+</span>
            <span class="stat-label">举办竞赛</span>
          </div>
          <div class="divider"></div>
          <div class="stat-item">
            <span class="stat-num">98%</span>
            <span class="stat-label">好评率</span>
          </div>
        </div>
      </div>

      <div class="visual-content">
        <div class="floating-card card-1">
          <div class="card-icon">🏆</div>
          <div class="card-info">
            <div class="card-title">竞赛报名</div>
            <div class="card-desc">一键快速参与</div>
          </div>
        </div>
        <div class="floating-card card-2">
          <div class="card-icon">📊</div>
          <div class="card-info">
            <div class="card-title">数据分析</div>
            <div class="card-desc">实时监控进度</div>
          </div>
        </div>
        <div class="main-visual">
          <div class="abstract-shape"></div>
        </div>
      </div>
    </div>
  </section>
</template>

<script setup>
defineProps({
  title: {
    type: String,
    default: '即刻开启你的竞赛之旅'
  },
  subtitle: {
    type: String,
    default: '一站式竞赛服务平台，为您提供从报名、组队到评审的全流程数字化解决方案。让竞赛管理更简单，让参与体验更卓越。'
  },
  actionText: {
    type: String,
    default: '立即体验'
  }
})

defineEmits(['action'])
</script>

<style scoped>
@import '../../styles/views-home.css';
</style>