<template>
  <div class="page">
    <div class="page-heading">
      <div><p class="eyebrow">Health overview</p><h1 class="page-title">早上好，{{ name }}</h1><p class="page-subtitle">你的整体状态平稳，今天把重点放在餐后血糖和轻量运动上。</p></div>
      <button class="alert-pill" type="button" @click="router.push('/monitoring')"><Bell /><span><b>{{ alerts.length || 1 }} 条健康提醒</b><small>点击查看详情</small></span><ArrowRight /></button>
    </div>

    <section class="hero-grid">
      <article class="score-card surface">
        <div><span class="soft-tag"><CircleCheck /> 状态良好</span><h2>今日健康评分</h2><p>较昨日提升 <b>2 分</b></p></div>
        <div class="score-ring" :style="{ '--score': healthScore }"><div><b>{{ healthScore }}</b><small>/ 100</small></div></div>
        <div class="score-foot"><span><i class="dot green" />代谢状态稳定</span><span>继续保持规律饮食</span></div>
      </article>

      <article class="insight-card">
        <div class="insight-icon"><MagicStick /></div><p class="eyebrow">DeepSeek insight</p><h2>午餐后安排 15 分钟散步</h2><p>根据你近 7 天的数据，餐后轻量活动可能有助于平稳血糖波动。</p><button type="button" @click="router.push('/plan')">查看今日方案 <ArrowRight /></button>
      </article>
    </section>

    <section class="metrics-grid">
      <article v-for="metric in metrics" :key="metric.label" class="metric surface" :class="metric.status">
        <div class="metric-head"><span :class="['metric-icon', metric.status]"><component :is="metric.icon" /></span><span class="trend">{{ metric.trend }}</span></div>
        <p>{{ metric.label }}</p><h3>{{ metric.value }} <small>{{ metric.unit }}</small></h3><div class="spark"><i v-for="n in 9" :key="n" :style="{ height: `${metric.bars[n-1]}%` }" /></div><small>{{ metric.note }}</small>
      </article>
    </section>

    <section class="lower-grid">
      <article class="surface plan-card">
        <div class="section-head"><div><p class="eyebrow">Today</p><h2 class="section-title">今天的行动</h2></div><button type="button" @click="router.push('/plan')">全部方案</button></div>
        <label v-for="task in tasks" :key="task.title" class="task"><input v-model="task.done" type="checkbox"><span class="task-check"><Check /></span><span class="task-icon"><component :is="task.icon" /></span><span><b>{{ task.title }}</b><small>{{ task.detail }}</small></span><em>{{ task.time }}</em></label>
      </article>

      <article class="surface trend-card">
        <div class="section-head"><div><p class="eyebrow">7 day trend</p><h2 class="section-title">血糖趋势</h2></div><span class="soft-tag">TIR 78%</span></div>
        <div ref="chartRef" class="chart" />
        <div class="chart-summary"><span><b>{{ stats.avg }}</b><small>平均值 mmol/L</small></span><span><b>{{ stats.max }}</b><small>最高值</small></span><span><b>{{ stats.min }}</b><small>最低值</small></span></div>
      </article>
    </section>

    <p class="ai-note">本页面中的健康提示由 AI 基于已记录数据生成，仅供健康管理参考，不替代专业医疗诊断与治疗意见。</p>
  </div>
</template>

