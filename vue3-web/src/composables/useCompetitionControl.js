import { ref, reactive, onMounted } from 'vue'
import axios from 'axios'
import { projectApi } from '../utils/api.js'
import { renderAsync } from 'docx-preview'

export function useCompetitionControl() {
  const loading = ref(false)
  const currentUser = ref(null)
  const competitions = ref([])
  const allCompetitions = ref([]) // 存储所有竞赛数据
  const participationFilter = ref('all') // 参赛方式筛选器
  const showCancelModal = ref(false)
  const selectedCompetition = ref(null)
  const showUploadModal = ref(false)
  const uploadingCompetition = ref(null)
  const uploadFile = ref(null)
  const uploading = ref(false)

  // 文档缓存：存储已下载的文档 Blob 对象
  const docCache = new Map() // key: fileUrl, value: { blob: Blob, timestamp: number, size: number }
  const MAX_CACHE_SIZE = 50 * 1024 * 1024 // 最大缓存大小：50MB
  const MAX_CACHE_AGE = 30 * 60 * 1000 // 缓存有效期：30分钟

  // 清理缓存
  const clearDocCache = (fileUrl = null) => {
    if (fileUrl) {
      // 清理指定文件的缓存
      if (docCache.has(fileUrl)) {
        docCache.delete(fileUrl)
        console.log('已清理缓存:', fileUrl)
      }
    } else {
      // 清理所有缓存
      const count = docCache.size
      docCache.clear()
      console.log(`已清理所有缓存，共 ${count} 个文件`)
    }
  }

  // 清理过期缓存
  const clearExpiredCache = () => {
    const now = Date.now()
    let clearedCount = 0

    for (const [url, cache] of docCache.entries()) {
      if (now - cache.timestamp > MAX_CACHE_AGE) {
        docCache.delete(url)
        clearedCount++
      }
    }

    if (clearedCount > 0) {
      console.log(`已清理 ${clearedCount} 个过期缓存`)
    }
  }

  // 检查并控制缓存大小
  const manageCacheSize = () => {
    let totalSize = 0
    const cacheArray = Array.from(docCache.entries())

    // 计算总大小
    for (const [, cache] of cacheArray) {
      totalSize += cache.size
    }

    // 如果超过限制，删除最旧的缓存
    if (totalSize > MAX_CACHE_SIZE) {
      // 按时间戳排序（旧的在前）
      cacheArray.sort((a, b) => a[1].timestamp - b[1].timestamp)

      while (totalSize > MAX_CACHE_SIZE && cacheArray.length > 0) {
        const [url, cache] = cacheArray.shift()
        docCache.delete(url)
        totalSize -= cache.size
        console.log(`缓存超限，已删除: ${url}`)
      }
    }
  }

  const loadUserInfo = () => {
    const userStr = localStorage.getItem('user')
    if (userStr) {
      try {
        currentUser.value = JSON.parse(userStr)
      } catch (e) {
        console.error('用户信息解析失败:', e)
      }
    }
  }

  const loadUserCompetitions = async () => {
    if (!currentUser.value) return

    loading.value = true
    try {
      const response = await axios.get(`/api/competitions/user/${currentUser.value.id}`)
      // 处理后端返回的UserCompetitionDTO数据
      allCompetitions.value = response.data.map(dto => {
        const competition = dto.competition
        // 构建处理后的竞赛对象
        const processedCompetition = {
          id: competition.id,
          title: competition.title,
          name: competition.title, // 兼容旧代码
          category: competition.category,
          description: competition.description,
          registrationStartTime: competition.patiStarttime,
          registrationEndTime: competition.patiEndtime,
          startTime: competition.startTime,
          endTime: competition.endTime,
          registrationDate: dto.createTime,
          participationMode: dto.participationMode, // individual 或 team
          role: dto.role, // 队长、队员、个人
          teamId: dto.teamId,
          teamName: dto.teamName,
          rank: dto.rank,
          competitionTeamUserId: dto.competitionTeamUserId
        }
        // 使用处理后的对象计算状态
        processedCompetition.status = getCompetitionStatus(processedCompetition)
        processedCompetition.canCancel = canCancelRegistration(processedCompetition)
        return processedCompetition
      })

      // 应用当前筛选
      filterCompetitions()
    } catch (error) {
      console.error('加载用户竞赛失败:', error)
      allCompetitions.value = []
      competitions.value = []
    } finally {
      loading.value = false
    }
  }

  const getCompetitionStatus = (competition) => {
    const now = new Date()
    const regStart = new Date(competition.registrationStartTime)
    const regEnd = new Date(competition.registrationEndTime)
    const compStart = new Date(competition.startTime)
    const compEnd = new Date(competition.endTime)

    if (now < regStart) {
      return '报名未开始'
    } else if (now >= regStart && now <= regEnd) {
      return '报名中'
    } else if (now > regEnd && now < compStart) {
      return '报名已结束'
    } else if (now >= compStart && now <= compEnd) {
      return '进行中'
    } else {
      return '已结束'
    }
  }

  const canCancelRegistration = (competition) => {
    const now = new Date()
    const regEnd = new Date(competition.registrationEndTime)
    const compStart = new Date(competition.startTime)
    
    // 只有在报名期间或报名结束后但竞赛未开始前可以取消
    return now <= regEnd || (now > regEnd && now < compStart)
  }

  const getStatusClass = (status) => {
    const classMap = {
      '报名未开始': 'status-not-started',
      '报名中': 'status-registering',
      '报名已结束': 'status-reg-ended',
      '即将开始': 'status-upcoming',
      '进行中': 'status-ongoing',
      '已结束': 'status-finished'
    }
    return classMap[status] || 'status-default'
  }

  const openCancelModal = (competition) => {
    selectedCompetition.value = competition
    showCancelModal.value = true
  }

  const closeCancelModal = () => {
    selectedCompetition.value = null
    showCancelModal.value = false
  }

  const cancelRegistration = async () => {
    if (!selectedCompetition.value) return

    try {
      // 调用更新后的API：传递竞赛ID、用户ID和团队ID
      const competitionId = selectedCompetition.value.id
      const userId = currentUser.value.id
      const teamId = selectedCompetition.value.teamId || null

      await axios.delete('/api/competitions/participate', {
        params: {
          competitionId,
          userId,
          teamId
        }
      })

      // 从列表中移除已取消的竞赛
      competitions.value = competitions.value.filter(
        comp => comp.id !== selectedCompetition.value.id
      )

      // 同时从allCompetitions中移除
      allCompetitions.value = allCompetitions.value.filter(
        comp => comp.id !== selectedCompetition.value.id
      )

      alert('取消报名成功')
      closeCancelModal()
    } catch (error) {
      console.error('取消报名失败:', error)
      alert('取消报名失败：' + (error.response?.data || error.message || '请重试'))
    }
  }

  const viewCompetitionDetail = (competitionId) => {
    // 这里可以跳转到竞赛详情页面
    window.open(`/competition/${competitionId}`, '_blank')
  }

  const downloadProjectPlan = async (competition) => {
    try {
      // 获取项目文档
      const project = await projectApi.getDocument(
        competition.id,
        competition.teamId,
        competition.participationMode === 'individual' ? currentUser.value.id : null
      )

      if (project && project.documentUrl && project.documentUrl.trim() !== '') {
        // 打开下载链接
        window.open(project.documentUrl, '_blank')
      } else {
        alert('暂无项目计划书，请先上传计划书文件')
      }
    } catch (error) {
      console.error('下载项目计划书失败:', error)
      alert('下载失败，请重试')
    }
  }

  const openUploadModal = (competition) => {
    uploadingCompetition.value = competition
    showUploadModal.value = true
    uploadFile.value = null
  }

  const closeUploadModal = () => {
    uploadingCompetition.value = null
    showUploadModal.value = false
    uploadFile.value = null
  }

  const handleFileSelect = (event) => {
    const file = event.target.files[0]
    if (file) {
      // 检查文件类型
      const allowedTypes = ['application/pdf', 'application/msword', 'application/vnd.openxmlformats-officedocument.wordprocessingml.document']
      if (!allowedTypes.includes(file.type)) {
        alert('仅支持上传 PDF、DOC 或 DOCX 格式的文件')
        return
      }

      // 检查文件大小（限制10MB）
      if (file.size > 10 * 1024 * 1024) {
        alert('文件大小不能超过10MB')
        return
      }

      uploadFile.value = file
    }
  }

  const uploadProjectPlan = async () => {
    if (!uploadFile.value) {
      alert('请选择文件')
      return
    }

    if (!uploadingCompetition.value) {
      alert('竞赛信息错误')
      return
    }

    uploading.value = true

    try {
      const response = await projectApi.uploadDocument(
        uploadingCompetition.value.id,
        uploadingCompetition.value.teamId,
        currentUser.value.id,
        uploadingCompetition.value.participationMode,
        uploadFile.value
      )

      if (response.success) {
        alert('上传成功')
        closeUploadModal()
      } else {
        alert(response.message || '上传失败')
      }
    } catch (error) {
      console.error('上传项目计划书失败:', error)
      alert('上传失败：' + (error.response?.data?.message || error.message || '请重试'))
    } finally {
      uploading.value = false
    }
  }

  const viewProjectPlan = async (competition) => {
    try {
      // 获取项目文档
      const project = await projectApi.getDocument(
        competition.id,
        competition.teamId,
        competition.participationMode === 'individual' ? currentUser.value.id : null
      )

      console.log('获取到的项目信息:', project)
      // console.log('文档URL:', project?.documentUrl)

      // // 检查 documentUrl 是否存在且不为空字符串
      // if (!project || !project.documentUrl || project.documentUrl.trim() === '') {
      //   alert('暂无项目计划书，请先上传计划书文件')
      //   return
      // }

      const fileUrl = project.documentUrl
      const fileExtension = fileUrl.substring(fileUrl.lastIndexOf('.')).toLowerCase()

      console.log('文件扩展名:', fileExtension)

      // PDF文件直接在新窗口打开预览
      if (fileExtension === '.pdf') {
        window.open(fileUrl, '_blank')
      }
      // DOCX文件使用docx-preview库预览
      else if (fileExtension === '.docx') {
        await previewDocxFile(fileUrl)
      }
      // DOC文件提示用户下载（老格式不支持在线预览）
      else if (fileExtension === '.doc') {
        if (confirm('旧版 .doc 格式不支持在线预览，是否下载查看？')) {
          window.open(fileUrl, '_blank')
        }
      }
      // 其他格式直接打开（浏览器会尝试预览或下载）
      else {
        window.open(fileUrl, '_blank')
      }
    } catch (error) {
      alert('暂无项目计划书，请先上传计划书文件')
    }
  }

  // 预览 DOCX 文件
  const previewDocxFile = async (fileUrl) => {
    try {
      console.log('原始文件URL:', fileUrl)

      // 将完整URL转换为相对路径以使用代理
      let requestUrl = fileUrl
      if (fileUrl.includes('://')) {
        // 提取路径部分（从协议之后的第一个/开始）
        const urlObj = new URL(fileUrl)
        requestUrl = urlObj.pathname
      }

      console.log('转换后的请求URL:', requestUrl)

      // 创建遮罩层和预览容器
      const overlay = document.createElement('div')
      overlay.style.cssText = `
        position: fixed;
        top: 0;
        left: 0;
        width: 100%;
        height: 100%;
        background: rgba(0, 0, 0, 0.8);
        z-index: 9999;
        display: flex;
        flex-direction: column;
        align-items: center;
        justify-content: center;
      `

      // 创建关闭按钮
      const closeBtn = document.createElement('button')
      closeBtn.textContent = '✕ 关闭'
      closeBtn.style.cssText = `
        position: absolute;
        top: 20px;
        right: 20px;
        padding: 10px 20px;
        background: #ff4d4f;
        color: #fff;
        border: none;
        border-radius: 4px;
        cursor: pointer;
        font-size: 16px;
        font-weight: bold;
        z-index: 10000;
        box-shadow: 0 2px 8px rgba(0,0,0,0.3);
        transition: all 0.3s ease;
      `
      closeBtn.onmouseover = () => {
        closeBtn.style.background = '#ff7875'
        closeBtn.style.transform = 'scale(1.05)'
      }
      closeBtn.onmouseout = () => {
        closeBtn.style.background = '#ff4d4f'
        closeBtn.style.transform = 'scale(1)'
      }
      closeBtn.onclick = () => document.body.removeChild(overlay)

      // 创建清理缓存按钮
      const clearCacheBtn = document.createElement('button')
      clearCacheBtn.textContent = '🗑️ 清理缓存'
      clearCacheBtn.style.cssText = `
        position: absolute;
        top: 20px;
        right: 140px;
        padding: 10px 20px;
        background: #1890ff;
        color: #fff;
        border: none;
        border-radius: 4px;
        cursor: pointer;
        font-size: 16px;
        font-weight: bold;
        z-index: 10000;
        box-shadow: 0 2px 8px rgba(0,0,0,0.3);
        transition: all 0.3s ease;
      `
      clearCacheBtn.onmouseover = () => {
        clearCacheBtn.style.background = '#40a9ff'
        clearCacheBtn.style.transform = 'scale(1.05)'
      }
      clearCacheBtn.onmouseout = () => {
        clearCacheBtn.style.background = '#1890ff'
        clearCacheBtn.style.transform = 'scale(1)'
      }
      clearCacheBtn.onclick = () => {
        if (confirm('确定要清理所有文档缓存吗？')) {
          clearDocCache()
          alert('缓存已清理')
        }
      }

      // 创建加载提示
      const loadingText = document.createElement('div')
      loadingText.textContent = '正在加载文档...'
      loadingText.style.cssText = `
        color: white;
        font-size: 18px;
        margin-bottom: 20px;
      `

      // 创建预览外层容器
      const wrapper = document.createElement('div')
      wrapper.style.cssText = `
        width: 90%;
        max-width: 1200px;
        height: 90%;
        background: white;
        border-radius: 8px;
        overflow: hidden;
        box-shadow: 0 4px 20px rgba(0, 0, 0, 0.3);
        display: flex;
        flex-direction: column;
      `

      // 创建文档标题栏
      const titleBar = document.createElement('div')
      titleBar.style.cssText = `
        padding: 15px 20px;
        background: #f5f5f5;
        border-bottom: 1px solid #ddd;
        font-weight: bold;
        font-size: 14px;
        color: #333;
      `
      titleBar.textContent = '文档预览'

      // 创建预览容器
      const container = document.createElement('div')
      container.style.cssText = `
        flex: 1;
        overflow: auto;
        padding: 30px;
        background: #fafafa;
      `

      wrapper.appendChild(titleBar)
      wrapper.appendChild(container)

      overlay.appendChild(closeBtn)
      overlay.appendChild(clearCacheBtn)
      overlay.appendChild(loadingText)
      overlay.appendChild(wrapper)
      document.body.appendChild(overlay)

      // 检查缓存
      let fileBlob
      if (docCache.has(fileUrl)) {
        const cached = docCache.get(fileUrl)
        const now = Date.now()

        // 检查缓存是否过期
        if (now - cached.timestamp <= MAX_CACHE_AGE) {
          console.log('从缓存加载文档')
          fileBlob = cached.blob
        } else {
          console.log('缓存已过期，重新下载')
          docCache.delete(fileUrl)
          fileBlob = null
        }
      }

      if (!fileBlob) {
        console.log('从服务器下载文档')
        // 使用 axios 获取文件（通过代理避免 CORS 问题）
        const response = await axios.get(requestUrl, {
          responseType: 'blob'
        })
        fileBlob = response.data
        console.log('文件下载成功，大小:', fileBlob.size, 'bytes')

        // 存入缓存
        docCache.set(fileUrl, {
          blob: fileBlob,
          timestamp: Date.now(),
          size: fileBlob.size
        })
        console.log('文档已缓存')

        // 管理缓存大小
        manageCacheSize()
      }

      // 移除加载提示
      loadingText.remove()

      // 使用 docx-preview 渲染
      await renderAsync(fileBlob, container, null, {
        className: 'docx-preview',
        inWrapper: false,
        ignoreWidth: false,
        ignoreHeight: false,
        ignoreFonts: false,
        breakPages: true,
        ignoreLastRenderedPageBreak: true,
        experimental: false,
        trimXmlDeclaration: true,
        useBase64URL: false,
        renderChanges: false,
        renderHeaders: true,
        renderFooters: true,
        renderFootnotes: true,
        renderEndnotes: true,
        debug: false
      })

      console.log('文档渲染完成')

      // 添加自定义样式优化显示
      const style = document.createElement('style')
      style.textContent = `
        .docx-preview {
          background: white;
          padding: 40px;
          margin: 0 auto;
          box-shadow: 0 0 10px rgba(0,0,0,0.1);
        }
        .docx-wrapper {
          background: white !important;
        }
        .docx-wrapper > section.docx {
          background: white;
          padding: 40px;
          min-height: 100%;
          box-sizing: border-box;
        }
      `
      document.head.appendChild(style)

      // 关闭时清理样式
      const originalOnclick = closeBtn.onclick
      closeBtn.onclick = () => {
        document.head.removeChild(style)
        originalOnclick()
      }

    } catch (error) {
      console.error('预览 DOCX 文件失败:', error)
      console.error('错误详情:', {
        message: error.message,
        status: error.response?.status,
        statusText: error.response?.statusText,
        url: error.config?.url
      })
      alert('预览失败：' + error.message)
      // 清理可能残留的遮罩层
      const overlay = document.querySelector('div[style*="z-index: 9999"]')
      if (overlay) {
        document.body.removeChild(overlay)
      }
    }
  }

  const formatDate = (dateString) => {
    if (!dateString) return '未知'
    const date = new Date(dateString)
    return date.toLocaleDateString('zh-CN')
  }

  const formatDateTime = (dateString) => {
    if (!dateString) return '未知'
    const date = new Date(dateString)
    return date.toLocaleString('zh-CN')
  }

  // 筛选竞赛
  const filterCompetitions = () => {
    if (participationFilter.value === 'all') {
      competitions.value = [...allCompetitions.value]
    } else {
      competitions.value = allCompetitions.value.filter(
        competition => competition.participationMode === participationFilter.value
      )
    }
  }

  // 获取参赛方式样式类
  const getParticipationClass = (mode) => {
    return {
      'individual': 'badge-individual',
      'team': 'badge-team'
    }[mode] || 'badge-default'
  }

  // 格式化参赛方式显示文本
  const formatParticipationMode = (mode) => {
    return {
      'individual': '个人参赛',
      'team': '团队参赛'
    }[mode] || mode
  }

  return {
    // 响应式数据
    loading,
    currentUser,
    competitions,
    participationFilter,
    showCancelModal,
    selectedCompetition,
    showUploadModal,
    uploadingCompetition,
    uploadFile,
    uploading,

    // 方法
    loadUserInfo,
    loadUserCompetitions,
    filterCompetitions,
    getCompetitionStatus,
    canCancelRegistration,
    getStatusClass,
    getParticipationClass,
    formatParticipationMode,
    openCancelModal,
    closeCancelModal,
    cancelRegistration,
    viewCompetitionDetail,
    downloadProjectPlan,
    openUploadModal,
    closeUploadModal,
    handleFileSelect,
    uploadProjectPlan,
    viewProjectPlan,
    formatDate,
    formatDateTime,
    clearDocCache,
    clearExpiredCache
  }
}