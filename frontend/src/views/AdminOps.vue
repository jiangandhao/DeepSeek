<template>
  <div class="page">
    <div class="page-heading">
      <div>
        <p class="eyebrow">Operations</p>
        <h1 class="page-title">管理员运维端</h1>
        <p class="page-subtitle">运营管理控制台，覆盖用户管理、日活管理与设备管理。</p>
      </div>
      <el-button :loading="loading" @click="load"><el-icon><Refresh /></el-icon> 刷新</el-button>
    </div>

    <section class="ops-metrics">
      <article v-for="item in metrics" :key="item.label" class="surface metric">
        <span><component :is="item.icon" /></span>
        <p>{{ item.label }}</p>
        <h2>{{ item.value }}</h2>
        <small>{{ item.note }}</small>
      </article>
    </section>

    <section class="surface console">
      <el-tabs v-model="tab" class="ops-tabs">
        <!-- 用户管理 -->
        <el-tab-pane name="users">
          <template #label><span class="tab-label"><User /> 用户管理</span></template>
          <div class="pane-tools">
            <el-input v-model="keyword" placeholder="搜索账号或昵称" clearable style="max-width: 260px" :prefix-icon="Search" @keyup.enter="loadUsers" @clear="loadUsers" />
            <el-button type="primary" plain @click="loadUsers">搜索</el-button>
            <span class="count">共 {{ users.length }} 个用户</span>
          </div>
          <el-table :data="users" v-loading="usersLoading" height="420">
            <el-table-column prop="id" label="ID" width="70" />
            <el-table-column prop="username" label="账号" min-width="120" />
            <el-table-column prop="nickname" label="昵称" min-width="120">
              <template #default="{ row }">{{ row.nickname || '—' }}</template>
            </el-table-column>
            <el-table-column prop="deviceCount" label="设备数" width="86" align="center" />
            <el-table-column label="注册时间" min-width="170">
              <template #default="{ row }">{{ formatTime(row.createdAt) }}</template>
            </el-table-column>
            <el-table-column label="状态" width="90">
              <template #default="{ row }">
                <el-tag :type="row.status === 1 ? 'success' : 'info'">{{ row.status === 1 ? '正常' : '禁用' }}</el-tag>
              </template>
            </el-table-column>
            <el-table-column label="操作" width="230" fixed="right">
              <template #default="{ row }">
                <template v-if="row.username !== 'admin'">
                  <el-button size="small" :type="row.status === 1 ? 'warning' : 'success'" plain @click="toggleUser(row)">
                    {{ row.status === 1 ? '禁用' : '启用' }}
                  </el-button>
                  <el-button size="small" plain @click="openReset(row)">重置密码</el-button>
                  <el-button size="small" type="danger" plain @click="removeUser(row)">删除</el-button>
                </template>
                <el-tag v-else type="warning" effect="plain">管理员</el-tag>
              </template>
            </el-table-column>
          </el-table>
        </el-tab-pane>

        <!-- 日活管理 -->
        <el-tab-pane name="active">
          <template #label><span class="tab-label"><TrendCharts /> 日活管理</span></template>
          <div class="active-head">
            <div><h3 class="section-title">近 7 日日活趋势</h3><p class="section-copy">按当日血糖上报去重用户数统计</p></div>
            <span class="soft-tag">日均 {{ avgActive }} 人</span>
          </div>
          <div ref="chartRef" class="chart" />
          <el-table :data="dailyActiveRows" height="220" style="margin-top: 12px">
            <el-table-column prop="date" label="日期" />
            <el-table-column prop="activeUsers" label="活跃用户数" />
            <el-table-column label="活跃占比">
              <template #default="{ row }">
                <div class="bar-cell">
                  <span class="bar"><i :style="{ width: ratio(row.activeUsers) + '%' }" /></span>
                  <b>{{ ratio(row.activeUsers) }}%</b>
                </div>
              </template>
            </el-table-column>
          </el-table>
        </el-tab-pane>

        <!-- 设备管理 -->
        <el-tab-pane name="devices">
          <template #label><span class="tab-label"><Watch /> 设备管理</span></template>
          <div class="pane-tools"><span class="count">共 {{ devices.length }} 台设备</span></div>
          <el-table :data="devices" v-loading="devicesLoading" height="500">
            <el-table-column prop="deviceNo" label="设备编号" min-width="150" />
            <el-table-column prop="deviceName" label="名称" min-width="130" />
            <el-table-column prop="deviceType" label="类型" min-width="110">
              <template #default="{ row }">{{ row.deviceType || '—' }}</template>
            </el-table-column>
            <el-table-column prop="userId" label="所属用户" width="90" align="center" />
            <el-table-column label="电量" width="84" align="center">
              <template #default="{ row }">{{ row.batteryLevel != null ? row.batteryLevel + '%' : '—' }}</template>
            </el-table-column>
            <el-table-column label="状态" width="100">
              <template #default="{ row }">
                <el-tag :type="statusTagType(row.status)">{{ statusText(row.status) }}</el-tag>
              </template>
            </el-table-column>
            <el-table-column label="操作" width="240" fixed="right">
              <template #default="{ row }">
                <el-button size="small" type="success" plain :disabled="row.status === 'ONLINE'" @click="setDeviceStatus(row, 'ONLINE')">上线</el-button>
                <el-button size="small" plain :disabled="row.status === 'OFFLINE'" @click="setDeviceStatus(row, 'OFFLINE')">下线</el-button>
                <el-button size="small" type="danger" plain @click="removeDevice(row)">删除</el-button>
              </template>
            </el-table-column>
          </el-table>
        </el-tab-pane>
      </el-tabs>
    </section>

    <!-- 重置密码弹窗 -->
    <el-dialog v-model="resetVisible" title="重置用户密码" width="400px">
      <p class="dialog-tip">为用户 <b>{{ resetTarget?.username }}</b> 设置新密码（6-32 位）。</p>
      <el-input v-model="newPassword" type="password" placeholder="新密码" show-password size="large" />
      <template #footer>
        <el-button @click="resetVisible = false">取消</el-button>
        <el-button type="primary" :loading="resetting" @click="confirmReset">确认重置</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { computed, nextTick, onBeforeUnmount, onMounted, ref } from 'vue'
