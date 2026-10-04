<template>
  <div class="page-container">
    <PageHeader :title="t('promotion.title')" :subtitle="t('promotion.subtitle')">
      <template #extra>
        <el-button type="primary" :icon="Plus" @click="openDialog()">{{ t('promotion.add') }}</el-button>
      </template>
    </PageHeader>

    <div class="page-card">
      <div class="card-body table-scroll-wrap">
        <el-table :data="tableData" stripe v-loading="loading">
          <el-table-column prop="name" :label="t('promotion.name')" min-width="140" />
          <el-table-column prop="typeLabel" :label="t('promotion.type')" width="120" />
          <el-table-column :label="t('promotion.discount')" width="120">
            <template #default="{ row }">
              <span v-if="row.type === 'PERCENT_OFF'">{{ row.discountValue }}%</span>
              <span v-else-if="row.type === 'FIXED_OFF'">{{ t('promotion.fixedOffDisplay', { amount: row.discountValue }) }}</span>
              <span v-else>{{ t('promotion.overrideDisplay', { amount: row.discountValue }) }}</span>
            </template>
          </el-table-column>
          <el-table-column :label="t('promotion.products')" min-width="160">
            <template #default="{ row }">
              <el-tag v-if="row.scope === 'ALL'" size="small">{{ t('promotion.scopeAll') }}</el-tag>
              <template v-else>
                <el-tag v-for="n in row.productNames" :key="n" size="small" style="margin:2px">{{ n }}</el-tag>
              </template>
            </template>
          </el-table-column>
          <el-table-column :label="t('promotion.time')" min-width="200">
            <template #default="{ row }">
              <div class="time-cell">{{ formatTime(row.startAt) }}</div>
              <div class="time-cell">{{ t('common.to') }} {{ formatTime(row.endAt) }}</div>
            </template>
          </el-table-column>
          <el-table-column :label="t('promotion.tag')" width="90">
            <template #default="{ row }">
              <el-tag v-if="row.holiday" type="warning" size="small">{{ t('promotion.holiday') }}</el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="priority" :label="t('promotion.priority')" width="80" />
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

    <el-dialog v-model="dialogVisible" :title="form.id ? t('promotion.edit') : t('promotion.add')" width="600px" destroy-on-close>
      <el-form :model="form" label-width="100px">
        <el-form-item :label="t('promotion.formName')" required><el-input v-model="form.name" /></el-form-item>
        <el-form-item :label="t('promotion.description')"><el-input v-model="form.description" type="textarea" :rows="2" /></el-form-item>
        <el-form-item :label="t('promotion.type')" required>
          <el-select v-model="form.type" style="width:100%">
            <el-option :label="t('promotion.typePercentOff')" value="PERCENT_OFF" />
            <el-option :label="t('promotion.typeFixedOff')" value="FIXED_OFF" />
            <el-option :label="t('promotion.typeOverridePrice')" value="OVERRIDE_PRICE" />
          </el-select>
        </el-form-item>
        <el-form-item :label="valueLabel" required>
          <el-input-number v-model="form.discountValue" :min="0" :precision="2" style="width:100%" />
        </el-form-item>
        <el-form-item :label="t('promotion.minAmount')">
          <el-input-number v-model="form.minAmount" :min="0" :precision="2" style="width:100%" :placeholder="t('promotion.minAmountPlaceholder')" />
        </el-form-item>
        <el-form-item v-if="form.type === 'PERCENT_OFF'" :label="t('promotion.maxDiscount')">
          <el-input-number v-model="form.maxDiscount" :min="0" :precision="2" style="width:100%" />
        </el-form-item>
        <el-form-item :label="t('promotion.activityTime')" required>
          <el-date-picker v-model="timeRange" type="datetimerange" style="width:100%"
            :start-placeholder="t('promotion.startPlaceholder')" :end-placeholder="t('promotion.endPlaceholder')" value-format="YYYY-MM-DDTHH:mm:ss" />
        </el-form-item>
        <el-form-item :label="t('promotion.products')">
          <el-select v-model="form.productIds" multiple filterable :placeholder="t('promotion.productScopePlaceholder')" style="width:100%">
            <el-option v-for="p in products" :key="p.id" :label="p.name" :value="p.id" />
          </el-select>
        </el-form-item>
        <el-form-item :label="t('promotion.holiday')">
          <el-switch v-model="form.holiday" />
        </el-form-item>
        <el-form-item :label="t('promotion.priority')">
          <el-input-number v-model="form.priority" :min="0" style="width:100%" />
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
import { ref, computed, onMounted } from 'vue'
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

const valueLabel = computed(() => {
  if (form.value.type === 'PERCENT_OFF') return t('promotion.valuePercent')
  if (form.value.type === 'OVERRIDE_PRICE') return t('promotion.valueOverride')
  return t('promotion.valueFixedOff')
})

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
    ElMessage.warning(t('promotion.fillRequired'))
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
  ElMessage.success(t('common.saveSuccess'))
  dialogVisible.value = false
  loadData()
}

async function handleDelete(id) {
  await ElMessageBox.confirm(t('promotion.deleteConfirm'), t('common.tip'), { type: 'warning' })
  await request.delete(`/admin/promotions/${id}`)
  ElMessage.success(t('common.deleteSuccess'))
  loadData()
}

onMounted(() => { loadProducts(); loadData() })
</script>

<style scoped>
.time-cell { font-size: 12px; color: #64748b; line-height: 1.5; }
</style>
