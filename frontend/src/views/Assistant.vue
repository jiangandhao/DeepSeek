<template>
  <div class="page">
    <div class="page-heading">
      <div>
        <p class="eyebrow">AI Agents</p>
        <h1 class="page-title">AI 智能体中心</h1>
        <p class="page-subtitle">一个入口集中调用各类 AI 健康能力，每个智能体单独分析并以可视化 / 结构化方式呈现。</p>
      </div>
    </div>

    <div class="hub">
      <!-- 左侧:智能体选择 -->
      <aside class="rail">
        <button
          v-for="a in agents" :key="a.key" type="button"
          class="agent-item" :class="{ active: current === a.key }"
          @click="current = a.key"
        >
          <span class="agent-icon"><component :is="a.icon" /></span>
          <span class="agent-text"><b>{{ a.name }}</b><small>{{ a.caption }}</small></span>
        </button>

        <div class="ctx-card">
          <p class="ctx-title">问诊参考</p>
          <span><b>身份</b>{{ ctx.name }} · {{ ctx.gender }}</span>
          <span><b>健康</b>{{ ctx.body }} · {{ ctx.diabetes }}</span>
          <span><b>设备</b>{{ ctx.device }}</span>
        </div>
      </aside>

      <!-- 右侧:工作区 -->
      <section class="workspace surface">
        <component :is="activeComponent" :ctx="ctx" :key="current" />
      </section>
    </div>
  </div>
</template>

<script setup>
import { computed, markRaw, onMounted, ref } from 'vue'
import { ChatDotRound, TrendCharts, Warning, Food, Document, Picture } from '@element-plus/icons-vue'
import { getMe, getProfile, listDevices } from '../api'
import ChatAgent from '../components/agents/ChatAgent.vue'
import GlucoseAgent from '../components/agents/GlucoseAgent.vue'
import RiskAgent from '../components/agents/RiskAgent.vue'
import PlanAgent from '../components/agents/PlanAgent.vue'
import ReportAgent from '../components/agents/ReportAgent.vue'
import ImagingAgent from '../components/agents/ImagingAgent.vue'

const agents = [
  { key: 'chat', name: '自由问答', caption: '流式对话助手', icon: markRaw(ChatDotRound), comp: markRaw(ChatAgent) },
  { key: 'glucose', name: '血糖分析师', caption: '趋势·预测·异常', icon: markRaw(TrendCharts), comp: markRaw(GlucoseAgent) },
  { key: 'risk', name: '风险评估师', caption: '评分与解读', icon: markRaw(Warning), comp: markRaw(RiskAgent) },
  { key: 'plan', name: '营养与运动', caption: '饮食+七日计划', icon: markRaw(Food), comp: markRaw(PlanAgent) },
  { key: 'report', name: '报告解读', caption: '体检报告分析', icon: markRaw(Document), comp: markRaw(ReportAgent) },
  { key: 'imaging', name: '影像识别', caption: '肺结节检测', icon: markRaw(Picture), comp: markRaw(ImagingAgent) }
]

const current = ref('chat')
const activeComponent = computed(() => agents.find(a => a.key === current.value)?.comp)

const me = ref({})
const profile = ref({})
const devices = ref([])
const ctx = computed(() => {
  const gender = { 1: '男', 2: '女' }[me.value.gender] || '未设置'
  const diabetes = ['无', '1 型', '2 型', '妊娠'][profile.value.diabetesType] || '未知'
  const active = devices.value.find(d => d.status === 'ONLINE') || devices.value[0]
  return {
    name: me.value.nickname || me.value.username || '健康用户',
    gender,
    diabetes,
    body: `${profile.value.heightCm || '-'} cm / ${profile.value.weightKg || '-'} kg`,
    device: active ? `${active.deviceName}（${active.status}）` : '未绑定设备'
  }
})

onMounted(async () => {
  try { me.value = await getMe() || {} } catch {}
  try { profile.value = await getProfile() || {} } catch {}
  try { devices.value = await listDevices() || [] } catch {}
})
</script>

<style scoped>
.hub { display: grid; grid-template-columns: 248px 1fr; gap: 18px; align-items: start; }
.rail { display: grid; gap: 8px; }
.agent-item { display: flex; align-items: center; gap: 12px; padding: 13px 14px; text-align: left; background: var(--surface); border: 1px solid var(--line); border-radius: 14px; cursor: pointer; transition: .18s ease; }
.agent-item:hover { border-color: var(--mint-500); transform: translateX(2px); }
.agent-item.active { background: var(--mint-900); border-color: var(--mint-900); }
.agent-icon { display: grid; width: 38px; height: 38px; place-items: center; color: var(--mint-700); background: var(--mint-100); border-radius: 11px; flex: none; }
.agent-icon svg { width: 19px; }
.agent-item.active .agent-icon { color: var(--mint-900); background: #bfe8d9; }
.agent-text b { display: block; font-size: 14px; color: var(--ink); }
.agent-text small { display: block; margin-top: 2px; color: var(--muted); font-size: 12px; }
.agent-item.active .agent-text b { color: #fff; }
.agent-item.active .agent-text small { color: #9ec4b9; }
.ctx-card { margin-top: 8px; padding: 14px; background: var(--surface); border: 1px solid var(--line); border-radius: 14px; display: grid; gap: 8px; }
.ctx-title { margin: 0 0 2px; color: var(--mint-700); font-size: 12px; font-weight: 800; letter-spacing: .1em; text-transform: uppercase; }
.ctx-card span { font-size: 12px; color: var(--muted); line-height: 1.5; }
.ctx-card b { display: block; color: var(--ink); font-size: 12px; }
.workspace { padding: 24px; min-height: 560px; }
@media (max-width: 900px) {
  .hub { grid-template-columns: 1fr; }
  .rail { grid-template-columns: repeat(2, 1fr); }
  .ctx-card { grid-column: 1 / -1; }
}
</style>
