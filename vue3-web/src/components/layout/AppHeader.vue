<template>
  <header class="app-header">
    <div class="header-left">
      <div class="logo">🏆</div>
      <span class="sys-title">竞赛管理系统</span>
    </div>
    
    <nav class="header-nav">
      <a href="#features" class="nav-link">功能介绍</a>
      <a @click="$emit('navigate', '/competitions')" class="nav-link">
        竞赛列表
      </a>
      
      <template v-if="!user">
        <button class="nav-btn" @click="$emit('showLogin')">登录</button>
        <button class="nav-btn" @click="$emit('showRegister')">注册</button>
      </template>
      
      <template v-else>
        <span class="nav-user">
          欢迎，{{ user.realName || user.username }}
        </span>
        <button class="nav-btn" @click="$emit('logout')">退出</button>
      </template>
    </nav>
  </header>
</template>

<script setup>
defineProps({
  user: {
    type: Object,
    default: null
  }
})

defineEmits(['navigate', 'showLogin', 'showRegister', 'logout'])
</script>

<style scoped>
@import '../../styles/views-home.css';
</style>
