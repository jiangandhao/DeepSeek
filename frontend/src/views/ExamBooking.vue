<template>
  <div class="page">
  <div class="page-heading">
    <div><p class="eyebrow">Examination</p><h1 class="page-title">体检与报告</h1><p class="page-subtitle">根据风险画像推荐检查项目，完成预约后统一管理历年报告。</p></div>
    <el-button type="primary" :loading="reportUploading" @click="fileInput?.click()">上传体检报告</el-button>
    <input ref="fileInput" class="file-input" type="file" accept="image/*,.pdf" @change="onReportUpload" />
  </div>
  <div class="recommend-banner">
    <span>AI 推荐</span><div><b>代谢健康进阶套餐</b><p>基于近期餐后血糖波动，建议增加糖化血红蛋白、血脂四项与肝肾功能检查。</p></div><strong>匹配度 92%</strong>
  </div>
  <el-row :gutter="16">
    <el-col :span="14">
      <el-card>
        <template #header>
          <div class="hd"><span>🏥 智能体检推荐</span>
            <div>
              <el-select v-model="location" size="small" style="width: 160px" @change="loadCenters">
                <el-option v-for="l in locations" :key="l.name" :label="l.name" :value="l.name" />
              </el-select>
            </div>
          </div>
        </template>
        <p class="muted">按「地理距离(60%) + 机构繁忙度(40%)」综合匹配排序,匹配分越高越推荐。</p>
        <el-table :data="centers" v-loading="loading">
          <el-table-column prop="name" label="机构" min-width="160" />
          <el-table-column prop="distanceKm" label="距离(km)" width="90" />
          <el-table-column label="繁忙度" width="120">
            <template #default="{ row }">
              <el-progress :percentage="row.busyness" :stroke-width="10"
                :color="row.busyness > 70 ? '#f56c6c' : row.busyness > 40 ? '#e6a23c' : '#67c23a'" />
            </template>
          </el-table-column>
          <el-table-column label="匹配分" width="90">
            <template #default="{ row }">
              <el-tag :type="row.matchScore >= 70 ? 'success' : 'info'">{{ row.matchScore }}</el-tag>
            </template>
          </el-table-column>
          <el-table-column label="操作" width="90">
            <template #default="{ row }">
              <el-button size="small" type="primary" @click="openBook(row)">预约</el-button>
            </template>
          </el-table-column>
        </el-table>
      </el-card>
    </el-col>

    <el-col :span="10">
      <el-card>
        <template #header><span>📅 我的预约</span></template>
        <el-table :data="appointments" size="small">
          <el-table-column prop="centerName" label="机构" show-overflow-tooltip />
          <el-table-column prop="examDate" label="日期" width="110" />
          <el-table-column prop="status" label="状态" width="90">
            <template #default="{ row }">
              <el-tag size="small" :type="row.status === 'CANCELLED' ? 'info' : 'success'">
                {{ statusCn(row.status) }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column label="" width="70">
            <template #default="{ row }">
              <el-button v-if="row.status === 'BOOKED'" text type="danger" size="small" @click="onCancel(row.id)">取消</el-button>
            </template>
          </el-table-column>
        </el-table>
      </el-card>
    </el-col>
  </el-row>

  <el-card class="report-card">
    <template #header><div class="hd"><div><b>报告管理</b><p class="muted">支持化验单、常规体检报告图片/PDF及医学影像</p></div><el-button @click="loadReports">刷新</el-button></div></template>
    <el-table :data="reports" empty-text="尚未上传体检报告">
      <el-table-column prop="filename" label="报告文件" min-width="180" />
      <el-table-column prop="reportType" label="类型" width="150"><template #default="{row}">{{ reportTypeCn(row.reportType) }}</template></el-table-column>
      <el-table-column prop="riskLevel" label="风险" width="90"><template #default="{row}"><el-tag :type="row.riskLevel==='HIGH'?'danger':row.riskLevel==='MEDIUM'?'warning':'success'">{{ levelCn(row.riskLevel) }}</el-tag></template></el-table-column>
      <el-table-column prop="abnormalCount" label="异常项" width="85" />
      <el-table-column prop="summary" label="AI 摘要" min-width="260" show-overflow-tooltip />
      <el-table-column label="操作" width="90"><template #default="{row}"><el-button text type="primary" @click="openReport(row.id)">详情</el-button></template></el-table-column>
    </el-table>
  </el-card>

  <el-dialog v-model="dialog" title="预约体检" width="420px">
    <el-form label-width="80px">
      <el-form-item label="机构"><span>{{ current?.name }}</span></el-form-item>
      <el-form-item label="日期">
        <el-date-picker v-model="bookForm.examDate" type="date" value-format="YYYY-MM-DD" />
      </el-form-item>
      <el-form-item label="套餐">
        <el-select v-model="bookForm.pkg" style="width: 100%">
          <el-option v-for="p in currentPackages" :key="p" :label="p" :value="p" />
        </el-select>
      </el-form-item>
    </el-form>
    <template #footer>
      <el-button @click="dialog = false">取消</el-button>
      <el-button type="primary" @click="onBook">确认预约</el-button>
    </template>
  </el-dialog>
  <el-dialog v-model="reportDialog" title="体检报告智能解读" width="min(760px, 92vw)">
    <div v-if="reportDetail" class="report-detail"><el-alert :title="reportDetail.summary" :type="reportDetail.riskLevel==='HIGH'?'error':reportDetail.riskLevel==='MEDIUM'?'warning':'success'" :closable="false"/><div v-if="parsedReport.indicators?.length" class="indicator-list"><div v-for="item in parsedReport.indicators" :key="item.code"><b>{{item.name}}</b><span :class="item.status.toLowerCase()">{{item.value}} {{item.unit}}</span><small>参考 {{item.reference}} · {{item.advice}}</small></div></div><p class="ai-note">{{parsedReport.disclaimer}}</p></div>
  </el-dialog>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { recommendCenters, bookAppointment, myAppointments, cancelAppointment, analyzeHealthReport, listHealthReports, getHealthReport } from '../api'

// 模拟用户定位(实际可用浏览器 Geolocation)
const locations = [
  { name: '市中心', lat: 31.2304, lng: 121.4737 },
  { name: '浦东', lat: 31.2215, lng: 121.5443 },
  { name: '徐汇', lat: 31.1902, lng: 121.4376 },
  { name: '长宁', lat: 31.2109, lng: 121.4012 }
]
const location = ref('市中心')
const centers = ref([])
const appointments = ref([])
const loading = ref(false)
const dialog = ref(false)
const current = ref(null)
const bookForm = ref({ examDate: '', pkg: '' })
const fileInput = ref(null)
const reportUploading = ref(false)
const reports = ref([])
const reportDialog = ref(false)
const reportDetail = ref(null)

const currentPackages = computed(() => (current.value?.packages ? current.value.packages.split(',') : []))
const statusCn = (s) => ({ BOOKED: '已预约', DONE: '已完成', CANCELLED: '已取消' }[s] || s)
const levelCn = (s) => ({ HIGH: '高', MEDIUM: '中', LOW: '低' }[s] || s)
const reportTypeCn = (s) => ({ LAB_REPORT: '化验/检验报告', GENERAL_EXAM_REPORT: '综合体检报告', MEDICAL_IMAGE: '医学影像' }[s] || s)
const parsedReport = computed(() => { try { return JSON.parse(reportDetail.value?.structuredJson || '{}') } catch { return {} } })

async function loadCenters() {
  loading.value = true
  try {
    const loc = locations.find((l) => l.name === location.value)
    centers.value = await recommendCenters(loc.lat, loc.lng)
  } finally { loading.value = false }
}
async function loadAppointments() { appointments.value = await myAppointments() }
async function loadReports() { try { reports.value = await listHealthReports() } catch { reports.value = [] } }
async function onReportUpload(event) {
  const file = event.target.files?.[0]
  if (!file) return
  reportUploading.value = true
  try { const report = await analyzeHealthReport(file); ElMessage.success('报告解析并归档完成'); await loadReports(); await openReport(report.id) }
  finally { reportUploading.value = false; event.target.value = '' }
}
async function openReport(id) { reportDetail.value = await getHealthReport(id); reportDialog.value = true }

function openBook(row) {
  current.value = row
  bookForm.value = { examDate: '', pkg: currentPackages.value[0] || '' }
  dialog.value = true
}
async function onBook() {
  if (!bookForm.value.examDate) { ElMessage.warning('请选择日期'); return }
  await bookAppointment({ centerId: current.value.id, examDate: bookForm.value.examDate, pkg: bookForm.value.pkg })
  ElMessage.success('预约成功')
  dialog.value = false
  loadAppointments()
}
async function onCancel(id) {
  await cancelAppointment(id)
  ElMessage.success('已取消')
  loadAppointments()
}

onMounted(() => { loadCenters(); loadAppointments(); loadReports() })
</script>

<style scoped>
.hd { display: flex; justify-content: space-between; align-items: center; }
.muted { color: #909399; font-size: 13px; margin-bottom: 10px; }
.file-input { display: none; }
.report-card { margin-top: 18px; }
.hd .muted { margin: 4px 0 0; }
.indicator-list { display: grid; gap: 10px; margin: 18px 0; }
.indicator-list > div { display: grid; grid-template-columns: 1fr auto; gap: 5px 15px; padding: 13px; background: #f7faf9; border-radius: 12px; }
.indicator-list small { grid-column: 1 / -1; color: var(--muted); }
.indicator-list .high, .indicator-list .low { color: var(--danger); font-weight: 800; }
.indicator-list .normal { color: var(--mint-700); font-weight: 800; }
.recommend-banner { display: grid; grid-template-columns: auto 1fr auto; align-items: center; gap: 16px; margin-bottom: 18px; padding: 19px 22px; color: #eaf7f2; background: linear-gradient(120deg, #1a594d, #103b33); border-radius: 20px; box-shadow: var(--shadow); }
.recommend-banner > span { padding: 7px 10px; color: #123d35; background: #bfe8d9; border-radius: 9px; font-size: 11px; font-weight: 800; }
.recommend-banner b, .recommend-banner p { display: block; margin: 0; }
.recommend-banner p { margin-top: 5px; color: #b8d5cd; font-size: 12px; }
.recommend-banner strong { color: #bfe8d9; }
:deep(.el-row) { row-gap: 16px; }
@media (max-width: 700px) { .recommend-banner { grid-template-columns: auto 1fr; } .recommend-banner strong { grid-column: 2; } }
</style>
