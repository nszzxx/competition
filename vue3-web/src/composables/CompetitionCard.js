import { computed } from 'vue'

export function useCompetitionCard(props) {
  const tagList = computed(() => {
    return props.competition.tags 
      ? props.competition.tags.split(',').map(tag => tag.trim()) 
      : []
  })

  const formatDate = (dateString) => {
    if (!dateString) return '待定'
    
    // 调试输出
    console.log('原始日期字符串:', dateString);
    console.log('日期类型:', typeof dateString);
    
    if (typeof dateString === 'object') {
      console.log('日期对象属性:', Object.keys(dateString));
    }
    
    try {
      // 尝试创建日期对象
      let date;
      
      // 处理不同格式的日期字符串
      if (typeof dateString === 'string') {
        // 如果是ISO格式的字符串
        if (dateString.includes('T')) {
          date = new Date(dateString);
        } 
        // 如果是时间戳格式的字符串
        else if (!isNaN(Number(dateString))) {
          date = new Date(Number(dateString));
        }
        // 其他格式的字符串
        else {
          date = new Date(dateString);
        }
      } 
      // 如果是数字类型的时间戳
      else if (typeof dateString === 'number') {
        date = new Date(dateString);
      }
      // 如果是Date对象
      else if (dateString instanceof Date) {
        date = dateString;
      }
      // 如果是Java的Timestamp对象转换后的格式
      else if (dateString && typeof dateString === 'object') {
        // 检查是否有time属性
        if (dateString.time) {
          date = new Date(dateString.time);
        } 
        // 检查是否有date属性
        else if (dateString.date) {
          date = new Date(dateString.date);
        }
        // 尝试将整个对象转换为时间戳
        else {
          const timestamp = new Date(dateString).getTime();
          if (!isNaN(timestamp)) {
            date = new Date(timestamp);
          }
        }
      }
      
      // 检查日期是否有效
      if (!date || isNaN(date.getTime())) {
        console.warn('无效的日期格式:', dateString);
        return '待定';
      }
      
      // 格式化日期
      const formattedDate = date.toLocaleDateString('zh-CN', {
        year: 'numeric',
        month: '2-digit',
        day: '2-digit'
      });
      
      console.log('格式化后的日期:', formattedDate);
      return formattedDate;
    } catch (error) {
      console.error('日期格式化错误:', error, dateString);
      return '待定';
    }
  }

  const formatParticipationType = (type) => {
    if (!type) return '不限'
    
    switch(type.toLowerCase()) {
      case 'individual':
        return '个人参赛'
      case 'team':
        return '团队参赛'
      case 'both':
        return '个人/团队均可'
      default:
        return type
    }
  }

  // 计算报名状态
  const getRegistrationStatus = computed(() => {
    const comp = props.competition
    if (!comp.patiStarttime || !comp.patiEndtime) return null
    
    const now = new Date()
    const startTime = new Date(comp.patiStarttime)
    const endTime = new Date(comp.patiEndtime)
    
    if (now < startTime) {
      return 0 // 报名未开始
    } else if (now > endTime) {
      return 2 // 报名已结束
    } else {
      return 1 // 报名中
    }
  })

  // 报名状态文本
  const getRegistrationStatusText = computed(() => {
    switch (getRegistrationStatus.value) {
      case 0:
        return '报名未开始'
      case 1:
        return '报名中'
      case 2:
        return '报名已结束'
      default:
        return '未知状态'
    }
  })

  // 报名状态样式类
  const getRegistrationStatusClass = computed(() => {
    switch (getRegistrationStatus.value) {
      case 0:
        return 'status-coming'
      case 1:
        return 'status-open'
      case 2:
        return 'status-closed'
      default:
        return ''
    }
  })

  return {
    tagList,
    formatDate,
    formatParticipationType,
    getRegistrationStatus,
    getRegistrationStatusText,
    getRegistrationStatusClass
  }
}