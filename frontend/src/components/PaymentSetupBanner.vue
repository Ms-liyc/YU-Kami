<template>
  <el-alert
    v-if="visible"
    class="payment-setup-banner"
    type="info"
    :closable="true"
    show-icon
    @close="dismiss"
  >
    <template #title>{{ t('setup.paymentTitle') }}</template>
    <p class="setup-text">{{ t('setup.paymentDesc') }}</p>
    <ul class="setup-list">
      <li>{{ t('setup.paymentMockHint') }}</li>
      <li>{{ t('setup.paymentRealHint') }}</li>
      <li v-if="status?.paymentBaseUrl">
        {{ t('setup.paymentBaseUrl') }}: <code>{{ status.paymentBaseUrl }}</code>
      </li>
    </ul>
    <div class="setup-actions">
      <el-button v-if="isSuperAdmin" type="primary" size="small" @click="goPaymentConfig">
        {{ t('setup.goPaymentConfig') }}
      </el-button>
      <el-button size="small" @click="openGuide">{{ t('setup.viewGuide') }}</el-button>
    </div>
  </el-alert>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useI18n } from 'vue-i18n'
import request from '../api/request'
import { useAuthStore } from '../stores/auth'

const DISMISS_KEY = 'yukami-payment-setup-dismissed'

const { t } = useI18n()
const router = useRouter()
const auth = useAuthStore()
const isSuperAdmin = computed(() => auth.isSuperAdmin)
const visible = ref(false)
const status = ref(null)

onMounted(async () => {
  if (localStorage.getItem(DISMISS_KEY) === '1') return
  try {
    const res = await request.get('/admin/setup/status')
    status.value = res.data
    visible.value = res.data?.needsPaymentSetup
  } catch {
    visible.value = false
  }
})

function dismiss() {
  localStorage.setItem(DISMISS_KEY, '1')
  visible.value = false
}

function goPaymentConfig() {
  router.push({ path: '/orders', query: { openPayment: '1' } })
}

function openGuide() {
  window.open('https://github.com/Ms-liyc/YU-Kami/blob/main/docs/PAYMENT.md', '_blank')
}
</script>

<style scoped>
.payment-setup-banner { margin-bottom: 16px; }
.setup-text { margin: 4px 0 8px; line-height: 1.6; }
.setup-list { margin: 0 0 12px 18px; padding: 0; line-height: 1.7; color: var(--text-secondary); }
.setup-list code { background: #f1f5f9; padding: 2px 6px; border-radius: 4px; font-size: 12px; }
.setup-actions { display: flex; gap: 8px; flex-wrap: wrap; }
</style>
