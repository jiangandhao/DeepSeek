<template>
  <div class="early-warning">
    <el-row :gutter="20">
      <el-col :span="16">
        <el-card>
          <template #header>
            <div class="card-header">
              <span>预警通知</span>
              <div>
                <el-button @click="markAllRead">全部已读</el-button>
                <el-button type="primary" @click="showSettings">
                  <el-icon><Setting /></el-icon>
                  预警设置
                </el-button>
              </div>
            </div>
          </template>

          <el-tabs v-model="activeTab">
            <el-tab-pane label="全部预警" name="all">
              <div v-for="warning in warnings" :key="warning.id" class="warning-item" :class="[warning.level, { unread: !warning.read }]">
                <div class="warning-icon">
                  <el-icon :size="24"><component :is="getWarningIcon(warning.level)" /></el-icon>
                </div>
                <div class="warning-content">
                  <div class="warning-header">
                    <el-tag :type="getLevelType(warning.level)" size="small">{{ warning.level }}</el-tag>
                    <span class="warning-time">{{ warning.time }}</span>
                  </div>
                  <h4>{{ warning.title }}</h4>
                  <p>{{ warning.content }}</p>
                  <div class="warning-actions">
                    <el-button type="primary" link @click="handleWarning(warning)">查看详情</el-button>
                    <el-button type="info" link @click="dismissWarning(warning)">忽略</el-button>
                  </div>
                </div>
              </div>
            </el-tab-pane>

            <el-tab-pane label="高风险预警" name="high">
              <div v-for="warning in highWarnings" :key="warning.id" class="warning-item high unread">
                <div class="warning-icon">
                  <el-icon :size="24"><CircleCloseFilled /></el-icon>
                </div>
                <div class="warning-content">
                  <div class="warning-header">
                    <el-tag type="danger" size="small">高风险</el-tag>
                    <span class="warning-time">{{ warning.time }}</span>
                  </div>
                  <h4>{{ warning.title }}</h4>
                  <p>{{ warning.content }}</p>
                  <el-alert :title="warning.action" type="error" :closable="false" show-icon style="margin-top: 10px;" />
                </div>
              </div>
            </el-tab-pane>

            <el-tab-pane label="健康提醒" name="reminder">
              <div v-for="reminder in reminders" :key="reminder.id" class="reminder-item">
                <div class="reminder-icon" :class="reminder.type">
                  <el-icon><Bell /></el-icon>
                </div>
                <div class="reminder-content">
                  <h4>{{ reminder.title }}</h4>
                  <p>{{ reminder.content }}</p>
                  <span class="reminder-time">{{ reminder.time }}</span>
                </div>
              </div>
            </el-tab-pane>
          </el-tabs>
        </el-card>
      </el-col>

      <el-col :span="8">
        <el-card>
          <template #header>
            <span>预警统计</span>
          </template>
          <el-descriptions :column="1" border>
            <el-descriptions-item label="今日预警">
              <el-badge :value="warningStats.today" :type="warningStats.today > 0 ? 'danger' : 'info'" />
            </el-descriptions-item>
            <el-descriptions-item label="本周预警">{{ warningStats.week }}次</el-descriptions-item>
            <el-descriptions-item label="未处理">{{ warningStats.unread }}次</el-descriptions-item>
            <el-descriptions-item label="已处理">{{ warningStats.resolved }}次</el-descriptions-item>
          </el-descriptions>
        </el-card>

        <el-card style="margin-top: 20px;">
          <template #header>
            <span>预警阈值设置</span>
          </template>
          <el-form label-width="100px">
            <el-form-item label="血糖上限">
              <el-input-number v-model="thresholds.bloodSugarHigh" :min="5" :max="15" :step="0.1" />
              <span style="margin-left: 5px;">mmol/L</span>
            </el-form-item>
            <el-form-item label="血压上限">
              <el-input-number v-model="thresholds.bloodPressureHigh" :min="100" :max="200" />
              <span style="margin-left: 5px;">mmHg</span>
            </el-form-item>
            <el-form-item label="心率上限">
              <el-input-number v-model="thresholds.heartRateHigh" :min="60" :max="150" />
              <span style="margin-left: 5px;">bpm</span>
            </el-form-item>
            <el-form-item label="BMI上限">
              <el-input-number v-model="thresholds.bmiHigh" :min="18" :max="40" :step="0.1" />
            </el-form-item>
            <el-form-item>
              <el-button type="primary" @click="saveThresholds">保存设置</el-button>
            </el-form-item>
          </el-form>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { ref, computed } from 'vue'
import { Setting, Bell, CircleCloseFilled, WarningFilled, Warning, InfoFilled } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'

const activeTab = ref('all')

