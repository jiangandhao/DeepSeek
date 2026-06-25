<template>
  <div class="auth-wrap">
    <!-- 背景装饰 -->
    <div class="bg-decor" aria-hidden="true">
      <span class="blob blob-1" />
      <span class="blob blob-2" />
      <span class="blob blob-3" />
      <span class="grid-dots" />
    </div>

    <div class="auth-shell">
      <!-- 品牌侧栏 -->
      <aside class="auth-aside">
        <div class="rings" aria-hidden="true"><i /><i /><i /></div>
        <button class="brand" type="button" @click="$router.push('/')">
          <span class="brand-mark"><FirstAidKit /></span>
          <span class="brand-text"><strong>知愈健康</strong><small>DeepSeek Care</small></span>
        </button>
        <div class="aside-copy">
          <h2>开启健康管理 🌱</h2>
          <p>创建账号，连接你的设备与数据，让 AI 帮你把健康目标变成每日行动。</p>
        </div>
        <ul class="aside-points">
          <li><span><TrendCharts /></span>实时监测与异常预警</li>
          <li><span><Calendar /></span>个性化每日健康方案</li>
          <li><span><ChatDotRound /></span>AI 健康助手随时答疑</li>
        </ul>
        <div class="aside-foot"><Lock /> 数据加密存储 · 默认不共享</div>
      </aside>

      <!-- 表单 -->
      <section class="auth-card">
        <div class="card-head">
          <div class="brand-mark mobile-only"><FirstAidKit /></div>
          <h1 class="title">注册账号</h1>
          <p class="subtitle">几秒钟创建你的专属健康档案</p>
        </div>
        <el-form :model="form" @submit.prevent>
          <el-form-item>
            <el-input v-model="form.username" size="large" placeholder="用户名(3-32位)" :prefix-icon="User" />
          </el-form-item>
          <el-form-item>
            <el-input v-model="form.nickname" size="large" placeholder="昵称(可选)" :prefix-icon="Avatar" />
          </el-form-item>
          <el-form-item>
            <el-input v-model="form.password" size="large" type="password" placeholder="密码(6-32位)" :prefix-icon="Lock" show-password @keyup.enter="onRegister" />
          </el-form-item>
          <el-button type="primary" size="large" :loading="loading" style="width: 100%" @click="onRegister">注册</el-button>
        </el-form>
        <div class="foot">已有账号?<router-link to="/login">去登录</router-link></div>
      </section>
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { FirstAidKit, User, Lock, Avatar, TrendCharts, Calendar, ChatDotRound } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import { register } from '../api'
import { useAuthStore } from '../store/auth'

const router = useRouter()
const auth = useAuthStore()
const form = ref({ username: '', password: '', nickname: '' })
const loading = ref(false)

