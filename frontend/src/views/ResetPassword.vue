<template>
  <div class="auth-page">
    <div class="auth-card">
      <h2>{{ t('auth.resetPassword') }}</h2>
      <el-form @submit.prevent="submit">
        <el-form-item>
          <el-input v-model="form.newPassword" type="password" show-password :placeholder="t('auth.newPassword')" />
        </el-form-item>
        <el-form-item>
          <el-input v-model="form.confirmPassword" type="password" show-password :placeholder="t('auth.confirmPassword')" />
        </el-form-item>
        <el-button type="primary" :loading="loading" style="width:100%" @click="submit">
          {{ t('common.confirm') }}
        </el-button>
      </el-form>
    </div>
  </div>
</template>

<script setup>
import { ref, computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useI18n } from 'vue-i18n'
import { ElMessage } from 'element-plus'
import request from '../api/request'
import shopRequest from '../api/shopRequest'

const { t } = useI18n()
const route = useRoute()
const router = useRouter()
const loading = ref(false)
const form = ref({ newPassword: '', confirmPassword: '' })
const token = computed(() => route.query.token || '')
const isShop = computed(() => route.meta.scope === 'shop')

async function submit() {
  if (!form.value.newPassword || form.value.newPassword !== form.value.confirmPassword) {
    ElMessage.warning(t('auth.passwordMismatch'))
    return
  }
  loading.value = true
  try {
    const payload = { token: token.value, newPassword: form.value.newPassword }
    if (isShop.value) {
      await shopRequest.post('/shop/auth/reset-password', payload)
      router.push('/shop/login')
    } else {
      await request.post('/admin/auth/reset-password', payload)
      router.push('/login')
    }
    ElMessage.success(t('auth.resetSuccess'))
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.auth-page { min-height: 100vh; display: flex; align-items: center; justify-content: center; background: #f8fafc; }
.auth-card { width: 400px; padding: 32px; background: #fff; border-radius: 16px; box-shadow: 0 8px 32px rgba(0,0,0,.08); }
.auth-card h2 { margin-bottom: 20px; }
</style>
