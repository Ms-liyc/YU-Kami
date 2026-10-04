<template>
  <div class="auth-page">
    <div class="auth-card">
      <h2>{{ t('auth.forgotPassword') }}</h2>
      <el-form @submit.prevent="submit">
        <el-form-item>
          <el-input v-model="username" :placeholder="t('shop.username')" />
        </el-form-item>
        <el-button type="primary" :loading="loading" style="width:100%" @click="submit">
          {{ t('auth.sendResetLink') }}
        </el-button>
        <router-link to="/login" class="back-link">{{ t('auth.backLogin') }}</router-link>
      </el-form>
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { useI18n } from 'vue-i18n'
import { ElMessage } from 'element-plus'
import request from '../api/request'

const { t } = useI18n()
const username = ref('')
const loading = ref(false)

async function submit() {
  if (!username.value) return
  loading.value = true
  try {
    await request.post('/admin/auth/forgot-password', { username: username.value })
    ElMessage.success(t('auth.resetEmailSent'))
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.auth-page { min-height: 100vh; display: flex; align-items: center; justify-content: center; background: #f8fafc; }
.auth-card { width: 400px; padding: 32px; background: #fff; border-radius: 16px; box-shadow: 0 8px 32px rgba(0,0,0,.08); }
.auth-card h2 { margin-bottom: 20px; }
.back-link { display: block; margin-top: 16px; text-align: center; color: #4f6ef7; font-size: 14px; }
</style>
