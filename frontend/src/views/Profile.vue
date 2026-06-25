<template>
  <div class="page">
    <div class="page-heading">
      <div>
        <p class="eyebrow">Identity card</p>
        <h1 class="page-title">个人身份卡</h1>
        <p class="page-subtitle">集中展示身份信息、健康信息与档案完整度，AI 医生会基于这些资料给出更贴合的建议。</p>
      </div>
      <el-button type="primary" :loading="saving" @click="saveAll"><Check /> 保存更改</el-button>
    </div>

    <section class="identity-card surface">
      <div class="avatar-block">
        <div class="big-avatar">{{ initial }}</div>
        <span class="soft-tag">{{ riskLabel }}</span>
      </div>
      <div class="identity-main">
        <p class="eyebrow">身份信息</p>
        <h2>{{ displayName }}</h2>
        <div class="identity-grid">
          <span><b>账号</b>{{ info.username || '-' }}</span>
          <span><b>性别</b>{{ genderText }}</span>
          <span><b>手机号</b>{{ info.phoneMasked || '未设置' }}</span>
          <span><b>用户编号</b>#{{ info.id || '-' }}</span>
        </div>
      </div>
      <div class="completeness">
        <el-progress type="dashboard" :percentage="completion" :width="112" color="#35a889" />
        <small>档案完整度</small>
      </div>
    </section>

    <section class="health-strip">
      <article v-for="item in healthCards" :key="item.label" class="surface health-card">
        <span><component :is="item.icon" /></span>
        <p>{{ item.label }}</p>
        <h3>{{ item.value }}</h3>
        <small>{{ item.note }}</small>
      </article>
    </section>

    <div class="profile-grid">
      <div class="main-column">
        <section class="surface form-card">
          <div class="card-head">
            <div><p class="eyebrow">Basic information</p><h2 class="section-title">身份信息维护</h2></div>
          </div>
          <el-form label-position="top" class="form-grid">
            <el-form-item label="昵称"><el-input v-model="form.nickname" /></el-form-item>
            <el-form-item label="性别">
              <el-select v-model="form.gender">
                <el-option :value="0" label="未设置" />
                <el-option :value="1" label="男" />
                <el-option :value="2" label="女" />
              </el-select>
            </el-form-item>
            <el-form-item label="手机号"><el-input v-model="form.phone" :placeholder="info.phoneMasked || '未设置'" /></el-form-item>
            <el-form-item label="糖尿病类型">
              <el-select v-model="profile.diabetesType">
                <el-option :value="0" label="无" />
                <el-option :value="1" label="1 型" />
                <el-option :value="2" label="2 型" />
                <el-option :value="3" label="妊娠" />
              </el-select>
            </el-form-item>
          </el-form>
        </section>

        <section class="surface form-card">
          <div class="card-head">
            <div><p class="eyebrow">Health profile</p><h2 class="section-title">健康信息维护</h2></div>
            <span class="soft-tag">用于 AI 医生判断</span>
          </div>
          <el-form label-position="top" class="form-grid">
            <el-form-item label="身高 (cm)"><el-input-number v-model="profile.heightCm" :min="50" :max="250" /></el-form-item>
            <el-form-item label="体重 (kg)"><el-input-number v-model="profile.weightKg" :min="20" :max="300" /></el-form-item>
            <el-form-item label="家族病史" class="wide">
              <el-input v-model="profile.familyHistory" type="textarea" :rows="3" placeholder="例如：父亲患 2 型糖尿病，母亲有高血压" />
            </el-form-item>
            <el-form-item label="基础疾病" class="wide">
              <el-input v-model="profile.chronic" placeholder="例如：高血压、高血脂" />
            </el-form-item>
          </el-form>
        </section>
      </div>

      <aside class="side-column">
        <section class="surface settings">
          <p class="eyebrow">Quick actions</p>
          <h2 class="section-title">健康资料联动</h2>
          <button v-for="item in settings" :key="item.title" type="button" @click="item.action">
            <span><component :is="item.icon" /></span>
            <div><b>{{ item.title }}</b><small>{{ item.caption }}</small></div>
            <ArrowRight />
          </button>
        </section>
        <section class="privacy surface">
          <span><Lock /></span>
          <h3>隐私优先</h3>
          <p>手机号等敏感字段加密存储，健康数据仅用于你授权的分析、预警与 AI 医生建议。</p>
          <el-switch v-model="shareData" active-text="允许匿名数据改进服务" />
        </section>
      </aside>
    </div>
  </div>
</template>

