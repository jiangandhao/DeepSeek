<template>
  <div class="page">
    <div class="page-heading">
      <div>
        <p class="eyebrow">Virtual device</p>
        <h1 class="page-title">智能设备</h1>
        <p class="page-subtitle">绑定虚拟设备，模拟设备接口上报血糖数据，并实时查看在线、电量与信号状态。</p>
      </div>
      <el-button type="primary" @click="bindDemo"><Plus /> 绑定虚拟设备</el-button>
    </div>

    <section class="device-grid">
      <article v-for="device in devices" :key="device.id" class="surface device-card">
        <div class="device-top">
          <span class="device-icon"><Watch /></span>
          <div>
            <h2>{{ device.deviceName }}</h2>
            <p>{{ device.deviceNo }} · {{ device.deviceType }}</p>
          </div>
          <el-tag :type="statusType(device.status)" round>{{ statusText(device.status) }}</el-tag>
        </div>
        <div class="device-metrics">
          <span><b>{{ device.batteryLevel ?? '-' }}%</b><small>电量</small></span>
          <span><b>{{ device.signalStrength ?? '-' }}%</b><small>信号</small></span>
          <span><b>{{ device.lastValueMmol ?? '-' }}</b><small>最近血糖</small></span>
        </div>
        <p class="last-time">最近上报：{{ formatTime(device.lastMeasuredAt) }}</p>
        <div class="device-actions">
          <el-button :loading="simulatingId === device.id" type="primary" @click="simulate(device)">模拟上报</el-button>
          <el-button @click="showPayload(device)">接口数据</el-button>
          <el-button type="danger" plain @click="unbind(device.id)">解绑</el-button>
        </div>
      </article>

      <article v-if="!devices.length" class="surface empty-card">
        <Watch />
        <h2>还没有绑定设备</h2>
        <p>点击右上角绑定一台虚拟连续血糖仪，随后可模拟接口数据上报。</p>
      </article>
    </section>

    <section class="surface simulator">
      <div class="card-head">
        <div><p class="eyebrow">Telemetry payload</p><h2 class="section-title">模拟上报参数</h2></div>
        <span class="soft-tag">写入血糖记录</span>
      </div>
      <el-form label-position="top" class="sim-form">
        <el-form-item label="血糖值 mmol/L"><el-input-number v-model="payload.valueMmol" :min="2.5" :max="25" :precision="1" :step="0.1" /></el-form-item>
        <el-form-item label="测量时段">
          <el-select v-model="payload.period">
            <el-option label="随机" value="RANDOM" />
            <el-option label="空腹" value="FASTING" />
            <el-option label="餐前" value="BEFORE_MEAL" />
            <el-option label="餐后" value="AFTER_MEAL" />
            <el-option label="睡前" value="BEDTIME" />
          </el-select>
        </el-form-item>
        <el-form-item label="电量"><el-slider v-model="payload.batteryLevel" :min="1" :max="100" /></el-form-item>
        <el-form-item label="信号"><el-slider v-model="payload.signalStrength" :min="1" :max="100" /></el-form-item>
      </el-form>
    </section>
  </div>
</template>

<script setup>
import { onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus, Watch } from '@element-plus/icons-vue'
import { bindDevice, listDevices, simulateDeviceData, unbindDevice } from '../api'

const devices = ref([])
const simulatingId = ref(null)
const payload = reactive({ valueMmol: 6.8, period: 'RANDOM', batteryLevel: 88, signalStrength: 91 })

async function load() {
  try { devices.value = await listDevices() || [] } catch { devices.value = [] }
}

async function bindDemo() {
  const no = `V-CGM-${Date.now().toString().slice(-6)}`
  await bindDevice({ deviceNo: no, deviceName: '虚拟连续血糖仪', deviceType: 'CGM' })
  ElMessage.success('虚拟设备已绑定')
  await load()
}

async function simulate(device) {
  simulatingId.value = device.id
  try {
    await simulateDeviceData(device.id, { ...payload, measuredAt: new Date().toISOString().slice(0, 19) })
    ElMessage.success('设备数据已上报，并写入血糖记录')
    await load()
  } finally {
    simulatingId.value = null
  }
}

async function unbind(id) {
  await unbindDevice(id)
  ElMessage.success('设备已解绑')
  await load()
}

function showPayload(device) {
  ElMessageBox.alert(device.lastPayload || '暂无接口数据', '最近一次接口负载', { confirmButtonText: '知道了' })
}

function statusText(status) {
  return { ONLINE: '在线', OFFLINE: '离线', LOW_BATTERY: '低电量', UNBOUND: '已解绑' }[status] || status || '未知'
}

function statusType(status) {
  return status === 'ONLINE' ? 'success' : status === 'LOW_BATTERY' ? 'warning' : 'info'
}

function formatTime(value) {
  return value ? new Date(value).toLocaleString('zh-CN', { hour12: false }) : '暂无'
}

onMounted(load)
</script>

<style scoped>
.device-grid { display:grid; grid-template-columns:repeat(2, minmax(0, 1fr)); gap:18px; }
.device-card,.empty-card,.simulator { padding:24px; }
.device-top { display:grid; grid-template-columns:46px 1fr auto; align-items:center; gap:12px; }
.device-icon,.empty-card svg { display:grid; width:46px; height:46px; place-items:center; color:var(--mint-700); background:#eef8f4; border-radius:14px; }
.device-icon svg { width:22px; }
.device-top h2 { margin:0 0 5px; font-size:19px; }
.device-top p,.last-time { margin:0; color:var(--muted); font-size:12px; }
.device-metrics { display:grid; grid-template-columns:repeat(3,1fr); gap:10px; margin:22px 0 12px; }
.device-metrics span { padding:14px; background:#f7fbfa; border:1px solid var(--line); border-radius:13px; }
.device-metrics b,.device-metrics small { display:block; }
.device-metrics b { font-size:22px; }
.device-metrics small { margin-top:5px; color:var(--muted); }
.device-actions { display:flex; flex-wrap:wrap; gap:8px; margin-top:18px; }
.empty-card { grid-column:1 / -1; text-align:center; }
.empty-card svg { margin:0 auto 12px; padding:11px; }
.empty-card h2 { margin:0 0 6px; }
.empty-card p { margin:0; color:var(--muted); }
.simulator { margin-top:18px; }
.card-head { display:flex; align-items:center; justify-content:space-between; margin-bottom:16px; }
.sim-form { display:grid; grid-template-columns:repeat(4,1fr); gap:0 16px; }
.sim-form :deep(.el-input-number), .sim-form :deep(.el-select) { width:100%; }
@media(max-width:1000px){ .device-grid,.sim-form{ grid-template-columns:1fr; } }
@media(max-width:620px){ .device-top{ grid-template-columns:46px 1fr; } .device-top .el-tag{ grid-column:1 / -1; justify-self:start; } .device-metrics{ grid-template-columns:1fr; } }
</style>
