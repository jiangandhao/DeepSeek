<template>
  <div class="page">
    <div class="page-heading">
      <div>
        <p class="eyebrow">Operations</p>
        <h1 class="page-title">管理员运维端</h1>
        <p class="page-subtitle">面向演示的运营看板，覆盖用户管理、日活管理与设备管理。</p>
      </div>
      <el-button :loading="loading" @click="load"><Refresh /> 刷新</el-button>
    </div>

    <section class="ops-metrics">
      <article v-for="item in metrics" :key="item.label" class="surface metric">
        <span><component :is="item.icon" /></span>
        <p>{{ item.label }}</p>
        <h2>{{ item.value }}</h2>
        <small>{{ item.note }}</small>
      </article>
    </section>

    <section class="surface chart-card">
      <div class="card-head">
        <div><p class="eyebrow">Daily active</p><h2 class="section-title">近 7 日日活管理</h2></div>
        <span class="soft-tag">按血糖上报活跃度统计</span>
      </div>
      <div ref="chartRef" class="chart" />
    </section>

    <section class="tables-grid">
      <article class="surface table-card">
        <div class="card-head"><div><p class="eyebrow">Users</p><h2 class="section-title">用户管理</h2></div></div>
        <el-table :data="users" height="330">
          <el-table-column prop="id" label="ID" width="72" />
          <el-table-column prop="username" label="账号" />
          <el-table-column prop="nickname" label="昵称" />
          <el-table-column prop="deviceCount" label="设备数" width="88" />
          <el-table-column label="状态" width="88">
            <template #default="{ row }"><el-tag :type="row.status === 1 ? 'success' : 'info'">{{ row.status === 1 ? '正常' : '禁用' }}</el-tag></template>
          </el-table-column>
        </el-table>
      </article>

      <article class="surface table-card">
        <div class="card-head"><div><p class="eyebrow">Devices</p><h2 class="section-title">设备管理</h2></div></div>
        <el-table :data="devices" height="330">
          <el-table-column prop="deviceNo" label="设备编号" min-width="140" />
          <el-table-column prop="deviceName" label="名称" min-width="130" />
          <el-table-column prop="batteryLevel" label="电量" width="80" />
          <el-table-column label="状态" width="96">
            <template #default="{ row }"><el-tag :type="row.status === 'ONLINE' ? 'success' : row.status === 'LOW_BATTERY' ? 'warning' : 'info'">{{ statusText(row.status) }}</el-tag></template>
          </el-table-column>
        </el-table>
      </article>
    </section>
  </div>
</template>

<script setup>
import { computed, nextTick, onBeforeUnmount, onMounted, ref } from 'vue'
import * as echarts from 'echarts'
import { Connection, Refresh, TrendCharts, User, Watch } from '@element-plus/icons-vue'
import { adminDevices, adminOverview, adminUsers } from '../api'

const loading = ref(false)
const overview = ref({})
const users = ref([])
const devices = ref([])
const chartRef = ref(null)
let chart

const metrics = computed(() => [
  { label: '总用户', value: overview.value.totalUsers ?? 0, note: `${overview.value.activeUsers ?? 0} 个正常账号`, icon: User },
  { label: '今日日活', value: overview.value.todayActive ?? 0, note: '今日血糖上报次数', icon: TrendCharts },
  { label: '设备总数', value: overview.value.devices ?? 0, note: `${overview.value.onlineDevices ?? 0} 台在线`, icon: Watch },
  { label: '在线率', value: onlineRate.value, note: '设备在线健康度', icon: Connection }
])
const onlineRate = computed(() => {
  const total = Number(overview.value.devices || 0)
  return total ? `${Math.round((Number(overview.value.onlineDevices || 0) / total) * 100)}%` : '0%'
})

async function load() {
  loading.value = true
  try {
    const [o, u, d] = await Promise.all([adminOverview(), adminUsers(), adminDevices()])
    overview.value = o || {}
    users.value = u || []
    devices.value = d || []
    await nextTick()
    renderChart()
  } finally {
    loading.value = false
  }
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

function resize() { chart?.resize() }
onMounted(() => { load(); window.addEventListener('resize', resize) })
onBeforeUnmount(() => { window.removeEventListener('resize', resize); chart?.dispose() })
</script>

<style scoped>
.ops-metrics { display:grid; grid-template-columns:repeat(4,1fr); gap:18px; }
.metric,.chart-card,.table-card { padding:22px; }
.metric span { display:grid; width:40px; height:40px; place-items:center; color:var(--mint-700); background:#eef8f4; border-radius:12px; }
.metric svg { width:20px; }
.metric p { margin:18px 0 6px; color:var(--muted); }
.metric h2 { margin:0; font-size:30px; }
.metric small { color:var(--muted); }
.chart-card { margin:18px 0; }
.card-head { display:flex; align-items:center; justify-content:space-between; margin-bottom:16px; gap:12px; }
.chart { height:260px; }
.tables-grid { display:grid; grid-template-columns:1fr 1fr; gap:18px; }
@media(max-width:1100px){ .ops-metrics{ grid-template-columns:repeat(2,1fr); } .tables-grid{ grid-template-columns:1fr; } }
@media(max-width:620px){ .ops-metrics{ grid-template-columns:1fr; } .card-head{ align-items:flex-start; flex-direction:column; } }
</style>
