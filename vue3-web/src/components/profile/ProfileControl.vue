<template>
  <div class="profile-control">
    <!-- 基本信息卡片 -->
    <div class="info-card">
      <div class="card-header">
        <h3>基本信息</h3>
        <button @click="toggleEditMode" class="edit-btn">
          {{ isEditing ? '取消' : '编辑' }}
        </button>
      </div>
      
      <div class="card-content">
        <div class="avatar-section">
          <div class="avatar-container">
            <img 
              v-if="userInfo.avatarUrl"
              :src="userInfo.avatarUrl"
              alt="头像"
              class="avatar-img"
            />
            <div v-else class="avatar-placeholder">
              {{ userInfo.username?.charAt(0)?.toUpperCase() || 'U' }}
            </div>
            <button v-if="isEditing" @click="uploadAvatar" class="avatar-upload-btn">
              <svg width="16" height="16" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                <path d="M12 16V8M8 12L12 8L16 12" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
                <path d="M16 16H8" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
              </svg>
              上传头像
            </button>
          </div>
        </div>

        <div class="info-grid">
          <div class="info-item">
            <label>用户名</label>
            <input 
              v-if="isEditing" 
              v-model="editForm.username" 
              type="text" 
              class="form-input"
            />
            <span v-else class="info-value">{{ userInfo.username || '未设置' }}</span>
          </div>

          <div class="info-item">
            <label>邮箱</label>
            <input 
              v-if="isEditing" 
              v-model="editForm.email" 
              type="email" 
              class="form-input"
            />
            <span v-else class="info-value">{{ userInfo.email || '未设置' }}</span>
          </div>

          <div class="info-item">
            <label>专业</label>
            <input 
              v-if="isEditing" 
              v-model="editForm.major" 
              type="text" 
              class="form-input"
            />
            <span v-else class="info-value">{{ userInfo.major || '未设置' }}</span>
          </div>

          <div class="info-item">
            <label>学号</label>
            <input 
              v-if="isEditing" 
              v-model="editForm.studentId" 
              type="text" 
              class="form-input"
            />
            <span v-else class="info-value">{{ userInfo.studentId || '未设置' }}</span>
          </div>

          <div class="info-item full-width">
            <label>个人简介</label>
            <textarea 
              v-if="isEditing" 
              v-model="editForm.bio" 
              class="form-textarea"
              placeholder="介绍一下自己..."
            ></textarea>
            <span v-else class="info-value">{{ userInfo.bio || '暂无个人简介' }}</span>
          </div>
        </div>

        <div v-if="isEditing" class="form-actions">
          <button @click="saveUserInfo" class="save-btn" :disabled="saving">
            {{ saving ? '保存中...' : '保存' }}
          </button>
          <button @click="cancelEdit" class="cancel-btn">取消</button>
        </div>
      </div>
    </div>

    <!-- 技能管理卡片 -->
    <div class="info-card">
      <div class="card-header">
        <h3>技能标签</h3>
        <button @click="showAddSkill = true" class="add-btn">
          <svg width="16" height="16" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
            <path d="M12 5V19M5 12H19" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
          </svg>
          添加技能
        </button>
      </div>
      
      <div class="card-content">
        <div v-if="skills.length === 0" class="empty-state">
          <p>暂无技能标签，快来添加一些吧！</p>
        </div>
        <div v-else class="skills-grid">
          <div 
            v-for="skill in skills" 
            :key="skill.id" 
            class="skill-tag"
          >
            <span class="skill-name">{{ skill.skill }}</span>
            <button @click="removeSkill(skill.id)" class="remove-skill-btn">×</button>
          </div>
        </div>

        <!-- 添加技能表单 -->
        <div v-if="showAddSkill" class="add-skill-form">
          <input 
            v-model="newSkill" 
            type="text" 
            placeholder="输入技能名称"
            class="skill-input"
            @keyup.enter="addSkill"
          />
          <button @click="addSkill" class="confirm-btn">添加</button>
          <button @click="cancelAddSkill" class="cancel-btn">取消</button>
        </div>
      </div>
    </div>

    <!-- 荣誉墙卡片 -->
    <div class="info-card">
      <div class="card-header">
        <h3>荣誉墙</h3>
        <button @click="showAddHonor = true" class="add-btn">
          <svg width="16" height="16" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
            <path d="M12 5V19M5 12H19" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
          </svg>
          添加荣誉
        </button>
      </div>
      
      <div class="card-content">
        <div v-if="honors.length === 0" class="empty-state">
          <p>暂无荣誉记录，快来添加一些成就吧！</p>
        </div>
        <div v-else class="honors-list">
          <div
            v-for="honor in honors"
            :key="honor.id"
          >
            <div class="honor-item">
              <div class="honor-icon">🏆</div>
              <div class="honor-content">
                <h4 class="honor-title">{{ honor.title }}</h4>
                <p class="honor-description">{{ honor.description }}</p>
                <span class="honor-date">{{ formatDate(honor.date) }}</span>
                <div v-if="honor.certificateImageUrl" class="honor-certificate">
                  <button @click="viewCertificate(honor.certificateImageUrl)" class="view-certificate-btn">
                    📜 查看证书
                  </button>
                </div>
              </div>
              <div class="honor-actions">
                <button @click="editHonor(honor)" class="edit-honor-btn" title="编辑">
                  <svg width="16" height="16" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                    <path d="M11 4H4C3.46957 4 2.96086 4.21071 2.58579 4.58579C2.21071 4.96086 2 5.46957 2 6V20C2 20.5304 2.21071 21.0391 2.58579 21.4142C2.96086 21.7893 3.46957 22 4 22H18C18.5304 22 19.0391 21.7893 19.4142 21.4142C19.7893 21.0391 20 20.5304 20 20V13" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
                    <path d="M18.5 2.50023C18.8978 2.1024 19.4374 1.87891 20 1.87891C20.5626 1.87891 21.1022 2.1024 21.5 2.50023C21.8978 2.89805 22.1213 3.43762 22.1213 4.00023C22.1213 4.56284 21.8978 5.1024 21.5 5.50023L12 15.0002L8 16.0002L9 12.0002L18.5 2.50023Z" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
                  </svg>
                </button>
                <button @click="removeHonor(honor.id)" class="remove-honor-btn" title="删除">
                  <svg width="16" height="16" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                    <path d="M18 6L6 18M6 6L18 18" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
                  </svg>
                </button>
              </div>
            </div>

            <!-- 编辑荣誉表单 - 放置在当前荣誉下方 -->
            <div v-if="showEditHonor && editingHonor && editingHonor.id === honor.id" class="edit-honor-form">
              <h4>编辑荣誉</h4>
              <input
                v-model="editingHonor.title"
                type="text"
                placeholder="荣誉标题"
                class="form-input"
              />
              <textarea
                v-model="editingHonor.description"
                placeholder="荣誉描述"
                class="form-textarea"
              ></textarea>
              <input
                v-model="editingHonor.date"
                type="date"
                class="form-input"
              />
              <div class="certificate-upload-section">
                <label class="upload-label">证书文件</label>
                <div class="upload-input-group">
                  <button @click="uploadCertificate" class="upload-certificate-btn" type="button">
                    <svg width="16" height="16" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                      <path d="M12 16V8M8 12L12 8L16 12" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
                      <path d="M16 16H8" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
                    </svg>
                    上传证书
                  </button>
                  <span v-if="editingHonor.certificateFileName" class="file-name">
                    {{ editingHonor.certificateFileName }}
                  </span>
                  <span v-else class="file-hint">支持图片、PDF、Word文档，最大3MB</span>
                </div>
              </div>
              <div class="form-actions">
                <button @click="updateHonor" class="confirm-btn">保存</button>
                <button @click="cancelEditHonor" class="cancel-btn">取消</button>
              </div>
            </div>
          </div>
        </div>

        <!-- 添加荣誉表单 -->
        <div v-if="showAddHonor" class="add-honor-form">
          <input
            v-model="newHonor.title"
            type="text"
            placeholder="荣誉标题"
            class="form-input"
          />
          <textarea
            v-model="newHonor.description"
            placeholder="荣誉描述"
            class="form-textarea"
          ></textarea>
          <input
            v-model="newHonor.date"
            type="date"
            class="form-input"
          />
          <div class="certificate-upload-section">
            <label class="upload-label">证书文件</label>
            <div class="upload-input-group">
              <button @click="uploadCertificate" class="upload-certificate-btn" type="button">
                <svg width="16" height="16" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                  <path d="M12 16V8M8 12L12 8L16 12" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
                  <path d="M16 16H8" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
                </svg>
                上传证书
              </button>
              <span v-if="newHonor.certificateFileName" class="file-name">
                {{ newHonor.certificateFileName }}
              </span>
              <span v-else class="file-hint">支持图片、PDF、Word文档，最大3MB</span>
            </div>
          </div>
          <div class="form-actions">
            <button @click="addHonor" class="confirm-btn">添加</button>
            <button @click="cancelAddHonor" class="cancel-btn">取消</button>
          </div>
        </div>
      </div>
    </div>

    <!-- 头像上传隐藏input -->
    <input
      ref="avatarInput"
      type="file"
      accept="image/*"
      @change="handleAvatarUpload"
      style="display: none"
    />

    <!-- 证书上传隐藏input -->
    <input
      ref="certificateInput"
      type="file"
      accept="image/*,.pdf,.doc,.docx,application/pdf,application/msword,application/vnd.openxmlformats-officedocument.wordprocessingml.document"
      @change="handleCertificateUpload"
      style="display: none"
    />
  </div>
</template>

<script setup>
import { onMounted } from 'vue'
import { useProfileControl } from '../../composables/useProfileControl.js'

const {
  // 响应式数据
  isEditing,
  saving,
  showAddSkill,
  showAddHonor,
  showEditHonor,
  newSkill,
  avatarInput,
  certificateInput,
  uploadingCertificate,
  userInfo,
  editForm,
  skills,
  honors,
  newHonor,
  editingHonor,

  // 方法
  loadUserInfo,
  loadUserSkills,
  loadUserHonors,
  toggleEditMode,
  cancelEdit,
  saveUserInfo,
  uploadAvatar,
  handleAvatarUpload,
  addSkill,
  removeSkill,
  cancelAddSkill,
  addHonor,
  removeHonor,
  cancelAddHonor,
  editHonor,
  updateHonor,
  cancelEditHonor,
  viewCertificate,
  formatDate,
  uploadCertificate,
  handleCertificateUpload
} = useProfileControl()

onMounted(async () => {
  await loadUserInfo()
  await loadUserSkills()
  await loadUserHonors()
})
</script>

<style scoped>
@import '../../styles/profile-control.css';
</style>