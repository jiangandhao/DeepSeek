<template>
  <div class="imaging-agent">
    <div class="agent-toolbar">
      <div><h3 class="block-title">医学影像识别（肺结节）</h3><p class="block-copy">上传 CT 切片图像，演示算法标注结节候选与置信度。</p></div>
    </div>

    <div class="imaging-grid">
      <div class="surface-inner">
        <el-upload drag :auto-upload="false" :show-file-list="false" accept="image/*" :on-change="onSelect">
          <el-icon class="up-icon"><UploadFilled /></el-icon>
          <div class="up-text">将 CT 图像拖到此处，或<em>点击上传</em></div>
          <template #tip><div class="tip">支持 PNG/JPG；演示算法，非临床诊断。</div></template>
        </el-upload>
        <el-button v-if="rawUrl" type="primary" :loading="loading" class="analyze-btn" @click="run">开始检测</el-button>
        <img v-if="rawUrl" :src="rawUrl" class="img" />
      </div>

      <div class="surface-inner">
        <div class="result-head"><h4>检测结果</h4>
          <el-tag v-if="result" :type="levelType(result.level)" effect="dark" round>{{ levelCn(result.level) }}风险</el-tag>
        </div>
        <template v-if="result">
          <img v-if="result.annotated_image" :src="result.annotated_image" class="img annotated" />
          <el-alert :title="result.summary" type="info" :closable="false" class="summary" />
          <el-table v-if="(result.candidates || []).length" :data="result.candidates" size="small" max-height="200">
            <el-table-column type="index" label="#" width="46" />
            <el-table-column label="位置(x,y)" :formatter="(r) => `${r.x}, ${r.y}`" />
            <el-table-column prop="diameter_px" label="直径(px)" width="90" />
            <el-table-column label="置信度" width="130">
              <template #default="{ row }"><el-progress :percentage="Math.round(row.confidence * 100)" :stroke-width="10" /></template>
            </el-table-column>
          </el-table>
          <p class="disclaimer">{{ result.note }}</p>
        </template>
        <el-empty v-else description="上传图像并点击『开始检测』" :image-size="70" />
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { UploadFilled } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import { detectImage } from '../../api'
import { loadInsight, saveInsight } from '../../utils/insightCache'

const file = ref(null)
const rawUrl = ref('')
const result = ref(null)
const loading = ref(false)

const levelCn = (l) => ({ HIGH: '高', MEDIUM: '中', LOW: '低' }[l] || l || '—')
const levelType = (l) => (l === 'HIGH' ? 'danger' : l === 'MEDIUM' ? 'warning' : 'success')

function onSelect(f) {
  file.value = f.raw
  rawUrl.value = URL.createObjectURL(f.raw)
  result.value = null
}

async function run() {
  if (!file.value) { ElMessage.warning('请先选择图像'); return }
  loading.value = true
  try { result.value = await detectImage(file.value) }
  finally { loading.value = false }
}
</script>

<style scoped>
.agent-toolbar { margin-bottom: 14px; }
.block-title { margin: 0; font-size: 18px; }
.block-copy { margin: 6px 0 0; color: var(--muted); font-size: 13px; }
.imaging-grid { display: grid; grid-template-columns: 1fr 1.2fr; gap: 16px; }
.surface-inner { padding: 20px; background: #fff; border: 1px solid var(--line); border-radius: 16px; }
.up-icon { font-size: 40px; color: #c0c4cc; }
.up-text { color: var(--ink); font-size: 14px; }
.up-text em { color: var(--mint-700); font-style: normal; }
.tip { color: var(--muted); font-size: 12px; margin-top: 6px; }
.analyze-btn { width: 100%; margin-top: 12px; }
.img { display: block; max-width: 100%; margin-top: 12px; border: 1px solid var(--line); border-radius: 10px; }
.result-head { display: flex; align-items: center; justify-content: space-between; margin-bottom: 12px; }
.result-head h4 { margin: 0; font-size: 16px; }
.summary { margin: 12px 0; }
.disclaimer { margin-top: 10px; color: var(--muted); font-size: 12px; }
@media (max-width: 900px) { .imaging-grid { grid-template-columns: 1fr; } }
</style>
