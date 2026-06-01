<template>
  <div class="login-page">
    <!-- 背景装饰 -->
    <div class="bg-decoration">
      <div class="bg-circle bg-circle-1"></div>
      <div class="bg-circle bg-circle-2"></div>
      <div class="bg-circle bg-circle-3"></div>
      <div class="bg-line bg-line-1"></div>
      <div class="bg-line bg-line-2"></div>
    </div>

    <!-- 左侧品牌区 -->
    <div class="brand-section">
      <div class="brand-content">
        <div class="brand-logo">
          <svg viewBox="0 0 60 60" class="logo-svg">
            <circle cx="30" cy="30" r="27" fill="none" stroke="currentColor" stroke-width="1" />
            <path d="M30 12 C22 20, 22 40, 30 48 C38 40, 38 20, 30 12Z" fill="currentColor" opacity="0.2" />
            <circle cx="30" cy="30" r="6" fill="currentColor" />
            <circle cx="30" cy="30" r="12" fill="none" stroke="currentColor" stroke-width="0.5" opacity="0.5" />
          </svg>
        </div>
        <h1 class="brand-title">Vitalis</h1>
        <p class="brand-subtitle">Health Intelligence System</p>
        <div class="brand-divider"></div>
        <p class="brand-description">
          融合前沿 AI 技术与专业医学知识<br />
          为您打造个性化的全周期健康管理方案
        </p>
        <div class="brand-features">
          <div class="feature-item">
            <div class="feature-icon">
              <svg viewBox="0 0 24 24" width="18" height="18" fill="none" stroke="currentColor" stroke-width="1.5">
                <path d="M22 12h-4l-3 9L9 3l-3 9H2" />
              </svg>
            </div>
            <span>智能血糖监测</span>
          </div>
          <div class="feature-item">
            <div class="feature-icon">
              <svg viewBox="0 0 24 24" width="18" height="18" fill="none" stroke="currentColor" stroke-width="1.5">
                <path d="M12 2a10 10 0 1 0 10 10A10 10 0 0 0 12 2z" />
                <path d="M12 6v6l4 2" />
              </svg>
            </div>
            <span>AI 健康管家</span>
          </div>
          <div class="feature-item">
            <div class="feature-icon">
              <svg viewBox="0 0 24 24" width="18" height="18" fill="none" stroke="currentColor" stroke-width="1.5">
                <path d="M10.29 3.86L1.82 18a2 2 0 0 0 1.71 3h16.94a2 2 0 0 0 1.71-3L13.71 3.86a2 2 0 0 0-3.42 0z" />
                <line x1="12" y1="9" x2="12" y2="13" />
                <line x1="12" y1="17" x2="12.01" y2="17" />
              </svg>
            </div>
            <span>风险预警系统</span>
          </div>
        </div>
      </div>
      <!-- 装饰线条 -->
      <div class="brand-deco">
        <svg viewBox="0 0 200 400" class="deco-svg">
          <path d="M100 0 Q150 100, 100 200 Q50 300, 100 400" fill="none" stroke="currentColor" stroke-width="0.5" opacity="0.2" />
          <path d="M120 0 Q170 100, 120 200 Q70 300, 120 400" fill="none" stroke="currentColor" stroke-width="0.3" opacity="0.15" />
        </svg>
      </div>
    </div>

    <!-- 右侧登录区 -->
    <div class="form-section">
      <div class="form-container">
        <!-- 顶部切换 -->
        <div class="form-header">
          <div class="form-logo-mobile">
            <svg viewBox="0 0 40 40" class="logo-svg">
              <circle cx="20" cy="20" r="18" fill="none" stroke="currentColor" stroke-width="1.5" />
              <path d="M20 8 C14 14, 14 26, 20 32 C26 26, 26 14, 20 8Z" fill="currentColor" opacity="0.3" />
              <circle cx="20" cy="20" r="4" fill="currentColor" />
            </svg>
          </div>
          <h2 class="form-title">{{ isLogin ? '欢迎回来' : '创建账户' }}</h2>
          <p class="form-subtitle">
            {{ isLogin ? '登录您的健康管理中心' : '开启您的健康管理之旅' }}
          </p>
        </div>

        <!-- 登录表单 -->
        <form v-if="isLogin" class="auth-form" @submit.prevent="handleLogin">
          <div class="form-group">
            <label class="form-label">用户名</label>
            <div class="input-wrapper" :class="{ focused: usernameFocused }">
              <svg viewBox="0 0 24 24" width="18" height="18" fill="none" stroke="currentColor" stroke-width="1.5" class="input-icon">
                <path d="M20 21v-2a4 4 0 0 0-4-4H8a4 4 0 0 0-4 4v2" />
                <circle cx="12" cy="7" r="4" />
              </svg>
              <input
                v-model="loginForm.username"
                type="text"
                placeholder="请输入用户名"
                @focus="usernameFocused = true"
                @blur="usernameFocused = false"
              />
            </div>
          </div>

          <div class="form-group">
            <label class="form-label">密码</label>
            <div class="input-wrapper" :class="{ focused: passwordFocused }">
              <svg viewBox="0 0 24 24" width="18" height="18" fill="none" stroke="currentColor" stroke-width="1.5" class="input-icon">
                <rect x="3" y="11" width="18" height="11" rx="2" ry="2" />
                <path d="M7 11V7a5 5 0 0 1 10 0v4" />
              </svg>
              <input
                v-model="loginForm.password"
                :type="showPassword ? 'text' : 'password'"
                placeholder="请输入密码"
                @focus="passwordFocused = true"
                @blur="passwordFocused = false"
              />
              <button type="button" class="toggle-password" @click="showPassword = !showPassword">
                <svg v-if="!showPassword" viewBox="0 0 24 24" width="16" height="16" fill="none" stroke="currentColor" stroke-width="1.5">
                  <path d="M1 12s4-8 11-8 11 8 11 8-4 8-11 8-11-8-11-8z" />
                  <circle cx="12" cy="12" r="3" />
                </svg>
                <svg v-else viewBox="0 0 24 24" width="16" height="16" fill="none" stroke="currentColor" stroke-width="1.5">
                  <path d="M17.94 17.94A10.07 10.07 0 0 1 12 20c-7 0-11-8-11-8a18.45 18.45 0 0 1 5.06-5.94M9.9 4.24A9.12 9.12 0 0 1 12 4c7 0 11 8 11 8a18.5 18.5 0 0 1-2.16 3.19m-6.72-1.07a3 3 0 1 1-4.24-4.24" />
                  <line x1="1" y1="1" x2="23" y2="23" />
                </svg>
              </button>
            </div>
          </div>

          <div class="form-options">
            <label class="checkbox-wrapper">
              <input type="checkbox" v-model="rememberMe" />
              <span class="checkbox-custom"></span>
              <span class="checkbox-label">记住我</span>
            </label>
            <a href="#" class="forgot-link">忘记密码？</a>
          </div>

          <button type="submit" class="submit-btn" :class="{ loading: isLoading }">
            <span v-if="!isLoading">登 录</span>
            <span v-else class="loading-spinner"></span>
          </button>
        </form>

        <!-- 注册表单 -->
        <form v-else class="auth-form" @submit.prevent="handleRegister">
          <div class="form-group">
            <label class="form-label">用户名</label>
            <div class="input-wrapper" :class="{ focused: regUsernameFocused }">
              <svg viewBox="0 0 24 24" width="18" height="18" fill="none" stroke="currentColor" stroke-width="1.5" class="input-icon">
                <path d="M20 21v-2a4 4 0 0 0-4-4H8a4 4 0 0 0-4 4v2" />
                <circle cx="12" cy="7" r="4" />
              </svg>
              <input
                v-model="registerForm.username"
                type="text"
                placeholder="请输入用户名"
                @focus="regUsernameFocused = true"
                @blur="regUsernameFocused = false"
              />
            </div>
          </div>

          <div class="form-group">
            <label class="form-label">密码</label>
            <div class="input-wrapper" :class="{ focused: regPasswordFocused }">
              <svg viewBox="0 0 24 24" width="18" height="18" fill="none" stroke="currentColor" stroke-width="1.5" class="input-icon">
                <rect x="3" y="11" width="18" height="11" rx="2" ry="2" />
                <path d="M7 11V7a5 5 0 0 1 10 0v4" />
              </svg>
              <input
                v-model="registerForm.password"
                type="password"
                placeholder="请输入密码（至少6位）"
                @focus="regPasswordFocused = true"
                @blur="regPasswordFocused = false"
              />
            </div>
          </div>

          <div class="form-group">
            <label class="form-label">确认密码</label>
            <div class="input-wrapper" :class="{ focused: regConfirmFocused }">
              <svg viewBox="0 0 24 24" width="18" height="18" fill="none" stroke="currentColor" stroke-width="1.5" class="input-icon">
                <rect x="3" y="11" width="18" height="11" rx="2" ry="2" />
                <path d="M7 11V7a5 5 0 0 1 10 0v4" />
              </svg>
              <input
                v-model="registerForm.confirmPassword"
                type="password"
                placeholder="请再次输入密码"
                @focus="regConfirmFocused = true"
                @blur="regConfirmFocused = false"
              />
            </div>
          </div>

          <button type="submit" class="submit-btn" :class="{ loading: isLoading }">
            <span v-if="!isLoading">注 册</span>
            <span v-else class="loading-spinner"></span>
          </button>
        </form>

        <!-- 切换登录/注册 -->
        <div class="form-footer">
          <p class="switch-text">
            {{ isLogin ? '还没有账户？' : '已有账户？' }}
            <a href="#" class="switch-link" @click.prevent="isLogin = !isLogin">
              {{ isLogin ? '立即注册' : '立即登录' }}
            </a>
          </p>
        </div>

        <!-- 底部信息 -->
        <div class="form-bottom">
          <p class="copyright">© 2024 Vitalis Health Intelligence</p>
          <div class="bottom-links">
            <a href="#">隐私政策</a>
            <span class="link-sep">·</span>
            <a href="#">服务条款</a>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { login, register } from '@/api/auth'