import * as echarts from 'echarts'
import { Connection, Refresh, Search, TrendCharts, User, Watch } from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  adminOverview, adminUsers, adminDevices,
  adminSetUserStatus, adminResetPassword, adminDeleteUser,
  adminSetDeviceStatus, adminDeleteDevice
} from '../api'

const loading = ref(false)
const usersLoading = ref(false)
const devicesLoading = ref(false)
const tab = ref('users')
const keyword = ref('')
const overview = ref({})
const users = ref([])
const devices = ref([])
const chartRef = ref(null)
let chart

const resetVisible = ref(false)
const resetTarget = ref(null)
const newPassword = ref('')
const resetting = ref(false)

const metrics = computed(() => [
  { label: '总用户', value: overview.value.totalUsers ?? 0, note: `${overview.value.activeUsers ?? 0} 个正常账号`, icon: User },
  { label: '今日日活', value: overview.value.todayActive ?? 0, note: '今日血糖上报用户', icon: TrendCharts },
  { label: '设备总数', value: overview.value.devices ?? 0, note: `${overview.value.onlineDevices ?? 0} 台在线`, icon: Watch },
  { label: '在线率', value: onlineRate.value, note: '设备在线健康度', icon: Connection }
])
const onlineRate = computed(() => {
  const total = Number(overview.value.devices || 0)
  return total ? `${Math.round((Number(overview.value.onlineDevices || 0) / total) * 100)}%` : '0%'
})
const dailyActiveRows = computed(() => (overview.value.dailyActive || []).map(i => ({ date: i.date, activeUsers: i.activeUsers })))
const maxActive = computed(() => Math.max(1, ...dailyActiveRows.value.map(i => Number(i.activeUsers || 0))))
const avgActive = computed(() => {
  const rows = dailyActiveRows.value
  if (!rows.length) return 0
  return Math.round(rows.reduce((s, i) => s + Number(i.activeUsers || 0), 0) / rows.length)
})
function ratio(v) { return Math.round((Number(v || 0) / maxActive.value) * 100) }

async function load() {
  loading.value = true
  try {
    overview.value = (await adminOverview()) || {}
    await Promise.all([loadUsers(), loadDevices()])
    await nextTick()
    renderChart()
  } finally {
    loading.value = false
  }
}

async function loadUsers() {
  usersLoading.value = true
  try { users.value = (await adminUsers(keyword.value)) || [] }
  finally { usersLoading.value = false }
}

async function loadDevices() {
  devicesLoading.value = true
  try { devices.value = (await adminDevices()) || [] }
  finally { devicesLoading.value = false }
}

async function refreshOverview() {
  overview.value = (await adminOverview()) || {}
  await nextTick()
  renderChart()
}

async function toggleUser(row) {
  const next = row.status === 1 ? 0 : 1
  await adminSetUserStatus(row.id, next)
  ElMessage.success(next === 1 ? '已启用' : '已禁用')
  await Promise.all([loadUsers(), refreshOverview()])
}

