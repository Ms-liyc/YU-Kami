<template>
  <div class="profile-page" v-loading="loading">
    <div class="profile-card reveal">
      <h1>{{ t('shop.profileTitle') }}</h1>

      <div class="balance-strip">
        <div>
          <span class="balance-label">{{ t('shop.walletBalance') }}</span>
          <strong class="balance-num">¥{{ formatBalance(profile.balance) }}</strong>
        </div>
        <el-button type="primary" link @click="$router.push('/shop/wallet')">{{ t('shop.walletHistory') }}</el-button>
      </div>

      <el-divider />

      <el-form label-width="100px" @submit.prevent>
        <el-form-item :label="t('shop.username')">
          <el-input :model-value="profile.username" disabled />
        </el-form-item>
        <el-form-item :label="t('shop.nickname')">
          <el-input v-model="form.nickname" />
        </el-form-item>
        <el-form-item :label="t('shop.email')">
          <el-input v-model="form.email" />
        </el-form-item>
        <el-button type="primary" :loading="saving" @click="saveProfile">{{ t('common.save') }}</el-button>
      </el-form>

      <el-divider />

      <h3>{{ t('shop.changePassword') }}</h3>
      <el-form label-width="100px">
        <el-form-item :label="t('shop.oldPassword')">
          <el-input v-model="pwd.oldPassword" type="password" show-password />
        </el-form-item>
        <el-form-item :label="t('shop.newPassword')">
          <el-input v-model="pwd.newPassword" type="password" show-password />
        </el-form-item>
        <el-button type="warning" :loading="changingPwd" @click="changePassword">{{ t('shop.changePassword') }}</el-button>
      </el-form>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useI18n } from 'vue-i18n'
import { ElMessage } from 'element-plus'
import shopRequest from '../../api/shopRequest'
import { useShopAuthStore } from '../../stores/shopAuth'
import { useScrollReveal } from '../../composables/useScrollReveal'

useScrollReveal()
const { t } = useI18n()
const auth = useShopAuthStore()
const loading = ref(false)
const saving = ref(false)
const changingPwd = ref(false)
const profile = ref({})
const form = reactive({ nickname: '', email: '' })
const pwd = reactive({ oldPassword: '', newPassword: '' })

function formatBalance(val) {
  return Number(val ?? 0).toFixed(2)
}

async function loadProfile() {
  loading.value = true
  try {
    const res = await shopRequest.get('/shop/auth/me')
    profile.value = res.data
    form.nickname = res.data.nickname || ''
    form.email = res.data.email || ''
  } finally {
    loading.value = false
  }
}

async function saveProfile() {
  saving.value = true
  try {
    const res = await shopRequest.put('/shop/auth/profile', form)
    profile.value = res.data
    auth.nickname = res.data.nickname
    ElMessage.success(t('common.success'))
  } finally {
    saving.value = false
  }
}

async function changePassword() {
  if (!pwd.oldPassword || !pwd.newPassword) return
  changingPwd.value = true
  try {
    await shopRequest.put('/shop/auth/password', pwd)
    pwd.oldPassword = ''
    pwd.newPassword = ''
    ElMessage.success(t('shop.passwordChanged'))
  } finally {
    changingPwd.value = false
  }
}

onMounted(loadProfile)
</script>

<style scoped>
.profile-page { display: flex; justify-content: center; padding: 48px 0; }
.profile-card {
  width: 100%; max-width: 520px;
  background: var(--shop-card); border-radius: 20px; padding: 40px;
  border: 1px solid var(--shop-border); box-shadow: var(--shop-card-shadow);
}
.profile-card h1 { font-size: 24px; font-weight: 800; margin-bottom: 16px; color: var(--shop-text); }
.profile-card h3 { font-size: 16px; font-weight: 700; margin-bottom: 16px; color: var(--shop-text); }
.balance-strip {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  padding: 16px 18px;
  border-radius: 14px;
  background: var(--shop-section-alt);
  border: 1px solid var(--shop-border);
}
.balance-label { display: block; font-size: 13px; color: var(--shop-text-muted); margin-bottom: 4px; }
.balance-num { font-size: 22px; color: #4f6ef7; }
</style>
