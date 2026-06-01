<template>
  <div class="appointment">
    <el-row :gutter="20">
      <el-col :span="16">
        <el-card>
          <template #header>
            <div class="card-header">
              <span>智能体检预约</span>
            </div>
          </template>

          <el-steps :active="currentStep" finish-status="success" align-center>
            <el-step title="选择套餐" />
            <el-step title="选择机构" />
            <el-step title="选择时间" />
            <el-step title="确认预约" />
          </el-steps>

          <!-- 步骤1: 选择套餐 -->
          <div v-if="currentStep === 0" class="step-content">
            <h3>请选择体检套餐</h3>
            <el-row :gutter="20">
              <el-col :span="8" v-for="pkg in packages" :key="pkg.id">
                <el-card shadow="hover" :class="{ 'selected': selectedPackage?.id === pkg.id }" @click="selectPackage(pkg)">
                  <div class="package-card">
                    <h4>{{ pkg.name }}</h4>
                    <div class="price">¥{{ pkg.price }}</div>
                    <ul>
                      <li v-for="item in pkg.items" :key="item">{{ item }}</li>
                    </ul>
                    <el-tag :type="pkg.suitable === '推荐' ? 'danger' : 'info'" size="small">
                      {{ pkg.suitable }}
                    </el-tag>
                  </div>
                </el-card>
              </el-col>
            </el-row>
          </div>

          <!-- 步骤2: 选择机构 -->
          <div v-if="currentStep === 1" class="step-content">
            <h3>请选择体检机构</h3>
            <el-row :gutter="20">
              <el-col :span="12" v-for="center in centers" :key="center.id">
                <el-card shadow="hover" :class="{ 'selected': selectedCenter?.id === center.id }" @click="selectCenter(center)">
                  <div class="center-card">
                    <h4>{{ center.name }}</h4>
                    <p><el-icon><Location /></el-icon> {{ center.address }}</p>
                    <p><el-icon><Phone /></el-icon> {{ center.phone }}</p>
                    <div class="center-info">
                      <el-rate v-model="center.rating" disabled show-score />
                      <span class="distance">{{ center.distance }}</span>
                    </div>
                    <el-tag :type="center.waitTime < 30 ? 'success' : 'warning'" size="small">
                      预计等待: {{ center.waitTime }}分钟
                    </el-tag>
                  </div>
                </el-card>
              </el-col>
            </el-row>
          </div>

          <!-- 步骤3: 选择时间 -->
          <div v-if="currentStep === 2" class="step-content">
            <h3>请选择预约时间</h3>
            <el-row :gutter="20">
              <el-col :span="12">
                <el-card>
                  <template #header><span>选择日期</span></template>
                  <el-calendar v-model="selectedDate" :disabled-date="disabledDate" />
                </el-card>
              </el-col>
              <el-col :span="12">
                <el-card>
                  <template #header><span>选择时段</span></template>
                  <div class="time-slots">
                    <div v-for="slot in timeSlots" :key="slot.time"
                         :class="['slot', { 'selected': selectedTime === slot.time, 'disabled': !slot.available }]"
                         @click="slot.available && (selectedTime = slot.time)">
                      <span>{{ slot.time }}</span>
                      <el-tag v-if="!slot.available" type="info" size="small">已满</el-tag>
                      <span v-else class="slot-count">剩余{{ slot.count }}个</span>
                    </div>
                  </div>
                </el-card>
              </el-col>
            </el-row>
          </div>

          <!-- 步骤4: 确认预约 -->
          <div v-if="currentStep === 3" class="step-content">
            <h3>确认预约信息</h3>
            <el-descriptions :column="1" border>
              <el-descriptions-item label="体检套餐">{{ selectedPackage?.name }}</el-descriptions-item>
              <el-descriptions-item label="体检机构">{{ selectedCenter?.name }}</el-descriptions-item>
              <el-descriptions-item label="预约日期">{{ selectedDate?.toLocaleDateString() }}</el-descriptions-item>
              <el-descriptions-item label="预约时段">{{ selectedTime }}</el-descriptions-item>
              <el-descriptions-item label="套餐价格">
                <span class="price">¥{{ selectedPackage?.price }}</span>
              </el-descriptions-item>
            </el-descriptions>

            <el-alert title="预约须知" type="info" :closable="false" style="margin-top: 20px;">
              <ul>
                <li>体检前一天请保持清淡饮食，避免饮酒</li>
                <li>体检当日请空腹前往，禁食禁水8小时以上</li>
                <li>请携带本人身份证原件</li>
              </ul>
            </el-alert>
          </div>

          <div class="step-actions">
            <el-button v-if="currentStep > 0" @click="currentStep--">上一步</el-button>
            <el-button v-if="currentStep < 3" type="primary" @click="nextStep" :disabled="!canNext">
              下一步
            </el-button>
            <el-button v-if="currentStep === 3" type="success" @click="confirmAppointment" :loading="loading">
              确认预约
            </el-button>
          </div>
        </el-card>
      </el-col>

      <el-col :span="8">
        <el-card>
          <template #header>
            <span>我的预约</span>
          </template>
          <div v-for="item in myAppointments" :key="item.id" class="appointment-item">
            <div class="appt-info">
              <h4>{{ item.package }}</h4>
              <p>{{ item.center }}</p>
              <p>{{ item.date }} {{ item.time }}</p>
            </div>
            <el-tag :type="getStatusType(item.status)">{{ item.status }}</el-tag>
          </div>
        </el-card>

        <el-card style="margin-top: 20px;">
          <template #header>
            <span>智能推荐</span>
          </template>
          <div class="ai-recommend">
            <el-icon><MagicStick /></el-icon>
            <p>根据您的健康档案，AI推荐您进行：</p>
            <ul>
              <li>胸部CT检查（家族有肺部疾病史）</li>
              <li>糖化血红蛋白检测（血糖偏高）</li>
              <li>颈动脉超声（年龄>40岁）</li>
            </ul>
          </div>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { ref, computed } from 'vue'
