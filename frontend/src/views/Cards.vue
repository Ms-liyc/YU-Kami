<template>
  <div class="page-container">
    <PageHeader title="卡密管理" subtitle="批量生成、查询与作废卡密">
      <template #extra>
        <div class="btn-group">
          <el-button :icon="Download" @click="handleExport">导出 CSV</el-button>
          <el-button type="primary" :icon="Plus" @click="genDialog = true">批量生成</el-button>
        </div>
      </template>
    </PageHeader>

    <div class="page-card">
      <div class="card-body">
        <div class="filter-bar">
          <el-select v-model="status" clearable placeholder="全部状态" style="width:140px" @change="loadData">
            <el-option label="未使用" :value="0" />
            <el-option label="已使用" :value="1" />
            <el-option label="已作废" :value="2" />
            <el-option label="已过期" :value="3" />
          </el-select>
          <el-button :icon="Refresh" @click="loadData">刷新</el-button>
        </div>
        <el-table :data="tableData" stripe v-loading="loading">
          <el-table-column prop="id" label="ID" width="180" show-overflow-tooltip />
          <el-table-column prop="batchId" label="批次ID" width="180" show-overflow-tooltip />
          <el-table-column prop="keyChecksum" label="校验码" width="100" />
          <el-table-column prop="status" label="状态" width="100">
            <template #default="{ row }">
              <el-tag :type="statusMap[row.status]?.type" size="small" effect="light">
                {{ statusMap[row.status]?.label }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="redeemUser" label="兑换用户" min-width="120" />
          <el-table-column prop="redeemAt" label="兑换时间" width="170" />
          <el-table-column prop="createdAt" label="创建时间" width="170" />
          <el-table-column label="操作" width="80" fixed="right">
            <template #default="{ row }">
              <el-button v-if="row.status === 0" link type="danger" @click="handleRevoke(row.id)">作废</el-button>
            </template>
          </el-table-column>
        </el-table>
        <el-pagination v-model:current-page="page" :page-size="size" :total="total" @current-change="loadData" />
      </div>
    </div>

    <el-dialog v-model="genDialog" title="批量生成卡密" width="520px" destroy-on-close>
      <el-form :model="genForm" label-width="90px">
        <el-form-item label="产品">
          <el-select v-model="genForm.productId" style="width:100%" placeholder="选择产品">
            <el-option v-for="p in products" :key="p.id" :label="p.name" :value="p.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="数量">
          <el-input-number v-model="genForm.count" :min="1" :max="10000" style="width:100%" />
        </el-form-item>
        <el-form-item label="前缀"><el-input v-model="genForm.prefix" placeholder="可选，如 VIP" /></el-form-item>
        <el-form-item label="备注"><el-input v-model="genForm.remark" /></el-form-item>
        <el-form-item label="过期时间">
          <el-date-picker v-model="genForm.expireAt" type="datetime" style="width:100%" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="genDialog = false">取消</el-button>
        <el-button type="primary" :loading="generating" @click="handleGenerate">生成</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="resultDialog" title="生成成功" width="640px">
      <el-alert type="warning" :closable="false" show-icon style="margin-bottom:16px"
        title="请立即保存以下卡密，系统不存储明文，关闭后无法找回！" />
      <el-input type="textarea" :rows="14" :model-value="generatedKeys.join('\n')" readonly />
      <template #footer>
        <el-button type="primary" :icon="CopyDocument" @click="copyKeys">复制全部</el-button>
        <el-button @click="resultDialog = false">关闭</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { Plus, Download, Refresh, CopyDocument } from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import request from '../api/request'
import { downloadExport } from '../api/export'
import PageHeader from '../components/PageHeader.vue'

const statusMap = {
  0: { label: '未使用', type: 'success' },
  1: { label: '已使用', type: 'info' },
  2: { label: '已作废', type: 'danger' },
  3: { label: '已过期', type: 'warning' }
}

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
    ElMessage.success(`成功生成 ${res.data.length} 张卡密`)
  } finally {
    generating.value = false
  }
}

function copyKeys() {
  navigator.clipboard.writeText(generatedKeys.value.join('\n'))
  ElMessage.success('已复制到剪贴板')
}

async function handleRevoke(id) {
  await ElMessageBox.confirm('确定作废该卡密？', '提示', { type: 'warning' })
  await request.post(`/admin/cards/${id}/revoke`)
  ElMessage.success('已作废')
  loadData()
}

async function handleExport() {
  let url = '/admin/export/cards?'
  if (status.value !== null) url += `status=${status.value}`
  await downloadExport(url, 'cards_export.csv')
  ElMessage.success('导出成功')
}

onMounted(() => { loadData(); loadProducts() })
</script>
