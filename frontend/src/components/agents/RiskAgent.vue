<template>
  <div class="risk-agent">
    <div class="agent-toolbar">
      <div><h3 class="block-title">疾病风险评估</h3><p class="block-copy">基于健康档案与近期血糖计算风险评分，并给出结构化解读。</p></div>
      <el-button type="primary" :loading="gaugeLoading" @click="loadGauge"><Refresh /> 重新评估</el-button>
    </div>

    <div class="risk-grid">
      <div class="surface-inner gauge-card">
        <div v-loading="gaugeLoading" ref="gaugeRef" class="gauge"></div>
        <div v-if="risk" class="risk-meta">
          <el-tag :type="levelType(risk.level)" effect="dark" round>{{ levelCn(risk.level) }}风险</el-tag>
          <div class="factors">
            <span class="factor-title">主要风险因素</span>
            <div v-if="(risk.factors || []).length" class="factor-tags">
              <el-tag v-for="(f, i) in risk.factors" :key="i" type="warning" effect="plain">{{ f }}</el-tag>
            </div>
            <p v-else class="muted">未发现明显风险因素</p>
          </div>
          <div v-if="risk.metrics" class="metric-mini">
            <span>均值 <b>{{ risk.metrics.mean ?? '—' }}</b></span>
            <span>估算 HbA1c <b>{{ risk.metrics.est_hba1c ?? '—' }}</b></span>
            <span>样本 <b>{{ risk.metrics.n ?? 0 }}</b></span>
          </div>
        </div>
        <el-empty v-else-if="!gaugeLoading" description="暂无足够血糖数据用于评估" :image-size="60" />
      </div>

      <div class="surface-inner insight-side">
        <template v-if="insightText || insightStreaming">
          <div class="saved-bar">
            <span class="saved-hint">AI 风险解读</span>
            <el-button size="small" text :loading="insightStreaming" @click="loadInsightData">重新解读</el-button>
          </div>
          <InsightStream :text="insightText" :streaming="insightStreaming" :saved-at="insightSavedAt" />
        </template>
        <div v-else class="insight-empty">
          <span class="ai-badge"><MagicStick /></span>
          <p>AI 可结合你的风险数据给出详细解读与个性化建议。</p>
          <el-button type="primary" :loading="insightStreaming" @click="loadInsightData"><MagicStick /> 生成 AI 解读</el-button>
          <small>流式输出，边生成边显示；生成后会自动保存</small>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { onMounted, onBeforeUnmount, ref, nextTick } from 'vue'
import * as echarts from 'echarts'
import { MagicStick, Refresh } from '@element-plus/icons-vue'
import { assessRisk, aiInsightStream } from '../../api'
import InsightStream from '../InsightStream.vue'
import { loadInsight, saveInsight } from '../../utils/insightCache'

const gaugeRef = ref(null)
let gauge
const gaugeLoading = ref(false)
const insightStreaming = ref(false)
const risk = ref(null)
const insightText = ref('')
const insightSavedAt = ref(null)

const levelCn = (l) => ({ HIGH: '高', MEDIUM: '中', LOW: '低' }[l] || l || '—')
const levelType = (l) => (l === 'HIGH' ? 'danger' : l === 'MEDIUM' ? 'warning' : 'success')

// 评分(快)与 AI 解读(慢,调 LLM)分开,互不阻塞
async function loadGauge() {
  gaugeLoading.value = true
  try {
    risk.value = await assessRisk()
    await nextTick()
    renderGauge()
  } catch { risk.value = null }
  finally { gaugeLoading.value = false }
}

async function loadInsightData() {
  insightStreaming.value = true
  insightText.value = ''
  insightSavedAt.value = null
  try {
    await aiInsightStream({ aspect: 'risk' }, (chunk) => { insightText.value += chunk })
    if (insightText.value) {
      insightSavedAt.value = Date.now()
      saveInsight('risk', insightText.value)
    }
  } catch (e) {
    insightText.value += `\n\n[生成出错] ${e.message}`
  } finally {
    insightStreaming.value = false
  }
}

