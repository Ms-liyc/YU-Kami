<template>
  <div class="page-container">
    <PageHeader title="Webhook 管理" subtitle="兑换成功后自动回调通知第三方系统">
      <template #extra>
        <el-button type="primary" :icon="Plus" @click="openDialog()">新增 Webhook</el-button>
      </template>
    </PageHeader>

    <el-row :gutter="16">
      <el-col :span="14">
        <div class="page-card">
          <div class="card-header"><h3>Webhook 配置</h3></div>
          <div class="card-body">
            <el-table :data="tableData" stripe v-loading="loading">
              <el-table-column prop="name" label="名称" min-width="120" />
              <el-table-column prop="url" label="回调 URL" min-width="200" show-overflow-tooltip />
              <el-table-column prop="events" label="事件" width="160">
                <template #default="{ row }">
                  <el-tag v-for="e in row.events.split(',')" :key="e" size="small" style="margin-right:4px">{{ e }}</el-tag>
                </template>
              </el-table-column>
              <el-table-column prop="status" label="状态" width="80">
                <template #default="{ row }">
                  <el-tag :type="row.status === 1 ? 'success' : 'danger'" size="small">
                    {{ row.status === 1 ? '启用' : '禁用' }}
                  </el-tag>
                </template>
              </el-table-column>
              <el-table-column label="操作" width="180" fixed="right">
                <template #default="{ row }">
                  <el-button link type="primary" @click="openDialog(row)">编辑</el-button>
                  <el-button link @click="handleToggle(row.id)">{{ row.status === 1 ? '禁用' : '启用' }}</el-button>
                  <el-button link type="danger" @click="handleDelete(row.id)">删除</el-button>
                </template>
              </el-table-column>
            </el-table>
            <el-pagination v-model:current-page="page" :page-size="size" :total="total" @current-change="loadData" />
          </div>
        </div>
      </el-col>
      <el-col :span="10">
        <div class="page-card">
          <div class="card-header"><h3>推送日志</h3></div>
          <div class="card-body" style="padding:0">
            <el-table :data="logs" stripe size="small" max-height="480">
              <el-table-column prop="event" label="事件" width="130" />
              <el-table-column prop="statusCode" label="状态码" width="70" />
              <el-table-column prop="success" label="结果" width="70">
                <template #default="{ row }">
                  <el-tag :type="row.success === 1 ? 'success' : 'danger'" size="small">
                    {{ row.success === 1 ? '成功' : '失败' }}
                  </el-tag>
                </template>
              </el-table-column>
              <el-table-column prop="createdAt" label="时间" width="160" />
            </el-table>
          </div>
        </div>
        <div class="page-card" style="margin-top:16px">
          <div class="card-body">
            <h4 style="margin-bottom:12px;font-size:14px">回调说明</h4>
            <el-descriptions :column="1" size="small" border>
              <el-descriptions-item label="请求方式">POST JSON</el-descriptions-item>
              <el-descriptions-item label="签名头">X-YK-Signature (HMAC-SHA256)</el-descriptions-item>
              <el-descriptions-item label="事件头">X-YK-Event</el-descriptions-item>
              <el-descriptions-item label="支持事件">REDEEM_SUCCESS, ORDER_DELIVERED, RECHARGE_SUCCESS, ORDER_REFUNDED, LOW_STOCK</el-descriptions-item>
            </el-descriptions>
          </div>
        </div>
      </el-col>
    </el-row>

    <el-dialog v-model="dialogVisible" :title="form.id ? '编辑 Webhook' : '新增 Webhook'" width="520px" destroy-on-close>
      <el-form :model="form" label-width="90px">
        <el-form-item label="名称"><el-input v-model="form.name" /></el-form-item>
        <el-form-item label="回调 URL"><el-input v-model="form.url" placeholder="https://your-server.com/webhook" /></el-form-item>
        <el-form-item label="签名密钥">
          <el-input v-model="form.secret" placeholder="留空则自动生成" />
        </el-form-item>
        <el-form-item label="订阅事件">
          <el-checkbox-group v-model="eventList">
            <el-checkbox label="REDEEM_SUCCESS">兑换成功</el-checkbox>
            <el-checkbox label="ORDER_DELIVERED">订单发货</el-checkbox>
            <el-checkbox label="RECHARGE_SUCCESS">余额充值</el-checkbox>
            <el-checkbox label="ORDER_REFUNDED">订单退款</el-checkbox>
            <el-checkbox label="LOW_STOCK">库存不足</el-checkbox>
          </el-checkbox-group>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSave">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { Plus } from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import request from '../api/request'
import PageHeader from '../components/PageHeader.vue'

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
      ElMessageBox.alert(`Webhook 已创建，签名密钥：${res.data.secret}`, '请保存密钥', { type: 'warning' })
    }
  }
  ElMessage.success('保存成功')
  dialogVisible.value = false
  loadData()
}

async function handleToggle(id) {
  await request.post(`/admin/webhooks/${id}/toggle`)
  ElMessage.success('操作成功')
  loadData()
}

async function handleDelete(id) {
  await ElMessageBox.confirm('确定删除该 Webhook？', '提示', { type: 'warning' })
  await request.delete(`/admin/webhooks/${id}`)
  ElMessage.success('删除成功')
  loadData()
}

onMounted(() => { loadData(); loadLogs() })
</script>
