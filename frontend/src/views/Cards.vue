<template>
  <div class="page-container">
    <PageHeader :title="t('admin.cardsTitle')" :subtitle="t('admin.cardsSubtitle')">
      <template #extra>
        <div class="btn-group">
          <el-button :icon="Upload" @click="importDialog = true">{{ t('admin.batchImport') }}</el-button>
          <el-dropdown @command="handleExport">
            <el-button :icon="Download">{{ t('common.export') }} <el-icon class="el-icon--right"><ArrowDown /></el-icon></el-button>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item command="csv">{{ t('admin.exportCsv') }}</el-dropdown-item>
                <el-dropdown-item command="xlsx">{{ t('admin.exportExcel') }}</el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
          <el-button type="primary" :icon="Plus" @click="genDialog = true">{{ t('admin.batchGenerate') }}</el-button>
        </div>
      </template>
    </PageHeader>

    <div class="page-card">
      <div class="card-body">
        <div class="filter-bar">
          <el-select v-model="status" clearable :placeholder="t('admin.allStatus')" style="width:140px" @change="loadData">
            <el-option :label="t('admin.cardStatusUnused')" :value="0" />
            <el-option :label="t('admin.cardStatusUsed')" :value="1" />
            <el-option :label="t('admin.cardStatusRevoked')" :value="2" />
            <el-option :label="t('admin.cardStatusExpired')" :value="3" />
          </el-select>
          <el-button :icon="Refresh" @click="loadData">{{ t('common.refresh') }}</el-button>
        </div>
        <el-table :data="tableData" stripe v-loading="loading">
          <el-table-column prop="id" label="ID" width="180" show-overflow-tooltip />
          <el-table-column prop="batchId" :label="t('admin.batchId')" width="180" show-overflow-tooltip />
          <el-table-column prop="keyChecksum" :label="t('admin.checksum')" width="100" />
          <el-table-column prop="status" :label="t('common.status')" width="100">
            <template #default="{ row }">
              <el-tag :type="statusMap[row.status]?.type" size="small" effect="light">
                {{ statusMap[row.status]?.label }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="redeemUser" :label="t('admin.redeemUser')" min-width="120" />
          <el-table-column prop="redeemAt" :label="t('admin.redeemAt')" width="170" />
          <el-table-column prop="createdAt" :label="t('order.createdAt')" width="170" />
          <el-table-column :label="t('common.actions')" width="80" fixed="right">
            <template #default="{ row }">
              <el-button v-if="row.status === 0" link type="danger" @click="handleRevoke(row.id)">{{ t('admin.revoke') }}</el-button>
            </template>
          </el-table-column>
        </el-table>
        <el-pagination v-model:current-page="page" :page-size="size" :total="total" @current-change="loadData" />
      </div>
    </div>

    <el-dialog v-model="genDialog" :title="t('admin.batchGenerateTitle')" width="520px" destroy-on-close>
      <el-form :model="genForm" label-width="90px">
        <el-form-item :label="t('order.product')">
          <el-select v-model="genForm.productId" style="width:100%" :placeholder="t('admin.selectProduct')">
            <el-option v-for="p in products" :key="p.id" :label="p.name" :value="p.id" />
          </el-select>
        </el-form-item>
        <el-form-item :label="t('admin.count')">
          <el-input-number v-model="genForm.count" :min="1" :max="10000" style="width:100%" />
        </el-form-item>
        <el-form-item :label="t('admin.prefix')"><el-input v-model="genForm.prefix" :placeholder="t('admin.prefixPlaceholder')" /></el-form-item>
        <el-form-item :label="t('admin.remark')"><el-input v-model="genForm.remark" /></el-form-item>
        <el-form-item :label="t('admin.expireAt')">
          <el-date-picker v-model="genForm.expireAt" type="datetime" style="width:100%" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="genDialog = false">{{ t('common.cancel') }}</el-button>
        <el-button type="primary" :loading="generating" @click="handleGenerate">{{ t('admin.batchGenerate') }}</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="importDialog" :title="t('admin.batchImportTitle')" width="520px" destroy-on-close>
      <el-alert type="info" :closable="false" show-icon style="margin-bottom:16px" :title="t('admin.importHint')" />
      <el-form label-width="90px">
        <el-form-item :label="t('order.product')">
          <el-select v-model="importForm.productId" style="width:100%" :placeholder="t('admin.selectProduct')">
            <el-option v-for="p in products" :key="p.id" :label="p.name" :value="p.id" />
          </el-select>
        </el-form-item>
        <el-form-item :label="t('admin.remark')"><el-input v-model="importForm.remark" :placeholder="t('admin.remarkOptional')" /></el-form-item>
        <el-form-item :label="t('admin.file')">
          <el-upload ref="uploadRef" :auto-upload="false" :limit="1" accept=".txt,.csv,.xlsx,.xls"
            :on-change="onFileChange" drag>
            <el-icon :size="40"><Upload /></el-icon>
            <div>{{ t('admin.uploadHint') }}</div>
          </el-upload>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="importDialog = false">{{ t('common.cancel') }}</el-button>
        <el-button type="primary" :loading="importing" @click="handleImport">{{ t('admin.startImport') }}</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="resultDialog" :title="t('admin.generateSuccessTitle')" width="640px">
      <el-alert type="warning" :closable="false" show-icon style="margin-bottom:16px" :title="t('admin.saveKeysWarning')" />
      <el-input type="textarea" :rows="14" :model-value="generatedKeys.join('\n')" readonly />
      <template #footer>
        <el-button type="primary" :icon="CopyDocument" @click="copyKeys">{{ t('admin.copyAll') }}</el-button>
        <el-button @click="resultDialog = false">{{ t('admin.close') }}</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useI18n } from 'vue-i18n'
