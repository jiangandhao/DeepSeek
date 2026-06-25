<template>
  <div class="chat">
    <div ref="msgBox" class="messages">
      <el-empty v-if="!messages.length" description="向 AI 健康助手提问，例如『最近血糖怎么样，饮食要注意什么？』" :image-size="80" />
      <div v-for="(m, i) in messages" :key="i" :class="['msg', m.role]">
        <div v-if="m.role === 'user'" class="bubble user">{{ m.content }}</div>
        <div v-else class="bubble assistant markdown" v-html="renderMd(m.content)"></div>
      </div>
    </div>
    <div class="input-area">
      <el-input v-model="input" type="textarea" :rows="2" placeholder="输入你的问题，回车发送" @keydown.enter.exact.prevent="send" />
      <el-button type="primary" :loading="loading" @click="send">发送</el-button>
    </div>
  </div>
</template>

<script setup>
import { ref, nextTick } from 'vue'
import { marked } from 'marked'
import { ElMessage } from 'element-plus'
import { aiChatStream } from '../../api'

const props = defineProps({ ctx: { type: Object, default: () => ({}) } })

const messages = ref([])
const input = ref('')
const loading = ref(false)
const msgBox = ref(null)

const renderMd = (text) => marked.parse(text || '')

async function scrollBottom() {
  await nextTick()
  if (msgBox.value) msgBox.value.scrollTop = msgBox.value.scrollHeight
}

function withContext(question) {
  const c = props.ctx || {}
  return `请以 AI 医生身份回答。我的个人信息：昵称 ${c.name || '-'}，性别 ${c.gender || '-'}，糖尿病类型 ${c.diabetes || '-'}，身高体重 ${c.body || '-'}，绑定设备 ${c.device || '-'}。问题：${question}`
}

async function send() {
  const q = input.value.trim()
  if (!q || loading.value) return
  messages.value.push({ role: 'user', content: q })
  input.value = ''
  await scrollBottom()
  loading.value = true
  const idx = messages.value.length
  messages.value.push({ role: 'assistant', content: '' })
  try {
    await aiChatStream(withContext(q), (chunk) => {
      messages.value[idx].content += chunk
      scrollBottom()
    })
  } catch (e) {
    messages.value[idx].content += `\n\n[出错] ${e.message}`
    ElMessage.error('生成失败，请检查模型配置')
  } finally {
    loading.value = false
    scrollBottom()
  }
}
</script>

<style scoped>
.chat { display: flex; flex-direction: column; height: 560px; }
.messages { flex: 1; overflow-y: auto; padding: 4px; }
.msg { margin-bottom: 14px; display: flex; }
.msg.user { justify-content: flex-end; }
.bubble { max-width: 82%; padding: 11px 15px; border-radius: 14px; line-height: 1.7; font-size: 14px; }
.bubble.user { background: var(--mint-700); color: #fff; white-space: pre-wrap; }
.bubble.assistant { background: #f4f8f6; border: 1px solid var(--line); color: var(--ink); }
.markdown :deep(h1), .markdown :deep(h2), .markdown :deep(h3) { font-size: 15px; margin: 8px 0 4px; }
.markdown :deep(p) { margin: 6px 0; }
.markdown :deep(ul) { padding-left: 20px; margin: 6px 0; }
.markdown :deep(strong) { color: var(--mint-700); }
.input-area { display: flex; gap: 8px; margin-top: 12px; }
</style>
