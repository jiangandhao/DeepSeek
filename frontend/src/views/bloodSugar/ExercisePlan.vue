<template>
  <div class="exercise-plan-container">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>运动计划</span>
          <el-button type="primary" @click="handleAdd">添加运动记录</el-button>
        </div>
      </template>
      <el-table :data="exerciseList" style="width: 100%">
        <el-table-column prop="date" label="日期" width="180"></el-table-column>
        <el-table-column prop="type" label="运动类型" width="120"></el-table-column>
        <el-table-column prop="duration" label="时长(分钟)" width="120"></el-table-column>
        <el-table-column prop="intensity" label="强度" width="100"></el-table-column>
        <el-table-column prop="calories" label="消耗热量(kcal)" width="140"></el-table-column>
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

const exerciseList = ref([
  { date: '2024-01-15', type: '快走', duration: 45, intensity: '中等', calories: 280 },
  { date: '2024-01-14', type: '游泳', duration: 60, intensity: '高', calories: 450 },
  { date: '2024-01-13', type: '瑜伽', duration: 90, intensity: '低', calories: 180 }
])

const handleAdd = () => {
  // TODO: 添加运动记录
}
</script>

<style scoped>
.exercise-plan-container {
  padding: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
</style>
