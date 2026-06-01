<template>
  <div class="diet-plan-container">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>饮食计划</span>
          <el-button type="primary" @click="handleAdd">添加饮食记录</el-button>
        </div>
      </template>
      <el-table :data="dietList" style="width: 100%">
        <el-table-column prop="date" label="日期" width="180"></el-table-column>
        <el-table-column prop="meal" label="餐次" width="120"></el-table-column>
        <el-table-column prop="food" label="食物"></el-table-column>
        <el-table-column prop="calories" label="热量(kcal)" width="120"></el-table-column>
        <el-table-column prop="carbs" label="碳水(g)" width="100"></el-table-column>
        <el-table-column label="操作" width="150">
          <template #default>
            <el-button size="small" type="primary">编辑</el-button>
            <el-button size="small" type="danger">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>
  </div>
</template>

<script setup>
import { ref } from 'vue'

const dietList = ref([
  { date: '2024-01-15', meal: '早餐', food: '燕麦粥、鸡蛋、牛奶', calories: 350, carbs: 45 },
  { date: '2024-01-15', meal: '午餐', food: '糙米饭、鸡胸肉、西兰花', calories: 550, carbs: 65 },
  { date: '2024-01-15', meal: '晚餐', food: '全麦面包、三文鱼、沙拉', calories: 480, carbs: 50 }
])

const handleAdd = () => {
  // TODO: 添加饮食记录
}
</script>

<style scoped>
.diet-plan-container {
  padding: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
</style>
