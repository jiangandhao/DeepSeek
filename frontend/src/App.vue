<template>
  <div id="app">
    <!-- 登录/注册页面不显示导航 -->
    <router-view v-if="!showLayout" />

    <!-- 主布局 -->
    <div v-else class="app-layout">
      <!-- 侧边导航栏 -->
      <aside class="sidebar" :class="{ collapsed: sidebarCollapsed }">
        <div class="sidebar-header">
          <div class="logo-mark">
            <svg viewBox="0 0 40 40" class="logo-svg">
              <circle cx="20" cy="20" r="18" fill="none" stroke="currentColor" stroke-width="1.5" />
              <path d="M20 8 C14 14, 14 26, 20 32 C26 26, 26 14, 20 8Z" fill="currentColor" opacity="0.3" />
              <circle cx="20" cy="20" r="4" fill="currentColor" />
            </svg>
          </div>
          <transition name="fade">
            <div v-if="!sidebarCollapsed" class="logo-text">
              <span class="brand-name">Vitalis</span>
              <span class="brand-tagline">Health Intelligence</span>
            </div>
          </transition>
        </div>

        <nav class="sidebar-nav">
          <router-link
            v-for="item in navItems"
            :key="item.path"
            :to="item.path"
            class="nav-item"
            :class="{ active: isActive(item.path) }"
          >
            <div class="nav-icon" v-html="item.icon"></div>
            <transition name="fade">
              <span v-if="!sidebarCollapsed" class="nav-label">{{ item.label }}</span>
            </transition>
            <transition name="fade">
              <span v-if="!sidebarCollapsed && item.badge" class="nav-badge">{{ item.badge }}</span>
            </transition>
          </router-link>
        </nav>

        <div class="sidebar-footer">
          <button class="collapse-btn" @click="toggleSidebar">
            <svg viewBox="0 0 24 24" width="18" height="18" fill="none" stroke="currentColor" stroke-width="2">
              <path v-if="!sidebarCollapsed" d="M15 18l-6-6 6-6" />
              <path v-else d="M9 18l6-6-6-6" />
            </svg>
          </button>
        </div>
      </aside>

      <!-- 主内容区 -->
      <div class="main-area">
        <!-- 顶部栏 -->
        <header class="top-bar">
          <div class="top-bar-left">
            <h2 class="page-title">{{ currentPageTitle }}</h2>
            <div class="breadcrumb">
              <span class="breadcrumb-item">首页</span>
              <span v-if="currentPageTitle" class="breadcrumb-sep">/</span>
              <span v-if="currentPageTitle" class="breadcrumb-item active">{{ currentPageTitle }}</span>
            </div>
          </div>
          <div class="top-bar-right">
            <div class="search-box">
              <svg viewBox="0 0 24 24" width="16" height="16" fill="none" stroke="currentColor" stroke-width="2">
                <circle cx="11" cy="11" r="8" /><path d="M21 21l-4.35-4.35" />
              </svg>
              <input type="text" placeholder="搜索健康数据..." />
            </div>
            <button class="icon-btn notification-btn">
              <svg viewBox="0 0 24 24" width="20" height="20" fill="none" stroke="currentColor" stroke-width="2">
                <path d="M18 8A6 6 0 0 0 6 8c0 7-3 9-3 9h18s-3-2-3-9" />
                <path d="M13.73 21a2 2 0 0 1-3.46 0" />
              </svg>
              <span class="notification-dot"></span>
            </button>
            <div class="user-avatar" @click="toggleUserMenu">
              <div class="avatar-circle">
                <span>{{ usernameInitial }}</span>
              </div>
              <div class="user-status"></div>
            </div>
            <!-- 用户菜单 -->
            <transition name="dropdown">
              <div v-if="showUserMenu" class="user-dropdown" @click.stop>
                <div class="dropdown-header">
                  <div class="dropdown-avatar">
                    <span>{{ usernameInitial }}</span>
                  </div>
                  <div class="dropdown-info">
                    <span class="dropdown-name">{{ username }}</span>
                    <span class="dropdown-role">健康管理师</span>
                  </div>
                </div>
                <div class="dropdown-divider"></div>
                <a class="dropdown-item" @click="goToProfile">
                  <svg viewBox="0 0 24 24" width="16" height="16" fill="none" stroke="currentColor" stroke-width="2">
                    <path d="M20 21v-2a4 4 0 0 0-4-4H8a4 4 0 0 0-4 4v2" /><circle cx="12" cy="7" r="4" />
                  </svg>
                  个人中心
                </a>
                <a class="dropdown-item" @click="logout">
                  <svg viewBox="0 0 24 24" width="16" height="16" fill="none" stroke="currentColor" stroke-width="2">
                    <path d="M9 21H5a2 2 0 0 1-2-2V5a2 2 0 0 1 2-2h4" /><polyline points="16 17 21 12 16 7" /><line x1="21" y1="12" x2="9" y2="12" />
                  </svg>
                  退出登录
                </a>
              </div>
            </transition>
          </div>
        </header>

        <!-- 页面内容 -->
        <main class="content-area">
          <router-view v-slot="{ Component }">
            <transition name="page" mode="out-in">
              <component :is="Component" />
            </transition>
          </router-view>
        </main>
      </div>
    </div>

    <!-- 点击外部关闭菜单 -->
    <div v-if="showUserMenu" class="overlay" @click="showUserMenu = false"></div>
  </div>
