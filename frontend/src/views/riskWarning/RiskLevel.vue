<template>
  <div class="risk-level">
    <el-row :gutter="20">
      <el-col :span="16">
        <el-card>
          <template #header>
            <div class="card-header">
              <span>疾病风险分级</span>
              <el-button type="primary" @click="refreshData">
                <el-icon><Refresh /></el-icon>
                刷新数据
              </el-button>
            </div>
          </template>

          <div class="risk-overview">
            <el-row :gutter="20">
              <el-col :span="8">
                <div class="risk-card high">
                  <div class="risk-count">{{ riskStats.high }}</div>
                  <div class="risk-label">高风险</div>
                  <el-icon><WarningFilled /></el-icon>
                </div>
              </el-col>
              <el-col :span="8">
                <div class="risk-card medium">
                  <div class="risk-count">{{ riskStats.medium }}</div>
                  <div class="risk-label">中风险</div>
                  <el-icon><Warning /></el-icon>
                </div>
              </el-col>
              <el-col :span="8">
                <div class="risk-card low">
                  <div class="risk-count">{{ riskStats.low }}</div>
                  <div class="risk-label">低风险</div>
                  <el-icon><CircleCheckFilled /></el-icon>
                </div>
              </el-col>
            </el-row>
          </div>

          <el-divider />

          <h4>疾病风险详情</h4>
          <el-table :data="diseaseRisks" border>
            <el-table-column prop="disease" label="疾病类型" width="150" />
            <el-table-column prop="level" label="风险等级" width="100">
              <template #default="{ row }">
                <el-tag :type="getLevelType(row.level)">{{ row.level }}</el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="probability" label="患病概率" width="120">
              <template #default="{ row }">
                <el-progress :percentage="row.probability" :color="getLevelColor(row.level)" />
              </template>
            </el-table-column>
            <el-table-column prop="factors" label="风险因素">
              <template #default="{ row }">
                <el-tag v-for="factor in row.factors" :key="factor" size="small" style="margin-right: 5px;">
                  {{ factor }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column label="操作" width="120">
              <template #default="{ row }">
                <el-button type="primary" link @click="viewDetail(row)">查看详情</el-button>
              </template>
            </el-table-column>
          </el-table>
        </el-card>
      </el-col>

      <el-col :span="8">
        <el-card>
          <template #header>
            <span>风险趋势</span>
          </template>
          <div ref="riskTrendChart" style="height: 300px;"></div>
        </el-card>

        <el-card style="margin-top: 20px;">
          <template #header>
            <span>影响因素分析</span>
          </template>
          <div v-for="factor in riskFactors" :key="factor.name" class="factor-item">
            <div class="factor-header">
              <span>{{ factor.name }}</span>
              <el-tag :type="factor.controllable ? 'success' : 'info'" size="small">
                {{ factor.controllable ? '可控' : '不可控' }}
              </el-tag>
            </div>
            <el-progress :percentage="factor.impact" :color="factor.color" />
            <p class="factor-desc">{{ factor.description }}</p>
          </div>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { Refresh, WarningFilled, Warning, CircleCheckFilled } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'

const riskStats = ref({
  high: 1,
  medium: 2,
  low: 5
})

const diseaseRisks = ref([
  {
    disease: '2型糖尿病',
    level: '高风险',
    probability: 75,
    factors: ['血糖偏高', '家族史', 'BMI偏高', '缺乏运动']
  },
  {
    disease: '心血管疾病',
    level: '中风险',
    probability: 45,
    factors: ['血脂异常', '轻度肥胖', '吸烟史']
  },
  {
    disease: '高血压',
    level: '中风险',
    probability: 40,
    factors: ['血压偏高', '高盐饮食', '压力大']
  },
  {
    disease: '脂肪肝',
    level: '低风险',
    probability: 25,
    factors: ['ALT轻度升高', '饮酒']
  },
  {
    disease: '骨质疏松',
    level: '低风险',
    probability: 15,
    factors: ['年龄', '缺乏钙摄入']
  }
])

const riskFactors = ref([
  { name: '血糖水平', impact: 85, controllable: true, color: '#F56C6C', description: '空腹血糖6.8，高于正常值' },
  { name: 'BMI指数', impact: 70, controllable: true, color: '#E6A23C', description: 'BMI 25.4，属于超重范围' },
  { name: '家族病史', impact: 60, controllable: false, color: '#909399', description: '父母有糖尿病史' },
  { name: '运动习惯', impact: 45, controllable: true, color: '#67C23A', description: '每周运动不足2次' }
])

const getLevelType = (level) => {
  const map = { '高风险': 'danger', '中风险': 'warning', '低风险': 'success' }
  return map[level] || 'info'
}

const getLevelColor = (level) => {
  const map = { '高风险': '#F56C6C', '中风险': '#E6A23C', '低风险': '#67C23A' }
  return map[level] || '#909399'
}

const refreshData = () => {
  ElMessage.success('数据已刷新')
}

const viewDetail = (row) => {
  ElMessage.info(`查看 ${row.disease} 详情`)
}
</script>

<style scoped>
.risk-level {
  padding: 20px;
}
.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
.risk-overview {
  margin-bottom: 20px;
}
.risk-card {
  position: relative;
  padding: 20px;
  border-radius: 8px;
  text-align: center;
  overflow: hidden;
}
.risk-card .el-icon {
  position: absolute;
  right: 10px;
  bottom: 10px;
  font-size: 32px;
  opacity: 0.2;
}
.risk-card.high { background: #fef0f0; color: #F56C6C; }
.risk-card.medium { background: #fdf6ec; color: #E6A23C; }
.risk-card.low { background: #f0f9eb; color: #67C23A; }
.risk-count {
  font-size: 36px;
  font-weight: bold;
}
.risk-label {
  font-size: 14px;
  margin-top: 5px;
}
.factor-item {
  margin-bottom: 15px;
  padding-bottom: 15px;
  border-bottom: 1px solid #eee;
}
.factor-header {
  display: flex;
  justify-content: space-between;
  margin-bottom: 8px;
}
.factor-desc {
  margin: 5px 0 0 0;
  color: #999;
  font-size: 12px;
}
</style>
