import request from './request'

// ---- 认证 ----
export const register = (data) => request.post('/api/auth/register', data)
export const login = (data) => request.post('/api/auth/login', data)

// ---- 血糖 ----
export const addGlucose = (data) => request.post('/api/glucose', data)
export const listGlucose = (params) => request.get('/api/glucose', { params })
export const deleteGlucose = (id) => request.delete(`/api/glucose/${id}`)

// ---- 饮食 ----
export const addDiet = (data) => request.post('/api/diet', data)
export const listDiet = (params) => request.get('/api/diet', { params })
export const deleteDiet = (id) => request.delete(`/api/diet/${id}`)

// ---- 运动 ----
export const addExercise = (data) => request.post('/api/exercise', data)
export const listExercise = (params) => request.get('/api/exercise', { params })
export const deleteExercise = (id) => request.delete(`/api/exercise/${id}`)

// ---- AI 智能体 ----
export const aiAdvice = (data) => request.post('/api/ai/advice', data)
export const aiHistory = () => request.get('/api/ai/advice')
export const aiPredict = (horizon = 6) => request.post(`/api/ai/predict?horizon=${horizon}`)
export const aiAnomaly = () => request.get('/api/ai/anomaly')

// ---- 健康档案 / 风险预警 / 数智健管师 ----
export const getProfile = () => request.get('/api/profile')
export const saveProfile = (data) => request.put('/api/profile', data)
export const assessRisk = () => request.post('/api/risk/assess')
export const healthPlan = (data) => request.post('/api/risk/health-plan', data)
export const structuredHealthPlan = () => request.post('/api/risk/structured-plan')
export const saveStructuredPlan = (data) => request.post('/api/risk/plans', data)
export const listSavedPlans = () => request.get('/api/risk/plans')
export const listAlerts = () => request.get('/api/risk/alerts')
export const readAlert = (id) => request.put(`/api/risk/alerts/${id}/read`)

// ---- 智能体检 ----
export const recommendCenters = (lat, lng) => request.get('/api/exam/centers', { params: { lat, lng } })
export const bookAppointment = (data) => request.post('/api/exam/appointments', data)
export const myAppointments = () => request.get('/api/exam/appointments')
export const cancelAppointment = (id) => request.delete(`/api/exam/appointments/${id}`)

// ---- 账户 ----
export const getMe = () => request.get('/api/user/me')
export const updateMe = (data) => request.put('/api/user/me', data)

// ---- 智能设备 ----
export const listDevices = () => request.get('/api/devices')
export const bindDevice = (data) => request.post('/api/devices/bind', data)
export const simulateDeviceData = (id, data) => request.post(`/api/devices/${id}/simulate`, data)
export const unbindDevice = (id) => request.delete(`/api/devices/${id}`)

// ---- Apple 健康数据导入 ----
export const importAppleHealth = (file, days = 90) => {
  const form = new FormData()
  form.append('file', file)
  form.append('days', days)
  return request.post('/api/health/import/apple', form, { headers: { 'Content-Type': 'multipart/form-data' }, timeout: 300000 })
}

// ---- 管理员运维 ----
export const adminOverview = () => request.get('/api/admin/overview')
export const adminUsers = (keyword) => request.get('/api/admin/users', { params: { keyword } })
export const adminSetUserStatus = (id, status) => request.put(`/api/admin/users/${id}/status`, { status })
export const adminResetPassword = (id, password) => request.put(`/api/admin/users/${id}/reset-password`, { password })
export const adminDeleteUser = (id) => request.delete(`/api/admin/users/${id}`)
export const adminDevices = () => request.get('/api/admin/devices')
export const adminSetDeviceStatus = (id, status) => request.put(`/api/admin/devices/${id}/status`, { status })
export const adminDeleteDevice = (id) => request.delete(`/api/admin/devices/${id}`)

// ---- 影像识别 ----
export const detectImage = (file) => {
  const form = new FormData()
  form.append('file', file)
  return request.post('/api/imaging/detect', form, { headers: { 'Content-Type': 'multipart/form-data' } })
}

// ---- 通用体检报告 ----
export const analyzeHealthReport = (file) => {
  const form = new FormData()
  form.append('file', file)
  return request.post('/api/reports/analyze', form, { headers: { 'Content-Type': 'multipart/form-data' } })
}
export const listHealthReports = () => request.get('/api/reports')
export const getHealthReport = (id) => request.get(`/api/reports/${id}`)
export const deleteHealthReport = (id) => request.delete(`/api/reports/${id}`)

/**
 * 流式对话:基于 fetch 读取 SSE。onChunk(text) 持续回调增量,完成时 resolve。
 */
export async function aiChatStream(question, onChunk) {
  const token = localStorage.getItem('token')
  const resp = await fetch('/api/ai/chat/stream', {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
      Authorization: token ? `Bearer ${token}` : ''
    },
    body: JSON.stringify({ question })
  })
  if (!resp.ok || !resp.body) {
    throw new Error('流式请求失败: ' + resp.status)
  }
  const reader = resp.body.getReader()
  const decoder = new TextDecoder('utf-8')
  let buffer = ''
  while (true) {
    const { done, value } = await reader.read()
    if (done) break
    buffer += decoder.decode(value, { stream: true })
    const lines = buffer.split('\n')
    buffer = lines.pop() // 末尾可能不完整,留到下一轮
    for (const line of lines) {
      if (!line.startsWith('data:')) continue
      let payload = line.slice(5)
      if (payload.startsWith(' ')) payload = payload.slice(1) // SSE 规范:去掉一个前导空格
      if (payload === '[DONE]') return
      if (payload.startsWith('[ERROR]')) throw new Error(payload)
      onChunk(payload)
    }
  }
}
