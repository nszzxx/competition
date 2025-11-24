<template>
  <div 
    ref="assistantElement"
    class="ai-assistant" 
    :class="{ expanded: isVisible, dragging: isDragging }"
  >
    <!-- 聊天触发按钮 -->
    <button
      v-if="!isVisible"
      @click="handleToggleClick"
      @mousedown="handleMouseDown"
      @touchstart="startDrag"
      class="assistant-trigger"
      :class="{ dragging: isDragging }"
      aria-label="打开AI助手"
    >
      <span class="ai-icon">🤖</span>
      <span v-if="unreadCount > 0" class="unread-badge">{{ unreadCount }}</span>
    </button>
    
    <!-- 聊天界面 -->
    <div v-if="isVisible" class="chat-container">
      <div class="chat-header">
        <div class="assistant-info">
          <div class="avatar">🤖</div>
          <div class="info">
            <h3>竞赛AI助手</h3>
            <span class="status">在线 · 随时为您服务</span>
          </div>
        </div>
        <div class="header-actions">
          <button @click="toggleHistory" class="history-btn" title="聊天历史">
            <span class="icon">📋</span>
          </button>
          <button @click="createNewConversation" class="new-chat-btn" title="清空对话">
            <span class="icon">🗑️</span>
          </button>
          <button @click="toggleAssistant" class="close-btn" title="关闭助手">
            <span class="icon">×</span>
          </button>
        </div>
      </div>
      
      <!-- 主要内容区域 -->
      <div class="chat-main" :class="{ 'history-open': showHistory }">
        <!-- 聊天历史侧边栏 -->
        <div class="chat-history-sidebar" :class="{ show: showHistory }">
          <div class="history-header">
            <h4>聊天历史</h4>
            <button @click="toggleHistory" class="close-history-btn" aria-label="关闭历史记录">×</button>
          </div>
          <div class="history-list">
            <div v-if="chatHistory.length === 0" class="no-history">
              <p>暂无聊天记录</p>
              <small>开始对话后会显示历史记录</small>
            </div>
            <div
              v-for="conversation in chatHistory"
              :key="conversation.id"
              class="history-item"
              :class="{ active: currentConversationId === conversation.id }"
            >
              <div 
                class="conversation-info"
                @click="async () => { 
                  await loadConversation(conversation.id); 
                  showHistory = false; 
                  await nextTick(); 
                  scrollToBottom(); 
                }"
              >
                <h5>{{ conversation.title || '对话记录' }}</h5>
                <p>{{ conversation.lastMessage || '点击查看详情' }}</p>
                <span class="conversation-time">{{ formatTime(conversation.timestamp) }}</span>
              </div>
              <button 
                class="delete-conversation-btn" 
                title="删除对话"
                @click.stop="confirmDeleteConversation(conversation)"
              >
                <span class="icon">🗑️</span>
              </button>
            </div>
          </div>
        </div>
        
        <!-- 聊天消息区域 -->
        <div class="chat-content">
          <div class="chat-messages" ref="messagesContainer">
            <div v-if="messages.length === 0" class="welcome-message">
              <div class="welcome-content">
                <div class="welcome-icon">🎯</div>
                <h4>欢迎使用竞赛AI助手！</h4>
                <p>我可以帮助您：</p>
                <ul>
                  <li>🏆 推荐适合的竞赛</li>
                  <li>📊 分析技能水平</li>
                  <li>📚 制定学习计划</li>
                  <li>💡 解答竞赛问题</li>
                </ul>
              </div>
            </div>
            
            <div
              v-for="message in messages"
              :key="message.id"
              class="message"
              :class="message.type"
            >
              <div v-if="message.type === 'assistant'" class="message-wrapper">
                <div class="message-avatar">🤖</div>
                <div class="message-info">
                  <span class="sender">AI助手</span>
                  <span class="time">{{ formatTime(message.timestamp) }}</span>
                </div>
              </div>
              <div class="message-content" :class="{ 'assistant-content': message.type === 'assistant' }">
                <div class="message-text" v-html="formatMessage(message.content)"></div>
                <!-- 打字光标 - 只在打字时显示 -->
                <span v-if="message.isTyping" class="typing-cursor">|</span>

                <!-- 快速建议按钮 -->
                <div v-if="message.suggestions && message.suggestions.length > 0" class="message-suggestions">
                  <button
                    v-for="suggestion in message.suggestions"
                    :key="suggestion"
                    @click="handleQuickAction(suggestion)"
                    class="suggestion-btn"
                  >
                    {{ suggestion }}
                  </button>
                </div>
              </div>
              <div v-if="message.type === 'user'" class="message-avatar">👤</div>
            </div>
            
            <!-- AI正在输入指示器 -->
            <div v-if="isLoading" class="typing-indicator">
              <div class="message-avatar">🤖</div>
              <div class="typing-dots">
                <span></span>
                <span></span>
                <span></span>
              </div>
            </div>
          </div>
          
          <!-- 快速问题按钮 -->
          <div v-if="quickQuestions.length > 0" class="quick-questions">
            <button
              v-for="question in quickQuestions"
              :key="question"
              @click="handleQuickMessage(question)"
              class="quick-question-btn"
            >
              {{ question }}
            </button>
          </div>
          
          <!-- 推荐竞赛卡片 -->
          <div v-if="showRecommendations && recommendedCompetitions.length > 0" class="recommended-competitions">
            <div class="recommendations-header">
              <h4>🎯 AI为您推荐</h4>
              <button @click="showRecommendations = false" class="close-recommendations" aria-label="关闭推荐">×</button>
            </div>
            <div class="competition-cards">
              <div
                v-for="competition in recommendedCompetitions"
                :key="competition.id"
                class="competition-card"
                @click="handleCompetitionClick(competition)"
              >
                <div class="card-header">
                  <h5>{{ competition.title }}</h5>
                  <span class="competition-category">{{ competition.category }}</span>
                </div>
                <div class="card-content">
                  <p class="competition-organizer">主办方: {{ competition.organizer }}</p>
                  <p class="competition-track">赛道: {{ competition.track }}</p>
                  <div v-if="competition.tags" class="competition-tags">
                    <span v-for="tag in competition.tags.split(',')" :key="tag" class="tag">
                      {{ tag.trim() }}
                    </span>
                  </div>
                </div>
                <div class="card-footer">
                  <span class="click-hint">点击了解更多 →</span>
                </div>
              </div>
            </div>
          </div>
          
          <!-- 输入区域 -->
          <div class="chat-input">
            <div class="input-wrapper">
              <textarea
                v-model="inputMessage"
                @keypress.enter.prevent="handleSendMessage"
                placeholder="询问竞赛相关问题..."
                class="message-input"
                :disabled="isLoading"
                rows="1"
                @input="autoResizeTextarea"
                ref="messageInput"
              ></textarea>
              <button 
                @click="handleSendMessage" 
                :disabled="!inputMessage.trim() || isLoading" 
                class="send-btn"
                aria-label="发送消息"
              >
                <span class="icon">➤</span>
              </button>
            </div>
          </div>
        </div>
      </div>
      
      <!-- 删除确认对话框 -->
      <div v-if="showDeleteConfirm" class="delete-confirm-modal" @click.self="cancelDeleteConversation">
        <div class="delete-confirm-content">
          <h4>确认删除</h4>
          <p>您确定要删除这个对话吗？此操作不可恢复。</p>
          <div class="delete-confirm-actions">
            <button @click="cancelDeleteConversation" class="cancel-btn" style="pointer-events: auto; z-index: 9999;">取消</button>
            <button @click="executeDeleteConversation" class="confirm-btn" style="pointer-events: auto; z-index: 9999;">删除</button>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, nextTick, watch } from 'vue'
