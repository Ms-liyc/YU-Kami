<template>
  <div class="login-page">
    <div class="login-left">
      <div class="brand">
        <img src="/logo.png" alt="YU-Kami" class="brand-logo" />
        <h1>YU-Kami</h1>
        <p>{{ t('admin.brandSubtitle') }}</p>
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
        <h2>{{ t('admin.welcome') }}</h2>
        <p class="subtitle">{{ t('admin.loginSubtitle') }}</p>
        <el-form :model="form" size="large" @submit.prevent="handleLogin">
          <el-form-item>
            <el-input v-model="form.username" :placeholder="t('shop.usernamePlaceholder')" :prefix-icon="User" />
          </el-form-item>
          <el-form-item>
            <el-input v-model="form.password" type="password" :placeholder="t('shop.passwordPlaceholder')" :prefix-icon="Lock" show-password />
          </el-form-item>
          <el-form-item v-if="captchaImage">
            <div class="captcha-row">
              <el-input v-model="captchaCode" :placeholder="t('auth.captcha')" />
              <img :src="captchaImage" alt="captcha" class="captcha-img" @click="refreshCaptcha" />
            </div>
          </el-form-item>
          <el-form-item>
            <el-input v-model="form.totpCode" :placeholder="t('auth.totpLoginPlaceholder')" />
          </el-form-item>
          <el-button type="primary" class="login-btn" :loading="loading" @click="handleLogin">
            {{ t('common.login') }}
          </el-button>
        </el-form>
        <p class="hint">
          <router-link to="/forgot-password">{{ t('auth.forgotPassword') }}</router-link>
          · {{ t('admin.defaultHint') }}
        </p>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { User, Lock } from '@element-plus/icons-vue'
import request from '../api/request'
import { useAuthStore } from '../stores/auth'
import { useI18n } from 'vue-i18n'
import { useCaptcha } from '../composables/useCaptcha'

const { t } = useI18n()
const router = useRouter()
const auth = useAuthStore()
const loading = ref(false)
const form = ref({ username: 'admin', password: 'admin123', totpCode: '' })
const { captchaImage, captchaCode, refreshCaptcha, captchaPayload } = useCaptcha()

onMounted(refreshCaptcha)

const features = computed(() => [
  { icon: 'Lock', title: t('admin.loginFeatureCryptoTitle'), desc: t('admin.loginFeatureCryptoDesc') },
  { icon: 'Lightning', title: t('admin.loginFeatureConcurrencyTitle'), desc: t('admin.loginFeatureConcurrencyDesc') },
  { icon: 'DataAnalysis', title: t('admin.loginFeatureAuditTitle'), desc: t('admin.loginFeatureAuditDesc') }
])

async function handleLogin() {
  loading.value = true
  try {
    const res = await request.post('/admin/auth/login', { ...form.value, ...captchaPayload() })
    auth.setAuth(res.data)
    ElMessage.success(t('common.success'))
    if (res.data.warnDefaultPassword) {
      ElMessage.warning(t('auth.defaultPasswordWarn'))
    }
    router.push('/dashboard')
  } catch {
    refreshCaptcha()
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
.brand-logo {
  width: 64px; height: 64px;
  border-radius: 14px;
  margin-bottom: 20px;
  object-fit: contain;
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
.hint a { color: #4f6ef7; text-decoration: none; }
.captcha-row { display: flex; gap: 8px; width: 100%; }
.captcha-img { height: 40px; border-radius: 6px; cursor: pointer; border: 1px solid #e2e8f0; flex-shrink: 0; }

@media (max-width: 900px) {
  .login-left { display: none; }
  .login-right { width: 100%; }
}
</style>
