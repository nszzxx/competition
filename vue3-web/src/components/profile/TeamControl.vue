<template>
  <div class="team-control">
    <!-- 标签页切换 -->
    <div class="team-tabs">
      <button 
        v-for="tab in tabs" 
        :key="tab.key"
        @click="setActiveTab(tab.key)"
        :class="['tab-button', { active: activeTab === tab.key }]"
      >
        <span class="tab-icon">{{ tab.icon }}</span>
        <span class="tab-text">{{ tab.label }}</span>
      </button>
    </div>

    <!-- 团队列表容器 -->
    <div class="teams-container">
      <div class="teams-header">
        <h3>{{ tabs.find(t => t.key === activeTab)?.label }}</h3>
      </div>
      
      <div class="teams-content">
        <!-- 加载状态 -->
        <div v-if="loading" class="loading-state">
          <p>正在加载团队信息...</p>
        </div>

        <!-- 空状态 -->
        <div v-else-if="teams[activeTab].length === 0" class="empty-state">
          <div class="empty-icon">
            {{ activeTab === 'created' ? '👑' : activeTab === 'applied' ? '⏳' : '👥' }}
          </div>
          <p>
            {{ activeTab === 'created' ? '您还没有创建任何团队' : 
               activeTab === 'applied' ? '您还没有申请加入任何团队' : 
               '您还没有加入任何团队' }}
          </p>
        </div>

        <!-- 团队列表 -->
        <div v-else class="teams-grid">
          <!-- 已创建的团队 -->
          <div 
            v-if="activeTab === 'created'"
            v-for="team in teams.created" 
            :key="team.id" 
            class="team-card"
          >
            <div class="team-header">
              <div class="team-info">
                <h4>{{ team.name }}</h4>
<!--                <div class="team-competition">{{ team.competitionName || '竞赛信息' }}</div>-->
              </div>
            </div>
            
            <div class="team-description">
              {{ team.description || '暂无描述' }}
            </div>

            <div v-if="team.needSkills" class="team-skills">
              <h5>需要技能：</h5>
              <div class="skills-list">
                <span 
                  v-for="skill in team.needSkills.split(',')" 
                  :key="skill" 
                  class="skill-tag"
                >
                  {{ skill.trim() }}
                </span>
              </div>
            </div>

            <div class="team-meta">
              <span>创建时间：{{ formatDate(team.createdAt) }}</span>
              <span>成员数量：{{ team.memberCount || 0 }}</span>
            </div>

            <div class="team-actions">
              <button @click="viewTeamMembers(team)" class="action-btn btn-primary">
                查看成员
              </button>
              <button @click="openInviteModal(team)" class="action-btn btn-secondary">
                邀请成员
              </button>
              <button @click="dissolveTeam(team.id)" class="action-btn btn-danger">
                解散团队
              </button>
            </div>
          </div>

          <!-- 申请中的团队 -->
<!--          <div -->
<!--            v-if="activeTab === 'applied'"-->
<!--            v-for="application in teams.applied" -->
<!--            :key="application.id" -->
<!--            class="team-card"-->
<!--          >-->
<!--            <div class="team-header">-->
<!--              <div class="team-info">-->
<!--                <h4>{{ application.teamName }}</h4>-->
<!--                <div class="team-competition">{{ application.competitionName || '竞赛信息' }}</div>-->
<!--              </div>-->
<!--              <span :class="['team-status', getStatusClass(application.status)]">-->
<!--                {{ getStatusText(application.status) }}-->
<!--              </span>-->
<!--            </div>-->
<!--            -->
<!--            <div class="team-description">-->
<!--              申请时间：{{ formatDate(application.applicationTime) }}-->
<!--            </div>-->

<!--            <div v-if="application.message" class="team-description">-->
<!--              申请理由：{{ application.message }}-->
<!--            </div>-->

