<template>
  <div class="competition-selectors">
    <div class="selector-group">
      <label>已参加的比赛：</label>
      <select v-model="selectedParticipated" @change="updateParticipated" class="form-select">
        <option value="">选择已参加的比赛</option>
        <option v-for="comp in participatedCompetitions" :key="comp.id" :value="comp.id">
          {{ comp.title }}
        </option>
      </select>
    </div>
    <div class="selector-group">
      <label>{{ availableLabel }}：</label>
      <select v-model="selectedAvailable" @change="updateAvailable" class="form-select">
        <option value="">{{ availablePlaceholder }}</option>
        <option v-for="comp in availableCompetitions" :key="comp.id" :value="comp.id">
          {{ comp.title }}
        </option>
      </select>
    </div>
  </div>
</template>

<script setup>
import { ref, watch } from 'vue'

const props = defineProps({
  participatedCompetitions: {
    type: Array,
    default: () => []
  },
  availableCompetitions: {
    type: Array,
    default: () => []
  },
  availableLabel: {
    type: String,
    default: '未参加的比赛'
  },
  availablePlaceholder: {
    type: String,
    default: '选择未参加的比赛'
  },
  modelParticipated: {
    type: String,
    default: ''
  },
  modelAvailable: {
    type: String,
    default: ''
  }
})

const emit = defineEmits(['update:modelParticipated', 'update:modelAvailable'])

const selectedParticipated = ref(props.modelParticipated)
const selectedAvailable = ref(props.modelAvailable)

const updateParticipated = () => {
  emit('update:modelParticipated', selectedParticipated.value)
}

const updateAvailable = () => {
  emit('update:modelAvailable', selectedAvailable.value)
}

// 监听外部变化
watch(() => props.modelParticipated, (newVal) => {
  selectedParticipated.value = newVal
})

watch(() => props.modelAvailable, (newVal) => {
  selectedAvailable.value = newVal
})
</script>

<style scoped>
@import '../../styles/competition-component.css';
</style>
