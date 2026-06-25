<template>
  <div class="report-agent">
    <div class="agent-toolbar">
      <div><h3 class="block-title">体检报告智能解读</h3><p class="block-copy">上传体检报告图片或 PDF，自动提取指标并标注异常。</p></div>
    </div>

    <el-upload
      drag :auto-upload="false" :show-file-list="false"
      accept="image/*,.pdf" :on-change="onSelect" class="uploader"
    >
      <el-icon class="up-icon"><UploadFilled /></el-icon>
      <div class="up-text">将报告拖到此处，或<em>点击上传</em></div>
      <template #tip><div class="tip">支持 PNG/JPG/PDF；演示用途，非临床诊断。</div></template>
    </el-upload>
    <el-button v-if="file" type="primary" :loading="loading" class="analyze-btn" @click="run">开始解读</el-button>

    <div v-if="result" class="surface-inner result">
      <div class="result-head">
        <div><h4>{{ result.title || '体检报告解读' }}</h4><p>{{ result.summary }}</p></div>
        <el-tag :type="levelType(result.risk_level)" effect="dark" round>{{ levelCn(result.risk_level) }}风险</el-tag>
      </div>
      <el-table v-if="(result.indicators || []).length" :data="result.indicators" size="small" max-height="300">
        <el-table-column prop="name" label="指标" min-width="120" />
        <el-table-column prop="value" label="数值" width="90" />
        <el-table-column prop="unit" label="单位" width="80" />
        <el-table-column prop="reference" label="参考范围" min-width="110" />
        <el-table-column label="状态" width="90">
          <template #default="{ row }"><el-tag :type="statusType(row.status)" size="small">{{ statusCn(row.status) }}</el-tag></template>
        </el-table-column>
      </el-table>
      <p class="disclaimer">{{ result.disclaimer }}</p>
    </div>

    <div v-if="historyList.length" class="surface-inner history">
      <h4>历史报告</h4>
      <div v-for="h in historyList" :key="h.id" class="history-row">
        <span>{{ (h.createdAt || '').replace('T', ' ').slice(0, 16) }}</span>
        <b>{{ h.summary || h.reportType }}</b>
        <el-tag :type="levelType(h.riskLevel)" size="small">{{ levelCn(h.riskLevel) }}</el-tag>
      </div>
    </div>
  </div>
</template>

<script setup>
import { onMounted, ref } from 'vue'
import { UploadFilled } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import { analyzeHealthReport, listHealthReports } from '../../api'
import { loadInsight, saveInsight } from '../../utils/insightCache'

const file = ref(null)
const result = ref(null)
const loading = ref(false)
const historyList = ref([])

const levelCn = (l) => ({ HIGH: '高', MEDIUM: '中', LOW: '低' }[l] || l || '—')
const levelType = (l) => (l === 'HIGH' ? 'danger' : l === 'MEDIUM' ? 'warning' : 'success')
const statusCn = (s) => ({ HIGH: '偏高', LOW: '偏低', NORMAL: '正常' }[s] || s)
const statusType = (s) => (s === 'NORMAL' ? 'success' : 'warning')

function onSelect(f) { file.value = f.raw; result.value = null }

async function run() {
  if (!file.value) { ElMessage.warning('请先选择文件'); return }
  loading.value = true
  try {
    result.value = await analyzeHealthReport(file.value)
    saveInsight('report', result.value)
    await loadHistory()
  } finally {
    loading.value = false
  }
}

async function loadHistory() {
  try { historyList.value = (await listHealthReports()) || [] } catch { historyList.value = [] }
}

onMounted(() => {
  const cached = loadInsight('report')
  if (cached && cached.data) result.value = cached.data
  loadHistory()
})
</script>

<style scoped>
.agent-toolbar { margin-bottom: 14px; }
.block-title { margin: 0; font-size: 18px; }
.block-copy { margin: 6px 0 0; color: var(--muted); font-size: 13px; }
.up-icon { font-size: 40px; color: #c0c4cc; }
.up-text { color: var(--ink); font-size: 14px; }
.up-text em { color: var(--mint-700); font-style: normal; }
.tip { color: var(--muted); font-size: 12px; margin-top: 6px; }
.analyze-btn { width: 100%; margin-top: 12px; }
.surface-inner { padding: 20px; background: #fff; border: 1px solid var(--line); border-radius: 16px; margin-top: 16px; }
.result-head { display: flex; align-items: flex-start; justify-content: space-between; gap: 12px; margin-bottom: 14px; }
.result-head h4 { margin: 0 0 6px; font-size: 16px; }
.result-head p { margin: 0; color: var(--muted); font-size: 13px; line-height: 1.6; }
.disclaimer { margin: 14px 0 0; color: var(--muted); font-size: 12px; line-height: 1.6; }
.history h4 { margin: 0 0 12px; font-size: 15px; }
.history-row { display: grid; grid-template-columns: 130px 1fr auto; align-items: center; gap: 10px; padding: 10px 0; border-bottom: 1px solid var(--line); font-size: 13px; }
.history-row:last-child { border-bottom: 0; }
.history-row span { color: var(--muted); }
</style>
