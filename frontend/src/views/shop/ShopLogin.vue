<template>
  <div class="auth-page">
    <div class="auth-card">
      <h2>{{ isRegister ? t('shop.createAccount') : t('shop.welcome') }}</h2>
      <el-form :model="form" size="large">
        <el-form-item><el-input v-model="form.username" placeholder="Username" /></el-form-item>
        <el-form-item v-if="isRegister"><el-input v-model="form.email" placeholder="Email" /></el-form-item>
        <el-form-item><el-input v-model="form.password" type="password" placeholder="Password" show-password /></el-form-item>
        <el-button type="primary" style="width:100%" :loading="loading" @click="handleSubmit">
          {{ isRegister ? t('common.register') : t('common.login') }}
        </el-button>
      </el-form>
      <p class="switch">
        <a @click="isRegister = !isRegister">
          {{ isRegister ? t('common.login') : t('common.register') }}
        </a>
      </p>
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { useI18n } from 'vue-i18n'
import { ElMessage } from 'element-plus'
import shopRequest from '../../api/shopRequest'
import { useShopAuthStore } from '../../stores/shopAuth'

const { t } = useI18n()
const router = useRouter()
const route = useRoute()
const auth = useShopAuthStore()
const isRegister = ref(false)
const loading = ref(false)
const form = ref({ username: '', password: '', email: '' })

async function handleSubmit() {
  loading.value = true
  try {
    const url = isRegister.value ? '/shop/auth/register' : '/shop/auth/login'
    const res = await shopRequest.post(url, form.value)
    auth.setAuth(res.data)
    ElMessage.success('OK')
    router.push(route.query.redirect || '/shop')
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.auth-page { display: flex; justify-content: center; padding: 40px 0; }
.auth-card { width: 400px; background: #fff; border-radius: 16px; padding: 40px; box-shadow: 0 8px 30px rgba(0,0,0,0.06); }
.auth-card h2 { text-align: center; margin-bottom: 24px; }
.switch { text-align: center; margin-top: 16px; }
.switch a { color: #4f6ef7; cursor: pointer; font-size: 14px; }
</style>
