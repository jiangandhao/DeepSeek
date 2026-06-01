<template>
  <div class="diet-prescription">
    <el-row :gutter="20">
      <el-col :span="16">
        <el-card>
          <template #header>
            <div class="card-header">
              <span>个性化饮食处方</span>
              <el-button type="primary" @click="generateDiet" :loading="loading">
                <el-icon><MagicStick /></el-icon>
                AI生成饮食方案
              </el-button>
            </div>
          </template>

          <el-form :model="dietForm" label-width="100px">
            <el-row :gutter="20">
              <el-col :span="8">
                <el-form-item label="身高(cm)">
                  <el-input-number v-model="dietForm.height" :min="100" :max="250" />
                </el-form-item>
              </el-col>
              <el-col :span="8">
                <el-form-item label="体重(kg)">
                  <el-input-number v-model="dietForm.weight" :min="30" :max="200" />
                </el-form-item>
              </el-col>
              <el-col :span="8">
                <el-form-item label="目标体重">
                  <el-input-number v-model="dietForm.targetWeight" :min="30" :max="200" />
                </el-form-item>
              </el-col>
            </el-row>
            <el-row :gutter="20">
              <el-col :span="8">
                <el-form-item label="健康目标">
                  <el-select v-model="dietForm.goal">
                    <el-option label="减重" value="lose" />
                    <el-option label="增重" value="gain" />
                    <el-option label="维持" value="maintain" />
                    <el-option label="控制血糖" value="bloodSugar" />
                    <el-option label="控制血压" value="bloodPressure" />
                  </el-select>
                </el-form-item>
              </el-col>
              <el-col :span="8">
                <el-form-item label="饮食偏好">
                  <el-select v-model="dietForm.preference">
                    <el-option label="普通饮食" value="normal" />
                    <el-option label="素食" value="vegetarian" />
                    <el-option label="低碳水" value="lowCarb" />
                    <el-option label="地中海饮食" value="mediterranean" />
                  </el-select>
                </el-form-item>
              </el-col>
              <el-col :span="8">
                <el-form-item label="过敏食物">
                  <el-select v-model="dietForm.allergies" multiple>
                    <el-option label="牛奶" value="milk" />
                    <el-option label="鸡蛋" value="egg" />
                    <el-option label="海鲜" value="seafood" />
                    <el-option label="花生" value="peanut" />
                    <el-option label="大豆" value="soy" />
                  </el-select>
                </el-form-item>
              </el-col>
            </el-row>
          </el-form>
        </el-card>

        <el-card v-if="dietPlan" style="margin-top: 20px;">
          <template #header>
            <span>今日饮食方案</span>
          </template>

          <el-collapse v-model="activeMeal">
            <el-collapse-item title="🌅 早餐 (建议 7:00-8:00)" name="breakfast">
              <div class="meal-content">
                <div v-for="item in dietPlan.breakfast" :key="item.name" class="food-item">
                  <div class="food-info">
                    <h4>{{ item.name }}</h4>
                    <p>{{ item.amount }} | {{ item.calories }}千卡</p>
                  </div>
                  <el-tag size="small">{{ item.category }}</el-tag>
                </div>
                <div class="meal-total">
                  <span>早餐总热量: {{ dietPlan.breakfastCalories }}千卡</span>
                </div>
              </div>
            </el-collapse-item>

            <el-collapse-item title="☀️ 午餐 (建议 12:00-13:00)" name="lunch">
              <div class="meal-content">
                <div v-for="item in dietPlan.lunch" :key="item.name" class="food-item">
                  <div class="food-info">
                    <h4>{{ item.name }}</h4>
                    <p>{{ item.amount }} | {{ item.calories }}千卡</p>
                  </div>
                  <el-tag size="small">{{ item.category }}</el-tag>
                </div>
                <div class="meal-total">
                  <span>午餐总热量: {{ dietPlan.lunchCalories }}千卡</span>
                </div>
              </div>
            </el-collapse-item>

            <el-collapse-item title="🌙 晚餐 (建议 18:00-19:00)" name="dinner">
              <div class="meal-content">
                <div v-for="item in dietPlan.dinner" :key="item.name" class="food-item">
                  <div class="food-info">
                    <h4>{{ item.name }}</h4>
                    <p>{{ item.amount }} | {{ item.calories }}千卡</p>
                  </div>
                  <el-tag size="small">{{ item.category }}</el-tag>
                </div>
                <div class="meal-total">
                  <span>晚餐总热量: {{ dietPlan.dinnerCalories }}千卡</span>
                </div>
              </div>
            </el-collapse-item>

            <el-collapse-item title="🍎 加餐建议" name="snack">
              <div class="meal-content">
                <div v-for="item in dietPlan.snacks" :key="item.name" class="food-item">
                  <div class="food-info">
                    <h4>{{ item.name }}</h4>
                    <p>{{ item.time }} | {{ item.calories }}千卡</p>
                  </div>
                </div>
              </div>
            </el-collapse-item>
          </el-collapse>

          <el-divider />

          <el-row :gutter="20">
            <el-col :span="6">
              <el-statistic title="每日总热量" :value="dietPlan.totalCalories" suffix="千卡" />
            </el-col>
            <el-col :span="6">
              <el-statistic title="蛋白质" :value="dietPlan.protein" suffix="g" />
            </el-col>
            <el-col :span="6">
              <el-statistic title="碳水化合物" :value="dietPlan.carbs" suffix="g" />
            </el-col>
            <el-col :span="6">
              <el-statistic title="脂肪" :value="dietPlan.fat" suffix="g" />
            </el-col>
          </el-row>
        </el-card>
      </el-col>

      <el-col :span="8">
        <el-card>
          <template #header>
            <span>营养摄入分析</span>
          </template>
          <div ref="nutritionChart" style="height: 300px;"></div>
        </el-card>

        <el-card style="margin-top: 20px;">
          <template #header>
            <span>饮食注意事项</span>
          </template>
          <ul class="tips-list">
            <li v-for="(tip, index) in dietTips" :key="index">
              <el-icon><InfoFilled /></el-icon>
              {{ tip }}
            </li>
          </ul>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { MagicStick, InfoFilled } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'