function renderGauge() {
  if (!gaugeRef.value) return
  gauge ||= echarts.init(gaugeRef.value)
  const score = Math.max(0, Math.min(100, Number(risk.value?.score ?? 0)))
  const color = risk.value?.level === 'HIGH' ? '#e35d65' : risk.value?.level === 'MEDIUM' ? '#e6a23c' : '#35a889'
  gauge.setOption({
    series: [{
      type: 'gauge', startAngle: 220, endAngle: -40, min: 0, max: 100, radius: '94%',
      progress: { show: true, width: 16, roundCap: true, itemStyle: { color } },
      axisLine: { roundCap: true, lineStyle: { width: 16, color: [[1, '#eef3f0']] } },
      axisTick: { show: false }, splitLine: { show: false }, axisLabel: { show: false }, pointer: { show: false }, anchor: { show: false },
      detail: { valueAnimation: true, fontSize: 40, fontWeight: 'bolder', offsetCenter: [0, '-4%'], color: '#18352f', formatter: '{value}' },
      title: { offsetCenter: [0, '26%'], color: '#6d827c', fontSize: 13 },
      data: [{ value: score, name: '风险评分 (0-100)' }]
    }]
  })
}

function resize() { gauge?.resize() }
onMounted(() => {
  const cached = loadInsight('risk')
  if (cached && typeof cached.data === 'string') { insightText.value = cached.data; insightSavedAt.value = cached.savedAt }
  loadGauge()
  window.addEventListener('resize', resize)
})
onBeforeUnmount(() => { window.removeEventListener('resize', resize); gauge?.dispose() })
</script>

<style scoped>
.agent-toolbar { display: flex; align-items: flex-end; justify-content: space-between; gap: 12px; margin-bottom: 14px; }
.block-title { margin: 0; font-size: 18px; }
.block-copy { margin: 6px 0 0; color: var(--muted); font-size: 13px; }
.risk-grid { display: grid; grid-template-columns: 320px 1fr; gap: 16px; align-items: start; }
.surface-inner { padding: 20px; background: #fff; border: 1px solid var(--line); border-radius: 16px; }
.gauge { height: 210px; }
.risk-meta { text-align: center; margin-top: 4px; }
.factors { margin-top: 16px; text-align: left; }
.factor-title { display: block; margin-bottom: 8px; color: var(--muted); font-size: 13px; }
.factor-tags { display: flex; flex-wrap: wrap; gap: 8px; }
.muted { color: var(--muted); font-size: 13px; margin: 0; }
.metric-mini { display: grid; grid-template-columns: repeat(3, 1fr); gap: 8px; margin-top: 16px; }
.metric-mini span { padding: 10px; background: #f7fbfa; border: 1px solid var(--line); border-radius: 10px; font-size: 11px; color: var(--muted); text-align: center; }
.metric-mini b { display: block; margin-top: 4px; color: var(--ink); font-size: 15px; }
.insight-side { min-height: 240px; }
.insight-empty { display: flex; flex-direction: column; align-items: center; justify-content: center; height: 100%; min-height: 220px; text-align: center; gap: 10px; }
.insight-empty .ai-badge { display: grid; width: 48px; height: 48px; place-items: center; color: var(--mint-700); background: var(--mint-100); border-radius: 14px; }
.insight-empty .ai-badge svg { width: 24px; }
.insight-empty p { margin: 0; max-width: 280px; color: var(--muted); font-size: 14px; line-height: 1.6; }
.insight-empty small { color: var(--muted); font-size: 12px; }
.saved-bar { display: flex; align-items: center; justify-content: space-between; margin-bottom: 12px; padding-bottom: 10px; border-bottom: 1px dashed var(--line); }
.saved-hint { display: flex; align-items: center; gap: 6px; color: var(--muted); font-size: 12px; }
.saved-hint svg { width: 13px; }
@media (max-width: 900px) { .risk-grid { grid-template-columns: 1fr; } }
</style>
