<template>
  <div class="auth-page">
    <div class="auth-card">
      <el-result v-if="done" icon="success" :title="t('shop.emailVerified')" />
      <el-result v-else-if="error" icon="error" :title="error" />
      <div v-else v-loading="true" style="min-height:120px" />
      <router-link v-if="done" to="/shop/login" class="back-link">{{ t('auth.backLogin') }}</router-link>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import { useI18n } from 'vue-i18n'
import shopRequest from '../../api/shopRequest'

const { t } = useI18n()
const route = useRoute()
const done = ref(false)
const error = ref('')

onMounted(async () => {
  const token = route.query.token
  if (!token) {
    error.value = t('shop.verifyInvalid')
    return
  }
  try {
    await shopRequest.get('/shop/auth/verify-email', { params: { token } })
    done.value = true
  } catch (e) {
    error.value = e?.response?.data?.message || t('shop.verifyInvalid')
  }
})
</script>

<style scoped>
.auth-page { display: flex; justify-content: center; padding: 60px 16px; }
.auth-card { width: 100%; max-width: 420px; background: var(--shop-card); border-radius: 16px; padding: 32px; border: 1px solid var(--shop-border); text-align: center; }
.back-link { display: inline-block; margin-top: 16px; color: #4f6ef7; }
</style>
