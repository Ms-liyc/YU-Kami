<template>
  <div class="page-container">
    <PageHeader title="优惠券管理" subtitle="发布优惠券码，可限定适用商品与使用次数">
      <template #extra>
        <el-button type="primary" :icon="Plus" @click="openDialog()">发布优惠券</el-button>
      </template>
    </PageHeader>

    <div class="page-card">
      <div class="card-body">
        <el-table :data="tableData" stripe v-loading="loading">
          <el-table-column prop="code" label="券码" width="140">
            <template #default="{ row }">
              <el-tag effect="plain">{{ row.code }}</el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="name" label="名称" min-width="120" />
          <el-table-column prop="typeLabel" label="类型" width="110" />
          <el-table-column label="优惠" width="110">
            <template #default="{ row }">
              <span v-if="row.type === 'PERCENT_OFF'">{{ row.discountValue }}%</span>
              <span v-else>减 ¥{{ row.discountValue }}</span>
            </template>
          </el-table-column>
          <el-table-column label="适用商品" min-width="140">
            <template #default="{ row }">
              <el-tag v-if="row.scope === 'ALL'" size="small">全场</el-tag>
              <template v-else>
                <el-tag v-for="n in row.productNames" :key="n" size="small" style="margin:2px">{{ n }}</el-tag>
              </template>
            </template>
          </el-table-column>
          <el-table-column label="用量" width="100">
            <template #default="{ row }">
              {{ row.usedCount }} / {{ row.usageLimit ?? '∞' }}
            </template>
          </el-table-column>
          <el-table-column label="有效期" min-width="180">
            <template #default="{ row }">
              <div class="time-cell">{{ formatTime(row.startAt) }}</div>
              <div class="time-cell">至 {{ formatTime(row.endAt) }}</div>
            </template>
          </el-table-column>
          <el-table-column prop="status" label="状态" width="80">
            <template #default="{ row }">
              <el-tag :type="row.status === 1 ? 'success' : 'danger'" size="small">
                {{ row.status === 1 ? '启用' : '禁用' }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column label="操作" width="140" fixed="right">
            <template #default="{ row }">
              <el-button link type="primary" @click="openDialog(row)">编辑</el-button>
              <el-button link type="danger" @click="handleDelete(row.id)">删除</el-button>
            </template>
          </el-table-column>
        </el-table>
        <el-pagination v-model:current-page="page" :page-size="size" :total="total" @current-change="loadData" />
      </div>
    </div>

    <el-dialog v-model="dialogVisible" :title="form.id ? '编辑优惠券' : '发布优惠券'" width="600px" destroy-on-close>
      <el-form :model="form" label-width="100px">
        <el-form-item label="券码" required>
          <el-input v-model="form.code" placeholder="如 SPRING2026" :disabled="!!form.id" />
        </el-form-item>
        <el-form-item label="名称" required><el-input v-model="form.name" /></el-form-item>
        <el-form-item label="描述"><el-input v-model="form.description" type="textarea" :rows="2" /></el-form-item>
        <el-form-item label="类型" required>
          <el-select v-model="form.type" style="width:100%">
            <el-option label="百分比折扣" value="PERCENT_OFF" />
            <el-option label="满减（固定金额）" value="FIXED_OFF" />
          </el-select>
        </el-form-item>
        <el-form-item :label="form.type === 'PERCENT_OFF' ? '折扣(%)' : '减免(元)'" required>
          <el-input-number v-model="form.discountValue" :min="0" :precision="2" style="width:100%" />
        </el-form-item>
        <el-form-item label="最低消费">
          <el-input-number v-model="form.minAmount" :min="0" :precision="2" style="width:100%" />
        </el-form-item>
        <el-form-item v-if="form.type === 'PERCENT_OFF'" label="封顶减免">
          <el-input-number v-model="form.maxDiscount" :min="0" :precision="2" style="width:100%" />
        </el-form-item>
        <el-form-item label="有效期" required>
          <el-date-picker v-model="timeRange" type="datetimerange" style="width:100%"
            value-format="YYYY-MM-DDTHH:mm:ss" />
        </el-form-item>
        <el-form-item label="适用商品">
          <el-select v-model="form.productIds" multiple filterable placeholder="不选则全场通用" style="width:100%">
            <el-option v-for="p in products" :key="p.id" :label="p.name" :value="p.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="总发行量">
          <el-input-number v-model="form.usageLimit" :min="1" style="width:100%" placeholder="不填则不限" />
        </el-form-item>
        <el-form-item label="每人限用">
          <el-input-number v-model="form.perUserLimit" :min="1" style="width:100%" />
        </el-form-item>
        <el-form-item label="状态">
          <el-switch v-model="form.status" :active-value="1" :inactive-value="0" />
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
const products = ref([])
const loading = ref(false)
const page = ref(1)
const size = ref(20)
const total = ref(0)
const dialogVisible = ref(false)
const form = ref({})
const timeRange = ref([])

function formatTime(t) {
  if (!t) return ''
  return String(t).replace('T', ' ').slice(0, 16)
}

async function loadProducts() {
  const res = await request.get('/admin/products', { params: { page: 1, size: 200 } })
  products.value = res.data.records || []
}

async function loadData() {
  loading.value = true
  try {
    const res = await request.get('/admin/coupons', { params: { page: page.value, size: size.value } })
    tableData.value = res.data.records
    total.value = res.data.total
  } finally {
    loading.value = false
  }
}

function openDialog(row) {
  if (row) {
    form.value = { ...row, productIds: row.productIds || [], perUserLimit: row.perUserLimit ?? 1, status: row.status ?? 1 }
    timeRange.value = [row.startAt, row.endAt]
  } else {
    form.value = { type: 'FIXED_OFF', status: 1, perUserLimit: 1, productIds: [] }
    timeRange.value = []
  }
  dialogVisible.value = true
}

async function handleSave() {
  if (!form.value.code || !form.value.name || !timeRange.value?.length) {
    ElMessage.warning('请填写券码、名称和有效期')
    return
  }
  const payload = { ...form.value, startAt: timeRange.value[0], endAt: timeRange.value[1] }
  if (form.value.id) {
    await request.put(`/admin/coupons/${form.value.id}`, payload)
  } else {
    await request.post('/admin/coupons', payload)
  }
  ElMessage.success('保存成功')
  dialogVisible.value = false
  loadData()
}

async function handleDelete(id) {
  await ElMessageBox.confirm('确定删除该优惠券？', '提示', { type: 'warning' })
  await request.delete(`/admin/coupons/${id}`)
  ElMessage.success('已删除')
  loadData()
}

onMounted(() => { loadProducts(); loadData() })
</script>

<style scoped>
.time-cell { font-size: 12px; color: #64748b; line-height: 1.5; }
</style>
