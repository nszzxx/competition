<template>
  <div class="modal-overlay" v-if="showModal" @click="closeModal">
    <div class="modal-container" @click.stop>
      <div class="modal-header">
        <h3>创建团队</h3>
        <button class="close-btn" @click="closeModal">&times;</button>
      </div>
      <div class="modal-body">
        <form @submit.prevent="handleTeamSubmit">
          <div class="form-group">
            <label for="teamName">团队名称 *</label>
            <input 
              type="text" 
              id="teamName" 
              v-model="teamForm.name" 
              required 
              placeholder="请输入团队名称"
            />
            <small class="form-text">团队名称将在竞赛中显示，请选择有意义的名称</small>
          </div>
          
          <div class="form-group">
            <label>队长信息</label>
            <div class="leader-info" v-if="currentUser">
              <div class="user-avatar">
                <img :src="currentUser.avatarUrl || '/default-avatar.png'" :alt="currentUser.realName || currentUser.username" />
              </div>
              <div class="user-details">
                <h4>{{ currentUser.realName || currentUser.username }}</h4>
                <p>{{ currentUser.major || '未设置专业' }}</p>
                <p>{{ currentUser.email || '未设置邮箱' }}</p>
              </div>
            </div>
          </div>
          
          <div class="form-group">
            <label for="teamDescription">团队描述</label>
            <textarea 
              id="teamDescription" 
              v-model="teamForm.description" 
              placeholder="简单介绍一下你的团队理念、目标或特色..."
              rows="3"
            ></textarea>
          </div>
          
          <div class="form-group">
            <label>招募信息</label>
            <div class="recruitment-section">
              <div class="checkbox-group">
                <label class="checkbox-label">
                  <input type="checkbox" v-model="teamForm.isRecruiting" />
                  <span>开放招募团队成员</span>
                </label>
              </div>
              
              <div v-if="teamForm.isRecruiting" class="recruitment-details">
                <div class="form-row">
                  <div class="form-col">
                    <label for="maxMembers">最大成员数</label>
                    <select id="maxMembers" v-model="teamForm.maxMembers" class="form-select">
                      <option value="2">2人</option>
                      <option value="3">3人</option>
                      <option value="4">4人</option>
                      <option value="5">5人</option>
                      <option value="6">6人</option>
                    </select>
                  </div>
                </div>
                
                <div class="form-group">
                  <label for="requiredSkills">需要的技能</label>
                  <input
                    id="requiredSkills"
                    v-model="teamForm.needSkills"
                    type="text"
                    placeholder="例如：Java, Python, 前端开发, UI设计"
                    class="form-input"
                  />
                  <small class="form-text">可使用中文或英文逗号分隔多个技能</small>
                </div>
                
                <div class="form-group">
                  <label for="recruitmentMessage">招募说明</label>
                  <textarea
                    id="recruitmentMessage"
                    v-model="teamForm.recruitmentMessage"
                    placeholder="告诉其他同学你们在寻找什么样的队友..."
                    class="form-textarea"
                    rows="3"
                  ></textarea>
                </div>
              </div>
            </div>
          </div>
          
          <div class="form-actions">
            <button type="button" class="btn-cancel" @click="closeModal">取消</button>
            <button type="submit" class="btn-submit" :disabled="isSubmitting">
              {{ isSubmitting ? '创建中...' : '创建团队' }}
            </button>
          </div>
        </form>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, watch } from 'vue'

// 定义 props
const props = defineProps({
  showModal: {
    type: Boolean,
    default: false
  },
  currentUser: {
    type: Object,
    default: null
  },
  competitionId: {
    type: [Number, String],
    default: null
  }
})

// 定义 emits
const emit = defineEmits(['update:showModal', 'success'])

// 表单状态
const isSubmitting = ref(false)
const teamForm = ref({
  name: '',
  description: '',
  needSkills: '',
  competitionId: props.competitionId,
  leaderId: props.currentUser?.id,
  isRecruiting: true,
  maxMembers: 5,
  recruitmentMessage: ''
})

// 监听 props 变化
watch(() => props.competitionId, (newVal) => {
  teamForm.value.competitionId = newVal
})

watch(() => props.currentUser, (newVal) => {
  if (newVal) {
    teamForm.value.leaderId = newVal.id
  }
})

// 关闭弹窗
const closeModal = () => {
  emit('update:showModal', false)
  // 重置表单
  resetForm()
}

// 重置表单
const resetForm = () => {
  teamForm.value = {
    name: '',
    description: '',
    needSkills: '',
    competitionId: props.competitionId,
    leaderId: props.currentUser?.id,
    isRecruiting: true,
    maxMembers: 5,
    recruitmentMessage: ''
  }
}

// 处理团队创建提交
const handleTeamSubmit = async () => {
  if (!props.currentUser || !props.competitionId) {
    alert('缺少必要信息，请重新登录')
    return
  }
  
  try {
    isSubmitting.value = true
    
    // 导入API
    const { teamApi } = await import('../../utils/api.js')
    
    // 提交创建请求
    await teamApi.create({
      name: teamForm.value.name,
      description: teamForm.value.description,
      needSkills: teamForm.value.needSkills,
      competitionId: props.competitionId,
      leaderId: props.currentUser.id,
      isRecruiting: teamForm.value.isRecruiting,
      maxMembers: teamForm.value.maxMembers,
      recruitmentMessage: teamForm.value.recruitmentMessage
    })
    
    // 关闭弹窗
    closeModal()
    
    // 触发成功事件
    emit('success')
    
    // 显示成功消息
    alert('团队创建成功！')
  } catch (error) {
    console.error('创建团队失败:', error)
    alert(`创建团队失败: ${error.message || '未知错误'}`)
  } finally {
    isSubmitting.value = false
  }
}
</script>

<style scoped>
@import '../../styles/team-modal.css';
</style>