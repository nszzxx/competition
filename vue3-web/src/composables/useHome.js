// 首页相关的组合式函数
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { authApi } from '../utils/api.js'

export function useHome() {
  const router = useRouter()

  // 响应式数据
  const showLogin = ref(false)
  const showRegister = ref(false)
  const user = ref(null)

  // 生命周期
  onMounted(() => {
    loadUserInfo()
  })

  // 加载用户信息
  const loadUserInfo = () => {
    const userStr = localStorage.getItem('user')
    if (userStr) {
      try {
        user.value = JSON.parse(userStr)
      } catch (e) {
        console.error('用户信息解析失败:', e)
        localStorage.removeItem('user')
      }
    }
  }

  // 处理导航
  const handleNavigate = (path) => {
    router.push(path)
  }

  // 处理体验按钮点击
  const handleExperience = () => {
    if (!user.value) {
      showRegister.value = true
    } else {
      router.push('/competitions')
    }
  }

  // 处理登录成功
  const handleLoginSuccess = (userData) => {
    user.value = userData
    showLogin.value = false

    // 保存用户信息到localStorage
    localStorage.setItem('user', JSON.stringify(userData))

    // 可以添加欢迎提示
    setTimeout(() => {
      alert(`欢迎回来，${userData.realName || userData.username}！`)
    }, 100)
  }

  // 处理注册成功
  const handleRegisterSuccess = (userData) => {
    // 注册成功后显示登录框
    showRegister.value = false
    showLogin.value = true

    // 显示成功提示
    setTimeout(() => {
      alert(`注册成功，请登录系统！`)
    }, 100)
  }

  // 处理退出登录
  const handleLogout = async () => {
    if (confirm('确定要退出登录吗？')) {
      try {
        await authApi.logout()
        user.value = null

        // 刷新页面或重定向到首页
        window.location.reload()
      } catch (error) {
        console.error('退出登录失败:', error)
        // 即使API调用失败，也清除本地数据
        localStorage.removeItem('user')
        localStorage.removeItem('token')
        user.value = null
        window.location.reload()
      }
    }
  }

  // 切换到注册模态框
  const switchToRegister = () => {
    showLogin.value = false
    showRegister.value = true
  }

  // 切换到登录模态框
  const switchToLogin = () => {
    showRegister.value = false
    showLogin.value = true
  }

  return {
    // 响应式状态
    showLogin,
    showRegister,
    user,

    // 方法
    handleNavigate,
    handleExperience,
    handleLoginSuccess,
    handleRegisterSuccess,
    handleLogout,
    switchToRegister,
    switchToLogin,
    loadUserInfo
  }
}
