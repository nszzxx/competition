<template>
  <div class="test-profile">
    <h1>个人主页功能测试</h1>
    
    <div class="test-section">
      <h2>导航条测试</h2>
      <Header />
    </div>

    <div class="test-section">
      <h2>个人信息管理测试</h2>
      <ProfileControl />
    </div>

    <div class="test-section">
      <h2>团队管理测试</h2>
      <TeamControl />
    </div>

    <div class="test-section">
      <h2>竞赛管理测试</h2>
      <CompetitionControl />
    </div>

    <div class="test-section">
      <h2>申请处理测试</h2>
      <Application />
    </div>
  </div>
</template>

<script setup>
import Header from '../components/common/Header.vue'
import ProfileControl from '../components/profile/ProfileControl.vue'
import TeamControl from '../components/profile/TeamControl.vue'
import CompetitionControl from '../components/profile/CompetitionControl.vue'
import Application from '../components/profile/Application.vue'
</script>

<style scoped>
.test-profile {
  padding: 20px;
  max-width: 1200px;
  margin: 0 auto;
}

.test-section {
  margin-bottom: 40px;
  padding: 20px;
  border: 1px solid #e5e7eb;
  border-radius: 8px;
}

.test-section h2 {
  margin-top: 0;
  color: #1f2937;
  border-bottom: 2px solid #667eea;
  padding-bottom: 10px;
}
</style>