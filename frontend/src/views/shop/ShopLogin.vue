<template>
  <div class="auth-page">
    <div class="auth-card reveal">
      <h2>{{ isRegister ? t('shop.createAccount') : t('shop.welcome') }}</h2>
      <p class="demo-hint">{{ t('shop.demoHint') }}</p>
      <el-alert v-if="!auth.token && route.query.redirect" type="info" :closable="false" show-icon :title="t('shop.loginRequired')" class="login-alert" />
      <el-form :model="form" size="large" @submit.prevent="handleSubmit">
        <el-form-item>
          <el-input v-model="form.username" :placeholder="t('shop.usernamePlaceholder')" />
        </el-form-item>
        <el-form-item v-if="isRegister">
          <el-input v-model="form.email" :placeholder="t('shop.emailPlaceholder')" />
        </el-form-item>
        <el-form-item>
          <el-input v-model="form.password" type="password" :placeholder="t('shop.passwordPlaceholder')" show-password />
        </el-form-item>
        <el-form-item v-if="captchaImage">
          <div class="captcha-row">
            <el-input v-model="captchaCode" :placeholder="t('auth.captcha')" />
            <img :src="captchaImage" alt="captcha" class="captcha-img" @click="refreshCaptcha" />
          </div>
        </el-form-item>
        <el-button type="primary" style="width:100%" :loading="loading" native-type="submit" @click="handleSubmit">
          {{ isRegister ? t('common.register') : t('common.login') }}
        </el-button>
      </el-form>
      <p class="switch">
        <a @click="isRegister = !isRegister">
          {{ isRegister ? t('common.login') : t('common.register') }}
        </a>
        · <router-link to="/shop/forgot-password">{{ t('auth.forgotPassword') }}</router-link>
      </p>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { useI18n } from 'vue-i18n'
import { ElMessage } from 'element-plus'
import shopRequest from '../../api/shopRequest'
import { useShopAuthStore } from '../../stores/shopAuth'
import { useScrollReveal } from '../../composables/useScrollReveal'
import { useCaptcha } from '../../composables/useCaptcha'

useScrollReveal()
const { t } = useI18n()
const router = useRouter()
const route = useRoute()
const auth = useShopAuthStore()
const isRegister = ref(false)
const loading = ref(false)
const form = ref({ username: '', password: '', email: '' })
const { captchaImage, captchaCode, refreshCaptcha, captchaPayload } = useCaptcha()

onMounted(refreshCaptcha)

async function handleSubmit() {
  if (!form.value.username.trim() || !form.value.password) return
  loading.value = true
  try {
    const url = isRegister.value ? '/shop/auth/register' : '/shop/auth/login'
    const res = await shopRequest.post(url, { ...form.value, ...captchaPayload() })
    auth.setAuth(res.data)
    ElMessage.success(t('common.success'))
    router.push(route.query.redirect || '/shop')
  } catch {
    refreshCaptcha()
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.auth-page { display: flex; justify-content: center; padding: 40px 0; }
.auth-card {
  width: 100%;
  max-width: 400px;
  background: var(--shop-card);
  border-radius: 16px;
  padding: 40px;
  border: 1px solid var(--shop-border);
  box-shadow: var(--shop-card-shadow);
}
.auth-card h2 { text-align: center; margin-bottom: 8px; color: var(--shop-text); }
.demo-hint {
  text-align: center;
  font-size: 13px;
  color: var(--shop-text-muted);
  margin-bottom: 20px;
}
.login-alert { margin-bottom: 16px; }
.switch { text-align: center; margin-top: 16px; }
.switch a { color: #4f6ef7; cursor: pointer; font-size: 14px; }
.captcha-row { display: flex; gap: 8px; width: 100%; }
.captcha-img { height: 40px; border-radius: 6px; cursor: pointer; border: 1px solid var(--shop-border); }
</style>
