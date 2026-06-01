<template>
  <div class="disease-management">
    <el-row :gutter="20">
      <el-col :span="16">
        <el-card>
          <template #header>
            <div class="card-header">
              <span>慢性病管理</span>
              <el-button type="primary" @click="addRecord">
                <el-icon><Plus /></el-icon>
                添加记录
              </el-button>
            </div>
          </template>

          <el-tabs v-model="activeTab">
            <el-tab-pane label="糖尿病管理" name="diabetes">
              <el-descriptions :column="2" border>
                <el-descriptions-item label="确诊时间">2023-06-15</el-descriptions-item>
                <el-descriptions-item label="当前用药">二甲双胍 500mg/次</el-descriptions-item>
                <el-descriptions-item label="血糖控制目标">空腹<7.0, 餐后<10.0</el-descriptions-item>
                <el-descriptions-item label="最近复查">2024-12-20</el-descriptions-item>
              </el-descriptions>

              <h4 style="margin-top: 20px;">血糖监测记录</h4>
              <el-table :data="diabetesRecords" border>
                <el-table-column prop="date" label="日期" width="120" />
                <el-table-column prop="fasting" label="空腹血糖" width="100">
                  <template #default="{ row }">
                    <span :class="{ 'warning': row.fasting > 7.0 }">{{ row.fasting }}</span>
                  </template>
                </el-table-column>
                <el-table-column prop="postprandial" label="餐后血糖" width="100">
                  <template #default="{ row }">
                    <span :class="{ 'warning': row.postprandial > 10.0 }">{{ row.postprandial }}</span>
                  </template>
                </el-table-column>
                <el-table-column prop="medication" label="用药情况" />
                <el-table-column prop="diet" label="饮食记录" />
                <el-table-column prop="note" label="备注" />
              </el-table>
            </el-tab-pane>

            <el-tab-pane label="高血压管理" name="hypertension">
              <el-descriptions :column="2" border>
                <el-descriptions-item label="确诊时间">2022-03-10</el-descriptions-item>
                <el-descriptions-item label="当前用药">氨氯地平 5mg/次</el-descriptions-item>
                <el-descriptions-item label="血压控制目标"><140/90 mmHg</el-descriptions-item>
                <el-descriptions-item label="最近复查">2025-01-05</el-descriptions-item>
              </el-descriptions>

              <h4 style="margin-top: 20px;">血压监测记录</h4>
              <el-table :data="hypertensionRecords" border>
                <el-table-column prop="date" label="日期" width="120" />
                <el-table-column prop="systolic" label="收缩压" width="80">
                  <template #default="{ row }">
                    <span :class="{ 'warning': row.systolic > 140 }">{{ row.systolic }}</span>
                  </template>
                </el-table-column>
                <el-table-column prop="diastolic" label="舒张压" width="80">
                  <template #default="{ row }">
                    <span :class="{ 'warning': row.diastolic > 90 }">{{ row.diastolic }}</span>
                  </template>
                </el-table-column>
                <el-table-column prop="heartRate" label="心率" width="80" />
                <el-table-column prop="medication" label="用药情况" />
                <el-table-column prop="note" label="备注" />
              </el-table>
            </el-tab-pane>

            <el-tab-pane label="用药提醒" name="medication">
              <div v-for="med in medications" :key="med.name" class="medication-item">
                <div class="med-info">
                  <h4>{{ med.name }}</h4>
                  <p>{{ med.dosage }} | {{ med.frequency }}</p>
                  <p class="med-purpose">用途: {{ med.purpose }}</p>
                </div>
                <div class="med-status">
                  <el-tag :type="med.taken ? 'success' : 'warning'">
                    {{ med.taken ? '已服用' : '待服用' }}
                  </el-tag>
                  <el-button v-if="!med.taken" size="small" type="primary" @click="markTaken(med)">
                    确认服用
                  </el-button>
                </div>
              </div>
            </el-tab-pane>
          </el-tabs>
        </el-card>
      </el-col>

      <el-col :span="8">
        <el-card>
          <template #header>
            <span>复查提醒</span>
          </template>
          <div v-for="item in followUps" :key="item.type" class="followup-item">
            <div class="followup-icon" :class="item.urgent ? 'urgent' : 'normal'">
              <el-icon><Calendar /></el-icon>
            </div>
            <div class="followup-info">
              <h4>{{ item.type }}</h4>
              <p>{{ item.date }}</p>
              <p class="days-left">{{ item.daysLeft }}</p>
            </div>
          </div>
        </el-card>

        <el-card style="margin-top: 20px;">
          <template #header>
            <span>AI健康建议</span>
          </template>
          <div v-for="(advice, index) in aiAdvice" :key="index" class="advice-item">
            <el-icon :style="{ color: advice.color }"><InfoFilled /></el-icon>
            <span>{{ advice.text }}</span>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 添加记录对话框 -->
    <el-dialog v-model="dialogVisible" title="添加监测记录" width="500px">
      <el-form :model="recordForm" label-width="100px">
        <el-form-item label="记录类型">
          <el-radio-group v-model="recordForm.type">
            <el-radio label="bloodSugar">血糖</el-radio>
            <el-radio label="bloodPressure">血压</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item v-if="recordForm.type === 'bloodSugar'" label="血糖值">
          <el-input-number v-model="recordForm.value" :min="2" :max="30" :step="0.1" />
          <span style="margin-left: 10px;">mmol/L</span>
        </el-form-item>
        <template v-if="recordForm.type === 'bloodPressure'">
          <el-form-item label="收缩压">
            <el-input-number v-model="recordForm.systolic" :min="60" :max="250" />
          </el-form-item>
          <el-form-item label="舒张压">
            <el-input-number v-model="recordForm.diastolic" :min="40" :max="150" />
          </el-form-item>
        </template>
        <el-form-item label="备注">
          <el-input v-model="recordForm.note" type="textarea" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="saveRecord">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { Plus, Calendar, InfoFilled } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'

