<template>
  <div class="team-create-page">
    <div class="page-container">
      <header class="page-header">
        <button class="back-btn" @click="goBack">
          ← 返回
        </button>
        <h1>创建团队</h1>
      </header>

      <main class="create-form-container">
        <div class="form-card">
          <div class="competition-info" v-if="competition">
            <h2>参赛竞赛</h2>
            <div class="competition-card">
              <h3>{{ competition.title }}</h3>
              <p>{{ competition.category }} - {{ competition.track }}</p>
              <p class="competition-desc">{{ competition.description }}</p>
            </div>
          </div>

          <form @submit.prevent="handleCreateTeam" class="team-form">
            <h2>团队信息</h2>
            
            <div class="form-group">
              <label for="teamName">团队名称 *</label>
              <input
                id="teamName"
                v-model="form.name"
                type="text"
                placeholder="请输入团队名称"
                required
                class="form-input"
              />
              <small class="form-hint">团队名称将在竞赛中显示，请选择有意义的名称</small>
            </div>

            <div class="form-group">
              <label>队长信息</label>
              <div class="leader-info" v-if="currentUser">
                <div class="user-avatar">
                  <img :src="currentUser.avatarUrl || '/default-avatar.png'" :alt="currentUser.realName" />
                </div>
                <div class="user-details">
                  <h4>{{ currentUser.realName || currentUser.username }}</h4>
                  <p>{{ currentUser.major || '未设置专业' }}</p>
                  <p>{{ currentUser.email }}</p>
                </div>
              </div>
            </div>

            <div class="form-group">
              <label>团队描述</label>
              <textarea
                v-model="form.description"
                placeholder="简单介绍一下你的团队理念、目标或特色..."
                class="form-textarea"
                rows="4"
              ></textarea>
            </div>

            <div class="form-group">
              <label>招募信息</label>
              <div class="recruitment-section">
                <div class="checkbox-group">
                  <label class="checkbox-label">
                    <input type="checkbox" v-model="form.isRecruiting" />
                    <span>开放招募团队成员</span>
                  </label>
                </div>
                
                <div v-if="form.isRecruiting" class="recruitment-details">
                  <div class="form-row">
                    <div class="form-col">
                      <label for="maxMembers">最大成员数</label>
                      <select id="maxMembers" v-model="form.maxMembers" class="form-select">
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
                      v-model="form.requiredSkills"
                      type="text"
                      placeholder="例如：Java, Python, 前端开发, UI设计"
                      class="form-input"
                    />
                    <small class="form-hint">用逗号分隔多个技能</small>
                  </div>
                  
                  <div class="form-group">
                    <label for="recruitmentMessage">招募说明</label>
                    <textarea
                      id="recruitmentMessage"
                      v-model="form.recruitmentMessage"
                      placeholder="告诉其他同学你们在寻找什么样的队友..."
                      class="form-textarea"
                      rows="3"
                    ></textarea>
                  </div>
                </div>
              </div>
            </div>

            <div class="form-actions">
              <button type="button" class="btn-secondary" @click="goBack">
                取消
              </button>
              <button type="submit" class="btn-primary" :disabled="loading">
                {{ loading ? '创建中...' : '创建团队' }}
              </button>
            </div>
          </form>

          <div v-if="error" class="error-message">
            {{ error }}
          </div>
        </div>
      </main>
    </div>
  </div>
</template>

<script setup>
// 使用团队创建组合式函数
import { useTeamCreate } from '../composables/useTeam.js'

const {
  // 响应式状态
  competition,
  currentUser,
  loading,
  error,
  form,
  
  // 方法
  handleCreateTeam,
  goBack
} = useTeamCreate()
</script>

<style scoped>
@import '../styles/views-team.css';
</style>
