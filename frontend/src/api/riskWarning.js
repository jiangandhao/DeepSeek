import request from '@/utils/request'

// 风险评估相关
export function getRiskAssessment() {
  return request({
    url: '/risk/assessment',
    method: 'get'
  })
}

export function submitAssessment(data) {
  return request({
    url: '/risk/assessment',
    method: 'post',
    data
  })
}

export function getRiskDetail(diseaseType) {
  return request({
    url: `/risk/assessment/${diseaseType}`,
    method: 'get'
  })
}

// 风险分级
export function getRiskLevels() {
  return request({
    url: '/risk/levels',
    method: 'get'
  })
}

export function getRiskFactors() {
  return request({
    url: '/risk/factors',
    method: 'get'
  })
}

// 预警相关
export function getWarnings(params) {
  return request({
    url: '/risk/warnings',
    method: 'get',
    params
  })
}

export function getWarningDetail(id) {
  return request({
    url: `/risk/warnings/${id}`,
    method: 'get'
  })
}

export function markWarningRead(id) {
  return request({
    url: `/risk/warnings/${id}/read`,
    method: 'put'
  })
}

export function markAllWarningsRead() {
  return request({
    url: '/risk/warnings/read-all',
    method: 'put'
  })
}

export function dismissWarning(id) {
  return request({
    url: `/risk/warnings/${id}/dismiss`,
    method: 'put'
  })
}

// 预警设置
export function getWarningThresholds() {
  return request({
    url: '/risk/thresholds',
    method: 'get'
  })
}

export function updateWarningThresholds(data) {
  return request({
    url: '/risk/thresholds',
    method: 'put',
    data
  })
}

// 预警统计
export function getWarningStats() {
  return request({
    url: '/risk/warnings/stats',
    method: 'get'
  })
}

// 预防方案
export function getPreventionPlan() {
  return request({
    url: '/risk/prevention-plan',
    method: 'get'
  })
}

export function generatePreventionPlan(data) {
  return request({
    url: '/risk/prevention-plan/generate',
    method: 'post',
    data
  })
}

// 健康目标
export function getHealthGoals() {
  return request({
    url: '/risk/goals',
    method: 'get'
  })
}

export function updateHealthGoal(id, data) {
  return request({
    url: `/risk/goals/${id}`,
    method: 'put',
    data
  })
}

// 健康建议
export function getHealthAdvice() {
  return request({
    url: '/risk/advice',
    method: 'get'
  })
}