<!--            <div class="team-actions">-->
<!--              <button -->
<!--                @click="cancelApplication(application.id)" -->
<!--                class="action-btn btn-warning"-->
<!--                v-if="application.status === 'PENDING'"-->
<!--              >-->
<!--                取消申请-->
<!--              </button>-->
<!--            </div>-->
<!--          </div>-->

          <!-- 已加入的团队 -->
          <div 
            v-if="activeTab === 'joined'"
            v-for="team in teams.joined" 
            :key="team.id" 
            class="team-card"
          >
            <div class="team-header">
              <div class="team-info">
                <h4>{{ team.name }}</h4>
                <div class="team-competition">{{ team.competitionName || '竞赛信息' }}</div>
              </div>
            </div>
            
            <div class="team-description">
              {{ team.description || '暂无描述' }}
            </div>

            <div v-if="team.needSkills" class="team-skills">
              <h5>团队技能：</h5>
              <div class="skills-list">
                <span 
                  v-for="skill in team.needSkills.split(',')" 
                  :key="skill" 
                  class="skill-tag"
                >
                  {{ skill.trim() }}
                </span>
              </div>
            </div>

            <div class="team-meta">
              <span>队长：{{ team.leaderName || '未知' }}</span>
              <span>成员数量：{{ team.memberCount || 0 }}</span>
            </div>

            <div class="team-actions">
              <button @click="viewTeamMembers(team)" class="action-btn btn-primary">
                查看成员
              </button>
              <button @click="leaveTeam(team.id)" class="action-btn btn-warning">
                退出团队
              </button>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- 邀请成员模态框 -->
    <div v-if="showInviteModal" class="modal-overlay" @click="closeInviteModal">
      <div class="modal-content" @click.stop>
        <div class="modal-header">
          <h3 class="modal-title">邀请成员加入团队</h3>
          <button @click="closeInviteModal" class="close-btn">×</button>
        </div>
        
        <div class="modal-body">
          <div class="form-group">
            <label class="form-label">用户标识</label>
            <input
              v-model="inviteForm.identifier"
              type="text"
              placeholder="输入用户名、邮箱或手机号"
              class="form-input"
            />
            <p class="form-hint">请输入要邀请用户的用户名、邮箱或手机号码</p>
          </div>

          <div class="form-group">
            <label class="form-label">邀请消息</label>
            <textarea
              v-model="inviteForm.message"
              placeholder="写一些邀请的理由..."
              class="form-textarea"
            ></textarea>
          </div>
        </div>

        <div class="modal-actions">
          <button @click="sendInvite" class="btn-confirm">发送邀请</button>
          <button @click="closeInviteModal" class="btn-cancel">取消</button>
        </div>
      </div>
    </div>

    <!-- 查看成员模态框 -->
    <div v-if="showMemberModal" class="modal-overlay" @click="closeMemberModal">
      <div class="modal-content" @click.stop>
        <div class="modal-header">
          <h3 class="modal-title">团队成员</h3>
          <button @click="closeMemberModal" class="close-btn">×</button>
        </div>
        
        <div class="modal-body">
          <div v-if="teamMembers.length === 0" class="empty-state">
            <p>暂无团队成员</p>
          </div>
          <div v-else class="members-list">
            <div 
              v-for="member in teamMembers" 
              :key="member.userId" 
              class="member-item"
            >
              <div class="member-avatar">
                <img 
                  v-if="member.avatarUrl"
                  :src="member.avatarUrl"
                  :alt="member.displayName || member.realName || member.username"
                  class="avatar-img"
                />
                <div v-else class="avatar-placeholder">
                  {{ (member.displayName || member.realName || member.username)?.charAt(0)?.toUpperCase() || 'U' }}
                </div>
              </div>
              <div class="member-info">
                <div class="member-name">
                  {{ member.realName || member.displayName || member.username || '未知用户' }}
                </div>
                <div class="member-username" v-if="member.realName || member.displayName">
                  @{{ member.username }}
                </div>
                <div class="member-role">{{ getRoleDisplayText(member.role) }}</div>
                <div class="member-major" v-if="member.major">
                  专业：{{ member.major }}
                </div>
                <div class="member-join-time">
                  加入时间：{{ formatDate(member.joinedAt || member.createTime) }}
                </div>
              </div>
              <div class="member-actions">
                <button 
                  @click="viewMemberDetail(member)" 
                  class="action-btn btn-info"
                  title="查看详情"
                >
                  查看详情
                </button>
                <button 
                  v-if="selectedTeam?.leaderId === currentUser?.id && member.userId !== currentUser?.id"
                  @click="removeMember(member.userId)" 
                  class="action-btn btn-danger"
                  title="移除成员"
                >
                  移除
                </button>
              </div>
            </div>
          </div>
        </div>


      </div>
    </div>

    <!-- 成员详情模态框 -->
    <MemberDetail
      v-model:showModal="showMemberDetailModal"
      :member="selectedMember"
      :teamId="selectedTeam?.id"
      :currentUserId="currentUser?.id"
    />
  </div>
</template>

<script setup>
import { onMounted } from 'vue'
import { useTeamControl } from '../../composables/useTeamControl.js'
import MemberDetail from './MemberDetail.vue'

const {
  // 响应式数据
  loading,
  currentUser,
  activeTab,
  showInviteModal,
  showMemberModal,
  showMemberDetailModal,
  selectedTeam,
  selectedMember,
  teams,
  teamMembers,
  searchKeyword,
  inviteForm,
  tabs,

  // 方法
  loadUserInfo,
  loadTeams,
  setActiveTab,
  viewTeamMembers,
  viewMemberDetail,
  getRoleDisplayText,
  removeMember,
  dissolveTeam,
  leaveTeam,
  cancelApplication,
  openInviteModal,
  sendInvite,
  closeInviteModal,
  closeMemberModal,
  getStatusText,
  getStatusClass
} = useTeamControl()

// 格式化日期的辅助函数
const formatDate = (dateString) => {
  if (!dateString) return '未知'
  const date = new Date(dateString)
  return date.toLocaleDateString('zh-CN')
}

onMounted(async () => {
  loadUserInfo()
  await loadTeams()
})
</script>

<style scoped>
@import '../../styles/team-control.css';
</style>