import { useAI } from '../../composables/useAI.js'
import { formatAIMessage } from '../../utils/textFormatter.js'
import '../../styles/ai-component.css'
import '../../styles/ai-assistant.css'
import '../../styles/delete-confirm.css'

// 使用AI聊天组合式函数
const {
  messages,
  inputMessage,
  isLoading,
  isVisible,
  sendMessage,
  clearChat,
  toggleVisibility,
  handleQuickAction,
  initializeChat,
  chatHistory,
  currentConversationId,
  loadChatHistoryList,
  loadConversation,
  deleteConversation,
  getCurrentUser,
  isTyping,
  typewriterController
} = useAI()

// 本地组件状态
const messagesContainer = ref(null)
const messageInput = ref(null)
const showHistory = ref(false)
const unreadCount = ref(0)
const recommendedCompetitions = ref([])
const showRecommendations = ref(false)
const showDeleteConfirm = ref(false)
const conversationToDelete = ref(null)

// 拖拽相关状态
const isDragging = ref(false)
const dragOffset = ref({ x: 0, y: 0 })
const assistantElement = ref(null)

const quickQuestions = ref([
  '推荐竞赛',
  '技能分析',
  '热门竞赛',
  '新手指南'
])

// 切换历史记录显示
const toggleHistory = async () => {
  showHistory.value = !showHistory.value
  if (showHistory.value) {
    // 每次打开历史记录都重新加载，不使用缓存
    await loadChatHistoryList()
  }
}

