<template>
  <div class="participate-test">
    <h1>参赛功能测试</h1>
    
    <div class="test-section">
      <h2>测试竞赛信息</h2>
      <div class="competition-info">
        <p><strong>竞赛名称:</strong> {{ testCompetition.title }}</p>
        <p><strong>参赛模式:</strong> {{ testCompetition.participationMode }}</p>
        <p><strong>竞赛ID:</strong> {{ testCompetition.id }}</p>
      </div>
      
      <button @click="openParticipateModal" class="test-btn">
        测试参赛弹窗
      </button>
    </div>
    
    <div class="test-section">
      <h2>模式切换测试</h2>
      <div class="mode-buttons">
        <button @click="setMode('individual')" class="mode-btn">个人参赛模式</button>
        <button @click="setMode('team')" class="mode-btn">团队参赛模式</button>
        <button @click="setMode('both')" class="mode-btn">两者均可模式</button>
      </div>
    </div>
    
    <!-- 参赛弹窗 -->
    <Participate 
      :visible="participateModalVisible"
      :competition="testCompetition"
      @close="participateModalVisible = false"
      @success="handleParticipateSuccess"
    />
  </div>
</template>

<script setup>
import { ref } from 'vue'
import Participate from '../components/competition/Participate.vue'

const participateModalVisible = ref(false)

const testCompetition = ref({
  id: 1,
  title: '测试竞赛',
  description: '这是一个用于测试参赛功能的竞赛',
  participationMode: 'both',
  category: '编程竞赛',
  organizer: '测试机构',
  patiStarttime: new Date(Date.now() - 86400000), // 昨天开始报名
  patiEndtime: new Date(Date.now() + 86400000 * 7) // 一周后结束报名
})

const openParticipateModal = () => {
  participateModalVisible.value = true
}

const setMode = (mode) => {
  testCompetition.value.participationMode = mode
  console.log('切换到模式:', mode)
}

const handleParticipateSuccess = (result) => {
  participateModalVisible.value = false
  console.log('参赛成功:', result)
  alert(`参赛成功！模式: ${result.mode}`)
}
</script>

<style scoped>
.participate-test {
  padding: 20px;
  max-width: 800px;
  margin: 0 auto;
}

.test-section {
  margin-bottom: 30px;
  padding: 20px;
  border: 1px solid #e5e7eb;
  border-radius: 8px;
}

.competition-info {
  background: #f9fafb;
  padding: 15px;
  border-radius: 6px;
  margin-bottom: 15px;
}

.test-btn, .mode-btn {
  padding: 10px 20px;
  margin: 5px;
  border: none;
  border-radius: 6px;
  background: #667eea;
  color: white;
  cursor: pointer;
  transition: background 0.2s;
}

.test-btn:hover, .mode-btn:hover {
  background: #5a67d8;
}

.mode-buttons {
  display: flex;
  gap: 10px;
  flex-wrap: wrap;
}
</style>