</template>

<script>
import { ref, computed, onMounted, onUnmounted } from 'vue'
import { useRouter } from 'vue-router'

export default {
  name: 'App',
  setup() {
    const router = useRouter()
    const username = ref('用户')
    const sidebarCollapsed = ref(false)
    const showUserMenu = ref(false)

    const noLayoutRoutes = ['/login', '/register']
    const showLayout = computed(() => {
      return !noLayoutRoutes.includes(router.currentRoute.value.path)
    })

    const usernameInitial = computed(() => {
      return username.value ? username.value.charAt(0).toUpperCase() : 'U'
    })

    const navItems = [
      {
        path: '/dashboard',
        label: '健康概览',
        icon: '<svg viewBox="0 0 24 24" width="20" height="20" fill="none" stroke="currentColor" stroke-width="1.5"><rect x="3" y="3" width="7" height="7" rx="1"/><rect x="14" y="3" width="7" height="7" rx="1"/><rect x="3" y="14" width="7" height="7" rx="1"/><rect x="14" y="14" width="7" height="7" rx="1"/></svg>'
      },
      {
        path: '/blood-sugar',
        label: '血糖管理',
        icon: '<svg viewBox="0 0 24 24" width="20" height="20" fill="none" stroke="currentColor" stroke-width="1.5"><path d="M22 12h-4l-3 9L9 3l-3 9H2"/></svg>'
      },
      {
        path: '/health-manager',
        label: 'AI 健管师',
        badge: 'AI',
        icon: '<svg viewBox="0 0 24 24" width="20" height="20" fill="none" stroke="currentColor" stroke-width="1.5"><path d="M12 2a10 10 0 1 0 10 10A10 10 0 0 0 12 2z"/><path d="M12 6v6l4 2"/></svg>'
      },
      {
        path: '/checkup',
        label: '智能体检',
        icon: '<svg viewBox="0 0 24 24" width="20" height="20" fill="none" stroke="currentColor" stroke-width="1.5"><path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z"/><polyline points="14 2 14 8 20 8"/><line x1="16" y1="13" x2="8" y2="13"/><line x1="16" y1="17" x2="8" y2="17"/></svg>'
      },
      {
        path: '/risk-warning',
        label: '风险预警',
        badge: '3',
        icon: '<svg viewBox="0 0 24 24" width="20" height="20" fill="none" stroke="currentColor" stroke-width="1.5"><path d="M10.29 3.86L1.82 18a2 2 0 0 0 1.71 3h16.94a2 2 0 0 0 1.71-3L13.71 3.86a2 2 0 0 0-3.42 0z"/><line x1="12" y1="9" x2="12" y2="13"/><line x1="12" y1="17" x2="12.01" y2="17"/></svg>'
      }
    ]

    const currentPageTitle = computed(() => {
      const path = router.currentRoute.value.path
      const item = navItems.find(n => path.startsWith(n.path))
      return item ? item.label : ''
    })

    const isActive = (path) => {
      return router.currentRoute.value.path.startsWith(path)
    }

    const toggleSidebar = () => {
      sidebarCollapsed.value = !sidebarCollapsed.value
    }

    const toggleUserMenu = () => {
      showUserMenu.value = !showUserMenu.value
    }

    const goToProfile = () => {
      showUserMenu.value = false
      router.push('/profile')
    }

    const logout = () => {
      showUserMenu.value = false
      localStorage.removeItem('token')
      router.push('/login')
    }

    // 点击外部关闭菜单
    const handleClickOutside = (e) => {
      if (!e.target.closest('.user-avatar') && !e.target.closest('.user-dropdown')) {
        showUserMenu.value = false
      }
    }

    onMounted(() => {
      document.addEventListener('click', handleClickOutside)
    })

    onUnmounted(() => {
      document.removeEventListener('click', handleClickOutside)
    })

    return {
      username, sidebarCollapsed, showUserMenu, showLayout,
      usernameInitial, navItems, currentPageTitle,
      isActive, toggleSidebar, toggleUserMenu, goToProfile, logout
    }
  }
}
</script>

