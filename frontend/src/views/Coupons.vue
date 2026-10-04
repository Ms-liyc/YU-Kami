<template>
  <div class="page-container">
    <PageHeader :title="t('coupon.title')" :subtitle="t('coupon.subtitle')">
      <template #extra>
        <el-button type="primary" :icon="Plus" @click="openDialog()">{{ t('coupon.add') }}</el-button>
      </template>
    </PageHeader>

    <div class="page-card">
      <div class="card-body table-scroll-wrap">
        <el-table :data="tableData" stripe v-loading="loading">
          <el-table-column prop="code" :label="t('coupon.code')" width="140">
            <template #default="{ row }">
              <el-tag effect="plain">{{ row.code }}</el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="name" :label="t('coupon.name')" min-width="120" />
          <el-table-column prop="typeLabel" :label="t('coupon.type')" width="110" />
          <el-table-column :label="t('coupon.discount')" width="110">
            <template #default="{ row }">
              <span v-if="row.type === 'PERCENT_OFF'">{{ row.discountValue }}%</span>
              <span v-else>{{ t('coupon.fixedOffDisplay', { amount: row.discountValue }) }}</span>
            </template>
          </el-table-column>
          <el-table-column :label="t('coupon.products')" min-width="140">
            <template #default="{ row }">
              <el-tag v-if="row.scope === 'ALL'" size="small">{{ t('coupon.scopeAll') }}</el-tag>
              <template v-else>
                <el-tag v-for="n in row.productNames" :key="n" size="small" style="margin:2px">{{ n }}</el-tag>
              </template>
            </template>
          </el-table-column>
          <el-table-column :label="t('coupon.usage')" width="100">
            <template #default="{ row }">
              {{ row.usedCount }} / {{ row.usageLimit ?? t('coupon.unlimited') }}
            </template>
          </el-table-column>
          <el-table-column :label="t('coupon.validity')" min-width="180">
            <template #default="{ row }">
              <div class="time-cell">{{ formatTime(row.startAt) }}</div>
              <div class="time-cell">{{ t('common.to') }} {{ formatTime(row.endAt) }}</div>
            </template>
          </el-table-column>
          <el-table-column prop="status" :label="t('common.status')" width="80">
            <template #default="{ row }">
              <el-tag :type="row.status === 1 ? 'success' : 'danger'" size="small">
                {{ row.status === 1 ? t('common.enabled') : t('common.disabled') }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column :label="t('common.actions')" width="140" fixed="right">
            <template #default="{ row }">
              <el-button link type="primary" @click="openDialog(row)">{{ t('common.edit') }}</el-button>
              <el-button link type="danger" @click="handleDelete(row.id)">{{ t('common.delete') }}</el-button>
            </template>
          </el-table-column>
        </el-table>
        <el-pagination v-model:current-page="page" :page-size="size" :total="total" @current-change="loadData" />
      </div>
    </div>

    <el-dialog v-model="dialogVisible" :title="form.id ? t('coupon.edit') : t('coupon.add')" width="600px" destroy-on-close>
      <el-form :model="form" label-width="100px">
        <el-form-item :label="t('coupon.code')" required>
          <el-input v-model="form.code" :placeholder="t('coupon.codePlaceholder')" :disabled="!!form.id" />
        </el-form-item>
        <el-form-item :label="t('coupon.name')" required><el-input v-model="form.name" /></el-form-item>
        <el-form-item :label="t('coupon.description')"><el-input v-model="form.description" type="textarea" :rows="2" /></el-form-item>
        <el-form-item :label="t('coupon.type')" required>
          <el-select v-model="form.type" style="width:100%">
            <el-option :label="t('coupon.typePercentOff')" value="PERCENT_OFF" />
            <el-option :label="t('coupon.typeFixedOff')" value="FIXED_OFF" />
          </el-select>
        </el-form-item>
        <el-form-item :label="form.type === 'PERCENT_OFF' ? t('coupon.valuePercent') : t('coupon.valueFixedOff')" required>
          <el-input-number v-model="form.discountValue" :min="0" :precision="2" style="width:100%" />
        </el-form-item>
        <el-form-item :label="t('promotion.minAmount')">
          <el-input-number v-model="form.minAmount" :min="0" :precision="2" style="width:100%" />
        </el-form-item>
        <el-form-item v-if="form.type === 'PERCENT_OFF'" :label="t('promotion.maxDiscount')">
          <el-input-number v-model="form.maxDiscount" :min="0" :precision="2" style="width:100%" />
        </el-form-item>
        <el-form-item :label="t('coupon.validity')" required>
          <el-date-picker v-model="timeRange" type="datetimerange" style="width:100%"
            value-format="YYYY-MM-DDTHH:mm:ss" />
        </el-form-item>
        <el-form-item :label="t('coupon.products')">
          <el-select v-model="form.productIds" multiple filterable :placeholder="t('coupon.productScopePlaceholder')" style="width:100%">
            <el-option v-for="p in products" :key="p.id" :label="p.name" :value="p.id" />
          </el-select>
        </el-form-item>
        <el-form-item :label="t('coupon.usageLimit')">
          <el-input-number v-model="form.usageLimit" :min="1" style="width:100%" :placeholder="t('coupon.usageLimitPlaceholder')" />
        </el-form-item>
        <el-form-item :label="t('coupon.perUserLimit')">
          <el-input-number v-model="form.perUserLimit" :min="1" style="width:100%" />
        </el-form-item>
        <el-form-item :label="t('common.status')">
          <el-switch v-model="form.status" :active-value="1" :inactive-value="0" />
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
const products = ref([])
const loading = ref(false)
const page = ref(1)
const size = ref(20)
const total = ref(0)
const dialogVisible = ref(false)
const form = ref({})
const timeRange = ref([])

function formatTime(val) {
  if (!val) return ''
  return String(val).replace('T', ' ').slice(0, 16)
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
    ElMessage.warning(t('coupon.fillRequired'))
    return
  }
  const payload = { ...form.value, startAt: timeRange.value[0], endAt: timeRange.value[1] }
  if (form.value.id) {
    await request.put(`/admin/coupons/${form.value.id}`, payload)
  } else {
    await request.post('/admin/coupons', payload)
  }
  ElMessage.success(t('common.saveSuccess'))
  dialogVisible.value = false
  loadData()
}

async function handleDelete(id) {
  await ElMessageBox.confirm(t('coupon.deleteConfirm'), t('common.tip'), { type: 'warning' })
  await request.delete(`/admin/coupons/${id}`)
  ElMessage.success(t('common.deleteSuccess'))
  loadData()
}

onMounted(() => { loadProducts(); loadData() })
</script>

<style scoped>
.time-cell { font-size: 12px; line-height: 1.5; }
</style>

