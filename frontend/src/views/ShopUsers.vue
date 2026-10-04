<template>
  <div class="page-container">
    <PageHeader :title="t('admin.shopUsersTitle')" :subtitle="t('admin.shopUsersSubtitle')">
      <template #extra>
        <el-input
          v-model="keyword"
          :placeholder="t('admin.shopUserSearch')"
          clearable
          class="search-input"
          @keyup.enter="loadData"
          @clear="loadData"
        />
        <el-button type="primary" @click="loadData">{{ t('common.search') }}</el-button>
      </template>
    </PageHeader>

    <div class="page-card">
      <div class="card-body table-scroll-wrap">
        <el-table :data="tableData" stripe v-loading="loading">
          <el-table-column prop="username" :label="t('shop.username')" width="120" />
          <el-table-column prop="nickname" :label="t('shop.nickname')" width="120" />
          <el-table-column prop="email" :label="t('shop.email')" min-width="160" show-overflow-tooltip />
          <el-table-column :label="t('shop.walletBalance')" width="110">
            <template #default="{ row }">
              <span class="balance-cell">¥{{ formatMoney(row.balance) }}</span>
            </template>
          </el-table-column>
          <el-table-column prop="status" :label="t('common.status')" width="90">
            <template #default="{ row }">
              <el-tag :type="row.status === 1 ? 'success' : 'danger'" size="small">
                {{ row.status === 1 ? t('admin.statusNormal') : t('admin.statusDisabled') }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="createdAt" :label="t('order.createdAt')" width="170" />
          <el-table-column :label="t('common.actions')" width="260" fixed="right">
            <template #default="{ row }">
              <el-button link type="primary" @click="openAdjust(row)">{{ t('admin.adjustBalance') }}</el-button>
              <el-button link @click="openTransactions(row)">{{ t('admin.walletTransactions') }}</el-button>
              <el-button
                v-if="row.status === 1"
                link
                type="danger"
                @click="toggleStatus(row, 0)"
              >{{ t('admin.disableUser') }}</el-button>
              <el-button
                v-else
                link
                type="success"
                @click="toggleStatus(row, 1)"
              >{{ t('admin.enableUser') }}</el-button>
            </template>
          </el-table-column>
        </el-table>
        <el-pagination
          v-model:current-page="page"
          :page-size="size"
          :total="total"
          layout="total, prev, pager, next"
          @current-change="loadData"
        />
      </div>
    </div>

    <el-dialog v-model="adjustDialog" :title="t('admin.adjustBalance')" width="440px" destroy-on-close>
      <p class="adjust-user">{{ adjustTarget?.username }} · {{ t('shop.walletBalance') }} ¥{{ formatMoney(adjustTarget?.balance) }}</p>
      <el-form label-width="88px">
        <el-form-item :label="t('admin.adjustAmount')">
          <el-input-number v-model="adjustForm.amount" :precision="2" :step="10" style="width:100%" />
          <p class="field-hint">{{ t('admin.adjustAmountHint') }}</p>
        </el-form-item>
        <el-form-item :label="t('shop.walletRemark')">
          <el-input v-model="adjustForm.remark" :placeholder="t('admin.adjustRemarkPlaceholder')" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="adjustDialog = false">{{ t('common.cancel') }}</el-button>
        <el-button type="primary" :loading="adjusting" @click="submitAdjust">{{ t('common.confirm') }}</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="txDialog" :title="t('admin.walletTransactions')" width="720px" destroy-on-close>
      <p class="adjust-user">{{ txTarget?.username }} · {{ t('shop.walletBalance') }} ¥{{ formatMoney(txTarget?.balance) }}</p>
      <el-table :data="txData" stripe v-loading="txLoading" size="small">
        <el-table-column prop="createdAt" :label="t('order.createdAt')" width="170" />
        <el-table-column prop="typeLabel" :label="t('shop.walletType')" width="110" />
        <el-table-column :label="t('shop.amount')" width="100">
          <template #default="{ row }">
            <span :class="row.amount >= 0 ? 'amount-plus' : 'amount-minus'">
              {{ row.amount >= 0 ? '+' : '' }}{{ row.amount }}
            </span>
          </template>
        </el-table-column>
        <el-table-column prop="orderNo" :label="t('shop.orderNo')" min-width="150" />
        <el-table-column prop="remark" :label="t('shop.walletRemark')" min-width="120" />
      </el-table>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useI18n } from 'vue-i18n'
import { ElMessage, ElMessageBox } from 'element-plus'
import request from '../api/request'
import PageHeader from '../components/PageHeader.vue'

const { t } = useI18n()
const tableData = ref([])
const loading = ref(false)
const page = ref(1)
const size = ref(20)
const total = ref(0)
const keyword = ref('')
const adjustDialog = ref(false)
const adjusting = ref(false)
const adjustTarget = ref(null)
const adjustForm = ref({ amount: 0, remark: '' })
const txDialog = ref(false)
const txLoading = ref(false)
const txTarget = ref(null)
const txData = ref([])

function formatMoney(val) {
  return Number(val ?? 0).toFixed(2)
}

async function loadData() {
  loading.value = true
  try {
    const res = await request.get('/admin/shop-users', {
      params: { page: page.value, size: size.value, keyword: keyword.value || undefined }
    })
    tableData.value = res.data.records
    total.value = res.data.total
  } finally {
    loading.value = false
  }
}

function openAdjust(row) {
  adjustTarget.value = row
  adjustForm.value = { amount: 0, remark: '' }
  adjustDialog.value = true
}

async function submitAdjust() {
  if (!adjustForm.value.amount) {
    ElMessage.warning(t('admin.adjustAmountRequired'))
    return
  }
  adjusting.value = true
  try {
    const res = await request.post(`/admin/shop-users/${adjustTarget.value.id}/wallet/adjust`, adjustForm.value)
    ElMessage.success(t('common.success'))
    adjustDialog.value = false
    const idx = tableData.value.findIndex(u => u.id === adjustTarget.value.id)
    if (idx >= 0) tableData.value[idx] = res.data
  } finally {
    adjusting.value = false
  }
}

async function toggleStatus(row, status) {
  const msg = status === 1 ? t('admin.confirmEnableUser') : t('admin.confirmDisableUser')
  await ElMessageBox.confirm(msg, t('common.confirm'))
  const res = await request.post(`/admin/shop-users/${row.id}/status`, { status })
  ElMessage.success(t('common.success'))
  const idx = tableData.value.findIndex(u => u.id === row.id)
  if (idx >= 0) tableData.value[idx] = res.data
}

async function openTransactions(row) {
  txTarget.value = row
  txDialog.value = true
  txLoading.value = true
  try {
    const res = await request.get(`/admin/shop-users/${row.id}/wallet/transactions`, { params: { page: 1, size: 50 } })
    txData.value = res.data?.records || []
  } finally {
    txLoading.value = false
  }
}

onMounted(loadData)
</script>

<style scoped>
.search-input { width: 220px; max-width: 100%; }
.balance-cell { font-weight: 700; color: #4f6ef7; }
.adjust-user { margin-bottom: 16px; color: var(--text-secondary); font-size: 14px; }
.field-hint { margin-top: 6px; font-size: 12px; color: var(--text-secondary); }
.amount-plus { color: #16a34a; font-weight: 600; }
.amount-minus { color: #ef4444; font-weight: 600; }
</style>
