<template>
  <el-alert
    v-if="visible"
    class="password-banner"
    type="warning"
    show-icon
    :closable="true"
    @close="dismiss"
  >
    <template #title>{{ t('auth.defaultPasswordTitle') }}</template>
    <p>{{ t('auth.defaultPasswordBody', { password: 'admin123' }) }}</p>
    <el-button type="primary" size="small" @click="dialogVisible = true">{{ t('auth.changePasswordNow') }}</el-button>
  </el-alert>

  <el-dialog v-model="dialogVisible" :title="t('auth.changePasswordTitle')" width="420px" @closed="resetForm">
    <el-form :model="form" label-width="90px">
      <el-form-item :label="t('auth.currentPassword')">
        <el-input v-model="form.oldPassword" type="password" show-password />
      </el-form-item>
      <el-form-item :label="t('auth.newPassword')">
        <el-input v-model="form.newPassword" type="password" show-password />
      </el-form-item>
    </el-form>
    <template #footer>
      <el-button @click="dialogVisible = false">{{ t('common.cancel') }}</el-button>
      <el-button type="primary" :loading="saving" @click="save">{{ t('common.save') }}</el-button>
    </template>
  </el-dialog>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useI18n } from 'vue-i18n'
import { ElMessage } from 'element-plus'
import request from '../api/request'

const { t } = useI18n()
const DISMISS_KEY = 'yukami-default-password-dismissed'

const visible = ref(false)
const dialogVisible = ref(false)
const saving = ref(false)
const form = ref({ oldPassword: '', newPassword: '' })

onMounted(() => {
  if (localStorage.getItem(DISMISS_KEY) === '1') return
  visible.value = localStorage.getItem('warnDefaultPassword') === '1'
})

function dismiss() {
  localStorage.setItem(DISMISS_KEY, '1')
  visible.value = false
}

function resetForm() {
  form.value = { oldPassword: '', newPassword: '' }
}

async function save() {
  saving.value = true
  try {
    await request.post('/admin/auth/change-password', form.value)
    ElMessage.success(t('auth.passwordChanged'))
    localStorage.removeItem('warnDefaultPassword')
    localStorage.setItem(DISMISS_KEY, '1')
    visible.value = false
    dialogVisible.value = false
  } finally {
    saving.value = false
  }
}
</script>

<style scoped>
.password-banner { margin-bottom: 16px; }
.password-banner p { margin: 6px 0 10px; line-height: 1.6; }
.password-banner code {
  background: var(--primary-light);
  color: var(--text-primary);
  padding: 2px 6px;
  border-radius: 4px;
}
</style>
