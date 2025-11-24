
// 获取当前用户ID的工具函数
function getCurrentUserId() {
  const user = JSON.parse(localStorage.getItem('user') || '{}')
  return user.id || null
}