import { Plus, Download, Refresh, CopyDocument, Upload, ArrowDown } from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import request from '../api/request'
import { downloadExport } from '../api/export'
import PageHeader from '../components/PageHeader.vue'

const { t } = useI18n()

const statusMap = computed(() => ({
  0: { label: t('admin.cardStatusUnused'), type: 'success' },
  1: { label: t('admin.cardStatusUsed'), type: 'info' },
  2: { label: t('admin.cardStatusRevoked'), type: 'danger' },
  3: { label: t('admin.cardStatusExpired'), type: 'warning' }
}))

const tableData = ref([])
const products = ref([])
const loading = ref(false)
const page = ref(1)
const size = ref(20)
const total = ref(0)
const status = ref(null)
const genDialog = ref(false)
const resultDialog = ref(false)
const generating = ref(false)
const generatedKeys = ref([])
const genForm = ref({ productId: null, count: 100, prefix: '', remark: '' })
const importDialog = ref(false)
const importing = ref(false)
const importFile = ref(null)
const importForm = ref({ productId: null, remark: '' })

async function loadData() {
  loading.value = true
  try {
    const res = await request.get('/admin/cards', { params: { page: page.value, size: size.value, status: status.value } })
    tableData.value = res.data.records
    total.value = res.data.total
  } finally {
    loading.value = false
  }
}

async function loadProducts() {
  const res = await request.get('/admin/products', { params: { page: 1, size: 100 } })
  products.value = res.data.records
}

async function handleGenerate() {
  generating.value = true
  try {
    const res = await request.post('/admin/cards/generate', genForm.value)
    generatedKeys.value = res.data
    genDialog.value = false
    resultDialog.value = true
    loadData()
    ElMessage.success(t('admin.generateCount', { count: res.data.length }))
  } finally {
    generating.value = false
  }
}

function copyKeys() {
  navigator.clipboard.writeText(generatedKeys.value.join('\n'))
  ElMessage.success(t('admin.copied'))
}

async function handleRevoke(id) {
  await ElMessageBox.confirm(t('admin.revokeConfirm'), t('common.tip'), { type: 'warning' })
  await request.post(`/admin/cards/${id}/revoke`)
  ElMessage.success(t('admin.revoked'))
  loadData()
}

function onFileChange(file) {
  importFile.value = file.raw
}

async function handleImport() {
  if (!importForm.value.productId) return ElMessage.warning(t('admin.selectProductRequired'))
  if (!importFile.value) return ElMessage.warning(t('admin.selectFileRequired'))
  importing.value = true
  try {
    const formData = new FormData()
    formData.append('file', importFile.value)
    formData.append('productId', importForm.value.productId)
    if (importForm.value.remark) formData.append('remark', importForm.value.remark)
    const res = await request.post('/admin/cards/import', formData, {
      headers: { 'Content-Type': 'multipart/form-data' }
    })
    ElMessage.success(t('admin.importResult', { success: res.data.success, skipped: res.data.skipped, failed: res.data.failed }))
    importDialog.value = false
    importFile.value = null
    loadData()
  } finally {
    importing.value = false
  }
}

async function handleExport(format) {
  let url = '/admin/export/cards'
  const params = []
  if (status.value !== null) params.push(`status=${status.value}`)
  if (params.length) url += '?' + params.join('&')
  const ext = format === 'xlsx' ? 'xlsx' : 'csv'
  await downloadExport(url, `cards_export.${ext}`, format)
  ElMessage.success(t('admin.exportSuccess'))
}

onMounted(() => { loadData(); loadProducts() })
</script>
