<template>
  <div class="image-analysis">
    <el-row :gutter="20">
      <el-col :span="16">
        <el-card>
          <template #header>
            <div class="card-header">
              <span>AI影像分析</span>
              <el-button type="primary" @click="analyzeImage" :loading="analyzing" :disabled="!uploadedFile">
                <el-icon><MagicStick /></el-icon>
                开始分析
              </el-button>
            </div>
          </template>

          <el-upload
            class="upload-area"
            drag
            :auto-upload="false"
            :on-change="handleFileChange"
            :show-file-list="false"
            accept="image/*,.dcm"
          >
            <div v-if="!uploadedFile">
              <el-icon :size="48"><UploadFilled /></el-icon>
              <div class="el-upload__text">
                拖拽文件到此处或 <em>点击上传</em>
              </div>
              <div class="el-upload__tip">
                支持 JPG、PNG、DICOM 格式的医学影像
              </div>
            </div>
            <div v-else class="uploaded-preview">
              <el-icon :size="32"><Document /></el-icon>
              <span>{{ uploadedFile.name }}</span>
            </div>
          </el-upload>

          <el-divider />

          <el-tabs v-model="activeType">
            <el-tab-pane label="肺部CT" name="lung">
              <div class="analysis-placeholder">
                <div ref="lungChart" style="height: 400px; background: #000; border-radius: 8px;">
                  <div v-if="analyzing" class="analyzing-overlay">
                    <el-icon class="is-loading" :size="32"><Loading /></el-icon>
                    <p>AI正在分析影像...</p>
                  </div>
                  <div v-else-if="analysisResult" class="result-overlay">
                    <div v-for="(finding, index) in analysisResult.findings" :key="index"
                         class="finding-marker"
                         :style="{ left: finding.x + '%', top: finding.y + '%' }">
                      <el-tooltip :content="finding.description" placement="top">
                        <div class="marker" :class="finding.severity"></div>
                      </el-tooltip>
                    </div>
                  </div>
                </div>
              </div>
            </el-tab-pane>
            <el-tab-pane label="X光片" name="xray">
              <div class="analysis-placeholder">
                <div style="height: 400px; background: #1a1a1a; border-radius: 8px; display: flex; align-items: center; justify-content: center; color: #666;">
                  请上传X光片进行分析
                </div>
              </div>
            </el-tab-pane>
            <el-tab-pane label="超声影像" name="ultrasound">
              <div class="analysis-placeholder">
                <div style="height: 400px; background: #1a1a1a; border-radius: 8px; display: flex; align-items: center; justify-content: center; color: #666;">
                  请上传超声影像进行分析
                </div>
              </div>
            </el-tab-pane>
          </el-tabs>
        </el-card>
      </el-col>

      <el-col :span="8">
        <el-card v-if="analysisResult">
          <template #header>
            <span>分析结果</span>
          </template>

          <div class="result-summary">
            <div class="result-status" :class="analysisResult.level">
              <el-icon :size="32"><component :is="analysisResult.icon" /></el-icon>
              <h3>{{ analysisResult.status }}</h3>
            </div>
          </div>

          <el-divider />

          <h4>发现的问题</h4>
          <div v-for="(finding, index) in analysisResult.findings" :key="index" class="finding-item">
            <div class="finding-header">
              <el-tag :type="getSeverityType(finding.severity)" size="small">{{ finding.severity }}</el-tag>
              <span class="finding-name">{{ finding.name }}</span>
            </div>
            <p class="finding-desc">{{ finding.description }}</p>
            <p class="finding-suggestion">建议: {{ finding.suggestion }}</p>
          </div>

          <el-divider />

          <h4>AI诊断意见</h4>
          <p class="diagnosis">{{ analysisResult.diagnosis }}</p>

          <el-alert :title="analysisResult.disclaimer" type="warning" :closable="false" show-icon style="margin-top: 15px;" />
        </el-card>

        <el-card v-else>
          <template #header>
            <span>使用说明</span>
          </template>
          <div class="instructions">
            <div class="instruction-item">
              <el-icon><CircleCheck /></el-icon>
              <div>
                <h4>上传影像</h4>
                <p>支持CT、X光、超声等医学影像</p>
              </div>
            </div>
            <div class="instruction-item">
              <el-icon><MagicStick /></el-icon>
              <div>
                <h4>AI分析</h4>
                <p>AI自动识别病变区域和异常指标</p>
              </div>
            </div>
            <div class="instruction-item">
              <el-icon><Document /></el-icon>
              <div>
                <h4>生成报告</h4>
                <p>自动生成分析报告和诊断建议</p>
              </div>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { MagicStick, UploadFilled, Document, Loading, CircleCheck } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'