<script setup>
import { computed, nextTick, onBeforeUnmount, onMounted, reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import * as echarts from 'echarts'
import { Bell, ArrowRight, CircleCheck, MagicStick, Check, Food, Bicycle, Calendar, MostlyCloudy, TrendCharts, ScaleToOriginal } from '@element-plus/icons-vue'
import { listGlucose, listAlerts } from '../api'
import { useAuthStore } from '../store/auth'

const router = useRouter()
const auth = useAuthStore()
const name = computed(() => auth.nickname || auth.username || '朋友')
const healthScore = 86
const records = ref([])
const alerts = ref([])
const chartRef = ref(null)
let chart
const metrics = computed(() => [
  { label: '最新血糖', value: stats.value.latest, unit: 'mmol/L', trend: '↘ 0.4', note: '处于目标范围', status: 'good', icon: TrendCharts, bars: [30,48,36,65,52,73,58,42,55] },
  { label: '血压', value: '118/76', unit: 'mmHg', trend: '稳定', note: '最近测量 08:20', status: 'good', icon: MostlyCloudy, bars: [48,52,45,55,49,53,50,51,48] },
  { label: '体重', value: '65.2', unit: 'kg', trend: '↘ 0.3', note: '本月下降 0.8 kg', status: 'neutral', icon: ScaleToOriginal, bars: [72,70,69,67,65,63,60,58,56] }
])
const tasks = reactive([
  { title: '低 GI 能量早餐', detail: '燕麦、鸡蛋与无糖酸奶', time: '08:00', done: true, icon: Food },
  { title: '餐后快走 30 分钟', detail: '建议心率 120–140 次/分', time: '18:30', done: false, icon: Bicycle },
  { title: '年度体检准备', detail: '周五上午，记得空腹', time: '3 天后', done: false, icon: Calendar }
])
const stats = computed(() => {
  const vals = records.value.map(r => Number(r.valueMmol)).filter(Number.isFinite)
  if (!vals.length) return { latest: '6.1', avg: '6.3', max: '8.1', min: '4.7' }
  return { latest: vals.at(-1).toFixed(1), avg: (vals.reduce((a,b)=>a+b,0)/vals.length).toFixed(1), max: Math.max(...vals).toFixed(1), min: Math.min(...vals).toFixed(1) }
})
async function load() {
  try { records.value = await listGlucose({ from: new Date(Date.now()-7*864e5).toISOString().slice(0,19) }) || [] } catch { records.value = [] }
  try { alerts.value = await listAlerts() || [] } catch { alerts.value = [] }
  await nextTick(); renderChart()
}
function renderChart() {
  if (!chartRef.value) return
  chart ||= echarts.init(chartRef.value)
  const fallback = [5.8,6.2,7.1,6.4,5.9,6.7,6.1]
  const data = records.value.length ? records.value.slice(-12).map(r => Number(r.valueMmol)) : fallback
  chart.setOption({ grid:{left:8,right:8,top:20,bottom:8,containLabel:true}, tooltip:{trigger:'axis'}, xAxis:{type:'category',boundaryGap:false,data:data.map((_,i)=>i+1),axisLine:{show:false},axisTick:{show:false},axisLabel:{show:false}}, yAxis:{type:'value',min:3,max:10,splitLine:{lineStyle:{color:'#edf3f0'}},axisLabel:{color:'#8ca09a'},axisLine:{show:false}}, series:[{type:'line',smooth:true,symbol:'none',data,lineStyle:{width:3,color:'#35a889'},areaStyle:{color:new echarts.graphic.LinearGradient(0,0,0,1,[{offset:0,color:'rgba(53,168,137,.28)'},{offset:1,color:'rgba(53,168,137,0)'}])},markArea:{silent:true,itemStyle:{color:'rgba(121,209,182,.09)'},data:[[{yAxis:3.9},{yAxis:7.8}]]}}] })
}
function resize(){ chart?.resize() }
onMounted(()=>{ load(); window.addEventListener('resize',resize) })
onBeforeUnmount(()=>{ window.removeEventListener('resize',resize); chart?.dispose() })
</script>

<style scoped>
.alert-pill { display:flex; align-items:center; gap:11px; padding:10px 14px; color:#8d3d43; background:#fff0f0; border:1px solid #f4d3d5; border-radius:14px; text-align:left; cursor:pointer; }.alert-pill>svg{width:19px}.alert-pill span{min-width:125px}.alert-pill b,.alert-pill small{display:block}.alert-pill small{margin-top:3px;color:#a47175;font-size:11px}.alert-pill>svg:last-child{width:14px}
.hero-grid{display:grid;grid-template-columns:1.35fr 1fr;gap:18px}.score-card{display:grid;grid-template-columns:1fr 150px;align-items:center;padding:27px 28px}.score-card h2{margin:18px 0 4px;font-size:22px}.score-card p{margin:0;color:var(--muted)}.score-card p b{color:var(--mint-700)}.score-ring{display:grid;width:132px;height:132px;place-items:center;background:conic-gradient(var(--mint-500) calc(var(--score)*1%),#e9f2ef 0);border-radius:50%}.score-ring:before{content:'';position:absolute;width:102px;height:102px;background:white;border-radius:50%}.score-ring div{z-index:1;text-align:center}.score-ring b{display:block;font-size:38px}.score-ring small{color:var(--muted)}.score-foot{grid-column:1/-1;display:flex;justify-content:space-between;margin-top:20px;padding-top:16px;color:var(--muted);border-top:1px solid var(--line);font-size:12px}.dot{display:inline-block;width:7px;height:7px;margin-right:7px;border-radius:50%}.green{background:var(--mint-500)}
.insight-card{position:relative;overflow:hidden;padding:28px;color:#e9f8f3;background:linear-gradient(135deg,#1a594d,#0f3932);border-radius:22px;box-shadow:var(--shadow)}.insight-card:after{content:'';position:absolute;width:190px;height:190px;right:-70px;bottom:-100px;background:rgba(121,209,182,.16);border-radius:50%}.insight-card .eyebrow{color:#92d8c4}.insight-card h2{max-width:390px;margin:10px 0;font-size:23px}.insight-card p:not(.eyebrow){max-width:430px;color:#b8d5cd;line-height:1.7}.insight-icon{display:grid;width:42px;height:42px;place-items:center;color:#143f36;background:#bfe8d9;border-radius:13px}.insight-icon svg{width:21px}.insight-card button{display:flex;align-items:center;gap:5px;margin-top:18px;padding:0;color:#d7f3ea;background:none;border:0;font-weight:700;cursor:pointer}.insight-card button svg{width:14px}
.metrics-grid{display:grid;grid-template-columns:repeat(3,1fr);gap:18px;margin-top:18px}.metric{padding:20px}.metric-head{display:flex;align-items:center;justify-content:space-between}.metric-icon{display:grid;width:38px;height:38px;place-items:center;background:#e4f5ef;border-radius:12px}.metric-icon svg{width:19px}.trend{color:var(--mint-700);font-size:12px;font-weight:700}.metric p{margin:18px 0 6px;color:var(--muted);font-size:13px}.metric h3{margin:0;font-size:25px}.metric h3 small{color:var(--muted);font-size:11px}.metric>small{color:var(--muted)}.spark{display:flex;height:34px;align-items:flex-end;gap:5px;margin:14px 0 9px}.spark i{flex:1;min-height:5px;background:#b8e2d4;border-radius:4px}
.lower-grid{display:grid;grid-template-columns:.9fr 1.1fr;gap:18px;margin:18px 0}.plan-card,.trend-card{padding:24px}.section-head{display:flex;align-items:center;justify-content:space-between;margin-bottom:15px}.section-head button{color:var(--mint-700);background:none;border:0;font-weight:700;cursor:pointer}.task{position:relative;display:grid;grid-template-columns:28px 39px 1fr auto;align-items:center;gap:10px;padding:14px 0;border-bottom:1px solid var(--line);cursor:pointer}.task:last-child{border:0}.task input{position:absolute;opacity:0}.task-check{display:grid;width:23px;height:23px;place-items:center;color:transparent;border:1px solid #b7cac4;border-radius:8px}.task input:checked+.task-check{color:#fff;background:var(--mint-500);border-color:var(--mint-500)}.task-check svg{width:14px}.task-icon{display:grid;width:39px;height:39px;place-items:center;color:var(--mint-700);background:#eef8f4;border-radius:11px}.task-icon svg{width:18px}.task b,.task small{display:block}.task small{margin-top:4px;color:var(--muted);font-size:11px}.task em{color:var(--muted);font-size:11px;font-style:normal}.chart{height:190px}.chart-summary{display:grid;grid-template-columns:repeat(3,1fr);padding-top:13px;border-top:1px solid var(--line)}.chart-summary span{text-align:center;border-right:1px solid var(--line)}.chart-summary span:last-child{border:0}.chart-summary b,.chart-summary small{display:block}.chart-summary b{font-size:18px}.chart-summary small{margin-top:4px;color:var(--muted);font-size:10px}
@media(max-width:1050px){.hero-grid,.lower-grid{grid-template-columns:1fr}.metrics-grid{grid-template-columns:repeat(2,1fr)}}@media(max-width:650px){.hero-grid,.metrics-grid{grid-template-columns:1fr}.score-card{grid-template-columns:1fr 110px;padding:22px}.score-ring{width:100px;height:100px}.score-ring:before{width:76px;height:76px}.score-ring b{font-size:28px}.score-foot{display:none}.task{grid-template-columns:26px 38px 1fr}.task em{display:none}}
</style>
