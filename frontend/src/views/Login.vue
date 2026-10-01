<template>
  <div class="login-page">
    <div class="login-left">
      <div class="brand">
        <div class="brand-icon">YK</div>
        <h1>YU-Kami</h1>
        <p>屿宸科技 · 企业级卡密管理平台</p>
      </div>
      <div class="features">
        <div class="feature-item" v-for="f in features" :key="f.title">
          <el-icon :size="20"><component :is="f.icon" /></el-icon>
          <div>
            <div class="feature-title">{{ f.title }}</div>
            <div class="feature-desc">{{ f.desc }}</div>
          </div>
        </div>
      </div>
    </div>
    <div class="login-right">
      <div class="login-card">
        <h2>欢迎回来</h2>
        <p class="subtitle">登录管理后台</p>
        <el-form :model="form" size="large" @submit.prevent="handleLogin">
          <el-form-item>
            <el-input v-model="form.username" placeholder="用户名" :prefix-icon="User" />
          </el-form-item>
          <el-form-item>
            <el-input v-model="form.password" type="password" placeholder="密码" :prefix-icon="Lock" show-password />
          </el-form-item>
          <el-button type="primary" class="login-btn" :loading="loading" @click="handleLogin">
            登 录
          </el-button>
        </el-form>
        <p class="hint">默认账号 admin / admin123</p>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { User, Lock } from '@element-plus/icons-vue'
import request from '../api/request'
import { useAuthStore } from '../stores/auth'

const router = useRouter()
const auth = useAuthStore()
const loading = ref(false)
const form = ref({ username: 'admin', password: 'admin123' })

const features = [
  { icon: 'Lock', title: '多重加密', desc: 'HMAC + AES + RSA + BCrypt 五重防护' },
  { icon: 'Lightning', title: '高并发', desc: 'Redis 分布式锁 + 乐观锁保障' },
  { icon: 'DataAnalysis', title: '全链路审计', desc: '操作日志与兑换记录可追溯' }
]

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
  height: 100vh;
  display: flex;
}
.login-left {
  flex: 1;
  background: linear-gradient(135deg, #0f172a 0%, #1e293b 50%, #312e81 100%);
  display: flex;
  flex-direction: column;
  justify-content: center;
  padding: 60px;
  color: #fff;
}
.brand { margin-bottom: 48px; }
.brand-icon {
  width: 56px; height: 56px;
  background: linear-gradient(135deg, #4f6ef7, #7c3aed);
  border-radius: 14px;
  display: flex; align-items: center; justify-content: center;
  font-weight: 800; font-size: 18px; margin-bottom: 20px;
}
.brand h1 { font-size: 36px; font-weight: 800; margin-bottom: 8px; }
.brand p { color: #94a3b8; font-size: 16px; }
.features { display: flex; flex-direction: column; gap: 24px; }
.feature-item {
  display: flex; align-items: flex-start; gap: 14px;
  padding: 16px; background: rgba(255,255,255,0.05);
  border-radius: 12px; border: 1px solid rgba(255,255,255,0.08);
}
.feature-title { font-weight: 600; margin-bottom: 4px; }
.feature-desc { font-size: 13px; color: #94a3b8; }
.login-right {
  width: 480px;
  display: flex; align-items: center; justify-content: center;
  background: #f8fafc;
}
.login-card {
  width: 360px; padding: 40px;
  background: #fff; border-radius: 16px;
  box-shadow: 0 20px 60px rgba(0,0,0,0.08);
}
.login-card h2 { font-size: 24px; font-weight: 700; margin-bottom: 4px; }
.subtitle { color: #94a3b8; margin-bottom: 32px; font-size: 14px; }
.login-btn { width: 100%; height: 44px; font-size: 15px; margin-top: 8px; }
.hint { text-align: center; color: #cbd5e1; font-size: 12px; margin-top: 20px; }

@media (max-width: 900px) {
  .login-left { display: none; }
  .login-right { width: 100%; }
}
</style>