const warnings = ref([
  {
    id: 1, level: '高风险', title: '血糖严重超标预警',
    content: '您今日空腹血糖测量值为8.2mmol/L，已超过警戒线。建议立即调整饮食并咨询医生。',
    action: '建议：1）立即就医；2）严格控制碳水摄入；3）增加运动量',
    time: '2025-01-10 09:30', read: false
  },
  {
    id: 2, level: '中风险', title: '血压波动预警',
    content: '近3天血压测量值波动较大（120-158/80-95），建议规律作息并监测血压。',
    time: '2025-01-10 08:15', read: false
  },
  {
    id: 3, level: '低风险', title: '体重增加提醒',
    content: '近一个月体重增加2kg，已接近超重上限，建议控制饮食。',
    time: '2025-01-09 20:00', read: true
  },
  {
    id: 4, level: '中风险', title: '血脂异常提醒',
    content: '上次体检血脂指标偏高，已超过3个月未复查，建议近期安排复查。',
    time: '2025-01-08 10:00', read: true
  }
])

const reminders = ref([
  { id: 1, type: 'medication', title: '用药提醒', content: '二甲双胍 500mg - 晚餐后服用', time: '18:00' },
  { id: 2, type: 'exercise', title: '运动提醒', content: '今日还未运动，建议进行30分钟散步', time: '19:00' },
  { id: 3, type: 'checkup', title: '复查提醒', content: '心血管科复查还有15天', time: '全天' },
  { id: 4, type: 'measurement', title: '测量提醒', content: '请测量今日睡前血压', time: '21:00' }
])

const thresholds = ref({
  bloodSugarHigh: 7.0,
  bloodPressureHigh: 140,
  heartRateHigh: 100,
  bmiHigh: 24
})

const warningStats = ref({
  today: 2,
  week: 8,
  unread: 2,
  resolved: 15
})

const highWarnings = computed(() => warnings.value.filter(w => w.level === '高风险'))

const getLevelType = (level) => {
  const map = { '高风险': 'danger', '中风险': 'warning', '低风险': 'info' }
  return map[level] || 'info'
}

const getWarningIcon = (level) => {
  const map = { '高风险': 'CircleCloseFilled', '中风险': 'WarningFilled', '低风险': 'InfoFilled' }
  return map[level] || 'InfoFilled'
}

const markAllRead = () => {
  warnings.value.forEach(w => w.read = true)
  ElMessage.success('已全部标记为已读')
}

const handleWarning = (warning) => {
  warning.read = true
  ElMessage.info(`处理预警: ${warning.title}`)
}

const dismissWarning = (warning) => {
  warning.read = true
  ElMessage.success('已忽略')
}

const showSettings = () => {
  ElMessage.info('打开预警设置')
}

const saveThresholds = () => {
  ElMessage.success('预警阈值保存成功')
}
</script>

<style scoped>
.early-warning {
  padding: 20px;
}
.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
.warning-item {
  display: flex;
  gap: 15px;
  padding: 15px;
  margin-bottom: 10px;
  border-radius: 8px;
  background: #f5f7fa;
}
.warning-item.unread {
  background: #ecf5ff;
  border-left: 3px solid #409EFF;
}
.warning-item.high {
  background: #fef0f0;
  border-left: 3px solid #F56C6C;
}
.warning-icon {
  flex-shrink: 0;
}
.warning-item.high .warning-icon { color: #F56C6C; }
.warning-item.medium .warning-icon { color: #E6A23C; }
.warning-item.low .warning-icon { color: #909399; }
.warning-content {
  flex: 1;
}
.warning-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 8px;
}
.warning-time {
  color: #999;
  font-size: 12px;
}
.warning-content h4 {
  margin: 0 0 5px 0;
}
.warning-content p {
  margin: 0;
  color: #666;
  font-size: 13px;
}
.warning-actions {
  margin-top: 10px;
}
.reminder-item {
  display: flex;
  gap: 15px;
  padding: 15px;
  margin-bottom: 10px;
  background: #f5f7fa;
  border-radius: 8px;
}
.reminder-icon {
  width: 40px;
  height: 40px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
}
.reminder-icon.medication { background: #e6f7ff; color: #409EFF; }
.reminder-icon.exercise { background: #f0f9eb; color: #67C23A; }
.reminder-icon.checkup { background: #fdf6ec; color: #E6A23C; }
.reminder-icon.measurement { background: #f4f4f5; color: #909399; }
.reminder-content h4 {
  margin: 0 0 5px 0;
}
.reminder-content p {
  margin: 0 0 5px 0;
  color: #666;
}
.reminder-time {
  color: #999;
  font-size: 12px;
}
</style>
