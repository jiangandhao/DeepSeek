<template>
  <div class="shell">
    <aside class="sidebar" :class="{ open: mobileOpen }">
      <button class="brand" type="button" @click="go('/dashboard')">
        <span class="brand-mark"><Pulse /></span>
        <span><strong>知愈健康</strong><small>DeepSeek Care</small></span>
      </button>
      <nav class="nav" aria-label="主要导航">
        <button v-for="item in navItems" :key="item.path" type="button" :class="{ active: route.path === item.path }" @click="go(item.path)">
          <el-icon><component :is="item.icon" /></el-icon>
          <span><b>{{ item.title }}</b><small>{{ item.caption }}</small></span>
        </button>
      </nav>
      <div class="privacy-card">
        <Lock />
        <div><b>数据由你掌控</b><small>加密存储 · 默认不共享</small></div>
      </div>
      <button class="profile" type="button" @click="go('/profile')">
        <span class="avatar">{{ displayName.slice(0, 1) }}</span>
        <span><b>{{ displayName }}</b><small>查看健康档案</small></span>
        <ArrowRight />
      </button>
    </aside>

    <div v-if="mobileOpen" class="mask" @click="mobileOpen = false" />
    <main class="main">
      <header class="topbar">
        <button class="menu-button" type="button" aria-label="打开菜单" @click="mobileOpen = true"><Menu /></button>
        <div class="date"><span>{{ today }}</span><small>愿你今天也轻松一点</small></div>
        <div class="top-actions">
          <button class="icon-button alert-button" type="button" aria-label="查看预警" @click="go('/monitoring')"><Bell /><i /></button>
          <el-dropdown trigger="click">
            <button class="icon-button" type="button" aria-label="账户菜单"><MoreFilled /></button>
            <template #dropdown><el-dropdown-menu><el-dropdown-item @click="go('/profile')">个人档案</el-dropdown-item><el-dropdown-item divided @click="onLogout">退出登录</el-dropdown-item></el-dropdown-menu></template>
          </el-dropdown>
        </div>
      </header>
      <div class="content"><router-view /></div>
    </main>

    <button v-if="route.path !== '/assistant'" class="ai-fab" type="button" @click="go('/assistant')">
      <span><ChatDotRound /></span><b>问问 AI 健康助手</b>
    </button>
  </div>
</template>

