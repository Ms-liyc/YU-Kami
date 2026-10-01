<template>
  <div class="page-container">
    <PageHeader title="API 客户端" subtitle="管理开放 API 接入凭证">
      <template #extra>
        <el-button type="primary" :icon="Plus" @click="openCreate">创建客户端</el-button>
      </template>
    </PageHeader>

    <div class="page-card">
      <div class="card-body">
        <el-table :data="tableData" stripe v-loading="loading">
          <el-table-column prop="name" label="名称" min-width="140" />
          <el-table-column prop="appKey" label="App Key" min-width="200">
            <template #default="{ row }">
              <code class="mono">{{ row.appKey }}</code>
            </template>
          </el-table-column>
          <el-table-column prop="appSecret" label="App Secret" min-width="160">
            <template #default="{ row }">
              <code class="mono">{{ row.appSecret }}</code>
            </template>
          </el-table-column>
          <el-table-column prop="status" label="状态" width="90">
            <template #default="{ row }">
              <el-tag :type="row.status === 1 ? 'success' : 'danger'" size="small">
                {{ row.status === 1 ? '启用' : '禁用' }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="createdAt" label="创建时间" width="170" />
          <el-table-column label="操作" width="140" fixed="right">
            <template #default="{ row }">
              <el-button link type="primary" @click="handleToggle(row)">
                {{ row.status === 1 ? '禁用' : '启用' }}
              </el-button>
              <el-button link type="danger" @click="handleDelete(row.id)">删除</el-button>
            </template>
          </el-table-column>
        </el-table>
        <el-pagination v-model:current-page="page" :page-size="size" :total="total" @current-change="loadData" />
      </div>
    </div>

    <el-dialog v-model="createDialog" title="创建 API 客户端" width="440px">
      <el-form label-width="80px">
        <el-form-item label="名称"><el-input v-model="createName" placeholder="如：商城系统" /></el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="createDialog = false">取消</el-button>
        <el-button type="primary" :loading="creating" @click="handleCreate">创建</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="secretDialog" title="密钥已生成" width="520px">
      <el-alert type="warning" :closable="false" show-icon style="margin-bottom:16px"
        title="App Secret 仅显示一次，请立即保存！" />
      <el-descriptions :column="1" border>
        <el-descriptions-item label="App Key"><code>{{ newClient.appKey }}</code></el-descriptions-item>
        <el-descriptions-item label="App Secret"><code>{{ newClient.appSecret }}</code></el-descriptions-item>
      </el-descriptions>
      <template #footer>
        <el-button type="primary" @click="copySecret">复制密钥</el-button>
        <el-button @click="secretDialog = false">关闭</el-button>
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
  ElMessage.success('已复制')
}

async function handleToggle(row) {
  await request.post(`/admin/api-clients/${row.id}/toggle`)
  ElMessage.success('操作成功')
  loadData()
}

async function handleDelete(id) {
  await ElMessageBox.confirm('确定删除该客户端？', '提示', { type: 'warning' })
  await request.delete(`/admin/api-clients/${id}`)
  ElMessage.success('删除成功')
  loadData()
}

onMounted(loadData)
</script>

<style scoped>
.mono { font-family: 'Consolas', monospace; font-size: 13px; color: var(--primary); }
</style>
