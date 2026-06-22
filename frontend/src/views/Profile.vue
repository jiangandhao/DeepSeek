<template>
  <div class="page">
    <div class="page-heading"><div><p class="eyebrow">My health file</p><h1 class="page-title">我的健康档案</h1><p class="page-subtitle">完善的信息会让风险评估与健康方案更贴合你的真实情况。</p></div><el-button type="primary" :loading="saving" @click="saveAll"><Check /> 保存更改</el-button></div>
    <section class="profile-hero surface"><div class="big-avatar">{{initial}}</div><div><h2>{{form.nickname||info.username||'健康用户'}}</h2><p>档案完整度 76% · 最近更新于今天</p><div><span class="soft-tag">低风险</span><span class="soft-tag">数据已加密</span></div></div><button type="button" @click="router.push('/health-manager')">查看风险评估 <ArrowRight /></button></section>
    <div class="profile-grid">
      <div class="main-column">
        <section class="surface form-card"><div class="card-head"><div><p class="eyebrow">Basic information</p><h2 class="section-title">基本信息</h2></div></div><el-form label-position="top" class="form-grid"><el-form-item label="昵称"><el-input v-model="form.nickname"/></el-form-item><el-form-item label="性别"><el-select v-model="form.gender"><el-option :value="0" label="未设置"/><el-option :value="1" label="男"/><el-option :value="2" label="女"/></el-select></el-form-item><el-form-item label="身高 (cm)"><el-input-number v-model="profile.heightCm" :min="50" :max="250"/></el-form-item><el-form-item label="体重 (kg)"><el-input-number v-model="profile.weightKg" :min="20" :max="300"/></el-form-item><el-form-item label="手机号"><el-input v-model="form.phone" :placeholder="info.phoneMasked||'未设置'"/></el-form-item><el-form-item label="糖尿病类型"><el-select v-model="profile.diabetesType"><el-option :value="0" label="无"/><el-option :value="1" label="1 型"/><el-option :value="2" label="2 型"/><el-option :value="3" label="妊娠"/></el-select></el-form-item></el-form></section>
        <section class="surface form-card"><div class="card-head"><div><p class="eyebrow">Medical history</p><h2 class="section-title">健康背景</h2></div><span class="soft-tag">用于风险评估</span></div><el-form label-position="top"><el-form-item label="家族病史"><el-input v-model="profile.familyHistory" type="textarea" :rows="3" placeholder="例如：父亲患 2 型糖尿病，母亲有高血压"/></el-form-item><el-form-item label="基础疾病"><el-input v-model="profile.chronic" placeholder="例如：高血压、高血脂"/></el-form-item></el-form></section>
      </div>
      <aside class="side-column">
        <section class="surface settings"><p class="eyebrow">Safety</p><h2 class="section-title">预警与安全</h2><button v-for="item in settings" :key="item.title" type="button" @click="handleSetting(item)"><span><component :is="item.icon"/></span><div><b>{{item.title}}</b><small>{{item.caption}}</small></div><ArrowRight/></button></section>
        <section class="privacy surface"><span><Lock /></span><h3>隐私优先</h3><p>健康数据加密存储，第三方共享默认关闭。你可以随时导出或申请删除全部数据。</p><el-switch v-model="shareData" active-text="允许匿名数据改进服务"/></section>
        <section class="danger surface"><h3>账户与数据</h3><button type="button" @click="ElMessage.success('数据导出申请已提交')">导出我的全部数据</button><button type="button" @click="ElMessage.warning('为保护数据安全，请在身份验证后操作')">删除全部健康数据</button></section>
      </aside>
    </div>
  </div>
