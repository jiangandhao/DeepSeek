import request from '@/utils/request'

// 体检套餐相关
export function getCheckupPackages() {
  return request({
    url: '/checkup/packages',
    method: 'get'
  })
}

export function getPackageDetail(id) {
  return request({
    url: `/checkup/packages/${id}`,
    method: 'get'
  })
}

// 体检机构相关
export function getCheckupCenters(params) {
  return request({
    url: '/checkup/centers',
    method: 'get',
    params
  })
}

export function getCenterDetail(id) {
  return request({
    url: `/checkup/centers/${id}`,
    method: 'get'
  })
}

// 预约相关
export function createAppointment(data) {
  return request({
    url: '/checkup/appointments',
    method: 'post',
    data
  })
}

export function getAppointments(params) {
  return request({
    url: '/checkup/appointments',
    method: 'get',
    params
  })
}

export function getAppointmentDetail(id) {
  return request({
    url: `/checkup/appointments/${id}`,
    method: 'get'
  })
}

export function cancelAppointment(id) {
  return request({
    url: `/checkup/appointments/${id}/cancel`,
    method: 'put'
  })
}

// 体检报告相关
export function getReports(params) {
  return request({
    url: '/checkup/reports',
    method: 'get',
    params
  })
}

export function getReportDetail(id) {
  return request({
    url: `/checkup/reports/${id}`,
    method: 'get'
  })
}

export function exportReport(id) {
  return request({
    url: `/checkup/reports/${id}/export`,
    method: 'get',
    responseType: 'blob'
  })
}

// AI报告解读
export function analyzeReport(reportId) {
  return request({
    url: `/checkup/reports/${reportId}/analyze`,
    method: 'post'
  })
}

// 影像分析
export function analyzeImage(data) {
  return request({
    url: '/checkup/image/analyze',
    method: 'post',
    headers: {
      'Content-Type': 'multipart/form-data'
    },
    data
  })
}

export function getAnalysisResult(taskId) {
  return request({
    url: `/checkup/image/result/${taskId}`,
    method: 'get'
  })
}

// 时间槽查询
export function getTimeSlots(centerId, date) {
  return request({
    url: '/checkup/timeslots',
    method: 'get',
    params: { centerId, date }
  })
}

// AI智能推荐
export function getAiRecommendation() {
  return request({
    url: '/checkup/recommendation',
    method: 'get'
  })
}
