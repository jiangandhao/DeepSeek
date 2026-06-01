<template>
  <div class="exercise-recommend">
    <el-row :gutter="20">
      <el-col :span="16">
        <el-card>
          <template #header>
            <div class="card-header">
              <span>个性化运动方案</span>
              <el-button type="primary" @click="generatePlan" :loading="loading">
                <el-icon><Promotion /></el-icon>
                生成运动方案
              </el-button>
            </div>
          </template>

          <el-form :model="exerciseForm" label-width="100px">
            <el-row :gutter="20">
              <el-col :span="8">
                <el-form-item label="年龄">
                  <el-input-number v-model="exerciseForm.age" :min="10" :max="80" />
                </el-form-item>
              </el-col>
              <el-col :span="8">
                <el-form-item label="运动基础">
                  <el-select v-model="exerciseForm.level">
                    <el-option label="新手" value="beginner" />
                    <el-option label="有一定基础" value="intermediate" />
                    <el-option label="运动达人" value="advanced" />
                  </el-select>
                </el-form-item>
              </el-col>
              <el-col :span="8">
                <el-form-item label="可用时间">
                  <el-select v-model="exerciseForm.duration">
                    <el-option label="30分钟" value="30" />
                    <el-option label="45分钟" value="45" />
                    <el-option label="60分钟" value="60" />
                    <el-option label="90分钟" value="90" />
                  </el-select>
                </el-form-item>
              </el-col>
            </el-row>
            <el-row :gutter="20">
              <el-col :span="8">
                <el-form-item label="运动目标">
                  <el-select v-model="exerciseForm.goal">
                    <el-option label="减脂" value="fatLoss" />
                    <el-option label="增肌" value="muscle" />
                    <el-option label="心肺功能" value="cardio" />
                    <el-option label="柔韧性" value="flexibility" />
                    <el-option label="综合健康" value="general" />
                  </el-select>
                </el-form-item>
              </el-col>
              <el-col :span="8">
                <el-form-item label="运动场所">
                  <el-select v-model="exerciseForm.location">
                    <el-option label="健身房" value="gym" />
                    <el-option label="家中" value="home" />
                    <el-option label="户外" value="outdoor" />
                  </el-select>
                </el-form-item>
              </el-col>
              <el-col :span="8">
                <el-form-item label="特殊状况">
                  <el-checkbox-group v-model="exerciseForm.conditions">
                    <el-checkbox label="knee">膝盖不适</el-checkbox>
                    <el-checkbox label="back">腰背疼痛</el-checkbox>
                    <el-checkbox label="heart">心脏问题</el-checkbox>
                  </el-checkbox-group>
                </el-form-item>
              </el-col>
            </el-row>
          </el-form>
        </el-card>

        <el-card v-if="exercisePlan" style="margin-top: 20px;">
          <template #header>
            <span>本周运动计划</span>
          </template>

          <el-table :data="exercisePlan.weeklyPlan" border>
            <el-table-column prop="day" label="日期" width="100" />
            <el-table-column prop="type" label="运动类型" width="120">
              <template #default="{ row }">
                <el-tag :type="getTypeColor(row.type)">{{ row.type }}</el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="exercises" label="训练内容">
              <template #default="{ row }">
                <div v-for="ex in row.exercises" :key="ex.name" class="exercise-item">
                  <span class="exercise-name">{{ ex.name }}</span>
                  <span class="exercise-detail">{{ ex.sets }}组 × {{ ex.reps }} | 休息{{ ex.rest }}秒</span>
                </div>
              </template>
            </el-table-column>
            <el-table-column prop="duration" label="时长" width="80" />
            <el-table-column prop="calories" label="消耗(kcal)" width="100" />
          </el-table>

          <el-divider />

          <el-alert :title="exercisePlan.notes" type="info" show-icon :closable="false" />
        </el-card>
      </el-col>

      <el-col :span="8">
        <el-card>
          <template #header>
            <span>运动数据统计</span>
          </template>
          <el-descriptions :column="1" border>
            <el-descriptions-item label="本周运动次数">{{ stats.weeklyCount }}次</el-descriptions-item>
            <el-descriptions-item label="本周消耗">{{ stats.weeklyCalories }}千卡</el-descriptions-item>
            <el-descriptions-item label="累计运动">{{ stats.totalHours }}小时</el-descriptions-item>
            <el-descriptions-item label="连续打卡">{{ stats.streak }}天</el-descriptions-item>
          </el-descriptions>
        </el-card>

        <el-card style="margin-top: 20px;">
          <template #header>
            <span>运动安全提示</span>
          </template>
          <div class="safety-tips">
            <div v-for="(tip, index) in safetyTips" :key="index" class="tip-item">
              <el-icon :style="{ color: tip.color }"><InfoFilled /></el-icon>
              <span>{{ tip.text }}</span>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { Promotion, InfoFilled } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'

