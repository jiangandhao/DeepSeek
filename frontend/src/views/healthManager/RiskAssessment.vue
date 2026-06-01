<template>
  <div class="risk-assessment">
    <el-row :gutter="20">
      <el-col :span="16">
        <el-card class="assessment-form">
          <template #header>
            <div class="card-header">
              <span>健康风险评估</span>
              <el-button type="primary" @click="startAssessment" :loading="loading">
                开始评估
              </el-button>
            </div>
          </template>

          <el-form :model="assessmentForm" label-width="120px" ref="formRef">
            <el-divider content-position="left">基本信息</el-divider>
            <el-row :gutter="20">
              <el-col :span="12">
                <el-form-item label="年龄" prop="age">
                  <el-input-number v-model="assessmentForm.age" :min="1" :max="120" />
                </el-form-item>
              </el-col>
              <el-col :span="12">
                <el-form-item label="性别" prop="gender">
                  <el-radio-group v-model="assessmentForm.gender">
                    <el-radio label="male">男</el-radio>
                    <el-radio label="female">女</el-radio>
                  </el-radio-group>
                </el-form-item>
              </el-col>
            </el-row>

            <el-divider content-position="left">生活习惯</el-divider>
            <el-row :gutter="20">
              <el-col :span="12">
                <el-form-item label="吸烟" prop="smoking">
                  <el-select v-model="assessmentForm.smoking">
                    <el-option label="从不" value="never" />
                    <el-option label="偶尔" value="occasional" />
                    <el-option label="经常" value="regular" />
                    <el-option label="已戒烟" value="quit" />
                  </el-select>
                </el-form-item>
              </el-col>
              <el-col :span="12">
                <el-form-item label="饮酒" prop="drinking">
                  <el-select v-model="assessmentForm.drinking">
                    <el-option label="从不" value="never" />
                    <el-option label="偶尔" value="occasional" />
                    <el-option label="经常" value="regular" />
                  </el-select>
                </el-form-item>
              </el-col>
            </el-row>
            <el-row :gutter="20">
              <el-col :span="12">
                <el-form-item label="运动频率" prop="exercise">
                  <el-select v-model="assessmentForm.exercise">
                    <el-option label="几乎不运动" value="none" />
                    <el-option label="每周1-2次" value="low" />
                    <el-option label="每周3-5次" value="medium" />
                    <el-option label="每天运动" value="high" />
                  </el-select>
                </el-form-item>
              </el-col>
              <el-col :span="12">
                <el-form-item label="睡眠质量" prop="sleep">
                  <el-select v-model="assessmentForm.sleep">
                    <el-option label="很差" value="poor" />
                    <el-option label="一般" value="fair" />
                    <el-option label="良好" value="good" />
                    <el-option label="很好" value="excellent" />
                  </el-select>
                </el-form-item>
              </el-col>
            </el-row>

            <el-divider content-position="left">家族病史</el-divider>
            <el-form-item label="家族病史" prop="familyHistory">
              <el-checkbox-group v-model="assessmentForm.familyHistory">
                <el-checkbox label="hypertension">高血压</el-checkbox>
                <el-checkbox label="diabetes">糖尿病</el-checkbox>
                <el-checkbox label="heartDisease">心脏病</el-checkbox>
                <el-checkbox label="cancer">癌症</el-checkbox>
                <el-checkbox label="stroke">中风</el-checkbox>
                <el-checkbox label="none">无</el-checkbox>
              </el-checkbox-group>
            </el-form-item>

            <el-divider content-position="left">体检指标</el-divider>
            <el-row :gutter="20">
              <el-col :span="8">
                <el-form-item label="身高(cm)">
                  <el-input-number v-model="assessmentForm.height" :min="100" :max="250" :step="0.1" />
                </el-form-item>
              </el-col>
              <el-col :span="8">
                <el-form-item label="体重(kg)">
                  <el-input-number v-model="assessmentForm.weight" :min="30" :max="200" :step="0.1" />
                </el-form-item>
              </el-col>
              <el-col :span="8">
                <el-form-item label="BMI">
                  <el-input :value="bmi" disabled />
                </el-form-item>
              </el-col>
            </el-row>
            <el-row :gutter="20">
              <el-col :span="8">
                <el-form-item label="收缩压(mmHg)">
                  <el-input-number v-model="assessmentForm.systolicBP" :min="60" :max="250" />
                </el-form-item>
              </el-col>
              <el-col :span="8">
                <el-form-item label="舒张压(mmHg)">
                  <el-input-number v-model="assessmentForm.diastolicBP" :min="40" :max="150" />
                </el-form-item>
              </el-col>
              <el-col :span="8">
                <el-form-item label="血糖(mmol/L)">
                  <el-input-number v-model="assessmentForm.bloodSugar" :min="2" :max="30" :step="0.1" />
                </el-form-item>
              </el-col>
            </el-row>
            <el-row :gutter="20">
              <el-col :span="8">
                <el-form-item label="总胆固醇">
                  <el-input-number v-model="assessmentForm.cholesterol" :min="2" :max="15" :step="0.1" />
                </el-form-item>
              </el-col>
              <el-col :span="8">
                <el-form-item label="甘油三酯">
                  <el-input-number v-model="assessmentForm.triglyceride" :min="0.5" :max="10" :step="0.1" />
                </el-form-item>
              </el-col>
              <el-col :span="8">
                <el-form-item label="尿酸(μmol/L)">
                  <el-input-number v-model="assessmentForm.uricAcid" :min="100" :max="800" />
                </el-form-item>
              </el-col>
            </el-row>
          </el-form>
        </el-card>
      </el-col>

      <el-col :span="8">
        <el-card class="result-card">
          <template #header>
            <span>评估结果</span>
          </template>

          <div v-if="result" class="result-content">
            <div class="risk-level" :class="result.level">
              <el-icon :size="48"><Warning /></el-icon>
              <h2>{{ result.levelText }}</h2>
              <p>综合风险评分: {{ result.score }}分</p>
            </div>

            <el-divider />

            <h4>疾病风险分析</h4>
            <div v-for="item in result.risks" :key="item.disease" class="risk-item">
              <div class="risk-label">
                <span>{{ item.disease }}</span>
                <el-tag :type="getRiskType(item.level)" size="small">{{ item.level }}</el-tag>
              </div>
              <el-progress :percentage="item.probability" :color="getRiskColor(item.level)" />
            </div>

            <el-divider />

            <h4>预防建议</h4>
            <ul class="suggestions">
              <li v-for="(suggestion, index) in result.suggestions" :key="index">
                {{ suggestion }}
              </li>
            </ul>
          </div>

          <el-empty v-else description="请填写信息后开始评估" />
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { ref, computed } from 'vue'
import { Warning } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'

