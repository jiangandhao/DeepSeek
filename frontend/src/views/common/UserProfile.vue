<template>
  <div class="user-profile-container">
    <el-card>
      <template #header>
        <span>个人资料</span>
      </template>
      <el-form :model="form" label-width="120px">
        <el-form-item label="头像">
          <el-avatar :size="100" :src="form.avatar"></el-avatar>
          <el-button style="margin-left: 20px;">更换头像</el-button>
        </el-form-item>
        <el-form-item label="用户名">
          <el-input v-model="form.username" disabled></el-input>
        </el-form-item>
        <el-form-item label="姓名">
          <el-input v-model="form.name"></el-input>
        </el-form-item>
        <el-form-item label="性别">
          <el-radio-group v-model="form.gender">
            <el-radio label="male">男</el-radio>
            <el-radio label="female">女</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="出生日期">
          <el-date-picker v-model="form.birthday" type="date" placeholder="选择日期"></el-date-picker>
        </el-form-item>
        <el-form-item label="手机号">
          <el-input v-model="form.phone"></el-input>
        </el-form-item>
        <el-form-item label="邮箱">
          <el-input v-model="form.email"></el-input>
        </el-form-item>
        <el-form-item label="身高(cm)">
          <el-input-number v-model="form.height" :min="100" :max="250"></el-input-number>
        </el-form-item>
        <el-form-item label="体重(kg)">
          <el-input-number v-model="form.weight" :min="30" :max="200"></el-input-number>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSave">保存修改</el-button>
        </el-form-item>
      </el-form>
    </el-card>
  </div>
</template>

<script setup>
import { reactive } from 'vue'
import { ElMessage } from 'element-plus'

const form = reactive({
  avatar: '',
  username: 'testuser',
  name: '张三',
  gender: 'male',
  birthday: '1990-01-01',
  phone: '13800138000',
  email: 'test@example.com',
  height: 175,
  weight: 70
})

const handleSave = () => {
  ElMessage.success('保存成功')
}
</script>

<style scoped>
.user-profile-container {
  padding: 20px;
  max-width: 800px;
  margin: 0 auto;
}
</style>
