<template>
  <div class="login-page">
    <div class="login-card">
      <h2>YU-Kami 卡密系统</h2>
      <p class="subtitle">屿宸科技 · 企业级卡密管理平台</p>
      <el-form :model="form" @submit.prevent="handleLogin">
        <el-form-item>
          <el-input v-model="form.username" placeholder="用户名" prefix-icon="User" size="large" />
        </el-form-item>
        <el-form-item>
          <el-input v-model="form.password" type="password" placeholder="密码" prefix-icon="Lock" size="large" show-password />
        </el-form-item>
        <el-button type="primary" size="large" style="width:100%" :loading="loading" @click="handleLogin">登 录</el-button>
      </el-form>
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import request from '../api/request'
import { useAuthStore } from '../stores/auth'

const router = useRouter()
const auth = useAuthStore()
const loading = ref(false)
const form = ref({ username: 'admin', password: 'admin123' })

async function handleLogin() {
  loading.value = true
  try {
    const res = await request.post('/admin/auth/login', form.value)
    auth.setAuth(res.data)
    ElMessage.success('登录成功')
    router.push('/dashboard')
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.login-page {
  height: 100vh; display: flex; align-items: center; justify-content: center;
  background: linear-gradient(135deg, #1d1e2c 0%, #2d3a5c 50%, #4a6fa5 100%);
}
.login-card {
  width: 400px; padding: 40px; background: #fff; border-radius: 12px;
  box-shadow: 0 20px 60px rgba(0,0,0,0.3);
}
h2 { text-align: center; margin: 0 0 8px; color: #1d1e2c; }
.subtitle { text-align: center; color: #999; margin-bottom: 32px; font-size: 14px; }
</style>
