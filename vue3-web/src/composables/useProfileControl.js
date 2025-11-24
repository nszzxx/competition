import { ref, reactive, onMounted } from 'vue'
import axios from 'axios'

export function useProfileControl() {
  const isEditing = ref(false)
  const saving = ref(false)
  const showAddSkill = ref(false)
  const showAddHonor = ref(false)
  const showEditHonor = ref(false)
  const newSkill = ref('')
  const avatarInput = ref(null)
  const certificateInput = ref(null)
  const uploadingCertificate = ref(false)

  const userInfo = reactive({
    id: null,
    username: '',
    email: '',
    major: '',
    studentId: '',
    bio: '',
    avatarUrl: ''
  })

  const editForm = reactive({
    username: '',
    email: '',
    major: '',
    studentId: '',
    bio: ''
  })

  const skills = ref([])
  const honors = ref([])

  const newHonor = reactive({
    title: '',
    description: '',
    date: '',
    certificateImageUrl: '',
    certificateFile: null,
    certificateFileName: ''
  })

  const editingHonor = ref(null)

  const loadUserInfo = async () => {
    const userStr = localStorage.getItem('user')
    if (userStr) {
      try {
        const user = JSON.parse(userStr)
        Object.assign(userInfo, user)
        
        // 从服务器获取完整用户信息
        const response = await axios.get(`/api/user/${user.id}`)
        Object.assign(userInfo, response.data)
      } catch (error) {
        console.error('加载用户信息失败:', error)
      }
    }
  }

  const loadUserSkills = async () => {
    if (!userInfo.id) return
    
    try {
      const response = await axios.get(`/api/user/${userInfo.id}/skills`)
      skills.value = response.data
    } catch (error) {
      console.error('加载用户技能失败:', error)
    }
  }

  const loadUserHonors = async () => {
    if (!userInfo.id) return
    
    try {
      const response = await axios.get(`/api/user-honours/user/${userInfo.id}`)
      honors.value = response.data.map(honor => ({
        id: honor.id,
        title: honor.honourTitle,
        description: honor.description || '',
        date: honor.obtainedTime ? honor.obtainedTime.split('T')[0] : '',
        certificateImageUrl: honor.certificateImageUrl || ''
      }))
    } catch (error) {
      console.error('加载用户荣誉失败:', error)
      honors.value = []
    }
  }

  const toggleEditMode = () => {
    if (isEditing.value) {
      cancelEdit()
    } else {
      Object.assign(editForm, userInfo)
      isEditing.value = true
    }
  }

  const cancelEdit = () => {
    isEditing.value = false
    Object.keys(editForm).forEach(key => {
      editForm[key] = ''
    })
  }

  const saveUserInfo = async () => {
    if (saving.value) return
    
    saving.value = true
    try {
      const response = await axios.put(`/api/user/${userInfo.id}`, editForm)
      Object.assign(userInfo, editForm)
      
      // 更新localStorage中的用户信息
      const currentUser = JSON.parse(localStorage.getItem('user'))
      Object.assign(currentUser, editForm)
      localStorage.setItem('user', JSON.stringify(currentUser))
      
      isEditing.value = false
      alert('保存成功！')
    } catch (error) {
      console.error('保存用户信息失败:', error)
      alert('保存失败，请重试')
    } finally {
      saving.value = false
    }
  }

  const uploadAvatar = () => {
    avatarInput.value?.click()
  }

  const handleAvatarUpload = async (event) => {
    const file = event.target.files[0]
    if (!file) return

    // 验证文件类型
    if (!file.type.startsWith('image/')) {
      alert('请选择图片文件')
      return
    }

    // 验证文件大小（限制为2MB）
    if (file.size > 2 * 1024 * 1024) {
      alert('图片大小不能超过2MB')
      return
    }

    try {
      const formData = new FormData()
      formData.append('avatar', file)
      formData.append('userId', userInfo.id)

      // 调用上传头像的API
      const response = await axios.post(`/api/user/${userInfo.id}/avatar`, formData, {
        headers: {
          'Content-Type': 'multipart/form-data'
        }
      })

      // 更新头像URL
      userInfo.avatarUrl = response.data.avatarUrl

      alert('头像上传成功！')
    } catch (error) {
      console.error('上传头像失败:', error)
      alert('上传失败，请重试')
    }
  }

  const addSkill = async () => {
    if (!newSkill.value.trim()) return

    try {
      const response = await axios.post(`/api/user/${userInfo.id}/skills`, {
        skill: newSkill.value.trim()
      })
      
      skills.value.push(response.data)
      newSkill.value = ''
      showAddSkill.value = false
    } catch (error) {
      console.error('添加技能失败:', error)
      alert('添加技能失败，请重试')
    }
  }

  const removeSkill = async (skillId) => {
    if (!confirm('确定要删除这个技能吗？')) return

    try {
      await axios.delete(`/api/user/${userInfo.id}/skills/${skillId}`)
      skills.value = skills.value.filter(skill => skill.id !== skillId)
    } catch (error) {
      console.error('删除技能失败:', error)
      alert('删除技能失败，请重试')
    }
  }

  const cancelAddSkill = () => {
    newSkill.value = ''
    showAddSkill.value = false
  }

  const resetHonorForm = () => {
    newHonor.title = ''
    newHonor.description = ''
    newHonor.date = ''
    newHonor.certificateImageUrl = ''
    newHonor.certificateFile = null
    newHonor.certificateFileName = ''
  }

  const uploadCertificate = () => {
    certificateInput.value?.click()
  }

  const handleCertificateUpload = (event) => {
    const file = event.target.files[0]
    if (!file) return

    // 验证文件类型（支持图片、PDF、Word）
    const allowedTypes = [
      'image/jpeg', 'image/jpg', 'image/png', 'image/gif', 'image/webp',
      'application/pdf',
      'application/msword',
      'application/vnd.openxmlformats-officedocument.wordprocessingml.document'
    ]

    if (!allowedTypes.includes(file.type)) {
      alert('仅支持上传图片（JPG、PNG、GIF等）、PDF或Word文档')
      return
    }

    // 验证文件大小（限制为3MB）
    if (file.size > 3 * 1024 * 1024) {
      alert('文件大小不能超过3MB')
      return
    }

    // 根据当前状态判断是编辑还是新增
    // 如果编辑表单打开且存在编辑对象，则保存到编辑对象
    if (showEditHonor.value && editingHonor.value) {
      editingHonor.value.certificateFile = file
      editingHonor.value.certificateFileName = file.name
    } else {
      // 否则保存到新增表单
      newHonor.certificateFile = file
      newHonor.certificateFileName = file.name
    }

    // 清空input的value，允许重复选择同一个文件
    event.target.value = ''
  }

  const addHonor = async () => {
    if (!newHonor.title.trim() || !newHonor.description.trim() || !newHonor.date) {
      alert('请填写完整的荣誉信息')
      return
    }

    try {
      const formData = new FormData()
      formData.append('userId', userInfo.id)
      formData.append('title', newHonor.title.trim())
      formData.append('description', newHonor.description.trim())
      formData.append('date', newHonor.date)

      // 如果有上传证书文件
      if (newHonor.certificateFile) {
        formData.append('certificate', newHonor.certificateFile)
      }

      const response = await axios.post('/api/user-honours', formData, {
        headers: {
          'Content-Type': 'multipart/form-data'
        }
      })

      // 添加到本地列表
      honors.value.push({
        id: response.data.id,
        title: response.data.honourTitle,
        description: response.data.description || '',
        date: response.data.obtainedTime ? response.data.obtainedTime.split('T')[0] : '',
        certificateImageUrl: response.data.certificateImageUrl || ''
      })

      // 重置表单
      resetHonorForm()
      showAddHonor.value = false

      alert('荣誉添加成功')
    } catch (error) {
      console.error('添加荣誉失败:', error)
      alert('添加荣誉失败，请重试')
    }
  }

  const removeHonor = async (honorId) => {
    if (!confirm('确定要删除这个荣誉吗？')) return
    
    try {
      await axios.delete(`/api/user-honours/${honorId}`)
      honors.value = honors.value.filter(honor => honor.id !== honorId)
      alert('荣誉删除成功')
    } catch (error) {
      console.error('删除荣誉失败:', error)
      alert('删除荣誉失败，请重试')
    }
  }

  const cancelAddHonor = () => {
    resetHonorForm()
    showAddHonor.value = false
  }

  const editHonor = (honor) => {
    editingHonor.value = {
      ...honor,
      certificateFile: null,
      certificateFileName: ''
    }
    showEditHonor.value = true
    showAddHonor.value = false
  }

  const updateHonor = async () => {
    if (!editingHonor.value.title.trim() || !editingHonor.value.description.trim() || !editingHonor.value.date) {
      alert('请填写完整的荣誉信息')
      return
    }

    try {
      const formData = new FormData()
      formData.append('title', editingHonor.value.title.trim())
      formData.append('description', editingHonor.value.description.trim())
      formData.append('date', editingHonor.value.date)

      // 如果有上传新的证书文件
      if (editingHonor.value.certificateFile) {
        formData.append('certificate', editingHonor.value.certificateFile)
      }

      const response = await axios.put(`/api/user-honours/${editingHonor.value.id}`, formData, {
        headers: {
          'Content-Type': 'multipart/form-data'
        }
      })

      // 更新本地列表
      const index = honors.value.findIndex(h => h.id === editingHonor.value.id)
      if (index !== -1) {
        honors.value[index] = {
          id: response.data.id,
          title: response.data.honourTitle,
          description: response.data.description || '',
          date: response.data.obtainedTime ? response.data.obtainedTime.split('T')[0] : '',
          certificateImageUrl: response.data.certificateImageUrl || ''
        }
      }

      showEditHonor.value = false
      editingHonor.value = null

      alert('荣誉更新成功')
    } catch (error) {
      console.error('更新荣誉失败:', error)
      alert('更新荣誉失败，请重试')
    }
  }

  const cancelEditHonor = () => {
    showEditHonor.value = false
    editingHonor.value = null
  }

  const viewCertificate = (certificateUrl) => {
    if (!certificateUrl) {
      alert('暂无证书图片')
      return
    }
    // 在新窗口中打开证书图片
    window.open(certificateUrl, '_blank')
  }

  const formatDate = (dateString) => {
    const date = new Date(dateString)
    return date.toLocaleDateString('zh-CN')
  }

  return {
    // 响应式数据
    isEditing,
    saving,
    showAddSkill,
    showAddHonor,
    showEditHonor,
    newSkill,
    avatarInput,
    certificateInput,
    uploadingCertificate,
    userInfo,
    editForm,
    skills,
    honors,
    newHonor,
    editingHonor,

    // 方法
    loadUserInfo,
    loadUserSkills,
    loadUserHonors,
    toggleEditMode,
    cancelEdit,
    saveUserInfo,
    uploadAvatar,
    handleAvatarUpload,
    addSkill,
    removeSkill,
    cancelAddSkill,
    addHonor,
    removeHonor,
    cancelAddHonor,
    editHonor,
    updateHonor,
    cancelEditHonor,
    viewCertificate,
    formatDate,
    uploadCertificate,
    handleCertificateUpload
  }
}