// 格式化消息内容
const formatMessage = (content) => {
  return formatAIMessage(content)
}

// 格式化时间
const formatTime = (timestamp) => {
  if (!timestamp) return ''
  
  const date = new Date(timestamp)
  const now = new Date()
  const diff = now - date
  
  if (diff < 60000) {
    return '刚刚'
  } else if (diff < 3600000) {
    return `${Math.floor(diff / 60000)}分钟前`
  } else if (diff < 86400000) {
    return `${Math.floor(diff / 3600000)}小时前`
  } else {
    return date.toLocaleDateString()
  }
}

// 创建新对话
const createNewConversation = () => {
  console.log('创建新对话')
  // 清空所有状态
  messages.value = []
  currentConversationId.value = null
  inputMessage.value = ''
  showHistory.value = false
  
  // 添加欢迎消息
  const user = getCurrentUser()
  const welcomeMessage = user 
    ? `您好 ${user.username}！我是您的AI竞赛助手。\n\n我可以帮助您：\n• 推荐适合的竞赛\n• 分析您的技能水平\n• 制定学习计划\n• 解答竞赛相关问题\n\n有什么我可以帮助您的吗？`
    : `您好！我是AI竞赛助手。\n\n我可以帮助您：\n• 推荐适合的竞赛\n• 分析技能水平\n• 制定学习计划\n• 解答竞赛相关问题\n\n有什么我可以帮助您的吗？`
  
  messages.value = [{
    id: Date.now(),
    type: 'assistant',
    content: welcomeMessage,
    timestamp: new Date(),
    isWelcome: true
  }]
  
  console.log('新对话创建完成，消息数量:', messages.value.length)
}

// 切换助手显示状态
const toggleAssistant = () => {
  toggleVisibility()
  if (isVisible.value) {
    // 如果没有消息，显示欢迎消息
    if (messages.value.length === 0) {
      const user = getCurrentUser()
      const welcomeMessage = user 
        ? `您好 ${user.username}！我是您的AI竞赛助手。\n\n我可以帮助您：\n• 推荐适合的竞赛\n• 分析您的技能水平\n• 制定学习计划\n• 解答竞赛相关问题\n\n有什么我可以帮助您的吗？`
        : `您好！我是AI竞赛助手。\n\n我可以帮助您：\n• 推荐适合的竞赛\n• 分析技能水平\n• 制定学习计划\n• 解答竞赛相关问题\n\n有什么我可以帮助您的吗？`
      
      messages.value = [{
        id: Date.now(),
        type: 'assistant',
        content: welcomeMessage,
        timestamp: new Date(),
        isWelcome: true
      }]
    }
    
    nextTick(() => {
      scrollToBottom()
      if (messageInput.value) {
        messageInput.value.focus()
      }
    })
  }
}

// 发送消息的包装函数
const handleSendMessage = async () => {
  if (inputMessage.value.trim() && !isLoading.value) {
    await sendMessage()
    nextTick(() => {
      scrollToBottom()
      // 重置输入框高度
      if (messageInput.value) {
        messageInput.value.style.height = 'auto'
      }
    })
  }
}

// 自动调整文本框高度
const autoResizeTextarea = (e) => {
  const textarea = e.target
  textarea.style.height = 'auto'
  textarea.style.height = Math.min(textarea.scrollHeight, 120) + 'px'
}

