/**
 * 文本格式化工具
 * 用于处理AI返回的文本，包括Markdown格式转换、学习路径解析等
 */

/**
 * 检测文本是否包含Markdown语法
 * @param {string} text - 要检测的文本
 * @returns {boolean} 是否包含Markdown语法
 */
export function hasMarkdownSyntax(text) {
  if (!text || typeof text !== 'string') return false
  
  const markdownPatterns = [
    /\*\*[^*]+\*\*/,  // 粗体
    /\*[^*]+\*/,      // 斜体
    /#{1,6}\s+/,      // 标题
    /^\s*[-*+]\s+/m,  // 列表
    /^\s*\d+\.\s+/m,  // 有序列表
    /`[^`]+`/,        // 行内代码
    /```[\s\S]*?```/, // 代码块
    /\[([^\]]+)\]\(([^)]+)\)/, // 链接
  ]
  
  return markdownPatterns.some(pattern => pattern.test(text))
}

/**
 * 清理Markdown格式符号，保留纯文本
 * @param {string} text - 包含Markdown的文本
 * @returns {string} 清理后的纯文本
 */
export function cleanMarkdown(text) {
  if (!text || typeof text !== 'string') return ''
  
  return text
    // 移除HTML标签
    .replace(/<[^>]*>/g, '')
    // 移除粗体标记
    .replace(/\*\*([^*]+)\*\*/g, '$1')
    // 移除斜体标记
    .replace(/\*([^*]+)\*/g, '$1')
    // 移除标题标记
    .replace(/#{1,6}\s*/g, '')
    // 移除列表标记
    .replace(/^\s*[-*+]\s*/gm, '')
    // 移除有序列表标记（保留数字，用于学习路径解析）
    .replace(/^\s*(\d+)\.\s*/gm, '$1. ')
    // 移除行内代码标记
    .replace(/`([^`]+)`/g, '$1')
    // 移除代码块
    .replace(/```[\s\S]*?```/g, '')
    // 移除链接，保留文本
    .replace(/\[([^\]]+)\]\([^)]+\)/g, '$1')
    // 清理多余的空白字符
    .replace(/\n\s*\n/g, '\n')
    .trim()
}

/**
 * 将Markdown转换为HTML
 * @param {string} text - Markdown文本
 * @returns {string} HTML文本
 */