<script setup>
import { computed, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { House, TrendCharts, Calendar, Document, User, Lock, ArrowRight, Menu, Bell, MoreFilled, ChatDotRound, FirstAidKit, Watch, Monitor } from '@element-plus/icons-vue'
import { useAuthStore } from '../store/auth'

const Pulse = FirstAidKit
const route = useRoute()
const router = useRouter()
const auth = useAuthStore()
const mobileOpen = ref(false)
const isAdmin = computed(() => auth.username === 'admin')
const baseNavItems = [
  { path: '/dashboard', title: '首页', caption: '健康概览', icon: House },
  { path: '/monitoring', title: '监测', caption: '趋势与预警', icon: TrendCharts },
  { path: '/plan', title: '方案', caption: '今日行动', icon: Calendar },
  { path: '/assistant', title: '智能体', caption: 'AI 健康助手', icon: ChatDotRound },
  { path: '/devices', title: '设备', caption: '绑定与上报', icon: Watch },
  { path: '/exam', title: '体检', caption: '预约与报告', icon: Document },
  { path: '/profile', title: '我的', caption: '健康档案', icon: User }
]
const adminNavItem = { path: '/admin', title: '运维', caption: '用户与设备', icon: Monitor }
const navItems = computed(() => isAdmin.value ? [...baseNavItems.slice(0, 5), adminNavItem, ...baseNavItems.slice(5)] : baseNavItems)
const displayName = computed(() => auth.nickname || auth.username || '健康用户')
const today = new Intl.DateTimeFormat('zh-CN', { month: 'long', day: 'numeric', weekday: 'long' }).format(new Date())
function go(path) { mobileOpen.value = false; router.push(path) }
function onLogout() { auth.logout(); router.push('/login') }
</script>

<style scoped>
.shell { min-height: 100vh; }
.sidebar { position: fixed; inset: 0 auto 0 0; z-index: 30; display: flex; width: 252px; padding: 28px 18px 20px; flex-direction: column; color: #eaf7f2; background: linear-gradient(165deg, #123d35 0%, #0b2f29 100%); }
.brand { display: flex; align-items: center; gap: 12px; padding: 0 10px 28px; color: inherit; background: none; border: 0; text-align: left; cursor: pointer; }
.brand-mark { display: grid; width: 42px; height: 42px; place-items: center; color: #123d35; background: #bfe8d9; border-radius: 14px; }
.brand-mark svg { width: 23px; }
.brand strong { display: block; font-size: 18px; letter-spacing: .05em; }
.brand small, .nav small, .profile small, .privacy-card small { display: block; margin-top: 3px; color: #91afa7; font-size: 11px; }
.nav { display: grid; gap: 7px; }
.nav button { display: flex; width: 100%; align-items: center; gap: 13px; padding: 12px 14px; color: #bad0c9; background: transparent; border: 0; border-radius: 14px; text-align: left; cursor: pointer; transition: .2s ease; }
.nav button:hover, .nav button.active { color: #fff; background: rgba(255,255,255,.1); transform: translateX(2px); }
.nav button.active { box-shadow: inset 3px 0 #79d1b6; }
.nav .el-icon { font-size: 20px; }
.nav b { font-size: 14px; }
.privacy-card { display: flex; gap: 10px; margin-top: auto; padding: 14px; color: #d9eee7; background: rgba(121,209,182,.1); border: 1px solid rgba(121,209,182,.14); border-radius: 14px; }
.privacy-card svg { width: 18px; flex: none; }
.profile { display: grid; grid-template-columns: 38px 1fr 16px; align-items: center; gap: 10px; margin-top: 14px; padding: 10px 8px; color: inherit; background: none; border: 0; text-align: left; cursor: pointer; }
.profile > svg { width: 16px; color: #91afa7; }
.avatar { display: grid; width: 38px; height: 38px; place-items: center; color: #123d35; background: #d8f2e9; border-radius: 12px; font-weight: 800; }
.main { min-height: 100vh; margin-left: 252px; }
.topbar { display: flex; height: 78px; align-items: center; justify-content: space-between; padding: 0 34px; background: rgba(244,248,246,.88); border-bottom: 1px solid rgba(222,235,230,.8); backdrop-filter: blur(14px); }
.date span { display: block; font-weight: 800; }
.date small { display: block; margin-top: 3px; color: var(--muted); }
.top-actions { display: flex; gap: 8px; }
.icon-button, .menu-button { position: relative; display: grid; width: 40px; height: 40px; place-items: center; color: var(--ink); background: #fff; border: 1px solid var(--line); border-radius: 12px; cursor: pointer; }
.icon-button svg, .menu-button svg { width: 18px; }
.alert-button i { position: absolute; top: 8px; right: 8px; width: 7px; height: 7px; background: var(--danger); border: 2px solid #fff; border-radius: 50%; }
.menu-button { display: none; }
.content { padding: 30px 34px 110px; }
.ai-fab { position: fixed; z-index: 25; right: 28px; bottom: 25px; display: flex; align-items: center; gap: 10px; padding: 10px 16px 10px 10px; color: #fff; background: var(--mint-900); border: 0; border-radius: 999px; box-shadow: 0 14px 36px rgba(18,61,53,.28); cursor: pointer; }
.ai-fab span { display: grid; width: 35px; height: 35px; place-items: center; color: var(--mint-900); background: #bfe8d9; border-radius: 50%; }
.ai-fab svg { width: 19px; }
.mask { display: none; }
@media (max-width: 900px) {
  .sidebar { transform: translateX(-100%); transition: transform .25s ease; }
  .sidebar.open { transform: translateX(0); }
  .mask { position: fixed; inset: 0; z-index: 29; display: block; background: rgba(7,32,27,.38); }
  .main { margin-left: 0; }
  .menu-button { display: grid; }
  .topbar { height: 68px; padding: 0 18px; }
  .date small { display: none; }
  .content { padding: 22px 16px 100px; }
  .ai-fab b { display: none; }
  .ai-fab { right: 18px; bottom: 18px; padding: 8px; }
}
</style>
