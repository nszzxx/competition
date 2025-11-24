// 简单的消息提示工具
export const showMessage = (message, type = 'info') => {
  // 创建消息元素
  const messageEl = document.createElement('div')
  messageEl.className = `message-popup message-${type}`
  messageEl.innerHTML = `
    <div class="message-content">
      <span class="message-icon">${getIcon(type)}</span>
      <span class="message-text">${message}</span>
    </div>
  `
  
  // 添加样式
  messageEl.style.cssText = `
    position: fixed;
    top: 20px;
    right: 20px;
    z-index: 9999;
    min-width: 300px;
    max-width: 500px;
    padding: 12px 16px;
    border-radius: 8px;
    box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
    background: white;
    border-left: 4px solid ${getBorderColor(type)};
    opacity: 0;
    transform: translateX(100%);
    transition: all 0.3s ease-in-out;
  `
  
  // 添加到页面
  document.body.appendChild(messageEl)
  
  // 显示动画
  setTimeout(() => {
    messageEl.style.opacity = '1'
    messageEl.style.transform = 'translateX(0)'
  }, 100)
  
  // 自动移除
  setTimeout(() => {
    messageEl.style.opacity = '0'
    messageEl.style.transform = 'translateX(100%)'
    setTimeout(() => {
      if (messageEl.parentNode) {
        messageEl.parentNode.removeChild(messageEl)
      }
    }, 300)
  }, 3000)
}

function getIcon(type) {
  switch (type) {
    case 'success':
      return '✅'
    case 'error':
      return '❌'
    case 'warning':
      return '⚠️'
    default:
      return 'ℹ️'
  }
}

function getBorderColor(type) {
  switch (type) {
    case 'success':
      return '#10b981'
    case 'error':
      return '#ef4444'
    case 'warning':
      return '#f59e0b'
    default:
      return '#3b82f6'
  }
}

// 添加CSS样式到页面
const addMessageStyles = () => {
  if (document.getElementById('message-styles')) return
  
  const style = document.createElement('style')
  style.id = 'message-styles'
  style.textContent = `
    .message-content {
      display: flex;
      align-items: center;
      gap: 8px;
    }
    
    .message-icon {
      font-size: 16px;
    }
    
    .message-text {
      flex: 1;
      font-size: 14px;
      color: #374151;
    }
    
    .message-success {
      background: #f0fdf4 !important;
    }
    
    .message-error {
      background: #fef2f2 !important;
    }
    
    .message-warning {
      background: #fffbeb !important;
    }
    
    .message-info {
      background: #eff6ff !important;
    }
  `
  
  document.head.appendChild(style)
}

// 初始化样式
addMessageStyles()

export const showSuccess = (message) => showMessage(message, 'success')
export const showError = (message) => showMessage(message, 'error')
export const showWarning = (message) => showMessage(message, 'warning')
export const showInfo = (message) => showMessage(message, 'info')