</template>
<script setup>
import { computed, onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { Check, ArrowRight, Lock, Bell, Phone, Watch, Key } from '@element-plus/icons-vue'
import { getMe, updateMe, getProfile, saveProfile } from '../api'
import { useAuthStore } from '../store/auth'
const router=useRouter(),auth=useAuthStore(),info=ref({}),form=ref({nickname:'',gender:0,phone:''}),profile=ref({heightCm:170,weightKg:65,diabetesType:0,familyHistory:'',chronic:''}),saving=ref(false),shareData=ref(false)
const initial=computed(()=>(form.value.nickname||info.value.username||'健').slice(0,1))
const settings=[{title:'预警阈值',caption:'血糖 3.9–7.8 mmol/L',icon:Bell},{title:'紧急联系人',caption:'尚未添加',icon:Phone},{title:'设备绑定',caption:'2 台设备已连接',icon:Watch},{title:'隐私授权',caption:'第三方共享已关闭',icon:Key}]
async function load(){try{info.value=await getMe();form.value={nickname:info.value.nickname||'',gender:info.value.gender??0,phone:''}}catch{}try{const p=await getProfile();if(p)profile.value={...profile.value,...p}}catch{}}
async function saveAll(){saving.value=true;try{const payload={nickname:form.value.nickname,gender:form.value.gender};if(form.value.phone)payload.phone=form.value.phone;await Promise.all([updateMe(payload),saveProfile(profile.value)]);auth.nickname=form.value.nickname;localStorage.setItem('nickname',form.value.nickname);form.value.phone='';ElMessage.success('健康档案已保存')}catch{ElMessage.error('保存失败，请稍后重试')}finally{saving.value=false}}
function handleSetting(item){ElMessage.info(`${item.title}设置将在下一迭代接入`)}
onMounted(load)
</script>
<style scoped>
.page-heading svg{width:16px}.profile-hero{display:grid;grid-template-columns:76px 1fr auto;align-items:center;gap:18px;padding:24px}.big-avatar{display:grid;width:72px;height:72px;place-items:center;color:#fff;background:linear-gradient(135deg,var(--mint-500),var(--mint-900));border-radius:22px;font-size:27px;font-weight:800}.profile-hero h2,.profile-hero p{margin:0}.profile-hero p{margin:6px 0 11px;color:var(--muted);font-size:12px}.profile-hero>div span+span{margin-left:7px}.profile-hero>button{display:flex;align-items:center;gap:5px;color:var(--mint-700);background:none;border:0;font-weight:700;cursor:pointer}.profile-hero>button svg{width:15px}.profile-grid{display:grid;grid-template-columns:1.3fr .7fr;gap:18px;margin-top:18px}.main-column,.side-column{display:grid;align-content:start;gap:18px}.form-card,.settings,.privacy,.danger{padding:24px}.card-head{display:flex;align-items:center;justify-content:space-between;margin-bottom:16px}.form-grid{display:grid;grid-template-columns:repeat(2,1fr);gap:0 16px}.form-grid :deep(.el-select),.form-grid :deep(.el-input-number){width:100%}.settings button{display:grid;width:100%;grid-template-columns:38px 1fr 15px;align-items:center;gap:10px;padding:15px 0;color:var(--ink);background:none;border:0;border-bottom:1px solid var(--line);text-align:left;cursor:pointer}.settings button>span{display:grid;width:38px;height:38px;place-items:center;color:var(--mint-700);background:#eef8f4;border-radius:11px}.settings svg{width:17px}.settings button>svg{width:14px;color:var(--muted)}.settings b,.settings small{display:block}.settings small{margin-top:4px;color:var(--muted);font-size:10px}.privacy>span{display:grid;width:44px;height:44px;place-items:center;color:var(--mint-700);background:var(--mint-100);border-radius:13px}.privacy svg{width:20px}.privacy h3{margin:15px 0 6px}.privacy p{color:var(--muted);font-size:12px;line-height:1.7}.privacy :deep(.el-switch__label){font-size:11px}.danger h3{margin-top:0}.danger button{width:100%;margin-top:8px;padding:10px;color:var(--muted);background:#fafcfc;border:1px solid var(--line);border-radius:10px;cursor:pointer}.danger button:last-child{color:var(--danger)}
@media(max-width:950px){.profile-grid{grid-template-columns:1fr}}@media(max-width:600px){.profile-hero{grid-template-columns:60px 1fr}.big-avatar{width:58px;height:58px}.profile-hero>button{grid-column:1/-1}.form-grid{grid-template-columns:1fr}}
</style>