async function onRegister() {
  loading.value = true
  try {
    const data = await register(form.value)
    auth.setAuth(data)
    ElMessage.success('注册成功')
    router.push('/dashboard')
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.auth-wrap {
  position: relative;
  min-height: 100vh;
  width: 100%;
  display: grid;
  place-items: center;
  padding: 32px 16px;
  overflow: hidden;
  background:
    radial-gradient(circle at 18% 18%, rgba(53, 168, 137, .14), transparent 32%),
    linear-gradient(135deg, #eef8f4 0%, #f7fbfa 48%, #ecf4ff 100%);
}

/* 背景装饰 */
.bg-decor { position: absolute; inset: 0; z-index: 0; pointer-events: none; }
.blob { position: absolute; border-radius: 50%; filter: blur(60px); opacity: .55; }
.blob-1 { top: -120px; left: -80px; width: 360px; height: 360px; background: radial-gradient(circle, rgba(53,168,137,.55), transparent 70%); }
.blob-2 { bottom: -140px; right: -100px; width: 420px; height: 420px; background: radial-gradient(circle, rgba(120,180,255,.45), transparent 70%); }
.blob-3 { top: 40%; right: 18%; width: 220px; height: 220px; background: radial-gradient(circle, rgba(121,209,182,.4), transparent 70%); }
.grid-dots { position: absolute; inset: 0; opacity: .5;
  background-image: radial-gradient(rgba(33, 107, 93, .12) 1.2px, transparent 1.2px);
  background-size: 26px 26px;
  mask-image: radial-gradient(circle at 50% 50%, #000 30%, transparent 72%);
  -webkit-mask-image: radial-gradient(circle at 50% 50%, #000 30%, transparent 72%); }

/* 卡片容器 */
.auth-shell {
  position: relative;
  z-index: 1;
  display: grid;
  grid-template-columns: 1fr 1fr;
  width: 100%;
  max-width: 920px;
  background: rgba(255, 255, 255, .82);
  border: 1px solid rgba(255, 255, 255, .6);
  border-radius: 26px;
  box-shadow: 0 30px 70px rgba(18, 61, 53, .16);
  backdrop-filter: blur(8px);
  overflow: hidden;
}

/* 品牌侧栏 */
.auth-aside {
  position: relative;
  display: flex;
  flex-direction: column;
  padding: 44px 40px;
  color: #eaf7f2;
  background: linear-gradient(165deg, #123d35 0%, #0b2f29 100%);
  overflow: hidden;
}
.rings { position: absolute; top: -90px; right: -90px; opacity: .5; }
.rings i { position: absolute; top: 0; right: 0; border: 1px solid rgba(191, 232, 217, .35); border-radius: 50%; }
.rings i:nth-child(1) { width: 200px; height: 200px; }
.rings i:nth-child(2) { width: 300px; height: 300px; top: -50px; right: -50px; }
.rings i:nth-child(3) { width: 400px; height: 400px; top: -100px; right: -100px; }
.brand { position: relative; display: flex; align-items: center; gap: 12px; padding: 0; color: inherit; background: none; border: 0; cursor: pointer; }
.brand-mark { display: grid; width: 46px; height: 46px; place-items: center; color: #123d35; background: #bfe8d9; border-radius: 14px; }
.brand-mark svg { width: 24px; }
.brand-text strong { display: block; font-size: 18px; letter-spacing: .05em; }
.brand-text small { display: block; margin-top: 2px; color: #91afa7; font-size: 11px; }
.aside-copy { position: relative; margin: 40px 0 28px; }
.aside-copy h2 { margin: 0 0 10px; font-size: 26px; font-weight: 800; letter-spacing: -.02em; }
.aside-copy p { margin: 0; max-width: 300px; color: #b9d6cd; font-size: 14px; line-height: 1.7; }
.aside-points { position: relative; display: grid; gap: 14px; margin: 0 0 auto; padding: 0; list-style: none; }
.aside-points li { display: flex; align-items: center; gap: 12px; font-size: 14px; color: #dff0ea; }
.aside-points span { display: grid; width: 34px; height: 34px; place-items: center; color: #bfe8d9; background: rgba(121, 209, 182, .14); border: 1px solid rgba(121, 209, 182, .18); border-radius: 11px; flex: none; }
.aside-points svg { width: 17px; }
.aside-foot { position: relative; display: flex; align-items: center; gap: 8px; margin-top: 28px; color: #9ec4b9; font-size: 12px; }
.aside-foot svg { width: 14px; }

/* 表单面板 */
.auth-card { display: flex; flex-direction: column; justify-content: center; padding: 48px 44px; min-width: 0; }
.card-head { margin-bottom: 26px; }
.brand-mark.mobile-only { display: none; margin: 0 0 16px; }
.title { margin: 0; color: var(--ink); font-size: 26px; line-height: 1.2; font-weight: 800; letter-spacing: -.02em; }
.subtitle { margin: 8px 0 0; color: var(--muted); font-size: 14px; }
.auth-card :deep(.el-input__wrapper) { min-height: 46px; border-radius: 12px; }
.auth-card :deep(.el-form-item) { margin-bottom: 20px; }
.auth-card :deep(.el-button) { min-height: 46px; border-radius: 12px; font-weight: 700; }
.foot { text-align: center; margin-top: 22px; font-size: 14px; color: var(--muted); }
.foot a { margin-left: 6px; color: var(--mint-700); font-weight: 700; text-decoration: none; }
.foot a:hover { color: var(--mint-500); }

/* 响应式 */
@media (max-width: 860px) {
  .auth-shell { grid-template-columns: 1fr; max-width: 420px; }
  .auth-aside { display: none; }
  .brand-mark.mobile-only { display: grid; }
  .card-head { text-align: center; }
  .brand-mark.mobile-only { margin: 0 auto 16px; }
  .auth-card { padding: 36px 30px; }
}
@media (max-width: 420px) {
  .auth-wrap { padding: 16px; }
  .auth-card { padding: 30px 22px; }
  .title { font-size: 23px; }
}
</style>
