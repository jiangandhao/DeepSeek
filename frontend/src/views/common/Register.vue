<template>
  <div class="login-page">
    <!-- 背景装饰 -->
    <div class="bg-decoration">
      <div class="bg-circle bg-circle-1"></div>
      <div class="bg-circle bg-circle-2"></div>
      <div class="bg-circle bg-circle-3"></div>
    </div>

    <!-- 左侧品牌区 -->
    <div class="brand-section">
      <div class="brand-content">
        <div class="brand-logo">
          <svg viewBox="0 0 60 60" class="logo-svg">
            <circle cx="30" cy="30" r="27" fill="none" stroke="currentColor" stroke-width="1" />
            <path d="M30 12 C22 20, 22 40, 30 48 C38 40, 38 20, 30 12Z" fill="currentColor" opacity="0.2" />
            <circle cx="30" cy="30" r="6" fill="currentColor" />
          </svg>
        </div>
        <h1 class="brand-title">Vitalis</h1>
        <p class="brand-subtitle">Health Intelligence System</p>
        <div class="brand-divider"></div>
        <p class="brand-description">
          开启您的智能健康管理之旅<br />
          AI 驱动的个性化健康方案
        </p>
      </div>
    </div>

    <!-- 右侧注册区 -->
    <div class="form-section">
      <div class="form-container">
        <div class="form-header">
          <div class="form-logo-mobile">
            <svg viewBox="0 0 40 40" class="logo-svg">
              <circle cx="20" cy="20" r="18" fill="none" stroke="currentColor" stroke-width="1.5" />
              <path d="M20 8 C14 14, 14 26, 20 32 C26 26, 26 14, 20 8Z" fill="currentColor" opacity="0.3" />
              <circle cx="20" cy="20" r="4" fill="currentColor" />
            </svg>
          </div>
          <h2 class="form-title">创建账户</h2>
          <p class="form-subtitle">开启您的健康管理之旅</p>
        </div>

        <form class="auth-form" @submit.prevent="handleRegister">
          <div class="form-group">
            <label class="form-label">用户名</label>
            <div class="input-wrapper" :class="{ focused: usernameFocused }">
              <svg viewBox="0 0 24 24" width="18" height="18" fill="none" stroke="currentColor" stroke-width="1.5" class="input-icon">
                <path d="M20 21v-2a4 4 0 0 0-4-4H8a4 4 0 0 0-4 4v2" />
                <circle cx="12" cy="7" r="4" />
              </svg>
              <input
                v-model="form.username"
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
                v-model="form.password"
                type="password"
                placeholder="请输入密码（至少6位）"
                @focus="passwordFocused = true"
                @blur="passwordFocused = false"
              />
            </div>
          </div>

          <div class="form-group">
            <label class="form-label">确认密码</label>
            <div class="input-wrapper" :class="{ focused: confirmFocused }">
              <svg viewBox="0 0 24 24" width="18" height="18" fill="none" stroke="currentColor" stroke-width="1.5" class="input-icon">
                <rect x="3" y="11" width="18" height="11" rx="2" ry="2" />
                <path d="M7 11V7a5 5 0 0 1 10 0v4" />
              </svg>
              <input
                v-model="form.confirmPassword"
                type="password"
                placeholder="请再次输入密码"
                @focus="confirmFocused = true"
                @blur="confirmFocused = false"
              />
            </div>
          </div>

          <button type="submit" class="submit-btn" :class="{ loading: isLoading }">
            <span v-if="!isLoading">注 册</span>
            <span v-else class="loading-spinner"></span>
          </button>
        </form>

        <div class="form-footer">
          <p class="switch-text">
            已有账户？
            <router-link to="/login" class="switch-link">立即登录</router-link>
          </p>
        </div>

        <div class="form-bottom">
          <p class="copyright">© 2024 Vitalis Health Intelligence</p>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { register } from '@/api/auth'

const router = useRouter()
const isLoading = ref(false)
const usernameFocused = ref(false)
const passwordFocused = ref(false)
const confirmFocused = ref(false)

const form = reactive({
  username: '',
  password: '',
  confirmPassword: ''
})

const handleRegister = async () => {
  if (!form.username || !form.password) {
    ElMessage.warning('请填写完整信息')
    return
  }
  if (form.password !== form.confirmPassword) {
    ElMessage.warning('两次输入密码不一致')
    return
  }
  if (form.password.length < 6) {
    ElMessage.warning('密码长度不能少于6位')
    return
  }

  isLoading.value = true
  try {
    await register({ username: form.username, password: form.password })
    ElMessage.success('注册成功，请登录')
    router.push('/login')
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

.brand-section {
  flex: 1;
  background: linear-gradient(135deg, #0f2b25 0%, #1a4a3e 50%, #0f2b25 100%);
  display: flex;
  align-items: center;
  justify-content: center;
  position: relative;
  padding: 64px;
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
  margin: 0 auto 32px;
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
  margin-bottom: 8px;
  line-height: 1;
}

.brand-subtitle {
  font-size: 0.8rem;
  color: #c4956a;
  text-transform: uppercase;
  letter-spacing: 0.25em;
  font-weight: 500;
  margin-bottom: 32px;
}

.brand-divider {
  width: 60px;
  height: 1px;
  background: linear-gradient(90deg, transparent, #c4956a, transparent);
  margin: 0 auto 32px;
}

.brand-description {
  font-size: 0.95rem;
  color: rgba(245, 240, 234, 0.7);
  line-height: 1.8;
}

.form-section {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 24px;
  position: relative;
  z-index: 1;
}

.form-container {
  width: 100%;
  max-width: 420px;
}

.form-header {
  margin-bottom: 40px;
  text-align: center;
}

.form-logo-mobile {
  display: none;
  width: 50px;
  height: 50px;
  margin: 0 auto 24px;
  color: #1a6b5a;
}

.form-title {
  font-family: 'Cormorant Garamond', serif;
  font-size: 2rem;
  font-weight: 600;
  color: #2c2c2c;
  margin-bottom: 8px;
}

.form-subtitle {
  font-size: 0.9rem;
  color: #6b6b6b;
}

.auth-form {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.form-group {
  display: flex;
  flex-direction: column;
  gap: 8px;
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
  gap: 12px;
  padding: 0 20px;
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
  margin-top: 8px;
}

.submit-btn:hover {
  transform: translateY(-2px);
  box-shadow: 0 8px 25px rgba(26, 107, 90, 0.3);
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
  margin-top: 32px;
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
  margin-top: 48px;
  text-align: center;
  padding-top: 24px;
  border-top: 1px solid rgba(0, 0, 0, 0.06);
}

.copyright {
  font-size: 0.75rem;
  color: #9a9a9a;
}

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
