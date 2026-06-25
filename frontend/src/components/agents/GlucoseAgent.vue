<template>
  <div class="glucose-agent">
    <div class="agent-toolbar">
      <div><h3 class="block-title">血糖趋势 · 预测 · 异常</h3><p class="block-copy">实线为真实血糖，虚线为模型预测，红点为检测到的异常。</p></div>
      <el-button type="primary" :loading="insightStreaming" :disabled="!hasData" @click="runInsight"><MagicStick /> {{ insightText ? '重新解读' : 'AI 解读' }}</el-button>
    </div>

    <div v-loading="chartLoading">
      <div v-show="hasData" ref="chartRef" class="chart"></div>
      <el-empty v-if="!hasData && !chartLoading" description="还没有血糖数据。可在『血糖记录』手动录入，或在『设备』页导入 Apple 健康数据。" :image-size="80" />
    </div>

    <template v-if="hasData">
      <div class="stat-row">
        <div class="stat"><span>记录条数</span><b>{{ history.length }}</b></div>
        <div class="stat"><span>近期均值</span><b>{{ avg }} <i>mmol/L</i></b></div>
        <div class="stat"><span>预测模型</span><b class="sm">{{ predict?.model || '—' }}</b></div>
        <div class="stat"><span>异常点</span><b :class="{ warn: anomalies.length }">{{ anomalies.length }} 个</b></div>
      </div>

      <div v-if="anomalies.length" class="anomaly-list">
        <el-alert v-for="(a, i) in anomalies.slice(0, 4)" :key="i" :title="a.message" :type="a.level === 'HIGH' ? 'error' : 'warning'" :closable="false" />
      </div>
    </template>

    <div class="insight-block surface-inner" v-if="insightText || insightStreaming">
      <InsightStream :text="insightText" :streaming="insightStreaming" :saved-at="insightSavedAt" />
    </div>
  </div>
</template>

<script setup>
import { computed, onMounted, onBeforeUnmount, ref, nextTick } from 'vue'
import * as echarts from 'echarts'
import { MagicStick } from '@element-plus/icons-vue'
import { listGlucose, aiPredict, aiAnomaly, aiInsightStream } from '../../api'
import InsightStream from '../InsightStream.vue'
import { loadInsight, saveInsight } from '../../utils/insightCache'

const chartRef = ref(null)
let chart
const chartLoading = ref(false)
const history = ref([])
const predict = ref(null)
const anomalies = ref([])
const insightText = ref('')
const insightStreaming = ref(false)
const insightSavedAt = ref(null)

const hasData = computed(() => history.value.length > 0)
const avg = computed(() => {
  if (!history.value.length) return '—'
  return (history.value.reduce((s, r) => s + Number(r.valueMmol || 0), 0) / history.value.length).toFixed(1)
})

const ts = (s) => (s ? new Date(s.replace(' ', 'T')).getTime() : null)

async function loadAll() {
  chartLoading.value = true
  try {
    const [g, p, a] = await Promise.all([
      listGlucose().catch(() => []),
      aiPredict(6).catch(() => null),
      aiAnomaly().catch(() => ({ anomalies: [] }))
    ])
    history.value = (g || []).slice(-60)
    predict.value = p
    anomalies.value = a?.anomalies || []
    await nextTick()
    renderChart()
  } finally {
    chartLoading.value = false
  }
}

function renderChart() {
  if (!hasData.value || !chartRef.value) return
  chart ||= echarts.init(chartRef.value)
  const hist = history.value.map(r => [ts(r.measuredAt), Number(r.valueMmol)]).filter(p => p[0])
  const predPts = (predict.value?.predictions || []).map(r => [ts(r.measured_at), Number(r.value_mmol)]).filter(p => p[0])
  const bridge = hist.length ? [hist[hist.length - 1]] : []
  const anomalyPoints = anomalies.value.map(a => ({
    value: [ts(a.measured_at), Number(a.value_mmol)],
    itemStyle: { color: a.level === 'HIGH' ? '#e35d65' : '#e6a23c' }
  })).filter(p => p.value[0])
  chart.setOption({
    tooltip: { trigger: 'axis', valueFormatter: v => (v != null ? v + ' mmol/L' : '') },
    legend: { data: ['真实血糖', '预测'], right: 0, top: 0 },
    grid: { left: 8, right: 14, top: 36, bottom: 8, containLabel: true },
    xAxis: { type: 'time', axisLine: { show: false }, axisTick: { show: false } },
    yAxis: { type: 'value', name: 'mmol/L', scale: true, splitLine: { lineStyle: { color: '#edf3f0' } }, axisLine: { show: false } },
    series: [
      {
        name: '真实血糖', type: 'line', smooth: true, showSymbol: false, data: hist,
        lineStyle: { width: 3, color: '#35a889' }, areaStyle: { color: 'rgba(53,168,137,.12)' },
        markLine: { silent: true, symbol: 'none', lineStyle: { color: '#e6a23c', type: 'dashed', width: 1 },
          data: [{ yAxis: 7.8, label: { formatter: '餐后上限 7.8', fontSize: 10, color: '#b07d24' } }] },
        markPoint: anomalyPoints.length ? { symbol: 'circle', symbolSize: 12, data: anomalyPoints } : undefined
      },
      {
        name: '预测', type: 'line', smooth: true, showSymbol: true, symbolSize: 6, data: [...bridge, ...predPts],
        lineStyle: { width: 2, type: 'dashed', color: '#216b5d' }, itemStyle: { color: '#216b5d' }
      }
    ]
  })
}

async function runInsight() {
  insightStreaming.value = true
  insightText.value = ''
  insightSavedAt.value = null
  try {
    await aiInsightStream({ aspect: 'glucose' }, (chunk) => { insightText.value += chunk })
    if (insightText.value) {
      insightSavedAt.value = Date.now()
      saveInsight('glucose', insightText.value)
    }
  } catch (e) {
    insightText.value += `\n\n[生成出错] ${e.message}`
  } finally {
    insightStreaming.value = false
  }
}

function resize() { chart?.resize() }
onMounted(() => {
  const cached = loadInsight('glucose')
  if (cached && typeof cached.data === 'string') { insightText.value = cached.data; insightSavedAt.value = cached.savedAt }
  loadAll()
  window.addEventListener('resize', resize)
})
onBeforeUnmount(() => { window.removeEventListener('resize', resize); chart?.dispose() })
</script>

<style scoped>
.agent-toolbar { display: flex; align-items: flex-end; justify-content: space-between; gap: 12px; margin-bottom: 14px; }
.block-title { margin: 0; font-size: 18px; }
.block-copy { margin: 6px 0 0; color: var(--muted); font-size: 13px; }
.chart { height: 300px; }
.stat-row { display: flex; flex-wrap: wrap; gap: 12px; margin: 16px 0; }
.stat { flex: 1; min-width: 120px; padding: 12px 14px; background: #f7fbfa; border: 1px solid var(--line); border-radius: 12px; }
.stat span { display: block; color: var(--muted); font-size: 12px; }
.stat b { display: block; margin-top: 4px; font-size: 18px; }
.stat b.sm { font-size: 14px; }
.stat b i { font-size: 12px; font-weight: 500; color: var(--muted); font-style: normal; }
.stat b.warn { color: var(--danger); }
.anomaly-list { display: grid; gap: 8px; margin-bottom: 16px; }
.insight-block { margin-top: 8px; }
.surface-inner { padding: 20px; background: #fff; border: 1px solid var(--line); border-radius: 16px; }
</style>
