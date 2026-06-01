<template>
  <div class="report-view">
    <el-row :gutter="20">
      <el-col :span="16">
        <el-card>
          <template #header>
            <div class="card-header">
              <span>体检报告</span>
              <div>
                <el-button @click="exportReport">
                  <el-icon><Download /></el-icon> 导出PDF
                </el-button>
                <el-button type="primary" @click="analyzeReport" :loading="analyzing">
                  <el-icon><MagicStick /></el-icon> AI解读报告
                </el-button>
              </div>
            </div>
          </template>

          <el-tabs v-model="activeTab">
            <el-tab-pane label="报告概览" name="overview">
              <div class="report-header">
                <h3>{{ reportInfo.hospital }} 体检报告</h3>
                <p>体检日期: {{ reportInfo.date }} | 报告编号: {{ reportInfo.reportNo }}</p>
              </div>

              <el-divider />

              <h4>总体评估</h4>
              <div class="overall-result">
                <div class="result-icon" :class="reportInfo.overallLevel">
                  <el-icon :size="48"><component :is="reportInfo.overallIcon" /></el-icon>
                </div>
                <div class="result-text">
                  <h3>{{ reportInfo.overallText }}</h3>
                  <p>{{ reportInfo.summary }}</p>
                </div>
              </div>

              <el-divider />

              <h4>异常指标汇总</h4>
              <el-table :data="abnormalItems" border>
                <el-table-column prop="category" label="检查项目" width="150" />
                <el-table-column prop="item" label="指标名称" width="180" />
                <el-table-column prop="result" label="检查结果" width="120">
                  <template #default="{ row }">
                    <span class="abnormal-value">{{ row.result }}</span>
                  </template>
                </el-table-column>
                <el-table-column prop="reference" label="参考范围" width="120" />
                <el-table-column prop="status" label="状态" width="80">
                  <template #default="{ row }">
                    <el-tag :type="row.status === '偏高' ? 'danger' : 'warning'" size="small">
                      {{ row.status }}
                    </el-tag>
                  </template>
                </el-table-column>
                <el-table-column prop="suggestion" label="建议" />
              </el-table>
            </el-tab-pane>

            <el-tab-pane label="详细报告" name="detail">
              <el-collapse v-model="activeCategories">
                <el-collapse-item v-for="category in reportDetails" :key="category.name" :title="category.name" :name="category.name">
                  <el-table :data="category.items" border size="small">
                    <el-table-column prop="item" label="项目" width="180" />
                    <el-table-column prop="result" label="结果" width="120">
                      <template #default="{ row }">
                        <span :class="{ 'abnormal': row.isAbnormal }">{{ row.result }}</span>
                      </template>
                    </el-table-column>
                    <el-table-column prop="unit" label="单位" width="80" />
                    <el-table-column prop="reference" label="参考范围" width="120" />
                    <el-table-column prop="status" label="状态" width="80">
                      <template #default="{ row }">
                        <el-tag :type="row.isAbnormal ? 'danger' : 'success'" size="small">
                          {{ row.isAbnormal ? '异常' : '正常' }}
                        </el-tag>
                      </template>
                    </el-table-column>
                  </el-table>
                </el-collapse-item>
              </el-collapse>
            </el-tab-pane>

            <el-tab-pane label="趋势分析" name="trend">
              <el-row :gutter="20">
                <el-col :span="12">
                  <el-card>
                    <template #header><span>血糖趋势</span></template>
                    <div ref="bloodSugarChart" style="height: 250px;"></div>
                  </el-card>
                </el-col>
                <el-col :span="12">
                  <el-card>
                    <template #header><span>血压趋势</span></template>
                    <div ref="bloodPressureChart" style="height: 250px;"></div>
                  </el-card>
                </el-col>
              </el-row>
              <el-row :gutter="20" style="margin-top: 20px;">
                <el-col :span="12">
                  <el-card>
                    <template #header><span>血脂趋势</span></template>
                    <div ref="lipidChart" style="height: 250px;"></div>
                  </el-card>
                </el-col>
                <el-col :span="12">
                  <el-card>
                    <template #header><span>肝功能趋势</span></template>
                    <div ref="liverChart" style="height: 250px;"></div>
                  </el-card>
                </el-col>
              </el-row>
            </el-tab-pane>
          </el-tabs>
        </el-card>
      </el-col>

      <el-col :span="8">
        <el-card v-if="aiAnalysis">
          <template #header>
            <span><el-icon><MagicStick /></el-icon> AI报告解读</span>
          </template>
          <div class="ai-analysis">
            <div v-for="(section, index) in aiAnalysis" :key="index" class="analysis-section">
              <h4>{{ section.title }}</h4>
              <p>{{ section.content }}</p>
            </div>
          </div>
        </el-card>

        <el-card style="margin-top: 20px;">
          <template #header>
            <span>历史报告</span>
          </template>
          <div v-for="item in historyReports" :key="item.id" class="history-item" @click="loadReport(item)">
            <div class="history-info">
              <h4>{{ item.hospital }}</h4>
              <p>{{ item.date }}</p>
            </div>
            <el-icon><ArrowRight /></el-icon>
          </div>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { Download, MagicStick, ArrowRight } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'

const activeTab = ref('overview')
const activeCategories = ref(['一般检查', '血常规'])
const analyzing = ref(false)
const aiAnalysis = ref(null)

const reportInfo = ref({
  hospital: '美年大健康',
  date: '2025-01-10',
  reportNo: 'MN20250110001',
  overallLevel: 'warning',
  overallIcon: 'Warning',
  overallText: '基本正常，有3项异常指标需关注',
  summary: '本次体检发现血糖偏高、血脂异常、轻度脂肪肝，建议进一步检查并调整生活方式。'
})