<style>
/* ============================================
   VITALIS HEALTH DESIGN SYSTEM
   Medical Luxury × Organic Wellness
   ============================================ */

@import url('https://fonts.googleapis.com/css2?family=Cormorant+Garamond:ital,wght@0,400;0,500;0,600;0,700;1,400&family=DM+Sans:ital,opsz,wght@0,9..40,300;0,9..40,400;0,9..40,500;0,9..40,600;0,9..40,700;1,9..40,400&display=swap');

:root {
  /* Primary Palette - Deep Emerald */
  --color-primary: #1a6b5a;
  --color-primary-light: #2d8f7a;
  --color-primary-dark: #0f4d40;
  --color-primary-muted: #1a6b5a20;

  /* Accent - Warm Gold */
  --color-accent: #c4956a;
  --color-accent-light: #d4ad8a;
  --color-accent-dark: #a67b52;

  /* Neutral Palette */
  --color-bg: #faf8f5;
  --color-bg-warm: #f5f0ea;
  --color-bg-card: #ffffff;
  --color-bg-sidebar: #0f2b25;

  /* Text */
  --color-text: #2c2c2c;
  --color-text-secondary: #6b6b6b;
  --color-text-muted: #9a9a9a;
  --color-text-inverse: #f5f0ea;

  /* Semantic */
  --color-success: #4a9d7e;
  --color-warning: #d4a843;
  --color-danger: #c75450;
  --color-info: #5a8fbf;

  /* Typography */
  --font-display: 'Cormorant Garamond', Georgia, serif;
  --font-body: 'DM Sans', -apple-system, BlinkMacSystemFont, sans-serif;

  /* Spacing */
  --space-xs: 4px;
  --space-sm: 8px;
  --space-md: 16px;
  --space-lg: 24px;
  --space-xl: 32px;
  --space-2xl: 48px;
  --space-3xl: 64px;

  /* Radius */
  --radius-sm: 6px;
  --radius-md: 10px;
  --radius-lg: 16px;
  --radius-xl: 24px;
  --radius-full: 9999px;

  /* Shadows */
  --shadow-sm: 0 1px 3px rgba(15, 43, 37, 0.06);
  --shadow-md: 0 4px 12px rgba(15, 43, 37, 0.08);
  --shadow-lg: 0 8px 30px rgba(15, 43, 37, 0.12);
  --shadow-glow: 0 0 20px rgba(26, 107, 90, 0.15);

  /* Transitions */
  --ease-out: cubic-bezier(0.16, 1, 0.3, 1);
  --ease-spring: cubic-bezier(0.34, 1.56, 0.64, 1);

  /* Sidebar */
  --sidebar-width: 260px;
  --sidebar-collapsed: 72px;
}

/* ============================================
   RESET & BASE
   ============================================ */
*, *::before, *::after {
  margin: 0;
  padding: 0;
  box-sizing: border-box;
}

html {
  font-size: 16px;
  -webkit-font-smoothing: antialiased;
  -moz-osx-font-smoothing: grayscale;
}

body {
  font-family: var(--font-body);
  color: var(--color-text);
  background-color: var(--color-bg);
  line-height: 1.6;
  overflow-x: hidden;
}