// 处理快速问题
const handleQuickMessage = (question) => {
  const questionMap = {
    '推荐竞赛': '请为我推荐一些适合的竞赛',
    '技能分析': '请分析一下我的技能水平',
    '热门竞赛': '当前有哪些热门的竞赛类型？',
    '新手指南': '我是新手，应该如何开始参加竞赛？'
  }
  
  inputMessage.value = questionMap[question] || question
  
  // 如果是推荐，同时获取推荐卡片
  if (question === '推荐竞赛') {
    fetchRecommendedCompetitions()
  }
  
  // 自动调整文本框高度
  if (messageInput.value) {
    nextTick(() => {
      autoResizeTextarea({ target: messageInput.value })
    })
  }
  
  handleSendMessage()
}

// 获取推荐竞赛
const fetchRecommendedCompetitions = async () => {
  try {
    const userStr = localStorage.getItem('user')
    const user = userStr ? JSON.parse(userStr) : null
    const userId = user?.id || 6 // 从用户状态获取或使用默认值
    
    const response = await fetch(`http://localhost:8080/api/ai/recommendations`, {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
      },
      body: JSON.stringify({ userId })
    })
    
    if (response.ok) {
      const competitions = await response.json()
      recommendedCompetitions.value = competitions.slice(0, 3) // 只显示前3个
      showRecommendations.value = true
    } else {
      console.error('获取推荐竞赛失败:', response.status)
    }
  } catch (error) {
    console.error('获取推荐竞赛失败:', error)
  }
}

// 点击推荐卡片
const handleCompetitionClick = (competition) => {
  inputMessage.value = `请告诉我更多关于"${competition.title}"竞赛的信息`
  
  // 自动调整文本框高度
  if (messageInput.value) {
    nextTick(() => {
      autoResizeTextarea({ target: messageInput.value })
    })
  }
  
  handleSendMessage()
}

// 滚动到底部
const scrollToBottom = () => {
  if (messagesContainer.value) {
    messagesContainer.value.scrollTop = messagesContainer.value.scrollHeight
  }
}

// 监听打字状态,自动滚动
watch(isTyping, (newValue) => {
  if (newValue) {
    // 使用 requestAnimationFrame 确保滚动流畅
    const smoothScroll = () => {
      if (isTyping.value) {
        scrollToBottom()
        requestAnimationFrame(smoothScroll)
      }
    }
    requestAnimationFrame(smoothScroll)
  }
})

// 确认删除对话
const confirmDeleteConversation = (conversation) => {
  console.log('确认删除对话:', conversation)
  conversationToDelete.value = conversation
  showDeleteConfirm.value = true
}

// 执行删除对话
const executeDeleteConversation = async () => {
  console.log('executeDeleteConversation 被调用')
  console.log('conversationToDelete.value:', conversationToDelete.value)
  console.log('showDeleteConfirm.value:', showDeleteConfirm.value)
  
  if (conversationToDelete.value) {
    console.log('开始删除对话:', conversationToDelete.value)
    try {
      const success = await deleteConversation(conversationToDelete.value.id)
      console.log('删除操作结果:', success)
      
      if (success) {
        console.log('删除对话成功')
        // deleteConversation 方法内部已经处理了刷新，这里不需要重复调用
        alert('对话删除成功')
      } else {
        console.error('删除对话失败')
        alert('删除对话失败，请稍后再试')
      }
    } catch (error) {
      console.error('删除对话出错:', error)
      alert('删除对话时发生错误：' + error.message)
    }
  } else {
    console.error('没有要删除的对话')
    alert('没有选择要删除的对话')
  }
  
  // 无论成功失败都关闭对话框
  showDeleteConfirm.value = false
  conversationToDelete.value = null
  console.log('删除确认对话框已关闭')
}

// 取消删除对话
const cancelDeleteConversation = () => {
  showDeleteConfirm.value = false
  conversationToDelete.value = null
}