const abnormalItems = ref([
  { category: '血常规', item: '空腹血糖', result: '6.8 mmol/L', reference: '3.9-6.1', status: '偏高' },
  { category: '血脂', item: '总胆固醇', result: '5.9 mmol/L', reference: '<5.2', status: '偏高' },
  { category: '血脂', item: '甘油三酯', result: '2.1 mmol/L', reference: '<1.7', status: '偏高' },
  { category: '肝功能', item: 'ALT', result: '48 U/L', reference: '0-40', status: '偏高' }
])

const reportDetails = ref([
  {
    name: '一般检查',
    items: [
      { item: '身高', result: '172', unit: 'cm', reference: '-', isAbnormal: false },
      { item: '体重', result: '75', unit: 'kg', reference: '-', isAbnormal: false },
      { item: 'BMI', result: '25.4', unit: 'kg/m²', reference: '18.5-24', isAbnormal: true },
      { item: '收缩压', result: '128', unit: 'mmHg', reference: '<140', isAbnormal: false },
      { item: '舒张压', result: '82', unit: 'mmHg', reference: '<90', isAbnormal: false }
    ]
  },
  {
    name: '血常规',
    items: [
      { item: '白细胞', result: '6.5', unit: '10^9/L', reference: '4-10', isAbnormal: false },
      { item: '红细胞', result: '4.8', unit: '10^12/L', reference: '4-5.5', isAbnormal: false },
      { item: '血红蛋白', result: '145', unit: 'g/L', reference: '120-160', isAbnormal: false },
      { item: '血小板', result: '220', unit: '10^9/L', reference: '100-300', isAbnormal: false }
    ]
  },
  {
    name: '生化检查',
    items: [
      { item: '空腹血糖', result: '6.8', unit: 'mmol/L', reference: '3.9-6.1', isAbnormal: true },
      { item: '总胆固醇', result: '5.9', unit: 'mmol/L', reference: '<5.2', isAbnormal: true },
      { item: '甘油三酯', result: '2.1', unit: 'mmol/L', reference: '<1.7', isAbnormal: true },
      { item: 'ALT', result: '48', unit: 'U/L', reference: '0-40', isAbnormal: true }
    ]
  }
])

const historyReports = ref([
  { id: 1, hospital: '美年大健康', date: '2024-06-15' },
  { id: 2, hospital: '爱康国宾', date: '2023-12-20' },
  { id: 3, hospital: '美年大健康', date: '2023-06-10' }
])

const analyzeReport = async () => {
  analyzing.value = true
  try {
    await new Promise(resolve => setTimeout(resolve, 2000))
    aiAnalysis.value = [
      {
        title: '📊 总体评价',
        content: '您的体检结果总体尚可，但有几项指标需要引起重视。建议定期复查并调整生活习惯。'
      },
      {
        title: '⚠️ 血糖偏高',
        content: '空腹血糖6.8mmol/L超出正常范围，可能处于糖尿病前期状态。建议：1）减少精制碳水摄入；2）增加运动量；3）3个月后复查糖化血红蛋白。'
      },
      {
        title: '⚠️ 血脂异常',
        content: '总胆固醇和甘油三酯均偏高，存在心血管疾病风险。建议：1）减少高脂肪食物摄入；2）增加有氧运动；3）必要时咨询医生是否需要用药。'
      },
      {
        title: '💡 生活建议',
        content: '1）控制体重，目标BMI<24；2）每周至少150分钟中等强度运动；3）戒烟限酒；4）保持规律作息；5）3-6个月后复查。'
      }
    ]
    ElMessage.success('AI解读完成')
  } catch (error) {
    ElMessage.error('解读失败')
  } finally {
    analyzing.value = false
  }
}

const exportReport = () => {
  ElMessage.success('报告导出成功')
}

const loadReport = (item) => {
  ElMessage.info(`加载报告: ${item.date}`)
}
</script>

<style scoped>
.report-view {
  padding: 20px;
}
.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
.report-header {
  text-align: center;
}
.report-header h3 {
  margin: 0 0 5px 0;
}
.report-header p {
  color: #666;
}
.overall-result {
  display: flex;
  align-items: center;
  gap: 20px;
  padding: 20px;
  background: #f5f7fa;
  border-radius: 8px;
}
.result-icon {
  width: 64px;
  height: 64px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
}
.result-icon.warning { background: #fdf6ec; color: #E6A23C; }
.result-icon.success { background: #f0f9eb; color: #67C23A; }
.result-icon.danger { background: #fef0f0; color: #F56C6C; }
.result-text h3 {
  margin: 0 0 8px 0;
}
.result-text p {
  margin: 0;
  color: #666;
}
.abnormal-value {
  color: #F56C6C;
  font-weight: bold;
}
.abnormal {
  color: #F56C6C;
  font-weight: bold;
}
.ai-analysis {
  max-height: 600px;
  overflow-y: auto;
}
.analysis-section {
  margin-bottom: 20px;
  padding-bottom: 15px;
  border-bottom: 1px solid #eee;
}
.analysis-section h4 {
  margin: 0 0 8px 0;
  color: #333;
}
.analysis-section p {
  margin: 0;
  color: #666;
  line-height: 1.6;
}
.history-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 15px 0;
  border-bottom: 1px solid #eee;
  cursor: pointer;
}
.history-item:hover {
  background: #f5f7fa;
}
.history-info h4 {
  margin: 0 0 5px 0;
}
.history-info p {
  margin: 0;
  color: #666;
  font-size: 13px;
}
</style>
