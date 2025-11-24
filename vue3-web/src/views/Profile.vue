<template>
  <div class="profile-page">
    <!-- 通用导航条 -->
    <Header />
    
    <!-- 个人主页内容 -->
    <ControlHeader />
  </div>
</template>

<script setup>
import Header from '../components/common/Header.vue'
import ControlHeader from '../components/profile/ControlHeader.vue'
</script>

<style scoped>
.profile-page {
  min-height: 100vh;
  background: #f5f7fa;
}
</style>