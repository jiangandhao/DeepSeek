<template>
  <el-row :gutter="16">
    <el-col :span="16">
      <el-card class="chat-card">
        <template #header>
          <div class="card-header">
            <span>🤖 血糖管理智能体</span>
            <el-button size="small" type="primary" :loading="loading" @click="genPlan">生成综合方案</el-button>
          </div>
        </template>

        <div ref="msgBox" class="messages">
          <el-empty v-if="!messages.length" description="向智能体提问,或点击『生成综合方案』" />
          <div v-for="(m, i) in messages" :key="i" :class="['msg', m.role]">
            <div class="bubble" v-if="m.role === 'user'">{{ m.content }}</div>
            <div class="bubble assistant" v-else v-html="renderMd(m.content)"></div>
          </div>
        </div>

        <div class="input-area">
          <el-input
            v-model="input"
            type="textarea"
            :rows="2"
            placeholder="例如:帮我看看最近血糖,给点饮食建议"
            @keydown.enter.exact.prevent="send"
          />
          <el-button type="primary" :loading="loading" @click="send">发送</el-button>
        </div>
      </el-card>
    </el-col>

    <el-col :span="8">
      <el-card class="context-card">
        <template #header>
          <div class="card-header"><span>问诊参考资料</span></div>
        </template>
        <div class="context-list">
          <span><b>身份</b>{{ contextSummary.name }} · {{ contextSummary.gender }}</span>
          <span><b>健康</b>{{ contextSummary.body }} · {{ contextSummary.diabetes }}</span>
          <span><b>设备</b>{{ contextSummary.device }}</span>
        </div>
      </el-card>

      <el-card>
        <template #header>
          <div class="card-header"><span>📈 血糖预测</span>
            <el-button size="small" @click="loadPredict" :loading="predLoading">刷新</el-button>
          </div>
        </template>
        <div v-if="predict">
          <p class="muted">模型:{{ predict.model }}
            <span v-if="predict.metrics"> | MAE {{ predict.metrics.mae }} / RMSE {{ predict.metrics.rmse }}</span>
          </p>
          <el-table :data="predict.predictions" size="small" max-height="200">
            <el-table-column prop="measured_at" label="时间" :formatter="(r) => r.measured_at?.slice(5,16)" />
            <el-table-column prop="value_mmol" label="预测(mmol/L)" />
          </el-table>
        </div>
        <el-empty v-else description="暂无预测" :image-size="60" />
      </el-card>

      <el-card style="margin-top: 16px">
        <template #header>
          <div class="card-header"><span>⚠️ 异常预警</span>
            <el-button size="small" @click="loadAnomaly" :loading="anoLoading">刷新</el-button>
          </div>
        </template>
        <div v-if="anomalies.length">
          <el-alert
            v-for="(a, i) in anomalies" :key="i"
            :title="a.message"
            :type="a.level === 'HIGH' ? 'error' : 'warning'"
            :closable="false"
            style="margin-bottom: 8px"
          />
        </div>
        <el-empty v-else description="未发现异常" :image-size="60" />
      </el-card>
    </el-col>
  </el-row>
</template>

<script setup>
import { computed, ref, onMounted, nextTick } from 'vue'
import { marked } from 'marked'
import { ElMessage } from 'element-plus'
import { aiChatStream, aiPredict, aiAnomaly, getMe, getProfile, listDevices } from '../api'

const messages = ref([])
const input = ref('')
const loading = ref(false)
const msgBox = ref(null)

const predict = ref(null)
const anomalies = ref([])
const predLoading = ref(false)
const anoLoading = ref(false)
const me = ref({})
const profile = ref({})
const devices = ref([])

