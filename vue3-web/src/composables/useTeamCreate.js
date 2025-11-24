import { ref } from 'vue'

/**
 * 团队创建组合式函数
 * @param {Ref<Object>} currentUser - 当前用户信息
 * @param {Ref<Number|String>} competitionId - 竞赛ID
 * @param {Function} onSuccess - 创建成功后的回调函数
 * @returns {Object} - 团队创建相关的状态和方法
 */
export function useTeamCreate(currentUser, competitionId, onSuccess) {
  // 弹窗状态
  const showCreateTeamModal = ref(false)
  
  // 打开创建团队弹窗
  const openCreateTeamModal = () => {
    // 检查用户是否登录
    if (!currentUser.value) {
      alert('请先登录后再创建团队')
      return
    }
    
    // 显示弹窗
    showCreateTeamModal.value = true
  }
  
  return {
    showCreateTeamModal,
    openCreateTeamModal
  }
}