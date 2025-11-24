<template>
  <header class="app-header">
    <div class="header-container">
      <!-- Logo区域 -->
      <div class="header-logo">
        <router-link to="/" class="logo-link">
          <span class="logo-text">竞赛系统</span>
        </router-link>
      </div>

      <!-- 导航菜单 -->
      <nav class="header-nav">
        <router-link 
          to="/competitions" 
          class="nav-item"
          :class="{ active: $route.path === '/competitions' }"
        >
          竞赛列表
        </router-link>
        <router-link 
          to="/ai-center" 
          class="nav-item"
          :class="{ active: $route.path === '/ai-center' }"
        >
          AI助手中心
        </router-link>
      </nav>

      <!-- 用户操作区域 -->
      <div class="header-actions">
        <div v-if="currentUser" class="user-section">
          <span class="user-name">{{ currentUser.username }}</span>
          <button @click="goToProfile" class="profile-btn" title="个人主页">
            <svg width="20" height="20" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
              <path d="M12 12C14.7614 12 17 9.76142 17 7C17 4.23858 14.7614 2 12 2C9.23858 2 7 4.23858 7 7C7 9.76142 9.23858 12 12 12Z" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
              <path d="M20.5899 22C20.5899 18.13 16.7399 15 11.9999 15C7.25991 15 3.40991 18.13 3.40991 22" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
            </svg>
          </button>
          <button @click="logout" class="logout-btn">退出</button>
        </div>
        <div v-else class="auth-section">
          <button @click="goToLogin" class="login-btn">登录</button>
        </div>
      </div>
    </div>
  </header>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'

const router = useRouter()
const currentUser = ref(null)

onMounted(() => {
  loadUserInfo()
})

const loadUserInfo = () => {
  const userStr = localStorage.getItem('user')
  if (userStr) {
    try {
      currentUser.value = JSON.parse(userStr)
    } catch (e) {
      console.error('用户信息解析失败:', e)
    }
  }
}

const goToProfile = () => {
  router.push('/profile')
}

const goToLogin = () => {
  router.push('/login')
}

const logout = () => {
  localStorage.removeItem('user')
  currentUser.value = null
}
</script>

<style scoped>
/* ===== 顶部导航栏 (Header) ===== */
.app-header {
  background: rgba(15, 23, 42, 0.8); /* Match Home HeroBanner background */
  backdrop-filter: blur(12px);
  border-bottom: 1px solid rgba(255, 255, 255, 0.05);
  position: sticky;
  top: 0;
  z-index: 1000;
  transition: all 0.3s ease;
}

.header-container {
  max-width: 1280px;
  margin: 0 auto;
  padding: 0 2rem;
  display: flex;
  align-items: center;
  height: 72px;
  justify-content: space-between;
}

/* Logo 区域 */
.header-logo {
  flex: 0 0 auto;
  margin-right: 2rem;
}

.header-logo .logo-link {
  text-decoration: none;
  color: white;
  font-size: 1.5rem;
  font-weight: 800;
  display: flex;
  align-items: center;
  letter-spacing: -0.02em;
  transition: opacity 0.3s ease;
}

.header-logo .logo-link:hover {
  opacity: 0.9;
}

.logo-text {
  background: linear-gradient(to right, #fff, #cbd5e1);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
}

/* 导航菜单 */
.header-nav {
  flex: 1;
  display: flex;
  justify-content: center;
  gap: 1rem;
}

.nav-item {
  color: #94a3b8;
  text-decoration: none;
  padding: 8px 16px;
  border-radius: 8px;
  transition: all 0.3s ease;
  font-weight: 500;
  font-size: 0.95rem;
  position: relative;
}

.nav-item:hover {
  color: white;
  background: rgba(255, 255, 255, 0.05);
}

.nav-item.active {
  color: white;
  background: rgba(255, 255, 255, 0.1);
  font-weight: 600;
}

.nav-item.active::after {
  content: '';
  position: absolute;
  bottom: -2px;
  left: 50%;
  transform: translateX(-50%);
  width: 20px;
  height: 2px;
  background: #4f46e5;
  border-radius: 2px;
}

/* 用户操作区域 */
.header-actions {
  flex: 0 0 auto;
  display: flex;
  align-items: center;
  gap: 16px;
}

.user-section {
  display: flex;
  align-items: center;
  gap: 16px;
}

.user-name {
  color: white;
  font-weight: 600;
  font-size: 0.95rem;
}

.profile-btn {
  background: rgba(255, 255, 255, 0.1);
  border: 1px solid rgba(255, 255, 255, 0.1);
  color: white;
  width: 36px;
  height: 36px;
  border-radius: 50%;
  cursor: pointer;
  transition: all 0.3s ease;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 0;
}

.profile-btn:hover {
  background: #4f46e5;
  border-color: #4f46e5;
  transform: translateY(-1px);
}

.logout-btn,
.login-btn {
  padding: 8px 20px;
  border-radius: 8px;
  font-weight: 600;
  font-size: 0.9rem;
  cursor: pointer;
  transition: all 0.3s ease;
  border: none;
}

.login-btn {
  background: linear-gradient(135deg, #4f46e5 0%, #7c3aed 100%);
  color: white;
  box-shadow: 0 4px 12px rgba(79, 70, 229, 0.3);
}

.login-btn:hover {
  transform: translateY(-1px);
  box-shadow: 0 6px 16px rgba(79, 70, 229, 0.4);
  filter: brightness(1.1);
}

.logout-btn {
  background: transparent;
  color: #94a3b8;
  border: 1px solid rgba(255, 255, 255, 0.1);
}

.logout-btn:hover {
  color: white;
  border-color: rgba(255, 255, 255, 0.3);
  background: rgba(255, 255, 255, 0.05);
}

/* 响应式适配 */
@media (max-width: 768px) {
  .header-container {
    padding: 0 1rem;
  }

  .header-nav {
    display: none; /* 移动端暂隐藏导航菜单 */
  }

  .user-name {
    display: none;
  }
  
  .header-actions {
    margin-left: auto;
  }
}
</style>