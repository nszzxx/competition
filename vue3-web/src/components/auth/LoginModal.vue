<template>
  <div class="modal-mask" @click.self="$emit('close')">
    <div class="modal-container">
      <button class="modal-close" @click="$emit('close')">×</button>
      <div class="modal-content">
        <div class="modal-title-row">
          <span class="modal-title">用户登录</span>
          <button class="modal-switch" @click="$emit('switch')">注册</button>
        </div>
        <form class="modal-form" @submit.prevent="handleLogin">
          <input v-model="form.username" type="text" placeholder="用户名" required />
          <input v-model="form.password" type="password" placeholder="密码" required />
          <button type="submit" :disabled="loading">{{ loading ? '登录中...' : '登录' }}</button>
        </form>
        <div v-if="error" class="error-msg">{{ error }}</div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { authApi } from '../../utils/api.js'

const emit = defineEmits(['close', 'switch', 'success'])

const form = ref({
  username: '',
  password: ''
})

const loading = ref(false)
const error = ref('')

const handleLogin = async () => {
  error.value = ''
  loading.value = true
  
  try {
    console.log('开始登录流程')
    console.log('表单数据:', form.value)
    console.log('用户名:', form.value.username)
    console.log('密码:', form.value.password)
    
    if (!form.value.username || !form.value.password) {
      error.value = '请输入用户名和密码'
      return
    }
    
    console.log('发送登录请求:', form.value)
    const userData = await authApi.login(form.value)
    console.log('登录响应:', userData)
    
    // 保存用户信息到localStorage
    localStorage.setItem('user', JSON.stringify(userData))
    
    // 如果有token，也保存token
    if (userData.token) {
      localStorage.setItem('token', userData.token)
    }
    
    // 触发成功事件
    emit('success', userData)
  } catch (err) {
    console.error('登录失败详情:', err)
    console.error('错误响应:', err.response)
    console.error('错误数据:', err.response?.data)
    error.value = err.response?.data?.message || err.message || '登录失败，请检查用户名和密码'
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.modal-mask {
  position: fixed;
  z-index: 9999;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(15, 23, 42, 0.75);
  backdrop-filter: blur(8px);
  display: flex;
  align-items: center;
  justify-content: center;
  animation: fadeIn 0.3s ease;
}

@keyframes fadeIn {
  from { opacity: 0; }
  to { opacity: 1; }
}

.modal-container {
  position: relative;
  width: 440px;
  background: rgba(255, 255, 255, 0.95);
  backdrop-filter: blur(20px);
  border-radius: 24px;
  box-shadow: 0 20px 60px rgba(0, 0, 0, 0.3);
  padding: 48px 40px;
  animation: modalIn 0.4s cubic-bezier(0.4, 0, 0.2, 1);
  border: 1px solid rgba(255, 255, 255, 0.2);
}

@keyframes modalIn {
  from {
    transform: scale(0.9) translateY(20px);
    opacity: 0;
  }
  to {
    transform: scale(1) translateY(0);
    opacity: 1;
  }
}

.modal-close {
  position: absolute;
  top: 16px;
  right: 16px;
  font-size: 1.5rem;
  background: rgba(100, 116, 139, 0.1);
  border: none;
  color: #64748b;
  cursor: pointer;
  z-index: 2;
  width: 36px;
  height: 36px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: all 0.2s ease;
}

.modal-close:hover {
  background: rgba(239, 68, 68, 0.1);
  color: #ef4444;
  transform: rotate(90deg);
}

.modal-title-row {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 32px;
}

.modal-title {
  font-size: 2rem;
  font-weight: 700;
  color: #0f172a;
  letter-spacing: -0.02em;
}

.modal-switch {
  background: rgba(79, 70, 229, 0.1);
  color: #4f46e5;
  border: 1px solid rgba(79, 70, 229, 0.2);
  border-radius: 10px;
  padding: 8px 20px;
  font-size: 0.95rem;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.2s ease;
}

.modal-switch:hover {
  background: rgba(79, 70, 229, 0.15);
  border-color: rgba(79, 70, 229, 0.3);
  transform: translateY(-1px);
}

.modal-form {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.modal-form input {
  width: 100%;
  padding: 14px 18px;
  border-radius: 12px;
  border: 2px solid #e2e8f0;
  background: #f8fafc;
  color: #0f172a;
  font-size: 1rem;
  outline: none;
  transition: all 0.3s ease;
  box-sizing: border-box;
  font-weight: 500;
}

.modal-form input::placeholder {
  color: #94a3b8;
}

.modal-form input:focus {
  border-color: #4f46e5;
  background: #fff;
  box-shadow: 0 0 0 4px rgba(79, 70, 229, 0.1);
  transform: translateY(-1px);
}

.modal-form button {
  width: 100%;
  padding: 14px;
  border-radius: 12px;
  border: none;
  background: linear-gradient(135deg, #4f46e5 0%, #7c3aed 100%);
  color: #fff;
  font-size: 1.1rem;
  font-weight: 600;
  cursor: pointer;
  margin-top: 8px;
  transition: all 0.3s ease;
  box-shadow: 0 10px 25px -5px rgba(79, 70, 229, 0.4);
}

.modal-form button:hover:not(:disabled) {
  transform: translateY(-2px);
  box-shadow: 0 20px 35px -10px rgba(79, 70, 229, 0.5);
  filter: brightness(1.1);
}

.modal-form button:active:not(:disabled) {
  transform: translateY(0);
}

.modal-form button:disabled {
  opacity: 0.6;
  cursor: not-allowed;
  transform: none;
}

.error-msg {
  color: #ef4444;
  background: rgba(239, 68, 68, 0.1);
  border: 1px solid rgba(239, 68, 68, 0.2);
  padding: 12px 16px;
  border-radius: 10px;
  margin-top: 16px;
  font-size: 0.95rem;
  text-align: center;
  font-weight: 500;
  animation: shake 0.4s ease;
}

@keyframes shake {
  0%, 100% { transform: translateX(0); }
  25% { transform: translateX(-8px); }
  75% { transform: translateX(8px); }
}

.success-msg {
  color: #10b981;
  background: rgba(16, 185, 129, 0.1);
  border: 1px solid rgba(16, 185, 129, 0.2);
  padding: 12px 16px;
  border-radius: 10px;
  margin-top: 16px;
  font-size: 0.95rem;
  text-align: center;
  font-weight: 500;
  animation: slideDown 0.3s ease;
}

@keyframes slideDown {
  from {
    opacity: 0;
    transform: translateY(-10px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

/* Responsive design */
@media (max-width: 768px) {
  .modal-container {
    width: 90%;
    padding: 36px 28px;
  }

  .modal-title {
    font-size: 1.75rem;
  }
}

@media (max-width: 480px) {
  .modal-container {
    width: 95%;
    padding: 32px 24px;
  }

  .modal-title {
    font-size: 1.5rem;
  }

  .modal-form input {
    padding: 12px 16px;
  }
}
</style>