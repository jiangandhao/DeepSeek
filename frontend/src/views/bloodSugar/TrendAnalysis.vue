<template>
  <div class="trend-analysis-container">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>血糖趋势分析</span>
          <el-date-picker
            v-model="dateRange"
            type="daterange"
            range-separator="至"
            start-placeholder="开始日期"
            end-placeholder="结束日期"
          ></el-date-picker>
        </div>
      </template>
      <div ref="chartRef" style="width: 100%; height: 400px;"></div>
      <el-table :data="trendData" style="width: 100%; margin-top: 20px;">
        <el-table-column prop="date" label="日期" width="180"></el-table-column>
        <el-table-column prop="fasting" label="空腹血糖(mmol/L)" width="160"></el-table-column>
        <el-table-column prop="postprandial" label="餐后血糖(mmol/L)" width="160"></el-table-column>
        <el-table-column prop="status" label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="row.status === '正常' ? 'success' : 'warning'">{{ row.status }}</el-tag>
          </template>
        </el-table-column>
      </el-table>
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import * as echarts from 'echarts'

const chartRef = ref(null)
const dateRange = ref([])
const trendData = ref([
  { date: '2024-01-10', fasting: 5.2, postprandial: 7.1, status: '正常' },
  { date: '2024-01-11', fasting: 5.5, postprandial: 7.8, status: '正常' },
  { date: '2024-01-12', fasting: 5.8, postprandial: 8.2, status: '偏高' },
  { date: '2024-01-13', fasting: 5.4, postprandial: 7.5, status: '正常' },
  { date: '2024-01-14', fasting: 5.6, postprandial: 7.9, status: '正常' },
  { date: '2024-01-15', fasting: 5.3, postprandial: 7.2, status: '正常' }
])

onMounted(() => {
  if (chartRef.value) {
    const chart = echarts.init(chartRef.value)
    const option = {
      title: {
        text: '血糖趋势图'
      },
      tooltip: {
        trigger: 'axis'
      },
      legend: {
        data: ['空腹血糖', '餐后血糖']
      },
      xAxis: {
        type: 'category',
        data: trendData.value.map(item => item.date)
      },
      yAxis: {
        type: 'value',
        name: 'mmol/L'
      },
      series: [
        {
          name: '空腹血糖',
          type: 'line',
          data: trendData.value.map(item => item.fasting),
          smooth: true
        },
        {
          name: '餐后血糖',
          type: 'line',
          data: trendData.value.map(item => item.postprandial),
          smooth: true
        }
      ]
    }
    chart.setOption(option)
  }
})
</script>

<style scoped>
.trend-analysis-container {
  padding: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
</style>