import { Location, Phone, MagicStick } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'

const currentStep = ref(0)
const loading = ref(false)
const selectedPackage = ref(null)
const selectedCenter = ref(null)
const selectedDate = ref(new Date())
const selectedTime = ref('')

const packages = ref([
  {
    id: 1, name: '基础体检套餐', price: 298, suitable: '',
    items: ['血常规', '尿常规', '肝功能', '肾功能', '心电图', '胸部X光']
  },
  {
    id: 2, name: '标准体检套餐', price: 598, suitable: '推荐',
    items: ['血常规', '尿常规', '肝功能', '肾功能', '血脂', '血糖', '心电图', '胸部X光', '腹部B超', '甲状腺B超']
  },
  {
    id: 3, name: '深度体检套餐', price: 1280, suitable: '',
    items: ['血常规', '尿常规', '肝功能', '肾功能', '血脂', '血糖', '肿瘤标志物', '心电图', '胸部CT', '腹部B超', '甲状腺B超', '骨密度']
  }
])

const centers = ref([
  { id: 1, name: '美年大健康(光谷店)', address: '武汉市洪山区光谷大道77号', phone: '027-87654321', rating: 4.5, distance: '2.3km', waitTime: 15 },
  { id: 2, name: '爱康国宾(武昌店)', address: '武汉市武昌区中南路99号', phone: '027-87651234', rating: 4.3, distance: '5.1km', waitTime: 25 },
  { id: 3, name: '瑞慈体检(汉口店)', address: '武汉市江汉区解放大道688号', phone: '027-85432109', rating: 4.7, distance: '8.5km', waitTime: 45 }
])

const timeSlots = ref([
  { time: '08:00-08:30', available: true, count: 5 },
  { time: '08:30-09:00', available: true, count: 3 },
  { time: '09:00-09:30', available: false, count: 0 },
  { time: '09:30-10:00', available: true, count: 8 },
  { time: '10:00-10:30', available: true, count: 12 },
  { time: '10:30-11:00', available: true, count: 10 }
])

const myAppointments = ref([
  { id: 1, package: '标准体检套餐', center: '美年大健康(光谷店)', date: '2025-01-20', time: '09:00-09:30', status: '待体检' },
  { id: 2, package: '基础体检套餐', center: '爱康国宾(武昌店)', date: '2024-06-15', time: '08:30-09:00', status: '已完成' }
])

const canNext = computed(() => {
  if (currentStep.value === 0) return selectedPackage.value !== null
  if (currentStep.value === 1) return selectedCenter.value !== null
  if (currentStep.value === 2) return selectedTime.value !== ''
  return true
})

const disabledDate = (date) => {
  return date < new Date(new Date().setHours(0, 0, 0, 0))
}

const getStatusType = (status) => {
  const map = { '待体检': 'warning', '已完成': 'success', '已取消': 'info' }
  return map[status] || ''
}

const selectPackage = (pkg) => { selectedPackage.value = pkg }
const selectCenter = (center) => { selectedCenter.value = center }

const nextStep = () => {
  if (canNext.value) currentStep.value++
}

const confirmAppointment = async () => {
  loading.value = true
  try {
    await new Promise(resolve => setTimeout(resolve, 1000))
    ElMessage.success('预约成功！请注意查收短信通知')
    currentStep.value = 0
  } catch (error) {
    ElMessage.error('预约失败')
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.appointment {
  padding: 20px;
}
.step-content {
  margin: 30px 0;
}
.step-actions {
  display: flex;
  justify-content: center;
  gap: 20px;
  margin-top: 30px;
}
.package-card, .center-card {
  text-align: center;
}
.package-card h4, .center-card h4 {
  margin: 0 0 10px 0;
}
.price {
  font-size: 24px;
  color: #F56C6C;
  font-weight: bold;
}
.package-card ul {
  list-style: none;
  padding: 0;
  text-align: left;
  font-size: 13px;
  color: #666;
}
.package-card ul li::before {
  content: '✓ ';
  color: #67C23A;
}
.selected {
  border-color: #409EFF;
  background: #ecf5ff;
}
.center-card p {
  margin: 5px 0;
  color: #666;
  font-size: 13px;
}
.center-info {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin: 10px 0;
}
.distance {
  color: #999;
}
.time-slots {
  display: flex;
  flex-direction: column;
  gap: 10px;
}
.slot {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 12px;
  border: 1px solid #dcdfe6;
  border-radius: 4px;
  cursor: pointer;
}
.slot.selected {
  border-color: #409EFF;
  background: #ecf5ff;
}
.slot.disabled {
  opacity: 0.5;
  cursor: not-allowed;
}
.slot-count {
  color: #67C23A;
  font-size: 12px;
}
.appointment-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 15px 0;
  border-bottom: 1px solid #eee;
}
.appt-info h4 {
  margin: 0 0 5px 0;
}
.appt-info p {
  margin: 2px 0;
  color: #666;
  font-size: 13px;
}
.ai-recommend {
  text-align: center;
  padding: 10px;
}
.ai-recommend .el-icon {
  font-size: 32px;
  color: #409EFF;
}
.ai-recommend ul {
  text-align: left;
  padding-left: 20px;
}
.ai-recommend li {
  margin: 5px 0;
  font-size: 13px;
}
</style>