const router = useRouter()
const isLogin = ref(true)
const isLoading = ref(false)
const showPassword = ref(false)
const rememberMe = ref(false)

// Focus states
const usernameFocused = ref(false)
const passwordFocused = ref(false)
const regUsernameFocused = ref(false)
const regPasswordFocused = ref(false)
const regConfirmFocused = ref(false)

const loginForm = reactive({
  username: '',
  password: ''
})

const registerForm = reactive({
  username: '',
  password: '',
  confirmPassword: ''
})

const handleLogin = async () => {
  if (!loginForm.username || !loginForm.password) {
    ElMessage.warning('请输入用户名和密码')
    return
  }

  isLoading.value = true
  try {
    const res = await login({ username: loginForm.username, password: loginForm.password })
    localStorage.setItem('token', res.data.token)
    ElMessage.success('登录成功')
    router.push('/dashboard')
  } catch (error) {
    // error is handled by axios interceptor
  } finally {
    isLoading.value = false
  }
}

const handleRegister = async () => {
  if (!registerForm.username || !registerForm.password) {
    ElMessage.warning('请填写完整信息')
    return
  }
  if (registerForm.password !== registerForm.confirmPassword) {
    ElMessage.warning('两次输入密码不一致')
    return
  }
  if (registerForm.password.length < 6) {
    ElMessage.warning('密码长度不能少于6位')
    return
  }

  isLoading.value = true
  try {
    await register({ username: registerForm.username, password: registerForm.password })
    ElMessage.success('注册成功，请登录')
    isLogin.value = true
  } catch (error) {
    // error is handled by axios interceptor
  } finally {
    isLoading.value = false
  }
}
</script>

