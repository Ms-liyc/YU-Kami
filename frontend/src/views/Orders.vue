<template>
  <div class="page-container">
    <PageHeader :title="t('order.title')" :subtitle="t('order.subtitle')">
      <template #extra>
        <el-button @click="openPaymentConfig">{{ t('order.paymentConfig') }}</el-button>
      </template>
    </PageHeader>

    <el-alert
      v-if="setupStatus?.needsPaymentSetup"
      type="warning"
      :closable="false"
      show-icon
      class="setup-alert"
      :title="t('setup.paymentTitle')"
    >
      <p>{{ t('setup.paymentOrderHint') }}</p>
      <p v-if="setupStatus.paymentBaseUrl" class="env-hint">
        {{ t('setup.paymentBaseUrl') }}: <code>{{ setupStatus.paymentBaseUrl }}</code>
        <span class="env-note">（{{ t('setup.envConfigurable') }}）</span>
      </p>
    </el-alert>
    <div class="page-card">
      <div class="card-body">
        <div class="filter-bar">
          <el-select v-model="status" clearable placeholder="Status" style="width:140px" @change="loadData">
            <el-option :label="t('order.statusPending')" value="PENDING" />
            <el-option :label="t('order.statusPaid')" value="PAID" />
            <el-option :label="t('order.statusDelivered')" value="DELIVERED" />
            <el-option :label="t('order.statusCancelled')" value="CANCELLED" />
          </el-select>
        </div>
        <el-table :data="tableData" stripe v-loading="loading">
          <el-table-column prop="orderNo" :label="t('shop.orderNo')" min-width="180" />
          <el-table-column prop="username" :label="t('order.user')" width="120" />
          <el-table-column prop="productName" :label="t('order.product')" />
          <el-table-column prop="amount" label="¥" width="100" />
          <el-table-column prop="statusLabel" :label="t('shop.status')" width="100" />
          <el-table-column prop="paymentMethod" :label="t('order.paymentMethod')" width="100" />
          <el-table-column prop="createdAt" :label="t('order.createdAt')" width="170" />
          <el-table-column label="" width="100">
            <template #default="{ row }">
              <el-button v-if="row.status === 'PENDING'" link type="danger" @click="handleCancel(row.id)">取消</el-button>
            </template>
          </el-table-column>
        </el-table>
        <el-pagination v-model:current-page="page" :page-size="size" :total="total" @current-change="loadData" />
      </div>
    </div>

    <el-dialog v-model="configDialog" :title="t('order.paymentConfig')" width="680px">
      <el-alert type="info" :closable="false" show-icon style="margin-bottom:16px">
        <template #title>{{ t('setup.paymentConfigGuide') }}</template>
        <p>{{ t('setup.paymentConfigSteps') }}</p>
        <p v-if="setupStatus?.paymentBaseUrl" class="env-hint">
          PAYMENT_BASE_URL=<code>{{ setupStatus.paymentBaseUrl }}</code>
        </p>
      </el-alert>
      <el-table :data="paymentConfigs" size="small">
        <el-table-column prop="channel" label="Channel" />
        <el-table-column prop="appId" label="App ID" />
        <el-table-column prop="status" label="Status">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'info'" size="small">{{ row.status === 1 ? 'ON' : 'OFF' }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="">
          <template #default="{ row }">
            <el-button link @click="editConfig(row)">Edit</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-dialog>

    <el-dialog v-model="editDialog" title="Edit Payment" width="480px">
      <el-form :model="editForm" label-width="100px">
        <el-form-item label="App ID"><el-input v-model="editForm.appId" /></el-form-item>
        <el-form-item label="App Secret"><el-input v-model="editForm.appSecret" type="password" show-password /></el-form-item>
        <el-form-item label="Notify URL"><el-input v-model="editForm.notifyUrl" placeholder="留空使用默认 /api/payment/{channel}/notify" /></el-form-item>
        <el-form-item label="扩展配置 (JSON)">
          <el-input v-model="editForm.configJson" type="textarea" :rows="8" placeholder='支付宝: {"privateKey":"...","alipayPublicKey":"...","sandbox":true}
微信: {"mchId":"...","privateKey":"...","merchantSerialNumber":"...","apiV3Key":"..."}' />
        </el-form-item>
        <el-form-item label="Status">
          <el-switch v-model="editForm.status" :active-value="1" :inactive-value="0" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="editDialog = false">{{ t('common.cancel') }}</el-button>
        <el-button type="primary" @click="saveConfig">{{ t('common.save') }}</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import { useI18n } from 'vue-i18n'
import { ElMessage, ElMessageBox } from 'element-plus'
import request from '../api/request'
import PageHeader from '../components/PageHeader.vue'

const { t } = useI18n()
const route = useRoute()
const tableData = ref([])
const setupStatus = ref(null)
const paymentConfigs = ref([])
const loading = ref(false)
const page = ref(1)
const size = ref(20)
const total = ref(0)
const status = ref(null)
const configDialog = ref(false)
const editDialog = ref(false)
const editForm = ref({})

async function loadData() {
  loading.value = true
  try {
    const res = await request.get('/admin/orders', { params: { page: page.value, size: size.value, status: status.value } })
    tableData.value = res.data.records
    total.value = res.data.total
  } finally {
    loading.value = false
  }
}

async function loadConfigs() {
  const res = await request.get('/admin/orders/payment-config')
  paymentConfigs.value = res.data
}

async function loadSetupStatus() {
  try {
    const res = await request.get('/admin/setup/status')
    setupStatus.value = res.data
  } catch {
    setupStatus.value = null
  }
}

function openPaymentConfig() {
  configDialog.value = true
}

function editConfig(row) {
  editForm.value = { ...row }
  editDialog.value = true
}

async function saveConfig() {
  await request.put(`/admin/orders/payment-config/${editForm.value.id}`, editForm.value)
  ElMessage.success('OK')
  editDialog.value = false
  loadConfigs()
}

async function handleCancel(id) {
  await ElMessageBox.confirm('Confirm cancel?', 'Tip')
  await request.post(`/admin/orders/${id}/cancel`)
  ElMessage.success('OK')
  loadData()
}

onMounted(async () => {
  await Promise.all([loadData(), loadConfigs(), loadSetupStatus()])
  if (route.query.openPayment === '1') {
    configDialog.value = true
  }
})
</script>

<style scoped>
.setup-alert { margin-bottom: 16px; }
.env-hint { margin-top: 8px; font-size: 13px; }
.env-hint code { background: #f1f5f9; padding: 2px 6px; border-radius: 4px; }
.env-note { color: var(--text-secondary); margin-left: 6px; }
</style>