const loading = ref(false)
const exercisePlan = ref(null)

const exerciseForm = ref({
  age: 30,
  level: 'intermediate',
  duration: '60',
  goal: 'general',
  location: 'gym',
  conditions: []
})

const stats = ref({
  weeklyCount: 3,
  weeklyCalories: 1200,
  totalHours: 48,
  streak: 7
})

const safetyTips = ref([
  { text: '运动前进行5-10分钟热身', color: '#409EFF' },
  { text: '运动中注意补充水分', color: '#67C23A' },
  { text: '感到不适立即停止运动', color: '#F56C6C' },
  { text: '运动后进行拉伸放松', color: '#E6A23C' }
])

const getTypeColor = (type) => {
  const map = { '有氧': '', '力量': 'success', '柔韧': 'warning', '休息': 'info' }
  return map[type] || ''
}

const generatePlan = async () => {
  loading.value = true
  try {
    await new Promise(resolve => setTimeout(resolve, 1500))

    exercisePlan.value = {
      weeklyPlan: [
        {
          day: '周一', type: '有氧', duration: '45分钟', calories: 350,
          exercises: [
            { name: '跑步机慢跑', sets: 1, reps: '30分钟', rest: 0 },
            { name: '椭圆机', sets: 1, reps: '15分钟', rest: 0 }
          ]
        },
        {
          day: '周二', type: '力量', duration: '60分钟', calories: 280,
          exercises: [
            { name: '哑铃卧推', sets: 4, reps: '12次', rest: 60 },
            { name: '杠铃划船', sets: 4, reps: '10次', rest: 60 },
            { name: '肩推', sets: 3, reps: '12次', rest: 60 }
          ]
        },
        {
          day: '周三', type: '休息', duration: '-', calories: 0,
          exercises: [{ name: '轻度拉伸', sets: 1, reps: '15分钟', rest: 0 }]
        },
        {
          day: '周四', type: '力量', duration: '60分钟', calories: 300,
          exercises: [
            { name: '深蹲', sets: 4, reps: '10次', rest: 90 },
            { name: '硬拉', sets: 4, reps: '8次', rest: 90 },
            { name: '腿举', sets: 3, reps: '12次', rest: 60 }
          ]
        },
        {
          day: '周五', type: '有氧', duration: '40分钟', calories: 320,
          exercises: [
            { name: '动感单车', sets: 1, reps: '25分钟', rest: 0 },
            { name: '跳绳', sets: 3, reps: '5分钟', rest: 60 }
          ]
        },
        {
          day: '周六', type: '柔韧', duration: '50分钟', calories: 150,
          exercises: [
            { name: '瑜伽基础', sets: 1, reps: '50分钟', rest: 0 }
          ]
        },
        {
          day: '周日', type: '休息', duration: '-', calories: 0,
          exercises: [{ name: '户外散步', sets: 1, reps: '30分钟', rest: 0 }]
        }
      ],
      notes: '请根据自身感觉调整运动强度，如有不适请立即停止。建议运动前后各补充200ml水。'
    }

    ElMessage.success('运动方案生成成功')
  } catch (error) {
    ElMessage.error('生成失败: ' + error.message)
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.exercise-recommend {
  padding: 20px;
}
.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
.exercise-item {
  display: flex;
  justify-content: space-between;
  padding: 5px 0;
}
.exercise-name {
  font-weight: bold;
}
.exercise-detail {
  color: #999;
}
.safety-tips {
  display: flex;
  flex-direction: column;
  gap: 12px;
}
.tip-item {
  display: flex;
  align-items: center;
  gap: 8px;
}
</style>