function openReset(row) {
  resetTarget.value = row
  newPassword.value = ''
  resetVisible.value = true
}
async function confirmReset() {
  if (newPassword.value.length < 6 || newPassword.value.length > 32) {
    ElMessage.warning('密码长度需为 6-32 位')
    return
  }
  resetting.value = true
  try {
    await adminResetPassword(resetTarget.value.id, newPassword.value)
    ElMessage.success('密码已重置')
    resetVisible.value = false
  } finally {
    resetting.value = false
  }
}

async function removeUser(row) {
  await ElMessageBox.confirm(`确定删除用户「${row.username}」及其设备数据吗？此操作不可恢复。`, '删除用户', { type: 'warning' })
  await adminDeleteUser(row.id)
  ElMessage.success('已删除')
  await Promise.all([loadUsers(), loadDevices(), refreshOverview()])
}

async function setDeviceStatus(row, status) {
  await adminSetDeviceStatus(row.id, status)
  ElMessage.success('状态已更新')
  await Promise.all([loadDevices(), refreshOverview()])
}

async function removeDevice(row) {
  await ElMessageBox.confirm(`确定删除设备「${row.deviceName || row.deviceNo}」吗？`, '删除设备', { type: 'warning' })
  await adminDeleteDevice(row.id)
  ElMessage.success('已删除')
  await Promise.all([loadDevices(), refreshOverview()])
}

function renderChart() {
  if (!chartRef.value) return
  chart ||= echarts.init(chartRef.value)
  const data = overview.value.dailyActive || []
  chart.setOption({
    grid: { left: 8, right: 12, top: 22, bottom: 8, containLabel: true },
    tooltip: { trigger: 'axis' },
    xAxis: { type: 'category', data: data.map(i => i.date?.slice(5)), axisTick: { show: false }, axisLine: { show: false } },
    yAxis: { type: 'value', minInterval: 1, splitLine: { lineStyle: { color: '#edf3f0' } }, axisLine: { show: false } },
    series: [{ type: 'bar', data: data.map(i => i.activeUsers), barWidth: 28, itemStyle: { color: '#35a889', borderRadius: [8, 8, 0, 0] } }]
  })
}

function statusText(status) {
  return { ONLINE: '在线', OFFLINE: '离线', LOW_BATTERY: '低电量', UNBOUND: '已解绑' }[status] || status || '未知'
}
function statusTagType(status) {
  return status === 'ONLINE' ? 'success' : status === 'LOW_BATTERY' ? 'warning' : 'info'
}
function formatTime(t) {
  if (!t) return '—'
  return String(t).replace('T', ' ').slice(0, 16)
}

function resize() { chart?.resize() }
onMounted(() => { load(); window.addEventListener('resize', resize) })
onBeforeUnmount(() => { window.removeEventListener('resize', resize); chart?.dispose() })
</script>

<style scoped>
.ops-metrics { display:grid; grid-template-columns:repeat(4,1fr); gap:18px; }
.metric { padding:22px; }
.metric span { display:grid; width:40px; height:40px; place-items:center; color:var(--mint-700); background:#eef8f4; border-radius:12px; }
.metric svg { width:20px; }
.metric p { margin:18px 0 6px; color:var(--muted); }
.metric h2 { margin:0; font-size:30px; }
.metric small { color:var(--muted); }
.console { margin-top:18px; padding:8px 22px 22px; }
.ops-tabs :deep(.el-tabs__item) { font-weight:600; }
.tab-label { display:inline-flex; align-items:center; gap:6px; }
.tab-label svg { width:16px; }
.pane-tools { display:flex; align-items:center; gap:12px; margin-bottom:14px; }
.pane-tools .count { margin-left:auto; color:var(--muted); font-size:13px; }
.active-head { display:flex; align-items:flex-end; justify-content:space-between; gap:12px; margin-bottom:14px; }
.chart { height:260px; }
.bar-cell { display:flex; align-items:center; gap:10px; }
.bar { flex:1; max-width:160px; height:8px; background:#eef3f0; border-radius:999px; overflow:hidden; }
.bar i { display:block; height:100%; background:var(--mint-500); border-radius:999px; }
.bar-cell b { color:var(--mint-700); font-size:13px; }
.dialog-tip { margin:0 0 14px; color:var(--muted); font-size:14px; }
@media(max-width:1100px){ .ops-metrics{ grid-template-columns:repeat(2,1fr); } }
@media(max-width:620px){ .ops-metrics{ grid-template-columns:1fr; } .active-head{ align-items:flex-start; flex-direction:column; } }
</style>