/* Subtle noise texture overlay */
body::before {
  content: '';
  position: fixed;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  opacity: 0.015;
  z-index: 9999;
  pointer-events: none;
  background-image: url("data:image/svg+xml,%3Csvg viewBox='0 0 256 256' xmlns='http://www.w3.org/2000/svg'%3E%3Cfilter id='noiseFilter'%3E%3CfeTurbulence type='fractalNoise' baseFrequency='0.9' numOctaves='4' stitchTiles='stitch'/%3E%3C/filter%3E%3Crect width='100%25' height='100%25' filter='url(%23noiseFilter)'/%3E%3C/svg%3E");
}

#app {
  min-height: 100vh;
}

a {
  text-decoration: none;
  color: inherit;
}

/* ============================================
   LAYOUT
   ============================================ */
.app-layout {
  display: flex;
  min-height: 100vh;
}

/* ============================================
   SIDEBAR
   ============================================ */
.sidebar {
  width: var(--sidebar-width);
  background: var(--color-bg-sidebar);
  display: flex;
  flex-direction: column;
  position: fixed;
  top: 0;
  left: 0;
  height: 100vh;
  z-index: 100;
  transition: width 0.4s var(--ease-out);
  overflow: hidden;
}

.sidebar::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background:
    radial-gradient(ellipse at 20% 80%, rgba(26, 107, 90, 0.15) 0%, transparent 60%),
    radial-gradient(ellipse at 80% 20%, rgba(196, 149, 106, 0.08) 0%, transparent 50%);
  pointer-events: none;
}

.sidebar.collapsed {
  width: var(--sidebar-collapsed);
}

.sidebar-header {
  padding: var(--space-lg);
  display: flex;
  align-items: center;
  gap: var(--space-md);
  position: relative;
}

.logo-mark {
  width: 36px;
  height: 36px;
  flex-shrink: 0;
  color: var(--color-accent);
  animation: breathe 4s ease-in-out infinite;
}

@keyframes breathe {
  0%, 100% { opacity: 0.8; transform: scale(1); }
  50% { opacity: 1; transform: scale(1.05); }
}

.logo-svg {
  width: 100%;
  height: 100%;
}

.logo-text {
  display: flex;
  flex-direction: column;
  white-space: nowrap;
}

.brand-name {
  font-family: var(--font-display);
  font-size: 1.4rem;
  font-weight: 600;
  color: var(--color-text-inverse);
  letter-spacing: 0.02em;
  line-height: 1.2;
}

.brand-tagline {
  font-size: 0.65rem;
  color: var(--color-accent);
  text-transform: uppercase;
  letter-spacing: 0.15em;
  font-weight: 500;
}

.sidebar-nav {
  flex: 1;
  padding: var(--space-md) var(--space-sm);
  display: flex;
  flex-direction: column;
  gap: var(--space-xs);
  overflow-y: auto;
}

.nav-item {
  display: flex;
  align-items: center;
  gap: var(--space-md);
  padding: var(--space-md) var(--space-md);
  border-radius: var(--radius-md);
  color: rgba(245, 240, 234, 0.6);
  transition: all 0.3s var(--ease-out);
  position: relative;
  cursor: pointer;
  white-space: nowrap;
}

.nav-item:hover {
  color: var(--color-text-inverse);
  background: rgba(255, 255, 255, 0.06);
}

.nav-item.active {
  color: var(--color-text-inverse);
  background: rgba(26, 107, 90, 0.3);
}

.nav-item.active::before {
  content: '';
  position: absolute;
  left: 0;
  top: 50%;
  transform: translateY(-50%);
  width: 3px;
  height: 60%;
  background: var(--color-accent);
  border-radius: 0 var(--radius-full) var(--radius-full) 0;
}

.nav-icon {
  width: 20px;
  height: 20px;
  flex-shrink: 0;
  display: flex;
  align-items: center;
  justify-content: center;
}

.nav-icon svg {
  width: 100%;
  height: 100%;
}

.nav-label {
  font-size: 0.9rem;
  font-weight: 500;
}

.nav-badge {
  margin-left: auto;
  background: var(--color-accent);
  color: var(--color-bg-sidebar);
  font-size: 0.65rem;
  font-weight: 700;
  padding: 2px 8px;
  border-radius: var(--radius-full);
  letter-spacing: 0.05em;
}

