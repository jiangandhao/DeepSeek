<template>
  <div class="prevention-plan">
    <el-row :gutter="20">
      <el-col :span="16">
        <el-card>
          <template #header>
            <div class="card-header">
              <span>个性化预防方案</span>
              <el-button type="primary" @click="generatePlan" :loading="loading">
                <el-icon><MagicStick /></el-icon>
                AI生成方案
              </el-button>
            </div>
          </template>

          <el-collapse v-model="activePlan">
            <el-collapse-item title="🏃 运动处方" name="exercise">
              <div class="plan-section">
                <h4>运动建议</h4>
                <el-table :data="exercisePlan" border size="small">
                  <el-table-column prop="type" label="运动类型" width="120" />
                  <el-table-column prop="frequency" label="频率" width="120" />
                  <el-table-column prop="duration" label="时长" width="100" />
                  <el-table-column prop="intensity" label="强度" width="100">
                    <template #default="{ row }">
                      <el-tag :type="getIntensityType(row.intensity)">{{ row.intensity }}</el-tag>
                    </template>
                  </el-table-column>
                  <el-table-column prop="note" label="注意事项" />
                </el-table>
              </div>
            </el-collapse-item>

            <el-collapse-item title="🥗 饮食方案" name="diet">
              <div class="plan-section">
                <h4>饮食原则</h4>
                <ul>
                  <li v-for="(principle, index) in dietPrinciples" :key="index">{{ principle }}</li>
                </ul>

                <h4>每日营养目标</h4>
                <el-row :gutter="20">
                  <el-col :span="6" v-for="nutrient in nutritionTargets" :key="nutrient.name">
                    <el-statistic :title="nutrient.name" :value="nutrient.value" :suffix="nutrient.unit" />
                  </el-col>
                </el-row>
              </div>
            </el-collapse-item>

            <el-collapse-item title="💊 用药指导" name="medication">
              <div class="plan-section">
                <el-table :data="medications" border size="small">
                  <el-table-column prop="name" label="药物名称" width="150" />
                  <el-table-column prop="purpose" label="用途" width="150" />
                  <el-table-column prop="dosage" label="剂量" width="120" />
                  <el-table-column prop="frequency" label="服用频率" width="120" />
                  <el-table-column prop="sideEffects" label="注意事项" />
                </el-table>
                <el-alert title="请遵医嘱用药，勿自行调整剂量" type="warning" :closable="false" style="margin-top: 15px;" />
              </div>
            </el-collapse-item>

            <el-collapse-item title="📋 定期检查" name="checkup">
              <div class="plan-section">
                <el-timeline>
                  <el-timeline-item v-for="item in checkupSchedule" :key="item.id"
                    :timestamp="item.date" placement="top" :type="item.urgent ? 'danger' : 'primary'">
                    <el-card>
                      <h4>{{ item.title }}</h4>
                      <p>{{ item.description }}</p>
                      <el-tag v-if="item.urgent" type="danger" size="small">紧急</el-tag>
                    </el-card>
                  </el-timeline-item>
                </el-timeline>
              </div>
            </el-collapse-item>

            <el-collapse-item title="🧘 心理健康" name="mental">
              <div class="plan-section">
                <h4>心理调适建议</h4>
                <div v-for="(advice, index) in mentalAdvice" :key="index" class="mental-item">
                  <el-icon :style="{ color: advice.color }"><component :is="advice.icon" /></el-icon>
                  <div>
                    <h4>{{ advice.title }}</h4>
                    <p>{{ advice.content }}</p>
                  </div>
                </div>
              </div>
            </el-collapse-item>
          </el-collapse>
        </el-card>
      </el-col>

      <el-col :span="8">
        <el-card>
          <template #header>
            <span>方案执行进度</span>
          </template>
          <div v-for="item in progress" :key="item.name" class="progress-item">
            <div class="progress-header">
              <span>{{ item.name }}</span>
              <span>{{ item.completed }}/{{ item.total }}</span>
            </div>
            <el-progress :percentage="(item.completed / item.total) * 100" :color="item.color" />
          </div>
        </el-card>

        <el-card style="margin-top: 20px;">
          <template #header>
            <span>健康目标</span>
          </template>
          <div v-for="goal in healthGoals" :key="goal.name" class="goal-item">
            <div class="goal-icon" :class="goal.status">
              <el-icon><component :is="goal.icon" /></el-icon>
            </div>
            <div class="goal-info">
              <h4>{{ goal.name }}</h4>
              <p>{{ goal.target }}</p>
              <el-progress :percentage="goal.progress" :show-text="false" />
            </div>
          </div>
        </el-card>

        <el-card style="margin-top: 20px;">
          <template #header>
            <span>专家建议</span>
          </template>
          <div class="expert-advice">
            <div class="expert-avatar">
              <el-avatar :size="48">AI</el-avatar>
            </div>
            <div class="expert-content">
              <p>{{ expertAdvice }}</p>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { MagicStick, CircleCheck, Warning, Timer } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'

const loading = ref(false)
const activePlan = ref(['exercise'])