// 拖拽功能
const startDrag = (e) => {
  if (isVisible.value) return // 如果聊天窗口已打开，不允许拖拽
  
  isDragging.value = true
  const rect = assistantElement.value.getBoundingClientRect()
  
  // 计算鼠标相对于元素的偏移
  if (e.type === 'mousedown') {
    dragOffset.value = {
      x: e.clientX - rect.left,
      y: e.clientY - rect.top
    }
    document.addEventListener('mousemove', drag)
    document.addEventListener('mouseup', stopDrag)
  } else if (e.type === 'touchstart') {
    const touch = e.touches[0]
    dragOffset.value = {
      x: touch.clientX - rect.left,
      y: touch.clientY - rect.top
    }
    document.addEventListener('touchmove', drag, { passive: false })
    document.addEventListener('touchend', stopDrag)
  }
  
  e.preventDefault()
}

const drag = (e) => {
  if (!isDragging.value) return
  
  let clientX, clientY
  if (e.type === 'mousemove') {
    clientX = e.clientX
    clientY = e.clientY
  } else if (e.type === 'touchmove') {
    clientX = e.touches[0].clientX
    clientY = e.touches[0].clientY
    e.preventDefault()
  }
  
  // 计算新位置
  const newX = clientX - dragOffset.value.x
  const newY = clientY - dragOffset.value.y
  
  // 获取窗口尺寸
  const windowWidth = window.innerWidth
  const windowHeight = window.innerHeight
  const elementWidth = 60
  const elementHeight = 60
  
  // 限制在窗口范围内
  const constrainedX = Math.max(0, Math.min(newX, windowWidth - elementWidth))
  const constrainedY = Math.max(0, Math.min(newY, windowHeight - elementHeight))
  
  // 更新位置
  assistantElement.value.style.left = constrainedX + 'px'
  assistantElement.value.style.top = constrainedY + 'px'
  assistantElement.value.style.right = 'auto'
  assistantElement.value.style.transform = 'none'
}

const stopDrag = () => {
  if (!isDragging.value) return
  
  isDragging.value = false
  
  // 移除事件监听器
  document.removeEventListener('mousemove', drag)
  document.removeEventListener('mouseup', stopDrag)
  document.removeEventListener('touchmove', drag)
  document.removeEventListener('touchend', stopDrag)
  
  // 保存位置到localStorage
  const rect = assistantElement.value.getBoundingClientRect()
  localStorage.setItem('aiAssistantPosition', JSON.stringify({
    left: rect.left,
    top: rect.top
  }))
}

// 恢复保存的位置
const restorePosition = () => {
  const savedPosition = localStorage.getItem('aiAssistantPosition')
  if (savedPosition) {
    try {
      const position = JSON.parse(savedPosition)
      const windowWidth = window.innerWidth
      const windowHeight = window.innerHeight
      const elementWidth = 60
      const elementHeight = 60
      
      // 确保位置在窗口范围内
      const constrainedX = Math.max(0, Math.min(position.left, windowWidth - elementWidth))
      const constrainedY = Math.max(0, Math.min(position.top, windowHeight - elementHeight))
      
      assistantElement.value.style.left = constrainedX + 'px'
      assistantElement.value.style.top = constrainedY + 'px'
      assistantElement.value.style.right = 'auto'
      assistantElement.value.style.transform = 'none'
    } catch (error) {
      console.error('恢复AI助手位置失败:', error)
    }
  }
}

// 点击事件处理
const handleToggleClick = (e) => {
  // 如果是拖拽结束后的点击，忽略
  if (isDragging.value) return
  
  // 简单的点击检测：如果鼠标按下和抬起的位置相近，认为是点击
  const startPos = e.target.dataset.startPos
  if (startPos) {
    const start = JSON.parse(startPos)
    const distance = Math.sqrt(
      Math.pow(e.clientX - start.x, 2) + Math.pow(e.clientY - start.y, 2)
    )
    if (distance > 5) return // 如果移动距离超过5px，认为是拖拽而不是点击
  }
  
  toggleAssistant()
}

// 记录鼠标按下位置
const handleMouseDown = (e) => {
  e.target.dataset.startPos = JSON.stringify({ x: e.clientX, y: e.clientY })
  startDrag(e)
}

// 组件挂载时初始化
onMounted(async () => {
  initializeChat()
  // 预加载聊天历史列表（不使用缓存）
  await loadChatHistoryList()
  
  // 恢复保存的位置
  nextTick(() => {
    if (assistantElement.value) {
      restorePosition()
    }
  })
})
</script>