const loading = ref(false)
const result = ref(null)

const assessmentForm = ref({
  age: 30,
  gender: 'male',
  smoking: 'never',
  drinking: 'never',
  exercise: 'medium',
  sleep: 'good',
  familyHistory: [],
  height: 170,
  weight: 65,
  systolicBP: 120,
  diastolicBP: 80,
  bloodSugar: 5.6,
  cholesterol: 4.5,
  triglyceride: 1.5,
  uricAcid: 350
})

const bmi = computed(() => {
  const h = assessmentForm.value.height / 100
  return (assessmentForm.value.weight / (h * h)).toFixed(1)
})

const getRiskType = (level) => {
  const map = { '低风险': 'success', '中风险': 'warning', '高风险': 'danger' }
  return map[level] || 'info'
}

const getRiskColor = (level) => {
  const map = { '低风险': '#67C23A', '中风险': '#E6A23C', '高风险': '#F56C6C' }
  return map[level] || '#909399'
}

const startAssessment = async () => {
  loading.value = true
  try {
    // 模拟AI评估 - 实际调用后端API
    await new Promise(resolve => setTimeout(resolve, 1500))

    const bmiVal = parseFloat(bmi.value)
    let totalScore = 0
    const risks = []

    // BMI风险
    if (bmiVal > 28) totalScore += 20
    else if (bmiVal > 24) totalScore += 10

    // 血压风险
    if (assessmentForm.value.systolicBP > 140) totalScore += 15
    if (assessmentForm.value.diastolicBP > 90) totalScore += 10

    // 血糖风险
    if (assessmentForm.value.bloodSugar > 7.0) totalScore += 20
    else if (assessmentForm.value.bloodSugar > 6.1) totalScore += 10

    // 家族史风险
    totalScore += assessmentForm.value.familyHistory.length * 5

    // 生成疾病风险
    risks.push({
      disease: '心血管疾病',
      level: totalScore > 30 ? '高风险' : totalScore > 15 ? '中风险' : '低风险',
      probability: Math.min(95, totalScore * 2 + 10)
    })
    risks.push({
      disease: '糖尿病',
      level: assessmentForm.value.bloodSugar > 6.1 ? '高风险' : '低风险',
      probability: assessmentForm.value.bloodSugar > 6.1 ? 75 : 20
    })
    risks.push({
      disease: '高血压',
      level: assessmentForm.value.systolicBP > 140 ? '高风险' : '低风险',
      probability: assessmentForm.value.systolicBP > 140 ? 80 : 15
    })

    const suggestions = []
    if (bmiVal > 24) suggestions.push('建议控制饮食，适当增加运动量，将BMI控制在正常范围')
    if (assessmentForm.value.systolicBP > 130) suggestions.push('注意低盐饮食，定期监测血压')
    if (assessmentForm.value.bloodSugar > 6.1) suggestions.push('减少糖分摄入，定期检查血糖')
    if (assessmentForm.value.smoking !== 'never') suggestions.push('建议戒烟，减少心血管疾病风险')
    suggestions.push('保持规律作息，每年定期体检')

    result.value = {
      score: totalScore,
      level: totalScore > 30 ? 'high' : totalScore > 15 ? 'medium' : 'low',
      levelText: totalScore > 30 ? '高风险' : totalScore > 15 ? '中风险' : '低风险',
      risks,
      suggestions
    }

    ElMessage.success('评估完成')
  } catch (error) {
    ElMessage.error('评估失败: ' + error.message)
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.risk-assessment {
  padding: 20px;
}
.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
.risk-level {
  text-align: center;
  padding: 20px;
  border-radius: 8px;
}
.risk-level.low { background: #f0f9eb; color: #67C23A; }
.risk-level.medium { background: #fdf6ec; color: #E6A23C; }
.risk-level.high { background: #fef0f0; color: #F56C6C; }
.risk-item {
  margin-bottom: 15px;
}
.risk-label {
  display: flex;
  justify-content: space-between;
  margin-bottom: 5px;
}
.suggestions {
  padding-left: 20px;
}
.suggestions li {
  margin-bottom: 8px;
  line-height: 1.6;
}
</style>
