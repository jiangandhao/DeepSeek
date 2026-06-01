<template>
  <div class="blood-sugar-dashboard">
    <el-row :gutter="20">
      <el-col :span="6">
        <el-card class="stat-card">
          <div class="stat-content">
            <div class="stat-icon" style="background-color: #409eff">
              <el-icon><Monitor /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-value">{{ todayAvg }}</div>
              <div class="stat-label">今日平均血糖</div>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card class="stat-card">
          <div class="stat-content">
            <div class="stat-icon" style="background-color: #67c23a">
              <el-icon><CircleCheck /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-value">{{ normalRate }}%</div>
              <div class="stat-label">血糖达标率</div>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card class="stat-card">
          <div class="stat-content">
            <div class="stat-icon" style="background-color: #e6a23c">
              <el-icon><Warning /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-value">{{ warningCount }}</div>
              <div class="stat-label">异常预警次数</div>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card class="stat-card">
          <div class="stat-content">
            <div class="stat-icon" style="background-color: #f56c6c">
              <el-icon><DataLine /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-value">{{ totalRecords }}</div>
              <div class="stat-label">累计记录数</div>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <el-row :gutter="20" style="margin-top: 20px">
      <el-col :span="16">
        <el-card>
          <template #header>
            <span>血糖趋势图</span>
          </template>
          <div ref="chartRef" style="height: 400px"></div>
        </el-card>
      </el-col>
      <el-col :span="8">
        <el-card>
          <template #header>
            <span>快速记录</span>
          </template>
          <el-form :model="recordForm" label-width="80px">
            <el-form-item label="血糖值">
              <el-input-number v-model="recordForm.value" :min="2" :max="30" :precision="1" />
            </el-form-item>
            <el-form-item label="测量时间">
              <el-time-picker v-model="recordForm.time" placeholder="选择时间" />
            </el-form-item>
            <el-form-item label="餐前/餐后">
              <el-radio-group v-model="recordForm.type">
                <el-radio label="fasting">空腹</el-radio>
                <el-radio label="before">餐前</el-radio>
                <el-radio label="after">餐后</el-radio>
              </el-radio-group>
            </el-form-item>
            <el-form-item label="备注">
              <el-input v-model="recordForm.note" type="textarea" />
            </el-form-item>
            <el-form-item>
              <el-button type="primary" @click="submitRecord">保存记录</el-button>
            </el-form-item>
          </el-form>
        </el-card>
      </el-col>
    </el-row>

    <el-row :gutter="20" style="margin-top: 20px">
      <el-col :span="12">
        <el-card>
          <template #header>
            <span>饮食建议</span>
          </template>
          <div class="recommendation-list">
            <div v-for="(item, index) in dietRecommendations" :key="index" class="recommendation-item">
              <el-tag :type="item.type">{{ item.tag }}</el-tag>
              <span>{{ item.content }}</span>
            </div>
          </div>
          <el-button type="primary" style="margin-top: 15px" @click="getDietAdvice">
            获取AI饮食建议
          </el-button>
        </el-card>
      </el-col>
      <el-col :span="12">
        <el-card>
          <template #header>
            <span>运动建议</span>
          </template>
          <div class="recommendation-list">
            <div v-for="(item, index) in exerciseRecommendations" :key="index" class="recommendation-item">
              <el-tag :type="item.type">{{ item.tag }}</el-tag>
              <span>{{ item.content }}</span>
            </div>
          </div>
          <el-button type="primary" style="margin-top: 15px" @click="getExerciseAdvice">
            获取AI运动建议
          </el-button>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script>
import { ref, onMounted } from 'vue'
import { Monitor, CircleCheck, Warning, DataLine } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import * as echarts from 'echarts'
import { getBloodSugarRecords, addBloodSugarRecord, getDietRecommendation, getExerciseRecommendation } from '../../api/bloodSugar'

export default {
  name: 'BloodSugarDashboard',
  components: { Monitor, CircleCheck, Warning, DataLine },
  setup() {
    const chartRef = ref(null)
    const todayAvg = ref(6.2)
    const normalRate = ref(85)
    const warningCount = ref(3)
    const totalRecords = ref(156)

    const recordForm = ref({
      value: 5.5,
      time: new Date(),
      type: 'fasting',
      note: ''
    })

    const dietRecommendations = ref([
      { tag: '推荐', type: 'success', content: '早餐可食用全麦面包配低脂牛奶' },
      { tag: '注意', type: 'warning', content: '午餐减少精制碳水化合物摄入' },
      { tag: '避免', type: 'danger', content: '晚餐后避免高糖水果' }
    ])

    const exerciseRecommendations = ref([
      { tag: '推荐', type: 'success', content: '餐后30分钟进行15分钟散步' },
      { tag: '建议', type: 'info', content: '每周进行3-5次有氧运动' },
      { tag: '注意', type: 'warning', content: '运动前后监测血糖变化' }
    ])

    const initChart = () => {
      if (chartRef.value) {
        const chart = echarts.init(chartRef.value)
        const option = {
          title: { text: '近7天血糖趋势' },
          tooltip: { trigger: 'axis' },
          legend: { data: ['空腹血糖', '餐后血糖'] },
          xAxis: { type: 'category', data: ['周一', '周二', '周三', '周四', '周五', '周六', '周日'] },
          yAxis: { type: 'value', name: 'mmol/L' },
          series: [
            { name: '空腹血糖', type: 'line', data: [5.8, 6.1, 5.9, 6.2, 5.7, 6.0, 5.9] },
            { name: '餐后血糖', type: 'line', data: [7.2, 7.8, 7.5, 8.1, 7.3, 7.6, 7.4] }
          ]
        }
        chart.setOption(option)
      }
    }

    const submitRecord = async () => {
      try {
        await addBloodSugarRecord(recordForm.value)
        ElMessage.success('记录保存成功')
      } catch (error) {
        console.error(error)
      }
    }

    const getDietAdvice = async () => {
      try {
        const res = await getDietRecommendation({ bloodSugar: todayAvg.value })
        dietRecommendations.value = res.data
      } catch (error) {
        console.error(error)
      }
    }

    const getExerciseAdvice = async () => {
      try {
        const res = await getExerciseRecommendation({ bloodSugar: todayAvg.value })
        exerciseRecommendations.value = res.data
      } catch (error) {
        console.error(error)
      }
    }

    onMounted(() => {
      initChart()
    })

    return {
      chartRef, todayAvg, normalRate, warningCount, totalRecords,
      recordForm, dietRecommendations, exerciseRecommendations,
      submitRecord, getDietAdvice, getExerciseAdvice
    }
  }
}
</script>

<style scoped>
.blood-sugar-dashboard {
  padding: 10px;
}

.stat-card {
  margin-bottom: 20px;
}

.stat-content {
  display: flex;
  align-items: center;
}

.stat-icon {
  width: 60px;
  height: 60px;
  border-radius: 10px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
  font-size: 24px;
  margin-right: 15px;
}

.stat-info {
  flex: 1;
}

.stat-value {
  font-size: 28px;
  font-weight: bold;
  color: #303133;
}

.stat-label {
  font-size: 14px;
  color: #909399;
  margin-top: 5px;
}

.recommendation-list {
  max-height: 200px;
  overflow-y: auto;
}

.recommendation-item {
  padding: 10px 0;
  border-bottom: 1px solid #ebeef5;
  display: flex;
  align-items: center;
}

.recommendation-item:last-child {
  border-bottom: none;
}

.recommendation-item .el-tag {
  margin-right: 10px;
}
</style>
