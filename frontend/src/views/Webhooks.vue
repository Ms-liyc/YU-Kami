<template>
  <div class="page-container">
    <PageHeader :title="t('webhook.title')" :subtitle="t('webhook.subtitle')">
      <template #extra>
        <el-button type="primary" :icon="Plus" @click="openDialog()">{{ t('webhook.add') }}</el-button>
      </template>
    </PageHeader>

    <el-row :gutter="16">
      <el-col :span="14">
        <div class="page-card">
          <div class="card-header"><h3>{{ t('webhook.configTitle') }}</h3></div>
          <div class="card-body">
            <el-table :data="tableData" stripe v-loading="loading">
              <el-table-column prop="name" :label="t('webhook.name')" min-width="120" />
              <el-table-column prop="url" :label="t('webhook.url')" min-width="200" show-overflow-tooltip />
              <el-table-column prop="events" :label="t('webhook.events')" width="160">
                <template #default="{ row }">
                  <el-tag v-for="e in row.events.split(',')" :key="e" size="small" style="margin-right:4px">{{ e }}</el-tag>
                </template>
              </el-table-column>
              <el-table-column prop="status" :label="t('common.status')" width="80">
                <template #default="{ row }">
                  <el-tag :type="row.status === 1 ? 'success' : 'danger'" size="small">
                    {{ row.status === 1 ? t('webhook.enabled') : t('webhook.disabled') }}
                  </el-tag>
                </template>
              </el-table-column>
              <el-table-column :label="t('common.actions')" width="180" fixed="right">
                <template #default="{ row }">
                  <el-button link type="primary" @click="openDialog(row)">{{ t('common.edit') }}</el-button>
                  <el-button link @click="handleToggle(row.id)">{{ row.status === 1 ? t('webhook.disable') : t('webhook.enable') }}</el-button>
                  <el-button link type="danger" @click="handleDelete(row.id)">{{ t('common.delete') }}</el-button>
                </template>
              </el-table-column>
            </el-table>
            <el-pagination v-model:current-page="page" :page-size="size" :total="total" @current-change="loadData" />
          </div>
        </div>
      </el-col>
      <el-col :span="10">
        <div class="page-card">
          <div class="card-header"><h3>{{ t('webhook.logTitle') }}</h3></div>
          <div class="card-body" style="padding:0">
            <el-table :data="logs" stripe size="small" max-height="480">
              <el-table-column prop="event" :label="t('webhook.events')" width="130" />
              <el-table-column prop="statusCode" :label="t('webhook.statusCode')" width="70" />
              <el-table-column prop="success" :label="t('webhook.result')" width="70">
                <template #default="{ row }">
                  <el-tag :type="row.success === 1 ? 'success' : 'danger'" size="small">
                    {{ row.success === 1 ? t('webhook.success') : t('webhook.fail') }}
                  </el-tag>
                </template>
              </el-table-column>
              <el-table-column prop="createdAt" :label="t('order.createdAt')" width="160" />
            </el-table>
          </div>
        </div>
        <div class="page-card" style="margin-top:16px">
          <div class="card-body">
            <h4 style="margin-bottom:12px;font-size:14px">{{ t('webhook.guideTitle') }}</h4>
            <el-descriptions :column="1" size="small" border>
              <el-descriptions-item :label="t('webhook.method')">POST JSON</el-descriptions-item>
              <el-descriptions-item :label="t('webhook.signHeader')">X-YK-Signature (HMAC-SHA256)</el-descriptions-item>
              <el-descriptions-item :label="t('webhook.eventHeader')">X-YK-Event</el-descriptions-item>
              <el-descriptions-item :label="t('webhook.supportedEvents')">REDEEM_SUCCESS, ORDER_DELIVERED, RECHARGE_SUCCESS, ORDER_REFUNDED, LOW_STOCK</el-descriptions-item>
            </el-descriptions>
          </div>
        </div>
      </el-col>
    </el-row>

    <el-dialog v-model="dialogVisible" :title="form.id ? t('webhook.edit') : t('webhook.add')" width="520px" destroy-on-close>
      <el-form :model="form" label-width="90px">
        <el-form-item :label="t('webhook.name')"><el-input v-model="form.name" /></el-form-item>
        <el-form-item :label="t('webhook.url')"><el-input v-model="form.url" :placeholder="t('webhook.urlPlaceholder')" /></el-form-item>
        <el-form-item :label="t('webhook.secret')">
          <el-input v-model="form.secret" :placeholder="t('webhook.secretPlaceholder')" />
        </el-form-item>
        <el-form-item :label="t('webhook.subscribe')">
          <el-checkbox-group v-model="eventList">
            <el-checkbox label="REDEEM_SUCCESS">{{ t('webhook.eventRedeem') }}</el-checkbox>
            <el-checkbox label="ORDER_DELIVERED">{{ t('webhook.eventDelivered') }}</el-checkbox>
            <el-checkbox label="RECHARGE_SUCCESS">{{ t('webhook.eventRecharge') }}</el-checkbox>
            <el-checkbox label="ORDER_REFUNDED">{{ t('webhook.eventRefund') }}</el-checkbox>
            <el-checkbox label="LOW_STOCK">{{ t('webhook.eventLowStock') }}</el-checkbox>
          </el-checkbox-group>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">{{ t('common.cancel') }}</el-button>
        <el-button type="primary" @click="handleSave">{{ t('common.save') }}</el-button>
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
const logs = ref([])
const loading = ref(false)
const page = ref(1)
const size = ref(20)
const total = ref(0)
const dialogVisible = ref(false)
const form = ref({})
const eventList = ref(['REDEEM_SUCCESS'])

async function loadData() {
  loading.value = true
  try {
    const res = await request.get('/admin/webhooks', { params: { page: page.value, size: size.value } })
    tableData.value = res.data.records
    total.value = res.data.total
  } finally {
    loading.value = false
  }
}

async function loadLogs() {
  const res = await request.get('/admin/webhooks/logs', { params: { page: 1, size: 20 } })
  logs.value = res.data.records
}

function openDialog(row) {
  if (row) {
    form.value = { ...row, secret: '' }
    eventList.value = row.events ? row.events.split(',') : ['REDEEM_SUCCESS']
  } else {
    form.value = { name: '', url: '', secret: '' }
    eventList.value = ['REDEEM_SUCCESS']
  }
  dialogVisible.value = true
}

async function handleSave() {
  const payload = { ...form.value, events: eventList.value.join(',') }
  if (form.value.id) {
    await request.put(`/admin/webhooks/${form.value.id}`, payload)
  } else {
    const res = await request.post('/admin/webhooks', payload)
    if (res.data.secret) {
      ElMessageBox.alert(t('webhook.secretCreated', { secret: res.data.secret }), t('webhook.saveSecret'), { type: 'warning' })
    }
  }
  ElMessage.success(t('common.success'))
  dialogVisible.value = false
  loadData()
}

async function handleToggle(id) {
  await request.post(`/admin/webhooks/${id}/toggle`)
  ElMessage.success(t('common.success'))
  loadData()
}

async function handleDelete(id) {
  await ElMessageBox.confirm(t('webhook.deleteConfirm'), t('common.tip'), { type: 'warning' })
  await request.delete(`/admin/webhooks/${id}`)
  ElMessage.success(t('common.success'))
  loadData()
}

onMounted(() => { loadData(); loadLogs() })
</script>