<style scoped>
@import url('https://fonts.googleapis.com/css2?family=Cormorant+Garamond:ital,wght@0,400;0,500;0,600;0,700;1,400&family=DM+Sans:ital,opsz,wght@0,9..40,300;0,9..40,400;0,9..40,500;0,9..40,600;0,9..40,700;1,9..40,400&display=swap');

.login-page {
  display: flex;
  min-height: 100vh;
  font-family: 'DM Sans', sans-serif;
  position: relative;
  overflow: hidden;
  background: #faf8f5;
}

/* ============================================
   BACKGROUND DECORATION
   ============================================ */
.bg-decoration {
  position: fixed;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  pointer-events: none;
  z-index: 0;
}

.bg-circle {
  position: absolute;
  border-radius: 50%;
  opacity: 0.04;
}

.bg-circle-1 {
  width: 800px;
  height: 800px;
  top: -400px;
  right: -200px;
  background: radial-gradient(circle, #1a6b5a, transparent 70%);
  animation: float 20s ease-in-out infinite;
}

.bg-circle-2 {
  width: 600px;
  height: 600px;
  bottom: -300px;
  left: -100px;
  background: radial-gradient(circle, #c4956a, transparent 70%);
  animation: float 25s ease-in-out infinite reverse;
}

.bg-circle-3 {
  width: 400px;
  height: 400px;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
  background: radial-gradient(circle, #1a6b5a, transparent 70%);
  animation: float 30s ease-in-out infinite;
}

@keyframes float {
  0%, 100% { transform: translate(0, 0); }
  33% { transform: translate(30px, -30px); }
  66% { transform: translate(-20px, 20px); }
}

.bg-line {
  position: absolute;
  height: 1px;
  background: linear-gradient(90deg, transparent, rgba(26, 107, 90, 0.1), transparent);
}

.bg-line-1 {
  width: 100%;
  top: 30%;
  left: 0;
  transform: rotate(-5deg);
}

.bg-line-2 {
  width: 100%;
  top: 70%;
  left: 0;
  transform: rotate(3deg);
}

/* ============================================
   BRAND SECTION (LEFT)
   ============================================ */
.brand-section {
  flex: 1;
  background: linear-gradient(135deg, #0f2b25 0%, #1a4a3e 50%, #0f2b25 100%);
  display: flex;
  align-items: center;
  justify-content: center;
  position: relative;
  padding: var(--space-3xl);
  overflow: hidden;
}

.brand-section::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background:
    radial-gradient(ellipse at 30% 20%, rgba(196, 149, 106, 0.1) 0%, transparent 50%),
    radial-gradient(ellipse at 70% 80%, rgba(26, 107, 90, 0.15) 0%, transparent 50%);
}

.brand-content {
  position: relative;
  z-index: 1;
  text-align: center;
  max-width: 400px;
}

.brand-logo {
  width: 80px;
  height: 80px;
  margin: 0 auto var(--space-xl);
  color: #c4956a;
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

.brand-title {
  font-family: 'Cormorant Garamond', serif;
  font-size: 3.5rem;
  font-weight: 600;
  color: #f5f0ea;
  letter-spacing: 0.08em;
  margin-bottom: var(--space-sm);
  line-height: 1;
}

.brand-subtitle {
  font-size: 0.8rem;
  color: #c4956a;
  text-transform: uppercase;
  letter-spacing: 0.25em;
  font-weight: 500;
  margin-bottom: var(--space-xl);
}

.brand-divider {
  width: 60px;
  height: 1px;
  background: linear-gradient(90deg, transparent, #c4956a, transparent);
  margin: 0 auto var(--space-xl);
}

.brand-description {
  font-size: 0.95rem;
  color: rgba(245, 240, 234, 0.7);
  line-height: 1.8;
  margin-bottom: var(--space-2xl);
}

.brand-features {
  display: flex;
  flex-direction: column;
  gap: var(--space-md);
  text-align: left;
}

.feature-item {
  display: flex;
  align-items: center;
  gap: var(--space-md);
  padding: var(--space-md) var(--space-lg);
  background: rgba(255, 255, 255, 0.04);
  border-radius: 12px;
  border: 1px solid rgba(255, 255, 255, 0.06);
  transition: all 0.3s ease;
}

.feature-item:hover {
  background: rgba(255, 255, 255, 0.08);
  transform: translateX(4px);
}

.feature-icon {
  width: 36px;
  height: 36px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: rgba(196, 149, 106, 0.15);
  border-radius: 10px;
  color: #c4956a;
  flex-shrink: 0;
}

.feature-item span {
  font-size: 0.9rem;
  color: rgba(245, 240, 234, 0.8);
  font-weight: 500;
}

.brand-deco {
  position: absolute;
  right: 0;
  top: 0;
  height: 100%;
  width: 200px;
  color: rgba(196, 149, 106, 0.3);
  pointer-events: none;
}

.deco-svg {
  height: 100%;
  width: 100%;
}

/* ============================================
   FORM SECTION (RIGHT)
   ============================================ */
.form-section {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: var(--space-xl);
  position: relative;
  z-index: 1;
}

.form-container {
  width: 100%;
  max-width: 420px;
}

.form-header {
  margin-bottom: var(--space-2xl);
  text-align: center;
}

.form-logo-mobile {
  display: none;
  width: 50px;
  height: 50px;
  margin: 0 auto var(--space-lg);
  color: #1a6b5a;
}

.form-title {
  font-family: 'Cormorant Garamond', serif;
  font-size: 2rem;
  font-weight: 600;
  color: #2c2c2c;
  margin-bottom: var(--space-sm);
}

.form-subtitle {
  font-size: 0.9rem;
  color: #6b6b6b;
}

/* ============================================
   FORM STYLES
   ============================================ */
.auth-form {
  display: flex;
  flex-direction: column;
  gap: var(--space-lg);
}

.form-group {
  display: flex;
  flex-direction: column;
  gap: var(--space-sm);
}

.form-label {
  font-size: 0.8rem;
  font-weight: 600;
  color: #2c2c2c;
  letter-spacing: 0.03em;
}

.input-wrapper {
  display: flex;
  align-items: center;
  gap: var(--space-md);
  padding: 0 var(--space-lg);
  background: #fff;
  border: 1.5px solid rgba(0, 0, 0, 0.08);
  border-radius: 12px;
  transition: all 0.3s cubic-bezier(0.16, 1, 0.3, 1);
  height: 52px;
}

.input-wrapper.focused {
  border-color: #1a6b5a;
  box-shadow: 0 0 0 3px rgba(26, 107, 90, 0.1);
}

.input-icon {
  color: #9a9a9a;
  flex-shrink: 0;
  transition: color 0.3s;
}

.input-wrapper.focused .input-icon {
  color: #1a6b5a;
}

.input-wrapper input {
  flex: 1;
  border: none;
  background: none;
  outline: none;
  font-family: 'DM Sans', sans-serif;
  font-size: 0.9rem;
  color: #2c2c2c;
  height: 100%;
}

.input-wrapper input::placeholder {
  color: #9a9a9a;
}

.toggle-password {
  background: none;
  border: none;
  padding: 4px;
  cursor: pointer;
  color: #9a9a9a;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: color 0.2s;
}

.toggle-password:hover {
  color: #2c2c2c;
}

.form-options {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.checkbox-wrapper {
  display: flex;
  align-items: center;
  gap: var(--space-sm);
  cursor: pointer;
}

.checkbox-wrapper input[type="checkbox"] {
  display: none;
}

.checkbox-custom {
  width: 18px;
  height: 18px;
  border: 1.5px solid rgba(0, 0, 0, 0.15);
  border-radius: 4px;
  transition: all 0.2s;
  position: relative;
}

.checkbox-wrapper input:checked + .checkbox-custom {
  background: #1a6b5a;
  border-color: #1a6b5a;
}

.checkbox-wrapper input:checked + .checkbox-custom::after {
  content: '';
  position: absolute;
  top: 2px;
  left: 5px;
  width: 5px;
  height: 9px;
  border: solid white;
  border-width: 0 2px 2px 0;
  transform: rotate(45deg);
}

.checkbox-label {
  font-size: 0.8rem;
  color: #6b6b6b;
}

.forgot-link {
  font-size: 0.8rem;
  color: #1a6b5a;
  text-decoration: none;
  font-weight: 500;
  transition: color 0.2s;
}

.forgot-link:hover {
  color: #0f4d40;
}

.submit-btn {
  width: 100%;
  height: 52px;
  background: linear-gradient(135deg, #1a6b5a, #2d8f7a);
  border: none;
  border-radius: 12px;
  color: white;
  font-family: 'DM Sans', sans-serif;
  font-size: 0.95rem;
  font-weight: 600;
  letter-spacing: 0.1em;
  cursor: pointer;
  transition: all 0.3s cubic-bezier(0.16, 1, 0.3, 1);
  position: relative;
  overflow: hidden;
}

.submit-btn::before {
  content: '';
  position: absolute;
  top: 0;
  left: -100%;
  width: 100%;
  height: 100%;
  background: linear-gradient(90deg, transparent, rgba(255, 255, 255, 0.15), transparent);
  transition: left 0.5s;
}

.submit-btn:hover {
  transform: translateY(-2px);
  box-shadow: 0 8px 25px rgba(26, 107, 90, 0.3);
}

.submit-btn:hover::before {
  left: 100%;
}

.submit-btn:active {
  transform: translateY(0);
}

.submit-btn.loading {
  pointer-events: none;
  opacity: 0.8;
}

.loading-spinner {
  display: inline-block;
  width: 20px;
  height: 20px;
  border: 2px solid rgba(255, 255, 255, 0.3);
  border-radius: 50%;
  border-top-color: white;
  animation: spin 0.8s linear infinite;
}

@keyframes spin {
  to { transform: rotate(360deg); }
}

.form-footer {
  margin-top: var(--space-xl);
  text-align: center;
}

.switch-text {
  font-size: 0.85rem;
  color: #6b6b6b;
}

.switch-link {
  color: #1a6b5a;
  font-weight: 600;
  text-decoration: none;
  transition: color 0.2s;
}

.switch-link:hover {
  color: #0f4d40;
}

.form-bottom {
  margin-top: var(--space-2xl);
  text-align: center;
  padding-top: var(--space-lg);
  border-top: 1px solid rgba(0, 0, 0, 0.06);
}

.copyright {
  font-size: 0.75rem;
  color: #9a9a9a;
  margin-bottom: var(--space-sm);
}

.bottom-links {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: var(--space-sm);
}

.bottom-links a {
  font-size: 0.75rem;
  color: #6b6b6b;
  text-decoration: none;
  transition: color 0.2s;
}

.bottom-links a:hover {
  color: #1a6b5a;
}

.link-sep {
  color: #9a9a9a;
  font-size: 0.75rem;
}

/* ============================================
   RESPONSIVE
   ============================================ */
@media (max-width: 900px) {
  .brand-section {
    display: none;
  }

  .form-logo-mobile {
    display: block;
  }

  .login-page {
    background: linear-gradient(180deg, #0f2b25 0%, #0f2b25 40%, #faf8f5 40%);
  }

  .form-section {
    padding-top: 120px;
  }
}
</style>
