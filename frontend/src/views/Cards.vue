<template>
  <el-card>
    <template #header>
      <div class="card-header">
        <span>卡密管理</span>
        <el-button type="primary" @click="genDialog = true">批量生成</el-button>
      </div>
    </template>
    <el-form inline>
      <el-form-item label="状态">
        <el-select v-model="status" clearable placeholder="全部" @change="loadData">
          <el-option label="未使用" :value="0" />
          <el-option label="已使用" :value="1" />
          <el-option label="已作废" :value="2" />
          <el-option label="已过期" :value="3" />
        </el-select>
      </el-form-item>
    </el-form>
    <el-table :data="tableData" stripe>
      <el-table-column prop="id" label="ID" width="180" />
      <el-table-column prop="batchId" label="批次ID" width="180" />
      <el-table-column prop="productId" label="产品ID" width="180" />
      <el-table-column prop="keyChecksum" label="校验码" width="100" />
      <el-table-column prop="status" label="状态">
        <template #default="{ row }">
          <el-tag :type="statusMap[row.status]?.type">{{ statusMap[row.status]?.label }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="redeemUser" label="兑换用户" />
      <el-table-column prop="redeemAt" label="兑换时间" />
      <el-table-column label="操作" width="100">
        <template #default="{ row }">
          <el-button v-if="row.status === 0" link type="danger" @click="handleRevoke(row.id)">作废</el-button>
        </template>
      </el-table-column>
    </el-table>
    <el-pagination class="pagination" v-model:current-page="page" :page-size="size" :total="total" @current-change="loadData" />

    <el-dialog v-model="genDialog" title="批量生成卡密" width="500px">
      <el-form :model="genForm" label-width="100px">
        <el-form-item label="产品">
          <el-select v-model="genForm.productId" style="width:100%" placeholder="选择产品">
            <el-option v-for="p in products" :key="p.id" :label="p.name" :value="p.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="数量"><el-input-number v-model="genForm.count" :min="1" :max="10000" /></el-form-item>
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

    <el-dialog v-model="resultDialog" title="生成结果" width="600px">
      <p>共生成 {{ generatedKeys.length }} 张卡密（请妥善保存，系统不存储明文）：</p>
      <el-input type="textarea" :rows="12" :model-value="generatedKeys.join('\n')" readonly />
      <template #footer>
        <el-button type="primary" @click="copyKeys">复制全部</el-button>
        <el-button @click="resultDialog = false">关闭</el-button>
      </template>
    </el-dialog>
  </el-card>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import request from '../api/request'

const statusMap = {
  0: { label: '未使用', type: 'success' },
  1: { label: '已使用', type: 'info' },
  2: { label: '已作废', type: 'danger' },
  3: { label: '已过期', type: 'warning' }
}

const tableData = ref([])
const products = ref([])
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
  const res = await request.get('/admin/cards', { params: { page: page.value, size: size.value, status: status.value } })
  tableData.value = res.data.records
  total.value = res.data.total
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
    ElMessage.success('生成成功')
  } finally {
    generating.value = false
  }
}

function copyKeys() {
  navigator.clipboard.writeText(generatedKeys.value.join('\n'))
  ElMessage.success('已复制到剪贴板')
}

async function handleRevoke(id) {
  await ElMessageBox.confirm('确定作废该卡密？', '提示')
  await request.post(`/admin/cards/${id}/revoke`)
  ElMessage.success('已作废')
  loadData()
}

onMounted(() => { loadData(); loadProducts() })
</script>

<style scoped>
.card-header { display: flex; justify-content: space-between; align-items: center; }
.pagination { margin-top: 16px; justify-content: flex-end; }
</style>