const contextSummary = computed(() => {
  const gender = { 1: '男', 2: '女' }[me.value.gender] || '未设置'
  const diabetes = ['无', '1 型', '2 型', '妊娠'][profile.value.diabetesType] || '未知'
  const activeDevice = devices.value.find(d => d.status === 'ONLINE') || devices.value[0]
  return {
    name: me.value.nickname || me.value.username || '健康用户',
    gender,
    diabetes,
    body: `${profile.value.heightCm || '-'} cm / ${profile.value.weightKg || '-'} kg`,
    device: activeDevice ? `${activeDevice.deviceName}（${activeDevice.status}，最近 ${activeDevice.lastValueMmol || '-'} mmol/L）` : '未绑定设备'
  }
})

const renderMd = (text) => marked.parse(text || '')

async function scrollBottom() {
  await nextTick()
  if (msgBox.value) msgBox.value.scrollTop = msgBox.value.scrollHeight
}

async function streamTo(question) {
  loading.value = true
  // 通过数组下标走响应式代理修改,确保流式增量能触发渲染
  const idx = messages.value.length
  messages.value.push({ role: 'assistant', content: '' })
  try {
    await aiChatStream(question, (chunk) => {
      messages.value[idx].content += chunk
      scrollBottom()
    })
  } catch (e) {
    messages.value[idx].content += `\n\n[出错] ${e.message}`
    ElMessage.error('生成失败,请检查 DeepSeek API Key 配置')
  } finally {
    loading.value = false
    scrollBottom()
  }
}

async function send() {
  const q = input.value.trim()
  if (!q) return
  messages.value.push({ role: 'user', content: q })
  input.value = ''
  await scrollBottom()
  await streamTo(withContext(q))
}

async function genPlan() {
  messages.value.push({ role: 'user', content: '【生成综合血糖管理方案】' })
  await scrollBottom()
  await streamTo(withContext('请基于我的个人身份信息、健康档案、最近记录和设备数据，生成一份今日健康指导。'))
}

function withContext(question) {
  const c = contextSummary.value
  return `请以 AI 医生身份回答。我的个人信息：姓名/昵称 ${c.name}，性别 ${c.gender}，糖尿病类型 ${c.diabetes}，身高体重 ${c.body}，绑定设备 ${c.device}。问题：${question}`
}

async function loadContext() {
  try { me.value = await getMe() || {} } catch {}
  try { profile.value = await getProfile() || {} } catch {}
  try { devices.value = await listDevices() || [] } catch {}
}

async function loadPredict() {
  predLoading.value = true
  try { predict.value = await aiPredict(6) } finally { predLoading.value = false }
}

async function loadAnomaly() {
  anoLoading.value = true
  try {
    const res = await aiAnomaly()
    anomalies.value = res.anomalies || []
  } finally { anoLoading.value = false }
}

onMounted(() => {
  loadContext()
  loadPredict()
  loadAnomaly()
})
</script>

<style scoped>
.card-header { display: flex; justify-content: space-between; align-items: center; }
.chat-card :deep(.el-card__body) { display: flex; flex-direction: column; }
.messages { height: 460px; overflow-y: auto; padding: 8px; }
.msg { margin-bottom: 14px; display: flex; }
.msg.user { justify-content: flex-end; }
.bubble { max-width: 80%; padding: 10px 14px; border-radius: 10px; background: #ecf5ff; white-space: pre-wrap; line-height: 1.6; }
.bubble.assistant { background: #f4f4f5; white-space: normal; }
.bubble.assistant :deep(h1), .bubble.assistant :deep(h2), .bubble.assistant :deep(h3) { font-size: 15px; margin: 8px 0 4px; }
.bubble.assistant :deep(p) { margin: 6px 0; }
.bubble.assistant :deep(ul) { padding-left: 20px; margin: 6px 0; }
.input-area { display: flex; gap: 8px; margin-top: 12px; }
.muted { color: #909399; font-size: 12px; }
.context-card { margin-bottom: 16px; }
.context-list { display:grid; gap:10px; }
.context-list span { padding:10px 12px; background:#f7fbfa; border:1px solid var(--line); border-radius:10px; color:var(--muted); font-size:12px; line-height:1.5; }
.context-list b { display:block; margin-bottom:3px; color:var(--ink); font-size:13px; }
</style>