<script setup>
import { computed, onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { ArrowRight, Bell, Check, Lock, MagicStick, ScaleToOriginal, TrendCharts, User, Watch } from '@element-plus/icons-vue'
import { getMe, getProfile, saveProfile, updateMe } from '../api'
import { useAuthStore } from '../store/auth'

const router = useRouter()
const auth = useAuthStore()
const info = ref({})
const form = ref({ nickname: '', gender: 0, phone: '' })
const profile = ref({ heightCm: 170, weightKg: 65, diabetesType: 0, familyHistory: '', chronic: '' })
const saving = ref(false)
const shareData = ref(false)

const displayName = computed(() => form.value.nickname || info.value.username || '健康用户')
const initial = computed(() => displayName.value.slice(0, 1))
const genderText = computed(() => ({ 1: '男', 2: '女' }[form.value.gender] || '未设置'))
const diabetesText = computed(() => ['无', '1 型', '2 型', '妊娠'][profile.value.diabetesType] || '未知')
const bmi = computed(() => {
  const h = Number(profile.value.heightCm) / 100
  const w = Number(profile.value.weightKg)
  return h && w ? (w / (h * h)).toFixed(1) : '-'
})
const riskLabel = computed(() => Number(bmi.value) >= 28 || profile.value.diabetesType > 0 ? '需关注' : '状态良好')
const completion = computed(() => {
  const fields = [form.value.nickname, form.value.gender, info.value.phoneMasked || form.value.phone, profile.value.heightCm, profile.value.weightKg, profile.value.diabetesType !== null, profile.value.familyHistory, profile.value.chronic]
  return Math.round((fields.filter(Boolean).length / fields.length) * 100)
})
const healthCards = computed(() => [
  { label: 'BMI', value: bmi.value, note: bmi.value === '-' ? '待完善身高体重' : bmiNote(Number(bmi.value)), icon: ScaleToOriginal },
  { label: '糖尿病类型', value: diabetesText.value, note: '影响血糖目标区间', icon: TrendCharts },
  { label: '基础疾病', value: profile.value.chronic || '未填写', note: '用于风险评估', icon: User }
])
const settings = [
  { title: '设备绑定', caption: '接入虚拟设备数据', icon: Watch, action: () => router.push('/devices') },
  { title: 'AI 医生', caption: '携带档案上下文提问', icon: MagicStick, action: () => router.push('/assistant') },
  { title: '预警管理', caption: '查看风险与异常提醒', icon: Bell, action: () => router.push('/monitoring') }
]

function bmiNote(value) {
  if (value < 18.5) return '偏低'
  if (value < 24) return '正常'
  if (value < 28) return '超重'
  return '肥胖'
}

async function load() {
  try {
    info.value = await getMe()
    form.value = { nickname: info.value.nickname || '', gender: info.value.gender ?? 0, phone: '' }
  } catch {}
  try {
    const data = await getProfile()
    if (data) profile.value = { ...profile.value, ...data }
  } catch {}
}

async function saveAll() {
  saving.value = true
  try {
    const payload = { nickname: form.value.nickname, gender: form.value.gender }
    if (form.value.phone) payload.phone = form.value.phone
    await Promise.all([updateMe(payload), saveProfile(profile.value)])
    auth.nickname = form.value.nickname
    localStorage.setItem('nickname', form.value.nickname)
    form.value.phone = ''
    await load()
    ElMessage.success('个人身份卡已更新')
  } catch {
    ElMessage.error('保存失败，请稍后重试')
  } finally {
    saving.value = false
  }
}

onMounted(load)
</script>

<style scoped>
.identity-card { display:grid; grid-template-columns:120px 1fr 140px; align-items:center; gap:24px; padding:26px; }
.avatar-block { display:grid; justify-items:center; gap:12px; }
.big-avatar { display:grid; width:86px; height:86px; place-items:center; color:#fff; background:linear-gradient(135deg,var(--mint-500),var(--mint-900)); border-radius:24px; font-size:32px; font-weight:800; }
.identity-main h2 { margin:0 0 16px; font-size:26px; }
.identity-grid { display:grid; grid-template-columns:repeat(4, minmax(110px, 1fr)); gap:12px; }
.identity-grid span { padding:12px; background:#f7fbfa; border:1px solid var(--line); border-radius:12px; color:var(--ink); }
.identity-grid b { display:block; margin-bottom:5px; color:var(--muted); font-size:11px; }
.completeness { text-align:center; }
.completeness small { display:block; margin-top:6px; color:var(--muted); }
.health-strip { display:grid; grid-template-columns:repeat(3,1fr); gap:18px; margin:18px 0; }
.health-card { padding:20px; }
.health-card span { display:grid; width:40px; height:40px; place-items:center; color:var(--mint-700); background:#eef8f4; border-radius:12px; }
.health-card svg { width:19px; }
.health-card p { margin:16px 0 6px; color:var(--muted); font-size:13px; }
.health-card h3 { margin:0; font-size:24px; word-break:break-word; }
.health-card small { display:block; margin-top:8px; color:var(--muted); }
.profile-grid { display:grid; grid-template-columns:1.3fr .7fr; gap:18px; }
.main-column,.side-column { display:grid; align-content:start; gap:18px; }
.form-card,.settings,.privacy { padding:24px; }
.card-head { display:flex; align-items:center; justify-content:space-between; margin-bottom:16px; }
.form-grid { display:grid; grid-template-columns:repeat(2,1fr); gap:0 16px; }
.wide { grid-column:1 / -1; }
.form-grid :deep(.el-select), .form-grid :deep(.el-input-number) { width:100%; }
.settings button { display:grid; width:100%; grid-template-columns:38px 1fr 15px; align-items:center; gap:10px; padding:15px 0; color:var(--ink); background:none; border:0; border-bottom:1px solid var(--line); text-align:left; cursor:pointer; }
.settings button>span { display:grid; width:38px; height:38px; place-items:center; color:var(--mint-700); background:#eef8f4; border-radius:11px; }
.settings svg { width:17px; }
.settings button>svg { width:14px; color:var(--muted); }
.settings b,.settings small { display:block; }
.settings small { margin-top:4px; color:var(--muted); font-size:11px; }
.privacy>span { display:grid; width:44px; height:44px; place-items:center; color:var(--mint-700); background:var(--mint-100); border-radius:13px; }
.privacy svg { width:20px; }
.privacy h3 { margin:15px 0 6px; }
.privacy p { color:var(--muted); font-size:12px; line-height:1.7; }
.privacy :deep(.el-switch__label) { font-size:11px; }
@media(max-width:1050px){ .identity-card,.profile-grid,.health-strip{ grid-template-columns:1fr; } .identity-grid{ grid-template-columns:repeat(2,1fr); } .completeness{ text-align:left; } }
@media(max-width:600px){ .identity-grid,.form-grid{ grid-template-columns:1fr; } .identity-card{ padding:20px; } }
</style>
