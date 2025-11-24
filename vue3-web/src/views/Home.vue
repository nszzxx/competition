<template>
  <div class="home-page">
    <AppHeader 
      :user="user"
      @navigate="handleNavigate"
      @showLogin="showLogin = true"
      @showRegister="showRegister = true"
      @logout="handleLogout"
    />
    
    <main>
      <HeroBanner 
        title="即刻开启你的竞赛之旅"
        subtitle="竞赛管理系统为你提供报名、项目提交、团队管理等一站式服务，助力你的每一次挑战。"
        action-text="立即体验"
        @action="handleExperience"
      />
      
      <FeatureSection 
        id="features"
        title="核心功能"
        subtitle="为大学生竞赛参与提供全方位支持，让每一个创意都能落地。"
      />
    </main>

    <AppFooter />
    
    <LoginModal 
      v-if="showLogin" 
      @close="showLogin = false" 
      @switch="switchToRegister"
      @success="handleLoginSuccess"
    />
    
    <RegisterModal 
      v-if="showRegister" 
      @close="showRegister = false" 
      @switch="switchToLogin"
      @success="handleRegisterSuccess"
    />
  </div>
</template>

<script setup>
// 组件导入
import AppHeader from '../components/layout/AppHeader.vue'
import HeroBanner from '../components/layout/HeroBanner.vue'
import FeatureSection from '../components/layout/FeatureSection.vue'
import AppFooter from '../components/layout/AppFooter.vue'
import LoginModal from '../components/auth/LoginModal.vue'
import RegisterModal from '../components/auth/RegisterModal.vue'

// 使用组合式函数
import { useHome } from '../composables/useHome.js'

// 使用首页组合式函数
const {
  showLogin,
  showRegister,
  user,
  handleNavigate,
  handleExperience,
  handleLoginSuccess,
  handleRegisterSuccess,
  handleLogout,
  switchToRegister,
  switchToLogin
} = useHome()
</script>

<style scoped>
.home-page {
  min-height: 100vh;
  background-color: #f8fafc;
  display: flex;
  flex-direction: column;
}

main {
  flex: 1;
}
</style>