const loading = ref(false)
const activeMeal = ref(['breakfast'])
const dietPlan = ref(null)
const nutritionChart = ref(null)

const dietForm = ref({
  height: 170,
  weight: 65,
  targetWeight: 60,
  goal: 'maintain',
  preference: 'normal',
  allergies: []
})

const dietTips = ref([
  '每日饮水量建议1500-2000ml',
  '细嚼慢咽，每餐20-30分钟',
  '避免暴饮暴食，定时定量',
  '减少油炸、烧烤类食物摄入',
  '多吃蔬菜水果，保证膳食纤维摄入'
])

const generateDiet = async () => {
  loading.value = true
  try {
    await new Promise(resolve => setTimeout(resolve, 1500))

    dietPlan.value = {
      breakfast: [
        { name: '全麦面包', amount: '2片', calories: 150, category: '主食' },
        { name: '水煮蛋', amount: '1个', calories: 80, category: '蛋白质' },
        { name: '低脂牛奶', amount: '250ml', calories: 120, category: '乳制品' },
        { name: '圣女果', amount: '100g', calories: 25, category: '水果' }
      ],
      breakfastCalories: 375,
      lunch: [
        { name: '糙米饭', amount: '150g', calories: 180, category: '主食' },
        { name: '清蒸鱼', amount: '150g', calories: 180, category: '蛋白质' },
        { name: '西兰花炒虾仁', amount: '200g', calories: 120, category: '蔬菜' },
        { name: '紫菜蛋花汤', amount: '1碗', calories: 50, category: '汤品' }
      ],
      lunchCalories: 530,
      dinner: [
        { name: '小米粥', amount: '1碗', calories: 100, category: '主食' },
        { name: '鸡胸肉沙拉', amount: '200g', calories: 200, category: '蛋白质' },
        { name: '凉拌黄瓜', amount: '150g', calories: 30, category: '蔬菜' }
      ],
      dinnerCalories: 330,
      snacks: [
        { name: '苹果', time: '上午10:00', calories: 80 },
        { name: '坚果(10颗)', time: '下午15:00', calories: 100 }
      ],
      totalCalories: 1385,
      protein: 75,
      carbs: 180,
      fat: 45
    }

    ElMessage.success('饮食方案生成成功')
  } catch (error) {
    ElMessage.error('生成失败: ' + error.message)
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.diet-prescription {
  padding: 20px;
}
.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
.meal-content {
  padding: 10px;
}
.food-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 10px 0;
  border-bottom: 1px dashed #eee;
}
.food-info h4 {
  margin: 0 0 5px 0;
}
.food-info p {
  margin: 0;
  color: #999;
  font-size: 12px;
}
.meal-total {
  text-align: right;
  padding: 10px 0;
  font-weight: bold;
  color: #409EFF;
}
.tips-list {
  list-style: none;
  padding: 0;
}
.tips-list li {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 8px 0;
  border-bottom: 1px solid #f0f0f0;
}
</style>
