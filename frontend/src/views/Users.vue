<template>
  <div class="page-container">
    <PageHeader :title="t('sysUser.title')" :subtitle="t('sysUser.subtitle')">
      <template #extra>
        <el-button type="primary" :icon="Plus" @click="createDialog = true">{{ t('sysUser.add') }}</el-button>
      </template>
    </PageHeader>

    <div class="page-card">
      <div class="card-body table-scroll-wrap">
        <el-table :data="tableData" stripe v-loading="loading">
          <el-table-column prop="username" :label="t('sysUser.username')" width="140" />
          <el-table-column prop="nickname" :label="t('sysUser.nickname')" width="140" />
          <el-table-column prop="role" :label="t('sysUser.role')" width="130">
            <template #default="{ row }">
              <el-tag :type="row.role === 'SUPER_ADMIN' ? 'danger' : 'primary'" size="small" effect="light">
                {{ row.role === 'SUPER_ADMIN' ? t('sysUser.superAdmin') : t('sysUser.admin') }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="status" :label="t('common.status')" width="90">
            <template #default="{ row }">
              <el-tag :type="row.status === 1 ? 'success' : 'danger'" size="small">
                {{ row.status === 1 ? t('admin.statusNormal') : t('admin.statusDisabled') }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="createdAt" :label="t('order.createdAt')" width="170" />
          <el-table-column :label="t('common.actions')" width="200" fixed="right">
            <template #default="{ row }">
              <el-button link type="primary" @click="openReset(row)">{{ t('sysUser.resetPassword') }}</el-button>
              <el-button link :type="row.status === 1 ? 'warning' : 'success'" @click="handleToggle(row.id)">
                {{ row.status === 1 ? t('admin.disableUser') : t('admin.enableUser') }}
              </el-button>
            </template>
          </el-table-column>
        </el-table>
        <el-pagination v-model:current-page="page" :page-size="size" :total="total" @current-change="loadData" />
      </div>
    </div>

    <el-dialog v-model="createDialog" :title="t('sysUser.add')" width="440px" destroy-on-close>
      <el-form :model="form" label-width="80px">
        <el-form-item :label="t('sysUser.username')"><el-input v-model="form.username" /></el-form-item>
        <el-form-item :label="t('sysUser.password')"><el-input v-model="form.password" type="password" show-password /></el-form-item>
        <el-form-item :label="t('sysUser.nickname')"><el-input v-model="form.nickname" /></el-form-item>
        <el-form-item :label="t('sysUser.role')">
          <el-select v-model="form.role" style="width:100%">
            <el-option :label="t('sysUser.admin')" value="ADMIN" />
            <el-option :label="t('sysUser.superAdmin')" value="SUPER_ADMIN" />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="createDialog = false">{{ t('common.cancel') }}</el-button>
        <el-button type="primary" @click="handleCreate">{{ t('common.create') }}</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="resetDialog" :title="t('sysUser.resetPassword')" width="400px">
      <el-input v-model="newPassword" type="password" :placeholder="t('sysUser.newPasswordPlaceholder')" show-password />
      <template #footer>
        <el-button @click="resetDialog = false">{{ t('common.cancel') }}</el-button>
        <el-button type="primary" @click="handleReset">{{ t('common.confirm') }}</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { Plus } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import { useI18n } from 'vue-i18n'
import request from '../api/request'
import PageHeader from '../components/PageHeader.vue'

const { t } = useI18n()
const tableData = ref([])
const loading = ref(false)
const page = ref(1)
const size = ref(20)
const total = ref(0)
const createDialog = ref(false)
const resetDialog = ref(false)
const resetUserId = ref(null)
const newPassword = ref('')
const form = ref({ username: '', password: '', nickname: '', role: 'ADMIN' })

async function loadData() {
  loading.value = true
  try {
    const res = await request.get('/admin/users', { params: { page: page.value, size: size.value } })
    tableData.value = res.data.records
    total.value = res.data.total
  } finally {
    loading.value = false
  }
}

async function handleCreate() {
  await request.post('/admin/users', form.value)
  ElMessage.success(t('common.createSuccess'))
  createDialog.value = false
  form.value = { username: '', password: '', nickname: '', role: 'ADMIN' }
  loadData()
}

function openReset(row) {
  resetUserId.value = row.id
  newPassword.value = ''
  resetDialog.value = true
}

async function handleReset() {
  await request.post(`/admin/users/${resetUserId.value}/reset-password`, { password: newPassword.value })
  ElMessage.success(t('sysUser.resetSuccess'))
  resetDialog.value = false
}

async function handleToggle(id) {
  await request.post(`/admin/users/${id}/toggle`)
  ElMessage.success(t('common.success'))
  loadData()
}

onMounted(loadData)
</script>
