<template>
  <el-alert
    v-if="visible"
    class="password-banner"
    type="warning"
    show-icon
    :closable="true"
    @close="dismiss"
  >
    <template #title>安全提示：请尽快修改默认密码</template>
    <p>当前账号仍在使用初始密码 <code>admin123</code>，存在安全风险。</p>
    <el-button type="primary" size="small" @click="dialogVisible = true">立即修改</el-button>
  </el-alert>

  <el-dialog v-model="dialogVisible" title="修改密码" width="420px" @closed="resetForm">
    <el-form :model="form" label-width="90px">
      <el-form-item label="当前密码">
        <el-input v-model="form.oldPassword" type="password" show-password />
      </el-form-item>
      <el-form-item label="新密码">
        <el-input v-model="form.newPassword" type="password" show-password />
      </el-form-item>
    </el-form>
    <template #footer>
      <el-button @click="dialogVisible = false">取消</el-button>
      <el-button type="primary" :loading="saving" @click="save">保存</el-button>
    </template>
  </el-dialog>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import request from '../api/request'
import { useAuthStore } from '../stores/auth'

const DISMISS_KEY = 'yukami-default-password-dismissed'

const auth = useAuthStore()
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
    ElMessage.success('密码已修改')
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
.password-banner code { background: #fef3c7; padding: 2px 6px; border-radius: 4px; }
</style>
