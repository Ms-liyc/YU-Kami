<template>
  <div class="page-container">
    <PageHeader :title="t('auth.securityTitle')" :subtitle="t('auth.securitySubtitle')" />

    <div class="page-card" style="margin-bottom: 16px">
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

    <div v-if="auth.isSuperAdmin" class="page-card" style="margin-bottom: 16px">
      <div class="card-body">
        <h3>{{ t('mail.title') }}</h3>
        <p class="hint">{{ t('mail.subtitle') }}</p>

        <el-alert
          v-if="mailStatus.hasStockAlertRecipient && !mailStatus.configured"
          type="warning"
          :closable="false"
          show-icon
          :title="t('mail.needsConfig')"
          class="status-alert"
        />

        <el-descriptions :column="1" border size="small" class="mail-desc">
          <el-descriptions-item :label="t('common.status')">
            <el-tag :type="mailStatus.configured ? 'success' : 'info'" size="small">
              {{ mailStatus.configured ? t('mail.configured') : t('mail.notConfigured') }}
            </el-tag>
          </el-descriptions-item>
          <el-descriptions-item :label="t('mail.stockAlert')">
            <el-tag :type="mailStatus.hasStockAlertRecipient ? 'success' : 'info'" size="small">
              {{ mailStatus.hasStockAlertRecipient ? t('mail.stockAlertSet') : t('mail.stockAlertUnset') }}
            </el-tag>
          </el-descriptions-item>
        </el-descriptions>

        <p class="hint qq-hint">{{ t('mail.qqHint') }}</p>
        <p class="hint">{{ t('mail.configHint') }}</p>

        <div class="test-box">
          <el-input v-model="testEmail" :placeholder="t('mail.testTo')" style="max-width:320px" />
          <el-button type="primary" :loading="mailTesting" :disabled="!testEmail" @click="sendTestMail">
            {{ t('mail.testSend') }}
          </el-button>
          <el-button link type="primary" @click="openMailGuide">{{ t('mail.viewGuide') }}</el-button>
        </div>
      </div>
    </div>

    <div v-if="auth.isSuperAdmin" class="page-card">
      <div class="card-body">
        <h3>{{ t('sms.title') }}</h3>
        <p class="hint">{{ t('sms.subtitle') }}</p>

        <el-alert
          v-if="smsStatus.hasStockAlertRecipient && !smsStatus.configured"
          type="warning"
          :closable="false"
          show-icon
          :title="t('sms.needsConfig')"
          class="status-alert"
        />

        <el-descriptions :column="1" border size="small" class="mail-desc">
          <el-descriptions-item :label="t('common.status')">
            <el-tag :type="smsStatus.configured ? 'success' : 'info'" size="small">
              {{ smsStatus.configured ? t('sms.configured') : t('sms.notConfigured') }}
            </el-tag>
          </el-descriptions-item>
          <el-descriptions-item :label="t('sms.provider')">
            <code>{{ smsStatus.provider || 'none' }}</code>
          </el-descriptions-item>
          <el-descriptions-item :label="t('sms.stockAlert')">
            <el-tag :type="smsStatus.hasStockAlertRecipient ? 'success' : 'info'" size="small">
              {{ smsStatus.hasStockAlertRecipient ? t('sms.stockAlertSet') : t('sms.stockAlertUnset') }}
            </el-tag>
          </el-descriptions-item>
        </el-descriptions>

        <p class="hint">{{ t('sms.providerHint') }}</p>
        <p class="hint">{{ t('sms.configHint') }}</p>

        <div class="test-box">
          <el-input v-model="testPhone" :placeholder="t('sms.testPhone')" style="max-width:320px" />
          <el-button type="primary" :loading="smsTesting" :disabled="!testPhone" @click="sendTestSms">
            {{ t('sms.testSend') }}
          </el-button>
          <el-button link type="primary" @click="openSmsGuide">{{ t('sms.viewGuide') }}</el-button>
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
import { useAuthStore } from '../stores/auth'
import PageHeader from '../components/PageHeader.vue'

const { t } = useI18n()
const auth = useAuthStore()
const loading = ref(false)
const totpEnabled = ref(false)
const setup = ref({})
const totpCode = ref('')
const mailStatus = ref({ configured: false, hasStockAlertRecipient: false })
const testEmail = ref('')
const mailTesting = ref(false)
const smsStatus = ref({ configured: false, hasStockAlertRecipient: false, provider: 'none' })
const testPhone = ref('')
const smsTesting = ref(false)

async function loadStatus() {
  const res = await request.get('/admin/auth/totp/status')
  totpEnabled.value = !!res.data?.enabled
}

async function loadMailStatus() {
  if (!auth.isSuperAdmin) return
  try {
    const res = await request.get('/admin/mail/status')
    mailStatus.value = res.data || { configured: false, hasStockAlertRecipient: false }
  } catch {
    mailStatus.value = { configured: false, hasStockAlertRecipient: false }
  }
}

async function loadSmsStatus() {
  if (!auth.isSuperAdmin) return
  try {
    const res = await request.get('/admin/sms/status')
    smsStatus.value = res.data || { configured: false, hasStockAlertRecipient: false, provider: 'none' }
  } catch {
    smsStatus.value = { configured: false, hasStockAlertRecipient: false, provider: 'none' }
  }
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

async function sendTestMail() {
  mailTesting.value = true
  try {
    await request.post('/admin/mail/test', { to: testEmail.value })
    ElMessage.success(t('mail.testSuccess'))
  } finally {
    mailTesting.value = false
  }
}

function openMailGuide() {
  window.open('https://github.com/Ms-liyc/YU-Kami/blob/main/docs/MAIL.md', '_blank')
}

async function sendTestSms() {
  smsTesting.value = true
  try {
    await request.post('/admin/sms/test', { phone: testPhone.value })
    ElMessage.success(t('sms.testSuccess'))
  } finally {
    smsTesting.value = false
  }
}

function openSmsGuide() {
  window.open('https://github.com/Ms-liyc/YU-Kami/blob/main/docs/SMS.md', '_blank')
}

onMounted(() => {
  loadStatus()
  loadMailStatus()
  loadSmsStatus()
})
</script>

<style scoped>
.status-alert { margin-bottom: 16px; }
.hint { color: var(--text-secondary); margin-bottom: 12px; font-size: 14px; line-height: 1.6; }
.qq-hint { margin-top: 12px; }
.setup-box { margin-top: 20px; padding: 16px; background: var(--page-bg); border: 1px solid var(--border); border-radius: 12px; }
.setup-box code { background: var(--primary-light); padding: 2px 6px; border-radius: 4px; }
.qr { margin-top: 12px; width: 180px; height: 180px; }
.actions { margin-top: 12px; display: flex; gap: 8px; }
.mail-desc { margin: 12px 0; max-width: 560px; }
.test-box { margin-top: 16px; display: flex; align-items: center; gap: 12px; flex-wrap: wrap; }
</style>