.sidebar-footer {
  padding: var(--space-md);
  border-top: 1px solid rgba(255, 255, 255, 0.06);
}

.collapse-btn {
  width: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: var(--space-sm);
  background: none;
  border: none;
  color: rgba(245, 240, 234, 0.4);
  cursor: pointer;
  border-radius: var(--radius-sm);
  transition: all 0.2s;
}

.collapse-btn:hover {
  color: var(--color-text-inverse);
  background: rgba(255, 255, 255, 0.06);
}

/* ============================================
   MAIN AREA
   ============================================ */
.main-area {
  flex: 1;
  margin-left: var(--sidebar-width);
  transition: margin-left 0.4s var(--ease-out);
  display: flex;
  flex-direction: column;
  min-height: 100vh;
}

.sidebar.collapsed ~ .main-area {
  margin-left: var(--sidebar-collapsed);
}

/* ============================================
   TOP BAR
   ============================================ */
.top-bar {
  height: 72px;
  background: var(--color-bg-card);
  border-bottom: 1px solid rgba(0, 0, 0, 0.04);
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 var(--space-xl);
  position: sticky;
  top: 0;
  z-index: 50;
  backdrop-filter: blur(12px);
  background: rgba(255, 255, 255, 0.85);
}

.top-bar-left {
  display: flex;
  flex-direction: column;
  gap: 2px;
}

.page-title {
  font-family: var(--font-display);
  font-size: 1.3rem;
  font-weight: 600;
  color: var(--color-text);
  letter-spacing: 0.01em;
}

.breadcrumb {
  display: flex;
  align-items: center;
  gap: var(--space-xs);
  font-size: 0.75rem;
  color: var(--color-text-muted);
}

.breadcrumb-sep {
  opacity: 0.4;
}

.breadcrumb-item.active {
  color: var(--color-primary);
}

.top-bar-right {
  display: flex;
  align-items: center;
  gap: var(--space-lg);
}

.search-box {
  display: flex;
  align-items: center;
  gap: var(--space-sm);
  background: var(--color-bg);
  padding: var(--space-sm) var(--space-md);
  border-radius: var(--radius-full);
  border: 1px solid transparent;
  transition: all 0.3s var(--ease-out);
}

.search-box:focus-within {
  border-color: var(--color-primary);
  box-shadow: var(--shadow-glow);
  background: var(--color-bg-card);
}

.search-box svg {
  color: var(--color-text-muted);
  flex-shrink: 0;
}

.search-box input {
  border: none;
  background: none;
  outline: none;
  font-family: var(--font-body);
  font-size: 0.85rem;
  color: var(--color-text);
  width: 180px;
}

.search-box input::placeholder {
  color: var(--color-text-muted);
}

.icon-btn {
  position: relative;
  width: 40px;
  height: 40px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: none;
  border: none;
  border-radius: var(--radius-full);
  color: var(--color-text-secondary);
  cursor: pointer;
  transition: all 0.2s;
}

.icon-btn:hover {
  background: var(--color-bg);
  color: var(--color-text);
}

.notification-dot {
  position: absolute;
  top: 8px;
  right: 8px;
  width: 8px;
  height: 8px;
  background: var(--color-danger);
  border-radius: 50%;
  border: 2px solid var(--color-bg-card);
  animation: pulse 2s infinite;
}

@keyframes pulse {
  0%, 100% { opacity: 1; }
  50% { opacity: 0.5; }
}

.user-avatar {
  position: relative;
  cursor: pointer;
}

.avatar-circle {
  width: 36px;
  height: 36px;
  border-radius: 50%;
  background: linear-gradient(135deg, var(--color-primary), var(--color-primary-light));
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
  font-weight: 600;
  font-size: 0.85rem;
  transition: transform 0.2s var(--ease-spring);
}

.user-avatar:hover .avatar-circle {
  transform: scale(1.08);
}

.user-status {
  position: absolute;
  bottom: 0;
  right: 0;
  width: 10px;
  height: 10px;
  background: var(--color-success);
  border-radius: 50%;
  border: 2px solid var(--color-bg-card);
}

/* ============================================
   USER DROPDOWN
   ============================================ */
