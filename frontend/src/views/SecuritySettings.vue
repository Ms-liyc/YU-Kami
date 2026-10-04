<template>
  <div class="page-container">
    <PageHeader :title="t('auth.securityTitle')" :subtitle="t('auth.securitySubtitle')" />
    <div class="page-card">
      <div class="card-body">
        <el-alert v-if="totpEnabled" type="success" :closable="false" show-icon :title="t('auth.totpEnabledHint')" class="status-alert" />
        <h3>{{ t('auth.totpTitle') }}</h3>
        <p class="hint">{{ t('auth.totpHint') }}</p>
        <el-button v-if="!totpEnabled" type="primary" :loading="loading" @click="loadSetup">{{ t('auth.totpSetup') }}</el-button>
        <div v-if="setup.secret && !totpEnabled" class="setup-box">
          <p>{{ t('auth.totpSecret') }}: <code>{{ setup.secret }}</code></p>
          <img v-if="setup.qrCodeUrl" :src="setup.qrCodeUrl" alt="QR" class="qr" />
          <el-input v-model="totpCode" :placeholder="t('auth.totpCode')" style="max-width:240px;margin-top:12px" />
          <div class="actions">
            <el-button type="success" @click="enableTotp">{{ t('auth.totpEnable') }}</el-button>
          </div>
        </div>
        <div v-if="totpEnabled" class="setup-box">
          <el-input v-model="totpCode" :placeholder="t('auth.totpCode')" style="max-width:240px" />
          <div class="actions">
            <el-button type="danger" @click="disableTotp">{{ t('auth.totpDisable') }}</el-button>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useI18n } from 'vue-i18n'
import { ElMessage } from 'element-plus'
import request from '../api/request'
import PageHeader from '../components/PageHeader.vue'

const { t } = useI18n()
const loading = ref(false)
const totpEnabled = ref(false)
const setup = ref({})
const totpCode = ref('')

async function loadStatus() {
  const res = await request.get('/admin/auth/totp/status')
  totpEnabled.value = !!res.data?.enabled
}

async function loadSetup() {
  loading.value = true
  try {
    const res = await request.get('/admin/auth/totp/setup')
    setup.value = res.data || {}
  } finally {
    loading.value = false
  }
}

async function enableTotp() {
  await request.post('/admin/auth/totp/enable', { totpCode: totpCode.value })
  ElMessage.success(t('common.success'))
  totpEnabled.value = true
  setup.value = {}
}

async function disableTotp() {
  await request.post('/admin/auth/totp/disable', { totpCode: totpCode.value })
  ElMessage.success(t('common.success'))
  totpEnabled.value = false
  setup.value = {}
}

onMounted(loadStatus)
</script>

<style scoped>
.status-alert { margin-bottom: 16px; }
.hint { color: var(--text-secondary); margin-bottom: 16px; font-size: 14px; }
.setup-box { margin-top: 20px; padding: 16px; background: var(--page-bg); border: 1px solid var(--border); border-radius: 12px; }
.setup-box code { background: var(--primary-light); padding: 2px 6px; border-radius: 4px; }
.qr { margin-top: 12px; width: 180px; height: 180px; }
.actions { margin-top: 12px; display: flex; gap: 8px; }
</style>
