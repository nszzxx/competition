<template>
  <header class="page-header">
    <div class="header-content">
      <div class="header-text">
        <h1 class="header-title">{{ title }}</h1>
        <p v-if="subtitle" class="header-subtitle">{{ subtitle }}</p>
      </div>
      <div v-if="$slots.actions" class="header-actions">
        <slot name="actions"></slot>
      </div>
    </div>
  </header>
</template>

<script setup>
defineProps({
  title: {
    type: String,
    required: true
  },
  subtitle: {
    type: String,
    default: ''
  }
})
</script>

<style scoped>
.page-header {
  background: rgba(255, 255, 255, 0.03);
  backdrop-filter: blur(20px);
  border-bottom: 1px solid rgba(255, 255, 255, 0.05);
  padding: 3rem 2rem;
  margin-bottom: 3rem;
  position: relative;
  z-index: 10;
}

.page-header::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  height: 1px;
  background: linear-gradient(90deg, transparent, rgba(255, 255, 255, 0.1), transparent);
}

.header-content {
  max-width: 1280px;
  margin: 0 auto;
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 2rem;
  position: relative;
  z-index: 1;
}

.header-text {
  flex: 1;
}

.header-title {
  font-size: 2.5rem;
  font-weight: 800;
  margin: 0 0 0.5rem 0;
  color: white;
  letter-spacing: -0.02em;
  background: linear-gradient(to right, #fff, #cbd5e1);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
}

.header-subtitle {
  font-size: 1.1rem;
  color: #94a3b8;
  margin: 0;
  line-height: 1.6;
  max-width: 600px;
}

.header-actions {
  display: flex;
  gap: 1rem;
  align-items: center;
}

@media (max-width: 768px) {
  .page-header {
    padding: 2rem 1.5rem;
  }
  
  .header-content {
    flex-direction: column;
    align-items: flex-start;
    gap: 1.5rem;
  }
  
  .header-title {
    font-size: 2rem;
  }
  
  .header-actions {
    width: 100%;
    flex-wrap: wrap;
  }
}
</style>