const activeTab = ref('diabetes')
const dialogVisible = ref(false)
const recordForm = ref({ type: 'bloodSugar', value: 5.6, systolic: 120, diastolic: 80, note: '' })

const diabetesRecords = ref([
  { date: '2025-01-10', fasting: 6.2, postprandial: 8.5, medication: '二甲双胍 500mg', diet: '正常饮食', note: '' },
  { date: '2025-01-09', fasting: 6.8, postprandial: 9.2, medication: '二甲双胍 500mg', diet: '晚餐偏多', note: '需控制晚餐量' },
  { date: '2025-01-08', fasting: 5.9, postprandial: 7.8, medication: '二甲双胍 500mg', diet: '正常饮食', note: '' }
])

const hypertensionRecords = ref([
  { date: '2025-01-10', systolic: 128, diastolic: 82, heartRate: 72, medication: '氨氯地平 5mg', note: '' },
  { date: '2025-01-09', systolic: 135, diastolic: 88, heartRate: 75, medication: '氨氯地平 5mg', note: '情绪紧张' },
  { date: '2025-01-08', systolic: 125, diastolic: 80, heartRate: 70, medication: '氨氯地平 5mg', note: '' }
])

const medications = ref([
  { name: '二甲双胍', dosage: '500mg', frequency: '每日2次，餐后服用', purpose: '控制血糖', taken: true },
  { name: '氨氯地平', dosage: '5mg', frequency: '每日1次，早晨服用', purpose: '控制血压', taken: true },
  { name: '阿托伐他汀', dosage: '20mg', frequency: '每日1次，睡前服用', purpose: '调节血脂', taken: false }
])

const followUps = ref([
  { type: '内分泌科复查', date: '2025-02-15', daysLeft: '还有36天', urgent: false },
  { type: '心血管科复查', date: '2025-01-25', daysLeft: '还有15天', urgent: true },
  { type: '年度体检', date: '2025-06-01', daysLeft: '还有142天', urgent: false }
])

const aiAdvice = ref([
  { text: '近期血糖控制良好，继续保持当前用药和饮食方案', color: '#67C23A' },
  { text: '建议每周至少进行150分钟中等强度有氧运动', color: '#409EFF' },
  { text: '血压偶有波动，注意情绪管理和规律作息', color: '#E6A23C' },
  { text: '下次复查请携带近3个月的监测记录', color: '#909399' }
])

const addRecord = () => {
  recordForm.value = { type: 'bloodSugar', value: 5.6, systolic: 120, diastolic: 80, note: '' }
  dialogVisible.value = true
}

const saveRecord = () => {
  const today = new Date().toISOString().split('T')[0]
  if (recordForm.value.type === 'bloodSugar') {
    diabetesRecords.value.unshift({
      date: today,
      fasting: recordForm.value.value,
      postprandial: '-',
      medication: '二甲双胍 500mg',
      diet: '',
      note: recordForm.value.note
    })
  } else {
    hypertensionRecords.value.unshift({
      date: today,
      systolic: recordForm.value.systolic,
      diastolic: recordForm.value.diastolic,
      heartRate: '-',
      medication: '氨氯地平 5mg',
      note: recordForm.value.note
    })
  }
  dialogVisible.value = false
  ElMessage.success('记录保存成功')
}

const markTaken = (med) => {
  med.taken = true
  ElMessage.success(`${med.name} 已标记为已服用`)
}
</script>

<style scoped>
.disease-management {
  padding: 20px;
}
.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
.warning {
  color: #F56C6C;
  font-weight: bold;
}
.medication-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 15px;
  margin-bottom: 10px;
  background: #f5f7fa;
  border-radius: 8px;
}
.med-info h4 {
  margin: 0 0 5px 0;
}
.med-info p {
  margin: 2px 0;
  color: #666;
  font-size: 13px;
}
.med-purpose {
  color: #999 !important;
}
.med-status {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 8px;
}
.followup-item {
  display: flex;
  gap: 15px;
  padding: 15px 0;
  border-bottom: 1px solid #eee;
}
.followup-icon {
  width: 40px;
  height: 40px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
}
.followup-icon.normal { background: #e6f7ff; color: #409EFF; }
.followup-icon.urgent { background: #fff2f0; color: #F56C6C; }
.followup-info h4 {
  margin: 0 0 5px 0;
}
.followup-info p {
  margin: 2px 0;
  color: #666;
  font-size: 13px;
}
.days-left {
  color: #409EFF !important;
  font-weight: bold;
}
.advice-item {
  display: flex;
  align-items: flex-start;
  gap: 8px;
  padding: 10px 0;
  border-bottom: 1px solid #f0f0f0;
}
</style>
