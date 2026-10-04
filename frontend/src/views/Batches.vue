<template>
  <div class="page-container">
    <PageHeader :title="t('admin.batchesTitle')" :subtitle="t('admin.batchesSubtitle')">
      <template #extra>
        <el-dropdown @command="handleExport">
          <el-button :icon="Download">{{ t('common.export') }} <el-icon class="el-icon--right"><ArrowDown /></el-icon></el-button>
          <template #dropdown>
            <el-dropdown-menu>
              <el-dropdown-item command="csv">{{ t('admin.exportCsv') }}</el-dropdown-item>
              <el-dropdown-item command="xlsx">{{ t('admin.exportExcel') }}</el-dropdown-item>
            </el-dropdown-menu>
          </template>
        </el-dropdown>
      </template>
    </PageHeader>

    <div class="page-card">
      <div class="card-body">
        <el-table :data="tableData" stripe v-loading="loading">
          <el-table-column prop="batchNo" :label="t('admin.batchNo')" min-width="180">
            <template #default="{ row }">
              <span class="batch-no">{{ row.batchNo }}</span>
            </template>
          </el-table-column>
          <el-table-column prop="productId" :label="t('admin.productId')" width="180" show-overflow-tooltip />
          <el-table-column prop="totalCount" :label="t('admin.totalCount')" width="80" align="center" />
          <el-table-column prop="usedCount" :label="t('admin.used')" width="80" align="center" />
          <el-table-column :label="t('admin.usageRate')" width="160">
            <template #default="{ row }">
              <el-progress :percentage="usageRate(row)" :stroke-width="8"
                :color="usageRate(row) > 80 ? '#10b981' : '#4f6ef7'" />
            </template>
          </el-table-column>
          <el-table-column prop="prefix" :label="t('admin.prefix')" width="80" />
          <el-table-column prop="remark" :label="t('admin.remark')" show-overflow-tooltip />
          <el-table-column prop="createdAt" :label="t('order.createdAt')" width="170" />
        </el-table>
        <el-pagination v-model:current-page="page" :page-size="size" :total="total" @current-change="loadData" />
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useI18n } from 'vue-i18n'
import { Download, ArrowDown } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import request from '../api/request'
import { downloadExport } from '../api/export'
import PageHeader from '../components/PageHeader.vue'

const { t } = useI18n()

const tableData = ref([])
const loading = ref(false)
const page = ref(1)
const size = ref(20)
const total = ref(0)

function usageRate(row) {
  return row.totalCount ? Math.round(row.usedCount / row.totalCount * 100) : 0
}

async function loadData() {
  loading.value = true
  try {
    const res = await request.get('/admin/cards/batches', { params: { page: page.value, size: size.value } })
    tableData.value = res.data.records
    total.value = res.data.total
  } finally {
    loading.value = false
  }
}

async function handleExport(format) {
  const ext = format === 'xlsx' ? 'xlsx' : 'csv'
  await downloadExport('/admin/export/batches', `batches_export.${ext}`, format)
  ElMessage.success(t('admin.exportSuccess'))
}

onMounted(loadData)
</script>

<style scoped>
.batch-no { font-family: 'Consolas', monospace; font-weight: 600; color: var(--primary); }
</style>
