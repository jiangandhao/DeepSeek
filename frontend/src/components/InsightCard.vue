<template>
  <div class="insight">
    <el-skeleton v-if="loading" :rows="5" animated />

    <template v-else-if="data">
      <div class="insight-head">
        <h3>{{ data.title || 'AI 健康分析' }}</h3>
        <p v-if="data.summary" class="insight-summary">{{ data.summary }}</p>
      </div>

      <div v-if="metrics.length" class="metric-row">
        <div v-for="(m, i) in metrics" :key="i" class="metric-chip" :class="statusClass(m.status)">
          <span class="metric-label">{{ m.label }}</span>
          <span class="metric-value">
            {{ m.value }}<i v-if="m.unit" class="metric-unit"> {{ m.unit }}</i>
            <em v-if="trendIcon(m.trend)" class="metric-trend">{{ trendIcon(m.trend) }}</em>
          </span>
        </div>
      </div>

      <div v-for="(s, i) in sections" :key="'s' + i" class="section">
        <h4 v-if="s.heading">{{ s.heading }}</h4>
        <div class="markdown" v-html="renderMd(s.body)"></div>
      </div>

      <div v-if="suggestions.length" class="suggestions">
        <h4>行动建议</h4>
        <ul>
          <li v-for="(sg, i) in suggestions" :key="'g' + i"><Check class="tick" />{{ sg }}</li>
        </ul>
      </div>

      <p v-if="data.disclaimer" class="disclaimer">{{ data.disclaimer }}</p>
    </template>

    <el-empty v-else :description="emptyText" :image-size="70" />
  </div>
</template>

<script setup>
import { computed } from 'vue'
import { marked } from 'marked'
import { Check } from '@element-plus/icons-vue'

const props = defineProps({
  data: { type: Object, default: null },
  loading: { type: Boolean, default: false },
  emptyText: { type: String, default: '点击生成 AI 分析' }
})

const metrics = computed(() => Array.isArray(props.data?.metrics) ? props.data.metrics : [])
const sections = computed(() => Array.isArray(props.data?.sections) ? props.data.sections : [])
const suggestions = computed(() => Array.isArray(props.data?.suggestions) ? props.data.suggestions : [])

const renderMd = (text) => marked.parse(text || '')
function statusClass(status) {
  return { good: 'good', warn: 'warn', danger: 'danger' }[status] || 'neutral'
}
function trendIcon(trend) {
  return { up: '↑', down: '↓', flat: '→' }[trend] || ''
}
</script>

<style scoped>
.insight-head h3 { margin: 0; font-size: 19px; letter-spacing: -.02em; }
.insight-summary { margin: 8px 0 0; color: var(--mint-700); font-size: 14px; line-height: 1.6; font-weight: 600; }
.metric-row { display: grid; grid-template-columns: repeat(auto-fit, minmax(140px, 1fr)); gap: 12px; margin: 18px 0; }
.metric-chip { padding: 14px 16px; border-radius: 14px; border: 1px solid var(--line); background: #f7fbfa; }
.metric-chip.good { background: #ecf8f2; border-color: #cdeedd; }
.metric-chip.warn { background: #fff7e9; border-color: #f3e2b3; }
.metric-chip.danger { background: #fdecec; border-color: #f5cdcd; }
.metric-label { display: block; color: var(--muted); font-size: 12px; }
.metric-value { display: block; margin-top: 6px; font-size: 22px; font-weight: 800; color: var(--ink); }
.metric-unit { font-size: 12px; font-weight: 500; color: var(--muted); font-style: normal; }
.metric-trend { margin-left: 6px; font-size: 15px; font-style: normal; color: var(--mint-500); }
.metric-chip.danger .metric-trend { color: var(--danger); }
.metric-chip.warn .metric-trend { color: var(--warning); }
.section { margin: 16px 0; }
.section h4, .suggestions h4 { margin: 0 0 6px; font-size: 15px; color: var(--mint-900); }
.markdown { color: var(--ink); font-size: 14px; line-height: 1.75; }
.markdown :deep(p) { margin: 6px 0; }
.markdown :deep(ul) { padding-left: 20px; margin: 6px 0; }
.markdown :deep(strong) { color: var(--mint-700); }
.suggestions { margin-top: 18px; padding: 16px; background: var(--mint-100); border-radius: 14px; }
.suggestions ul { margin: 0; padding: 0; list-style: none; display: grid; gap: 8px; }
.suggestions li { display: flex; align-items: flex-start; gap: 8px; font-size: 14px; color: var(--mint-900); line-height: 1.6; }
.suggestions .tick { width: 16px; height: 16px; margin-top: 2px; padding: 2px; color: #fff; background: var(--mint-500); border-radius: 50%; flex: none; }
.disclaimer { margin: 16px 0 0; padding-top: 12px; border-top: 1px dashed var(--line); color: var(--muted); font-size: 12px; line-height: 1.6; }
</style>
