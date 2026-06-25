<template>
  <div class="insight-stream">
    <div v-if="savedAt && !streaming && text" class="saved-bar">
      <span class="saved-hint"><Clock /> 已保存的分析 · {{ savedAgo(savedAt) }}，再次进入无需重新生成</span>
    </div>
    <div v-if="text" class="markdown" v-html="rendered"></div>
    <p v-if="streaming" class="streaming-tip"><span class="dot" /> AI 正在生成分析…</p>
    <p v-if="text && !streaming" class="disclaimer">本分析由 AI 生成，仅供健康管理参考，不替代专业医疗意见；涉及用药与治疗请咨询执业医师。</p>
  </div>
</template>

<script setup>
import { computed } from 'vue'
import { marked } from 'marked'
import { Clock } from '@element-plus/icons-vue'
import { savedAgo } from '../utils/insightCache'

const props = defineProps({
  text: { type: String, default: '' },
  streaming: { type: Boolean, default: false },
  savedAt: { type: Number, default: null }
})
const rendered = computed(() => marked.parse(props.text || ''))
</script>

<style scoped>
.saved-bar { margin-bottom: 12px; padding-bottom: 10px; border-bottom: 1px dashed var(--line); }
.saved-hint { display: inline-flex; align-items: center; gap: 6px; color: var(--muted); font-size: 12px; }
.saved-hint svg { width: 13px; }
.markdown { color: var(--ink); font-size: 14px; line-height: 1.8; }
.markdown :deep(h1), .markdown :deep(h2), .markdown :deep(h3) { font-size: 16px; margin: 14px 0 6px; color: var(--mint-900); }
.markdown :deep(p) { margin: 8px 0; }
.markdown :deep(ul), .markdown :deep(ol) { padding-left: 20px; margin: 8px 0; }
.markdown :deep(li) { margin: 4px 0; }
.markdown :deep(strong) { color: var(--mint-700); }
.markdown :deep(table) { border-collapse: collapse; margin: 8px 0; }
.markdown :deep(td), .markdown :deep(th) { border: 1px solid var(--line); padding: 6px 10px; }
.streaming-tip { display: flex; align-items: center; gap: 8px; margin: 10px 0 0; color: var(--mint-700); font-size: 13px; }
.streaming-tip .dot { width: 8px; height: 8px; border-radius: 50%; background: var(--mint-500); animation: blink 1s infinite; }
@keyframes blink { 0%, 100% { opacity: .3; } 50% { opacity: 1; } }
.disclaimer { margin: 16px 0 0; padding-top: 12px; border-top: 1px dashed var(--line); color: var(--muted); font-size: 12px; line-height: 1.6; }
</style>