.user-dropdown {
  position: absolute;
  top: calc(100% + 8px);
  right: 0;
  width: 240px;
  background: var(--color-bg-card);
  border-radius: var(--radius-lg);
  box-shadow: var(--shadow-lg);
  border: 1px solid rgba(0, 0, 0, 0.06);
  z-index: 200;
  overflow: hidden;
}

.dropdown-header {
  display: flex;
  align-items: center;
  gap: var(--space-md);
  padding: var(--space-lg);
  background: var(--color-bg-warm);
}

.dropdown-avatar {
  width: 40px;
  height: 40px;
  border-radius: 50%;
  background: linear-gradient(135deg, var(--color-primary), var(--color-primary-light));
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
  font-weight: 600;
  font-size: 0.9rem;
}

.dropdown-info {
  display: flex;
  flex-direction: column;
}

.dropdown-name {
  font-weight: 600;
  font-size: 0.9rem;
}

.dropdown-role {
  font-size: 0.75rem;
  color: var(--color-text-muted);
}

.dropdown-divider {
  height: 1px;
  background: rgba(0, 0, 0, 0.06);
}

.dropdown-item {
  display: flex;
  align-items: center;
  gap: var(--space-md);
  padding: var(--space-md) var(--space-lg);
  font-size: 0.85rem;
  color: var(--color-text-secondary);
  cursor: pointer;
  transition: all 0.2s;
}

.dropdown-item:hover {
  background: var(--color-bg);
  color: var(--color-text);
}

.dropdown-item:last-child {
  color: var(--color-danger);
}

.dropdown-item:last-child:hover {
  background: rgba(199, 84, 80, 0.06);
}

/* ============================================
   CONTENT AREA
   ============================================ */
.content-area {
  flex: 1;
  padding: var(--space-xl);
  background: var(--color-bg);
  position: relative;
}

/* Organic background shape */
.content-area::before {
  content: '';
  position: fixed;
  top: -200px;
  right: -200px;
  width: 600px;
  height: 600px;
  background: radial-gradient(circle, rgba(26, 107, 90, 0.03) 0%, transparent 70%);
  pointer-events: none;
  z-index: 0;
}

/* ============================================
   TRANSITIONS
   ============================================ */
.fade-enter-active,
.fade-leave-active {
  transition: opacity 0.2s var(--ease-out);
}

.fade-enter-from,
.fade-leave-to {
  opacity: 0;
}

.page-enter-active {
  transition: all 0.4s var(--ease-out);
}

.page-leave-active {
  transition: all 0.2s var(--ease-out);
}

.page-enter-from {
  opacity: 0;
  transform: translateY(12px);
}

.page-leave-to {
  opacity: 0;
  transform: translateY(-4px);
}

.dropdown-enter-active {
  transition: all 0.3s var(--ease-spring);
}

.dropdown-leave-active {
  transition: all 0.2s var(--ease-out);
}

.dropdown-enter-from {
  opacity: 0;
  transform: translateY(-8px) scale(0.96);
}

.dropdown-leave-to {
  opacity: 0;
  transform: translateY(-4px);
}

.overlay {
  position: fixed;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  z-index: 99;
}

/* ============================================
   SCROLLBAR
   ============================================ */
::-webkit-scrollbar {
  width: 6px;
}

::-webkit-scrollbar-track {
  background: transparent;
}

::-webkit-scrollbar-thumb {
  background: rgba(0, 0, 0, 0.12);
  border-radius: var(--radius-full);
}

::-webkit-scrollbar-thumb:hover {
  background: rgba(0, 0, 0, 0.2);
}

/* ============================================
   ELEMENT PLUS OVERRIDES
   ============================================ */
.el-card {
  border: none;
  border-radius: var(--radius-lg);
  box-shadow: var(--shadow-sm);
}

.el-button--primary {
  background: var(--color-primary);
  border-color: var(--color-primary);
}

.el-button--primary:hover {
  background: var(--color-primary-light);
  border-color: var(--color-primary-light);
}

/* Responsive */
@media (max-width: 768px) {
  .sidebar {
    transform: translateX(-100%);
  }

  .main-area {
    margin-left: 0;
  }

  .search-box {
    display: none;
  }
}
</style>