const activeType = ref('lung')
const analyzing = ref(false)
const uploadedFile = ref(null)
const analysisResult = ref(null)

const handleFileChange = (file) => {
  uploadedFile.value = file.raw
  analysisResult.value = null
}

const getSeverityType = (severity) => {
  const map = { '高': 'danger', '中': 'warning', '低': 'info' }
  return map[severity] || 'info'
}

const analyzeImage = async () => {
  analyzing.value = true
  analysisResult.value = null

  try {
    await new Promise(resolve => setTimeout(resolve, 3000))

    analysisResult.value = {
      level: 'warning',
      icon: 'Warning',
      status: '发现异常，需进一步检查',
      findings: [
        {
          name: '肺结节',
          severity: '中',
          x: 35,
          y: 40,
          description: '右肺上叶见一磨玻璃结节，大小约6mm',
          suggession: '建议3个月后复查CT，观察结节变化'
        },
        {
          name: '肺纹理增多',
          severity: '低',
          x: 60,
          y: 55,
          description: '双肺纹理增多、增粗',
          suggestion: '可能与吸烟或慢性炎症有关，建议戒烟'
        }
      ],
      diagnosis: '1. 右肺上叶磨玻璃结节（GGN），建议随访观察；2. 双肺纹理增多，考虑慢性支气管炎可能。',
      disclaimer: '⚠️ 本分析结果仅供参考，不能替代专业医生的诊断。请将结果带给主治医生进行综合判断。'
    }

    // 修正 findings 中的 suggestion 字段
    analysisResult.value.findings[0].suggestion = '建议3个月后复查CT，观察结节变化'

    ElMessage.success('分析完成')
  } catch (error) {
    ElMessage.error('分析失败')
  } finally {
    analyzing.value = false
  }
}
</script>

<style scoped>
.image-analysis {
  padding: 20px;
}
.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
.upload-area {
  width: 100%;
}
.uploaded-preview {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 20px;
}
.analyzing-overlay {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  background: rgba(0, 0, 0, 0.7);
  color: white;
}
.result-overlay {
  position: relative;
  width: 100%;
  height: 100%;
}
.finding-marker {
  position: absolute;
  transform: translate(-50%, -50%);
}
.marker {
  width: 20px;
  height: 20px;
  border-radius: 50%;
  border: 2px solid;
  cursor: pointer;
  animation: pulse 1.5s infinite;
}
.marker.高 { border-color: #F56C6C; background: rgba(245, 108, 108, 0.3); }
.marker.中 { border-color: #E6A23C; background: rgba(230, 162, 60, 0.3); }
.marker.低 { border-color: #909399; background: rgba(144, 147, 153, 0.3); }
@keyframes pulse {
  0%, 100% { transform: scale(1); }
  50% { transform: scale(1.2); }
}
.result-summary {
  text-align: center;
}
.result-status {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 10px;
  padding: 20px;
  border-radius: 8px;
}
.result-status.warning { background: #fdf6ec; color: #E6A23C; }
.result-status.success { background: #f0f9eb; color: #67C23A; }
.result-status.danger { background: #fef0f0; color: #F56C6C; }
.finding-item {
  padding: 12px;
  margin-bottom: 10px;
  background: #f5f7fa;
  border-radius: 8px;
}
.finding-header {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 8px;
}
.finding-name {
  font-weight: bold;
}
.finding-desc {
  margin: 5px 0;
  color: #666;
  font-size: 13px;
}
.finding-suggestion {
  margin: 5px 0 0 0;
  color: #409EFF;
  font-size: 13px;
}
.diagnosis {
  line-height: 1.8;
  color: #333;
}
.instructions {
  display: flex;
  flex-direction: column;
  gap: 20px;
}
.instruction-item {
  display: flex;
  gap: 12px;
}
.instruction-item .el-icon {
  font-size: 24px;
  color: #409EFF;
}
.instruction-item h4 {
  margin: 0 0 5px 0;
}
.instruction-item p {
  margin: 0;
  color: #666;
  font-size: 13px;
}
</style>
