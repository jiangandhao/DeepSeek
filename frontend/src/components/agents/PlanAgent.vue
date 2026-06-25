<template>
  <div class="plan-agent" v-loading="loading">
    <div class="agent-toolbar">
      <div>
        <h3 class="block-title">饮食营养 + 运动方案</h3>
        <p class="block-copy">依据健康档案生成的结构化每日饮食处方与七日运动计划。<span v-if="savedAt && !loading" class="saved-tag">已保存 · {{ savedAgo(savedAt) }}</span></p>
      </div>
      <el-button type="primary" :loading="loading" @click="load"><Refresh /> 重新生成</el-button>
    </div>

    <template v-if="plan">
      <div class="plan-grid">
        <div class="surface-inner">
          <div class="card-head"><h4>今日饮食处方</h4><span class="soft-tag">{{ targets.calories || '—' }} kcal/天</span></div>
          <div v-for="meal in plan.meals" :key="meal.meal_type" class="meal">
            <span class="meal-time">{{ meal.time }}</span>
            <div class="meal-body">
              <b>{{ meal.name }}</b>
              <p>{{ (meal.foods || []).map(f => `${f.name} ${f.amount}`).join('、') }}</p>
              <small>{{ meal.calories }} kcal · 碳水 {{ meal.carbs_g }}g · GL {{ meal.gl }}</small>
            </div>
          </div>
        </div>

        <div class="surface-inner">
          <div class="card-head"><h4>营养目标</h4></div>
          <div ref="pieRef" class="pie"></div>
          <div class="targets">
            <span>膳食纤维 <b>{{ targets.fiber_g || 25 }}g</b></span>
            <span>钠上限 <b>{{ targets.sodium_mg_max || 2000 }}mg</b></span>
            <span>每日饮水 <b>{{ targets.water_ml || 1950 }}ml</b></span>
          </div>
        </div>
      </div>

      <div class="surface-inner week-card">
        <div class="card-head"><h4>本周运动计划</h4><span class="soft-tag">心率 {{ (plan.exercise_week[0] || {}).heart_rate }} 次/分</span></div>
        <div class="week">
          <div v-for="d in plan.exercise_week" :key="d.day" class="day" :class="intensityClass(d.intensity)">
            <small>第 {{ d.day }} 天</small>
            <b>{{ d.type }}</b>
            <span>{{ d.duration_min }} 分钟</span>
            <em>{{ intLabel(d.intensity) }}强度</em>
          </div>
        </div>
      </div>

      <p class="disclaimer">{{ plan.disclaimer }}</p>
    </template>
    <el-empty v-else-if="!loading" description="点击『重新生成』获取方案" :image-size="80" />
  </div>
</template>

<script setup>
import { computed, onMounted, onBeforeUnmount, ref, nextTick } from 'vue'
import * as echarts from 'echarts'
import { Refresh } from '@element-plus/icons-vue'
import { structuredHealthPlan } from '../../api'
import { loadInsight, saveInsight, savedAgo } from '../../utils/insightCache'

const pieRef = ref(null)
let pie
const loading = ref(false)
const plan = ref(null)
const savedAt = ref(null)
const targets = computed(() => plan.value?.daily_targets || {})

const intLabel = (v) => ({ LOW: '低', MEDIUM: '中', HIGH: '高' }[v] || v)
const intensityClass = (v) => ({ LOW: 'low', MEDIUM: 'mid', HIGH: 'high' }[v] || 'mid')

async function load() {
  loading.value = true
  try {
    plan.value = await structuredHealthPlan()
    savedAt.value = Date.now()
    saveInsight('plan', plan.value)
    await nextTick()
    renderPie()
  } finally {
    loading.value = false
  }
}

function renderPie() {
  if (!pieRef.value) return
  pie ||= echarts.init(pieRef.value)
  const t = targets.value
  pie.setOption({
    tooltip: { trigger: 'item', formatter: '{b}: {c}%' },
    legend: { bottom: 0, icon: 'circle' },
    series: [{
      type: 'pie', radius: ['45%', '70%'], center: ['50%', '44%'], avoidLabelOverlap: false,
      label: { show: false }, labelLine: { show: false },
      data: [
        { value: t.carbohydrate_percent || 45, name: '碳水', itemStyle: { color: '#35a889' } },
        { value: t.protein_percent || 25, name: '蛋白质', itemStyle: { color: '#216b5d' } },
        { value: t.fat_percent || 30, name: '脂肪', itemStyle: { color: '#9fd9c5' } }
      ]
    }]
  })
}

function resize() { pie?.resize() }
onMounted(async () => {
  const cached = loadInsight('plan')
  if (cached && cached.data) {
    plan.value = cached.data
    savedAt.value = cached.savedAt
    await nextTick()
    renderPie()
  } else {
    load()
  }
  window.addEventListener('resize', resize)
})
onBeforeUnmount(() => { window.removeEventListener('resize', resize); pie?.dispose() })
</script>

<style scoped>
.agent-toolbar { display: flex; align-items: flex-end; justify-content: space-between; gap: 12px; margin-bottom: 14px; }
.block-title { margin: 0; font-size: 18px; }
.block-copy { margin: 6px 0 0; color: var(--muted); font-size: 13px; }
.saved-tag { margin-left: 8px; color: var(--mint-700); }
.plan-grid { display: grid; grid-template-columns: 1.3fr 1fr; gap: 16px; }
.surface-inner { padding: 20px; background: #fff; border: 1px solid var(--line); border-radius: 16px; }
.card-head { display: flex; align-items: center; justify-content: space-between; margin-bottom: 14px; }
.card-head h4 { margin: 0; font-size: 16px; }
.meal { display: grid; grid-template-columns: 52px 1fr; gap: 12px; padding: 12px 0; border-bottom: 1px solid var(--line); }
.meal:last-child { border-bottom: 0; }
.meal-time { color: var(--mint-700); font-weight: 700; font-size: 13px; }
.meal-body b { font-size: 15px; }
.meal-body p { margin: 4px 0; color: var(--muted); font-size: 13px; }
.meal-body small { color: var(--muted); font-size: 12px; }
.pie { height: 200px; }
.targets { display: grid; grid-template-columns: repeat(3, 1fr); gap: 8px; margin-top: 8px; }
.targets span { padding: 10px; background: #f7fbfa; border: 1px solid var(--line); border-radius: 10px; font-size: 12px; color: var(--muted); text-align: center; }
.targets b { display: block; margin-top: 4px; color: var(--ink); font-size: 15px; }
.week-card { margin-top: 16px; }
.week { display: grid; grid-template-columns: repeat(7, 1fr); gap: 10px; }
.day { padding: 14px 10px; border-radius: 12px; text-align: center; border: 1px solid var(--line); background: #f7fbfa; }
.day.mid { background: #ecf8f2; border-color: #cdeedd; }
.day.high { background: #fff7e9; border-color: #f3e2b3; }
.day small { display: block; color: var(--muted); font-size: 11px; }
.day b { display: block; margin: 6px 0 4px; font-size: 13px; }
.day span { display: block; color: var(--mint-700); font-size: 12px; font-weight: 700; }
.day em { display: block; margin-top: 4px; color: var(--muted); font-size: 11px; font-style: normal; }
.disclaimer { margin-top: 16px; color: var(--muted); font-size: 12px; line-height: 1.6; }
@media (max-width: 900px) { .plan-grid { grid-template-columns: 1fr; } .week { grid-template-columns: repeat(3, 1fr); } }
</style>
