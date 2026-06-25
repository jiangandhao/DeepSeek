<template>
  <div class="auth-wrap">
    <el-card class="auth-card">
      <div class="brand-mark"><FirstAidKit /></div>
      <h1 class="title">DeepSeek 健康管理系统</h1>
      <el-form :model="form" @submit.prevent>
        <el-form-item>
          <el-input v-model="form.username" placeholder="用户名" :prefix-icon="User" />
        </el-form-item>
        <el-form-item>
          <el-input v-model="form.password" type="password" placeholder="密码" :prefix-icon="Lock" show-password />
        </el-form-item>
        <el-button type="primary" :loading="loading" style="width: 100%" @click="onLogin">登录</el-button>
      </el-form>
      <div class="foot">还没有账号?<router-link to="/register">立即注册</router-link></div>
    </el-card>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { FirstAidKit, User, Lock } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import { login } from '../api'
import { useAuthStore } from '../store/auth'

const router = useRouter()
const auth = useAuthStore()
const form = ref({ username: '', password: '' })
const loading = ref(false)

async function onLogin() {
  if (!form.value.username || !form.value.password) {
    ElMessage.warning('请输入用户名和密码')
    return
  }
  loading.value = true
  try {
    const data = await login(form.value)
    auth.setAuth(data)
    ElMessage.success('登录成功')
    router.push('/dashboard')
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.auth-wrap {
  min-height: 100vh;
  width: 100%;
  padding: 32px 16px;
  display: grid;
  grid-template-columns: minmax(0, 420px);
  place-items: center;
  background:
    radial-gradient(circle at 18% 18%, rgba(53, 168, 137, .16), transparent 30%),
    linear-gradient(135deg, #eef8f4 0%, #f7fbfa 48%, #ecf4ff 100%);
}
.auth-card {
  width: 100%;
  max-width: 100%;
  min-width: 0;
  border-radius: 20px;
}
.auth-card :deep(.el-card__body) {
  padding: 34px 32px 30px;
  min-width: 0;
}
.auth-card :deep(.el-form) {
  min-width: 0;
}
.brand-mark {
  display: grid;
  width: 54px;
  height: 54px;
  margin: 0 auto 16px;
  place-items: center;
  color: #123d35;
  background: #d8f2e9;
  border-radius: 16px;
}
.brand-mark svg {
  width: 28px;
}
.title {
  text-align: center;
  margin: 0 0 26px;
  color: var(--ink);
  font-size: 24px;
  line-height: 1.25;
  font-weight: 800;
}
.auth-card :deep(.el-input__wrapper) {
  min-height: 44px;
  border-radius: 12px;
}
.auth-card :deep(.el-button) {
  min-height: 44px;
  border-radius: 12px;
  font-weight: 700;
}
.foot {
  text-align: center;
  margin-top: 18px;
  font-size: 14px;
  color: var(--muted);
}
.foot a {
  margin-left: 6px;
  color: var(--mint-700);
  font-weight: 700;
  text-decoration: none;
}
@media (max-width: 480px) {
  .auth-wrap { grid-template-columns: minmax(0, 320px); padding: 12vh 18px 18px; place-items: start center; }
  .auth-card :deep(.el-card__body) { padding: 28px 22px 24px; }
  .title { font-size: 21px; }
}
@media (max-width: 360px) {
  .auth-wrap { grid-template-columns: minmax(0, 100%); }
}
</style>
