<template>
  <div class="redeem-page">
    <div class="redeem-card reveal">
      <h1>{{ t('shop.redeemTitle') }}</h1>
      <p>{{ t('shop.redeemDesc') }}</p>
      <el-form label-width="100px" @submit.prevent="handleRedeem">
        <el-form-item :label="t('shop.cardKey')">
          <el-input v-model="form.cardKey" :placeholder="t('shop.cardKeyPlaceholder')" />
        </el-form-item>
        <el-form-item :label="t('shop.redeemUser')">
          <el-input v-model="form.redeemUser" :placeholder="t('shop.redeemUserPlaceholder')" />
        </el-form-item>
        <el-button type="primary" style="width:100%" :loading="loading" @click="handleRedeem">
          {{ t('shop.redeemBtn') }}
        </el-button>
      </el-form>

      <el-alert v-if="result" :type="result.success ? 'success' : 'error'" :title="result.message" show-icon class="result-alert" />
    </div>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { useI18n } from 'vue-i18n'
import axios from 'axios'
import { useShopAuthStore } from '../../stores/shopAuth'
import { useScrollReveal } from '../../composables/useScrollReveal'

useScrollReveal()
const { t } = useI18n()
const auth = useShopAuthStore()
const loading = ref(false)
const result = ref(null)
const form = reactive({
  cardKey: '',
  redeemUser: auth.username || ''
})

async function handleRedeem() {
  if (!form.cardKey.trim() || !form.redeemUser.trim()) return
  loading.value = true
  result.value = null
  try {
    const res = await axios.post('/api/v1/redeem', {
      cardKey: form.cardKey.trim(),
      redeemUser: form.redeemUser.trim()
    })
    if (res.data?.code === 200) {
      result.value = { success: true, message: res.data.data?.message || t('shop.redeemSuccess') }
    } else {
      result.value = { success: false, message: res.data?.message || t('shop.redeemFail') }
    }
  } catch (e) {
    result.value = { success: false, message: e.response?.data?.message || t('shop.redeemFail') }
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.redeem-page { display: flex; justify-content: center; padding: 48px 0; }
.redeem-card {
  width: 100%; max-width: 520px;
  background: var(--shop-card); border-radius: 20px; padding: 40px;
  border: 1px solid var(--shop-border); box-shadow: var(--shop-card-shadow);
}
.redeem-card h1 { font-size: 24px; font-weight: 800; margin-bottom: 8px; color: var(--shop-text); }
.redeem-card p { color: var(--shop-text-muted); margin-bottom: 24px; font-size: 14px; }
.result-alert { margin-top: 20px; }
</style>
