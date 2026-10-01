<template>
  <div class="page-container">
    <PageHeader title="促销活动" subtitle="配置满减、折扣与节假日特价，可限定适用商品">
      <template #extra>
        <el-button type="primary" :icon="Plus" @click="openDialog()">新增活动</el-button>
      </template>
    </PageHeader>

    <div class="page-card">
      <div class="card-body">
        <el-table :data="tableData" stripe v-loading="loading">
          <el-table-column prop="name" label="活动名称" min-width="140" />
          <el-table-column prop="typeLabel" label="类型" width="120" />
          <el-table-column label="优惠" width="120">
            <template #default="{ row }">
              <span v-if="row.type === 'PERCENT_OFF'">{{ row.discountValue }}%</span>
              <span v-else-if="row.type === 'FIXED_OFF'">减 ¥{{ row.discountValue }}</span>
              <span v-else>¥{{ row.discountValue }}</span>
            </template>
          </el-table-column>
          <el-table-column label="适用商品" min-width="160">
            <template #default="{ row }">
              <el-tag v-if="row.scope === 'ALL'" size="small">全场</el-tag>
              <template v-else>
                <el-tag v-for="n in row.productNames" :key="n" size="small" style="margin:2px">{{ n }}</el-tag>
              </template>
            </template>
          </el-table-column>
          <el-table-column label="时间" min-width="200">
            <template #default="{ row }">
              <div class="time-cell">{{ formatTime(row.startAt) }}</div>
              <div class="time-cell">至 {{ formatTime(row.endAt) }}</div>
            </template>
          </el-table-column>
          <el-table-column label="标签" width="90">
            <template #default="{ row }">
              <el-tag v-if="row.holiday" type="warning" size="small">节假日</el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="priority" label="优先级" width="80" />
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

    <el-dialog v-model="dialogVisible" :title="form.id ? '编辑活动' : '新增活动'" width="600px" destroy-on-close>
      <el-form :model="form" label-width="100px">
        <el-form-item label="名称" required><el-input v-model="form.name" /></el-form-item>
        <el-form-item label="描述"><el-input v-model="form.description" type="textarea" :rows="2" /></el-form-item>
        <el-form-item label="类型" required>
          <el-select v-model="form.type" style="width:100%">
            <el-option label="百分比折扣" value="PERCENT_OFF" />
            <el-option label="满减（固定金额）" value="FIXED_OFF" />
            <el-option label="特价/节日价" value="OVERRIDE_PRICE" />
          </el-select>
        </el-form-item>
        <el-form-item :label="valueLabel" required>
          <el-input-number v-model="form.discountValue" :min="0" :precision="2" style="width:100%" />
        </el-form-item>
        <el-form-item label="最低消费">
          <el-input-number v-model="form.minAmount" :min="0" :precision="2" style="width:100%" placeholder="不填则无门槛" />
        </el-form-item>
        <el-form-item v-if="form.type === 'PERCENT_OFF'" label="封顶减免">
          <el-input-number v-model="form.maxDiscount" :min="0" :precision="2" style="width:100%" />
        </el-form-item>
        <el-form-item label="活动时间" required>
          <el-date-picker v-model="timeRange" type="datetimerange" style="width:100%"
            start-placeholder="开始" end-placeholder="结束" value-format="YYYY-MM-DDTHH:mm:ss" />
        </el-form-item>
        <el-form-item label="适用商品">
          <el-select v-model="form.productIds" multiple filterable placeholder="不选则全场通用" style="width:100%">
            <el-option v-for="p in products" :key="p.id" :label="p.name" :value="p.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="节假日">
          <el-switch v-model="form.holiday" />
        </el-form-item>
        <el-form-item label="优先级">
          <el-input-number v-model="form.priority" :min="0" style="width:100%" />
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
import { ref, computed, onMounted } from 'vue'
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

const valueLabel = computed(() => {
  if (form.value.type === 'PERCENT_OFF') return '折扣(%)'
  if (form.value.type === 'OVERRIDE_PRICE') return '特价(元)'
  return '减免(元)'
})

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
    const res = await request.get('/admin/promotions', { params: { page: page.value, size: size.value } })
    tableData.value = res.data.records
    total.value = res.data.total
  } finally {
    loading.value = false
  }
}

function openDialog(row) {
  if (row) {
    form.value = {
      ...row,
      productIds: row.productIds || [],
      status: row.status ?? 1,
      priority: row.priority ?? 0,
      holiday: row.holiday ?? false
    }
    timeRange.value = [row.startAt, row.endAt]
  } else {
    form.value = { type: 'PERCENT_OFF', status: 1, priority: 0, holiday: false, productIds: [] }
    timeRange.value = []
  }
  dialogVisible.value = true
}

async function handleSave() {
  if (!form.value.name || !timeRange.value?.length) {
    ElMessage.warning('请填写名称和活动时间')
    return
  }
  const payload = {
    ...form.value,
    startAt: timeRange.value[0],
    endAt: timeRange.value[1]
  }
  if (form.value.id) {
    await request.put(`/admin/promotions/${form.value.id}`, payload)
  } else {
    await request.post('/admin/promotions', payload)
  }
  ElMessage.success('保存成功')
  dialogVisible.value = false
  loadData()
}

async function handleDelete(id) {
  await ElMessageBox.confirm('确定删除该活动？', '提示', { type: 'warning' })
  await request.delete(`/admin/promotions/${id}`)
  ElMessage.success('已删除')
  loadData()
}

onMounted(() => { loadProducts(); loadData() })
</script>

<style scoped>
.time-cell { font-size: 12px; color: #64748b; line-height: 1.5; }
</style>
