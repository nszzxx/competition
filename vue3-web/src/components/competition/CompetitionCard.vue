<template>
  <div class="competition-card" @click="$emit('view', competition.id)">
    <div class="card-header">
      <h3 class="competition-title">{{ competition.title }}</h3>
      <span class="competition-category">{{ competition.category }}</span>
    </div>
    
    <div class="card-body">
      <div class="competition-info">
        <p class="competition-organizer">
          <span class="icon">🏢</span>
          主办方: {{ competition.organizer }}
        </p>
        <p class="competition-track">
          <span class="icon">🏆</span>
          赛道: {{ competition.track }}
        </p>
        <p class="competition-type">
          <span class="icon">👥</span>
          参赛方式: {{ formatParticipationType(competition.participationMode) }}
        </p>
      </div>
      
      <p class="competition-description">{{ competition.description }}</p>
      
      <div class="competition-time">
        <div class="time-item">
          <span class="icon">📅</span>
          <span class="time-label">比赛时间</span>
          <span class="time-value">{{ formatDate(competition.startTime) }} - {{ formatDate(competition.endTime) }}</span>
        </div>
        <div class="time-item">
          <span class="icon">🕒</span>
          <span class="time-label">报名时间</span>
          <span class="time-value">{{ formatDate(competition.patiStarttime) }} - {{ formatDate(competition.patiEndtime) }}</span>
        </div>
        <div class="time-item" v-if="getRegistrationStatus">
          <span class="icon">📌</span>
          <span class="time-label">报名状态</span>
          <span class="registration-status" :class="getRegistrationStatusClass">
            {{ getRegistrationStatusText }}
          </span>
        </div>
      </div>
      
      <div class="competition-tags" v-if="tagList.length > 0">
        <span v-for="tag in tagList" :key="tag" class="tag">
          {{ tag }}
        </span>
      </div>
    </div>
    
    <div class="card-footer">
      <!-- 根据参赛状态显示不同按钮 -->
      <button
        v-if="!isJoined"
        class="btn btn-primary"
        @click.stop="$emit('join', competition)"
        :disabled="getRegistrationStatus !== 1"
        :title="getRegistrationStatus !== 1 ? getRegistrationStatusText : ''"
      >
        报名参加
      </button>
      <button
        v-else
        class="btn btn-danger"
        @click.stop="$emit('cancel', competition.id)"
      >
        取消参赛
      </button>
      <button
        class="btn btn-secondary"
        @click.stop="$emit('view', competition.id)"
      >
        查看详情
      </button>
    </div>
    
  </div>
</template>

<script setup>
import { useCompetitionCard } from '../../composables/CompetitionCard.js'

  const props = defineProps({
  competition: {
    type: Object,
    required: true
  },
  isJoined: {
    type: Boolean,
    default: false
  }
})

defineEmits(['view', 'join', 'cancel'])

// 使用竞赛卡片组合式函数
const {
  tagList,
  formatDate,
  formatParticipationType,
  getRegistrationStatus,
  getRegistrationStatusText,
  getRegistrationStatusClass
} = useCompetitionCard(props)
</script>

<style>
@import '../../styles/competition-component.css';
</style>