const exercisePlan = ref([
  { type: '快走/慢跑', frequency: '每周5次', duration: '30分钟', intensity: '中等', note: '饭后1小时进行' },
  { type: '力量训练', frequency: '每周2次', duration: '20分钟', intensity: '低', note: '以轻重量为主' },
  { type: '瑜伽/拉伸', frequency: '每周3次', duration: '15分钟', intensity: '低', note: '睡前进行有助于睡眠' }
])

const dietPrinciples = ref([
  '控制总热量摄入，每日不超过1800千卡',
  '减少精制碳水，增加全谷物摄入',
  '增加蔬菜水果摄入，每日500g以上',
  '限制盐分摄入，每日不超过6g',
  '减少饱和脂肪，增加不饱和脂肪酸',
  '定时定量，避免暴饮暴食'
])

const nutritionTargets = ref([
  { name: '热量', value: 1800, unit: '千卡' },
  { name: '蛋白质', value: 70, unit: 'g' },
  { name: '碳水', value: 225, unit: 'g' },
  { name: '脂肪', value: 50, unit: 'g' }
])

const medications = ref([
  { name: '二甲双胍', purpose: '控制血糖', dosage: '500mg', frequency: '每日2次', sideEffects: '可能出现胃肠不适' },
  { name: '阿托伐他汀', purpose: '调节血脂', dosage: '20mg', frequency: '每晚1次', sideEffects: '定期检查肝功能' }
])

const checkupSchedule = ref([
  { id: 1, date: '2025-01-25', title: '心血管科复查', description: '血压监测、心电图检查', urgent: true },
  { id: 2, date: '2025-02-15', title: '内分泌科复查', description: '血糖、糖化血红蛋白检查', urgent: false },
  { id: 3, date: '2025-03-01', title: '血脂复查', description: '血脂全套检查', urgent: false },
  { id: 4, date: '2025-06-01', title: '年度体检', description: '全面体检', urgent: false }
])

const mentalAdvice = ref([
  { title: '压力管理', content: '学习深呼吸和冥想技巧，每天10分钟', color: '#409EFF', icon: 'Timer' },
  { title: '规律作息', content: '保持固定的睡眠时间，每晚7-8小时', color: '#67C23A', icon: 'CircleCheck' },
  { title: '社交活动', content: '保持适度社交，避免孤独感', color: '#E6A23C', icon: 'Warning' }
])

const progress = ref([
  { name: '运动计划', completed: 4, total: 7, color: '#67C23A' },
  { name: '饮食控制', completed: 5, total: 7, color: '#409EFF' },
  { name: '用药依从', completed: 7, total: 7, color: '#67C23A' },
  { name: '血糖监测', completed: 6, total: 7, color: '#E6A23C' }
])

const healthGoals = ref([
  { name: '血糖达标', target: '空腹<6.1mmol/L', progress: 60, status: 'in-progress', icon: 'Timer' },
  { name: '体重控制', target: 'BMI<24', progress: 40, status: 'in-progress', icon: 'Timer' },
  { name: '血压稳定', target: '<130/85mmHg', progress: 80, status: 'good', icon: 'CircleCheck' }
])

const expertAdvice = ref('根据您的健康数据，建议重点关注血糖控制和体重管理。近期血糖波动较大，建议减少精制碳水摄入，增加膳食纤维。同时，每周至少进行150分钟中等强度有氧运动，有助于改善胰岛素敏感性。')

const getIntensityType = (intensity) => {
  const map = { '低': 'success', '中等': 'warning', '高': 'danger' }
  return map[intensity] || 'info'
}

const generatePlan = async () => {
  loading.value = true
  try {
    await new Promise(resolve => setTimeout(resolve, 2000))
    ElMessage.success('预防方案生成成功')
  } catch (error) {
    ElMessage.error('生成失败')
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.prevention-plan {
  padding: 20px;
}
.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
.plan-section {
  padding: 15px;
}
.plan-section h4 {
  margin: 0 0 15px 0;
  color: #333;
}
.plan-section ul {
  padding-left: 20px;
}
.plan-section li {
  margin-bottom: 8px;
  line-height: 1.6;
}
.mental-item {
  display: flex;
  gap: 15px;
  padding: 15px;
  margin-bottom: 10px;
  background: #f5f7fa;
  border-radius: 8px;
}
.mental-item .el-icon {
  font-size: 24px;
  flex-shrink: 0;
}
.mental-item h4 {
  margin: 0 0 5px 0;
}
.mental-item p {
  margin: 0;
  color: #666;
  font-size: 13px;
}
.progress-item {
  margin-bottom: 15px;
}
.progress-header {
  display: flex;
  justify-content: space-between;
  margin-bottom: 5px;
}
.goal-item {
  display: flex;
  gap: 15px;
  padding: 15px 0;
  border-bottom: 1px solid #eee;
}
.goal-icon {
  width: 40px;
  height: 40px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
}
.goal-icon.good { background: #f0f9eb; color: #67C23A; }
.goal-icon.in-progress { background: #fdf6ec; color: #E6A23C; }
.goal-info {
  flex: 1;
}
.goal-info h4 {
  margin: 0 0 5px 0;
}
.goal-info p {
  margin: 0 0 8px 0;
  color: #666;
  font-size: 13px;
}
.expert-advice {
  display: flex;
  gap: 15px;
}
.expert-content p {
  margin: 0;
  line-height: 1.8;
  color: #333;
}
</style>