export function markdownToHtml(text) {
  if (!text || typeof text !== 'string') return ''
  
  return text
    // 粗体
    .replace(/\*\*([^*]+)\*\*/g, '<strong>$1</strong>')
    // 斜体
    .replace(/\*([^*]+)\*/g, '<em>$1</em>')
    // 标题
    .replace(/^### (.*$)/gm, '<h3>$1</h3>')
    .replace(/^## (.*$)/gm, '<h2>$1</h2>')
    .replace(/^# (.*$)/gm, '<h1>$1</h1>')
    // 列表
    .replace(/^\s*[-*+]\s+(.*)$/gm, '<li>$1</li>')
    // 有序列表
    .replace(/^\s*\d+\.\s+(.*)$/gm, '<li>$1</li>')
    // 行内代码
    .replace(/`([^`]+)`/g, '<code>$1</code>')
    // 链接
    .replace(/\[([^\]]+)\]\(([^)]+)\)/g, '<a href="$2">$1</a>')
    // 换行
    .replace(/\n/g, '<br>')
}

/**
 * 格式化AI聊天消息
 * @param {string} message - AI返回的消息
 * @returns {string} 格式化后的消息
 */
export function formatAIMessage(message) {
  if (!message || typeof message !== 'string') return ''
  
  // 如果包含Markdown语法，转换为HTML
  if (hasMarkdownSyntax(message)) {
    return markdownToHtml(message)
  }
  
  // 否则只进行基本的换行处理
  return message.replace(/\n/g, '<br>')
}

/**
 * 提取时间信息
 * @param {string} text - 包含时间信息的文本
 * @returns {string} 提取的时间信息
 */
function extractDuration(text) {
  if (!text) return null
  
  // 匹配各种时间格式
  const timePatterns = [
    /(\d+)\s*周/,
    /(\d+)\s*个?月/,
    /(\d+)\s*天/,
    /预计时间[：:]\s*(\d+\s*[周月天])/,
    /时间[：:]\s*(\d+\s*[周月天])/,
    /duration[：:]\s*(\d+\s*[周月天])/i
  ]
  
  for (const pattern of timePatterns) {
    const match = text.match(pattern)
    if (match) {
      return match[1].includes('周') || match[1].includes('月') || match[1].includes('天') 
        ? match[1] 
        : match[1] + '周'
    }
  }
  
  return null
}

/**
 * 解析学习路径文本为结构化数据
 * @param {string} text - AI返回的学习路径文本
 * @returns {Array} 解析后的阶段数组
 */
export function parseLearningPath(text) {
  if (!text || typeof text !== 'string') {
    return getDefaultLearningPath()
  }

  try {
    // 清理文本，移除HTML标签和特殊符号
    let cleanText = text
      .replace(/<[^>]*>/g, '') // 移除HTML标签
      .replace(/\*\*([^*]+)\*\*/g, '$1') // 移除粗体标记
      .replace(/\*([^*]+)\*/g, '$1') // 移除斜体标记
      .replace(/#{1,6}\s*/g, '') // 移除标题标记
      .trim()
    
    // 尝试多种策略来解析学习路径
    
    // 策略1: 尝试查找明确的阶段标记
    const stageMarkers = [
      { pattern: /第一阶段|基础准备阶段|基础阶段|准备阶段/, title: '基础准备阶段', defaultDuration: '4周' },
      { pattern: /第二阶段|技能提升阶段|提升阶段|进阶阶段/, title: '技能提升阶段', defaultDuration: '6周' },
      { pattern: /第三阶段|实战演练阶段|实战阶段|演练阶段/, title: '实战演练阶段', defaultDuration: '2周' }
    ]
    
    // 尝试按阶段分割文本
    let stageContents = [null, null, null]
    let lastFoundIndex = -1
    
    // 首先尝试找到所有阶段标记的位置
    const stagePositions = []
    
    for (let i = 0; i < stageMarkers.length; i++) {
      const marker = stageMarkers[i]
      const match = cleanText.match(marker.pattern)
      
      if (match) {
        stagePositions.push({
          index: i,
          position: match.index,
          title: marker.title,
          defaultDuration: marker.defaultDuration
        })
      }
    }
    
    // 按位置排序
    stagePositions.sort((a, b) => a.position - b.position)
    
    // 根据位置分割文本
    if (stagePositions.length >= 2) {
      for (let i = 0; i < stagePositions.length; i++) {
        const current = stagePositions[i]
        const next = stagePositions[i + 1]
        
        const startPos = current.position
        const endPos = next ? next.position : cleanText.length
        
        const content = cleanText.substring(startPos, endPos).trim()
        stageContents[current.index] = {
          title: current.title,
          content: content,
          defaultDuration: current.defaultDuration
        }
      }
    } else {
      // 策略2: 如果没有找到明确的阶段标记，尝试按数字分割
      const numberSections = cleanText.split(/(?=\d+\.\s+)/).filter(s => s.trim())
      
      if (numberSections.length >= 3) {
        for (let i = 0; i < Math.min(numberSections.length, 3); i++) {
          stageContents[i] = {
            title: stageMarkers[i].title,
            content: numberSections[i].trim(),
            defaultDuration: stageMarkers[i].defaultDuration
          }
        }
      } else {
        // 策略3: 如果没有数字分割，尝试平均分配内容
        const paragraphs = cleanText.split(/\n\s*\n/).filter(p => p.trim())
        
        if (paragraphs.length >= 3) {
          // 如果有足够的段落，每个阶段分配一个或多个段落
          const paragraphsPerStage = Math.ceil(paragraphs.length / 3)
          
          for (let i = 0; i < 3; i++) {
            const startIdx = i * paragraphsPerStage
            const endIdx = Math.min((i + 1) * paragraphsPerStage, paragraphs.length)
            const stageContent = paragraphs.slice(startIdx, endIdx).join('\n\n')
            
            stageContents[i] = {
              title: stageMarkers[i].title,
              content: stageContent,
              defaultDuration: stageMarkers[i].defaultDuration
            }
          }
        } else {
          // 策略4: 如果段落不足，按字符长度平均分配
          const totalLength = cleanText.length
          const lengthPerStage = Math.ceil(totalLength / 3)
          
          for (let i = 0; i < 3; i++) {
            const startIdx = i * lengthPerStage
            const endIdx = Math.min((i + 1) * lengthPerStage, totalLength)
            const stageContent = cleanText.substring(startIdx, endIdx).trim()
            
            stageContents[i] = {
              title: stageMarkers[i].title,
              content: stageContent,
              defaultDuration: stageMarkers[i].defaultDuration
            }
          }
        }
      }
    }
    
    // 处理可能的null值并提取信息
    const stages = []
    
    for (let i = 0; i < 3; i++) {
      if (!stageContents[i]) {
        stages.push({
          title: stageMarkers[i].title,
          description: i === 0 ? '学习基础知识和技能，为竞赛做好准备' :
                      i === 1 ? '深入学习相关技能，提升专业能力' :
                                '模拟竞赛环境，进行实战训练',
          duration: stageMarkers[i].defaultDuration
        })
        continue
      }
      
      const stageData = stageContents[i]
      const content = stageData.content
      
      // 提取标题（如果有更具体的标题）
      let title = stageData.title
      const titleMatch = content.match(/^[^.。:：\n]+[.。:：]/m)
      if (titleMatch) {
        const extractedTitle = titleMatch[0].replace(/[.。:：]$/, '').trim()
        if (extractedTitle.length > 0 && extractedTitle.length < 30) {
          title = extractedTitle
        }
      }
      
      // 提取描述
      let description = content
        .replace(/^[^.。:：\n]+[.。:：]/m, '') // 移除标题
        .replace(/预计时间[：:].+/g, '') // 移除时间信息
        .replace(/时间[：:].+/g, '')
        .replace(/\d+\.\s*/g, '') // 移除数字编号
        .replace(/[-•]\s*/g, '') // 移除列表标记
        .trim()
      
      // 如果描述太长，截取前150个字符
      if (description.length > 150) {
        description = description.substring(0, 147) + '...'
      }
      
      // 如果描述为空，使用默认描述
      if (!description) {
        description = i === 0 ? '学习基础知识和技能，为竞赛做好准备' :
                     i === 1 ? '深入学习相关技能，提升专业能力' :
                               '模拟竞赛环境，进行实战训练'
      }
      
      // 提取时间信息
      const duration = extractDuration(content) || stageData.defaultDuration
      
      stages.push({
        title: title.substring(0, 30), // 限制标题长度
        description: description,
        duration
      })
    }
    
    return stages
    
  } catch (error) {
    console.warn('解析学习路径失败:', error)
    return getDefaultLearningPath()
  }
}

/**
 * 获取默认学习路径（降级处理）
 */
function getDefaultLearningPath() {
  return [
    {
      title: '基础准备阶段',
      description: '学习基础知识和技能，建立扎实的理论基础',
      duration: '4周'
    },
    {
      title: '技能提升阶段',
      description: '深入学习相关技能，提升专业能力和实践经验',
      duration: '6周'
    },
    {
      title: '实战演练阶段',
      description: '模拟竞赛环境，进行实战训练和能力检验',
      duration: '2周'
    }
  ]
}

/**
 * 格式化技能分析结果
 * @param {string} analysis - AI返回的技能分析文本
 * @returns {string} 格式化后的分析结果
 */
export function formatSkillAnalysis(analysis) {
  if (!analysis || typeof analysis !== 'string') return ''
  
  let formatted = analysis
  
  // 处理标题格式
  formatted = formatted
    // 处理主标题（### 技能分析报告）
    .replace(/###\s*([^(\n]+)(\([^)]+\))?/g, '<h3 class="analysis-title">$1<span class="user-tag">$2</span></h3>')
    // 处理二级标题（#### 1. 技能水平评估）
    .replace(/####\s*(\d+\.\s*[^\n]+)/g, '<h4 class="section-title">$1</h4>')
    // 处理粗体技能项（- **前端/后端开发**：）
    .replace(/^\s*-\s*\*\*([^*]+)\*\*[：:]\s*([^\n]+)/gm, '<div class="skill-item"><strong class="skill-name">$1</strong>：<span class="skill-desc">$2</span></div>')
    // 处理普通列表项
    .replace(/^\s*-\s*([^\n]+)/gm, '<li class="analysis-point">$1</li>')
    // 处理换行
    .replace(/\n\s*\n/g, '</div><div class="analysis-section">')
    .replace(/\n/g, '<br>')
  
  // 包装在容器中
  return `<div class="skill-analysis-content"><div class="analysis-section">${formatted}</div></div>`
}

/**
 * 格式化竞赛趋势分析
 * @param {string} trends - AI返回的趋势分析文本
 * @returns {string} 格式化后的趋势分析
 */
export function formatTrendsAnalysis(trends) {
  if (!trends || typeof trends !== 'string') return ''

  let formatted = trends

  // 处理标题格式
  formatted = formatted
    // 处理主标题
    .replace(/###\s*([^\n]+)/g, '<h3 class="trends-title">$1</h3>')
    // 处理二级标题
    .replace(/####\s*(\d+\.\s*[^\n]+)/g, '<h4 class="trends-section-title">$1</h4>')
    // 处理粗体关键词
    .replace(/\*\*([^*]+)\*\*/g, '<strong class="trend-keyword">$1</strong>')
    // 处理列表项
    .replace(/^\s*-\s*([^\n]+)/gm, '<li class="trend-point">$1</li>')
    // 高亮趋势关键词
    .replace(/(热门|趋势|发展|增长|上升|下降|新兴|流行)/g, '<span class="highlight-keyword">$1</span>')
    // 处理换行
    .replace(/\n\s*\n/g, '</div><div class="trends-section">')
    .replace(/\n/g, '<br>')

  // 包装在容器中
  return `<div class="trends-analysis-content"><div class="trends-section">${formatted}</div></div>`
}

/**
 * 创建打字机效果
 * @param {string} text - 要显示的文本
 * @param {Function} callback - 每次更新时的回调函数
 * @param {number} speed - 打字速度（毫秒/字符）
 * @returns {Object} 包含控制方法的对象
 */
export function createTypewriterEffect(text, callback, speed = 30) {
  let index = 0
  let currentText = ''
  let timer = null
  let isPaused = false
  let isStopped = false

  // 将HTML标签视为一个整体，不拆分
  const segments = []
  let tempText = text
  const tagRegex = /<[^>]+>/g
  let lastIndex = 0
  let match

  // 提取HTML标签和文本
  while ((match = tagRegex.exec(text)) !== null) {
    // 添加标签之前的文本（按字符分割）
    const beforeTag = text.slice(lastIndex, match.index)
    for (let char of beforeTag) {
      segments.push(char)
    }
    // 添加完整的HTML标签
    segments.push(match[0])
    lastIndex = match.index + match[0].length
  }

  // 添加剩余的文本
  const remaining = text.slice(lastIndex)
  for (let char of remaining) {
    segments.push(char)
  }

  const type = () => {
    if (isStopped || isPaused) return

    if (index < segments.length) {
      currentText += segments[index]
      callback(currentText)
      index++

      // 如果当前是HTML标签，立即显示下一个字符
      if (segments[index - 1]?.startsWith('<')) {
        timer = setTimeout(type, 0)
      } else {
        timer = setTimeout(type, speed)
      }
    }
  }

  // 开始打字
  const start = () => {
    if (!isStopped) {
      isPaused = false
      type()
    }
  }

  // 暂停打字
  const pause = () => {
    isPaused = true
    if (timer) {
      clearTimeout(timer)
      timer = null
    }
  }

  // 停止打字
  const stop = () => {
    isStopped = true
    isPaused = true
    if (timer) {
      clearTimeout(timer)
      timer = null
    }
  }

  // 立即完成
  const complete = () => {
    stop()
    currentText = text
    callback(currentText)
  }

  return {
    start,
    pause,
    stop,
    complete,
    isComplete: () => index >= segments.length,
    getCurrentText: () => currentText
  }
}
