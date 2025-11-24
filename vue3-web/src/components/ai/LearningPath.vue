<template>
  <div class="learning-path" v-if="learningPath">
    <div class="path-header">
      <h3>个性化学习路径</h3>
      <div class="path-meta">
        <span class="estimated-time">预计用时：{{ learningPath.estimatedTime }}</span>
        <span class="difficulty">难度：{{ learningPath.difficulty }}</span>
      </div>
    </div>
    
    <div class="path-content">
      <!-- 学习阶段 -->
      <div class="learning-stages" v-if="learningPath.stages">
        <div 
          v-for="(stage, index) in learningPath.stages" 
          :key="index" 
          class="stage-item"
        >
          <div class="stage-number">{{ index + 1 }}</div>
          <div class="stage-content">
            <h4 class="stage-title">{{ stage.title }}</h4>
            <p class="stage-description">{{ stage.description }}</p>
            <div class="stage-duration">预计时间：{{ stage.duration }}</div>
            
            <!-- 学习资源 -->
            <div class="stage-resources" v-if="stage.resources">
              <h5>推荐资源：</h5>
              <ul class="resources-list">
                <li v-for="resource in stage.resources" :key="resource.id">
                  <a :href="resource.url" target="_blank">{{ resource.title }}</a>
                  <span class="resource-type">{{ resource.type }}</span>
                </li>
              </ul>
            </div>
            
            <!-- 练习任务 -->
            <div class="stage-tasks" v-if="stage.tasks">
              <h5>练习任务：</h5>
              <ul class="tasks-list">
                <li v-for="task in stage.tasks" :key="task.id">
                  <span class="task-title">{{ task.title }}</span>
                  <span class="task-difficulty">{{ task.difficulty }}</span>
                </li>
              </ul>
            </div>
          </div>
        </div>
      </div>
      
      <!-- AI建议 -->
      <div class="ai-advice" v-if="learningPath.advice">
        <h4>AI学习建议</h4>
        <div class="advice-content">
          <p>{{ learningPath.advice }}</p>
        </div>
      </div>
    </div>
  </div>
  
  <div v-else class="no-path">
    <p>暂无学习路径，请选择目标竞赛并点击"生成学习路径"。</p>
  </div>
</template>

<script setup>
import { parseLearningPath } from '../../utils/textFormatter.js'

defineProps({
  learningPath: {
    type: Object,
    default: null
  }
})

// 解析学习路径文本
const parsePathText = (text) => {
  return parseLearningPath(text)
}
</script>

<style scoped>
@import '../styles/AI-component.css';
</style>