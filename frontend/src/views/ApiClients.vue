<template>
  <div class="page-container">
    <PageHeader :title="t('apiClient.title')" :subtitle="t('apiClient.subtitle')">
      <template #extra>
        <el-button type="primary" :icon="Plus" @click="openCreate">{{ t('apiClient.create') }}</el-button>
      </template>
    </PageHeader>

    <div class="page-card">
      <div class="card-body table-scroll-wrap">
        <el-table :data="tableData" stripe v-loading="loading">
          <el-table-column prop="name" :label="t('apiClient.name')" min-width="140" />
          <el-table-column prop="appKey" :label="t('apiClient.appKey')" min-width="200">
            <template #default="{ row }">
              <code class="mono">{{ row.appKey }}</code>
            </template>
          </el-table-column>
          <el-table-column prop="appSecret" :label="t('apiClient.appSecret')" min-width="160">
            <template #default="{ row }">
              <code class="mono">{{ row.appSecret }}</code>
            </template>
          </el-table-column>
          <el-table-column prop="status" :label="t('common.status')" width="90">
            <template #default="{ row }">
              <el-tag :type="row.status === 1 ? 'success' : 'danger'" size="small">
                {{ row.status === 1 ? t('common.enabled') : t('common.disabled') }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="createdAt" :label="t('order.createdAt')" width="170" />
          <el-table-column :label="t('common.actions')" width="140" fixed="right">
            <template #default="{ row }">
              <el-button link type="primary" @click="handleToggle(row)">
                {{ row.status === 1 ? t('admin.disableUser') : t('admin.enableUser') }}
              </el-button>
              <el-button link type="danger" @click="handleDelete(row.id)">{{ t('common.delete') }}</el-button>
            </template>
          </el-table-column>
        </el-table>
        <el-pagination v-model:current-page="page" :page-size="size" :total="total" @current-change="loadData" />
      </div>
    </div>

    <el-dialog v-model="createDialog" :title="t('apiClient.createTitle')" width="440px">
      <el-form label-width="80px">
        <el-form-item :label="t('apiClient.name')"><el-input v-model="createName" :placeholder="t('apiClient.namePlaceholder')" /></el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="createDialog = false">{{ t('common.cancel') }}</el-button>
        <el-button type="primary" :loading="creating" @click="handleCreate">{{ t('common.create') }}</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="secretDialog" :title="t('apiClient.secretTitle')" width="520px">
      <el-alert type="warning" :closable="false" show-icon style="margin-bottom:16px"
        :title="t('apiClient.secretOnceWarning')" />
      <el-descriptions :column="1" border>
        <el-descriptions-item :label="t('apiClient.appKey')"><code>{{ newClient.appKey }}</code></el-descriptions-item>
        <el-descriptions-item :label="t('apiClient.appSecret')"><code>{{ newClient.appSecret }}</code></el-descriptions-item>
      </el-descriptions>
      <template #footer>
        <el-button type="primary" @click="copySecret">{{ t('apiClient.copySecret') }}</el-button>
        <el-button @click="secretDialog = false">{{ t('admin.close') }}</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { Plus } from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox } from 'element-plus'
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
const secretDialog = ref(false)
const creating = ref(false)
const createName = ref('')
const newClient = ref({})

async function loadData() {
  loading.value = true
  try {
    const res = await request.get('/admin/api-clients', { params: { page: page.value, size: size.value } })
    tableData.value = res.data.records
    total.value = res.data.total
  } finally {
    loading.value = false
  }
}

function openCreate() {
  createName.value = ''
  createDialog.value = true
}

async function handleCreate() {
  creating.value = true
  try {
    const res = await request.post('/admin/api-clients', { name: createName.value })
    newClient.value = res.data
    createDialog.value = false
    secretDialog.value = true
    loadData()
  } finally {
    creating.value = false
  }
}

function copySecret() {
  navigator.clipboard.writeText(`AppKey: ${newClient.value.appKey}\nAppSecret: ${newClient.value.appSecret}`)
  ElMessage.success(t('admin.copied'))
}

async function handleToggle(row) {
  await request.post(`/admin/api-clients/${row.id}/toggle`)
  ElMessage.success(t('common.success'))
  loadData()
}

async function handleDelete(id) {
  await ElMessageBox.confirm(t('apiClient.deleteConfirm'), t('common.tip'), { type: 'warning' })
  await request.delete(`/admin/api-clients/${id}`)
  ElMessage.success(t('common.deleteSuccess'))
  loadData()
}

onMounted(loadData)
</script>

<style scoped>
.mono { font-family: 'Consolas', monospace; font-size: 13px; color: var(--primary); }
</style>
