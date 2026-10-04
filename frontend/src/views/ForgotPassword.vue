<template>
  <div class="auth-page">
    <div class="auth-toolbar">
      <ThemeSwitcher />
      <LanguageSwitcher />
    </div>
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
import ThemeSwitcher from '../components/ThemeSwitcher.vue'
import LanguageSwitcher from '../components/LanguageSwitcher.vue'

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

