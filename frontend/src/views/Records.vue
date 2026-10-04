<template>
  <div class="page-container">
    <PageHeader :title="t('admin.recordsTitle')" :subtitle="t('admin.recordsSubtitle')">
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
          <el-table-column prop="redeemUser" :label="t('admin.recordUser')" width="150" />
          <el-table-column prop="result" :label="t('admin.recordResult')" width="110">
            <template #default="{ row }">
              <el-tag :type="row.result === 'SUCCESS' ? 'success' : 'danger'" size="small" effect="light">
                {{ row.result }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="message" :label="t('admin.recordMessage')" min-width="160" show-overflow-tooltip />
          <el-table-column prop="redeemIp" :label="t('admin.recordIp')" width="140" />
          <el-table-column prop="userAgent" :label="t('admin.recordUserAgent')" min-width="200" show-overflow-tooltip />
          <el-table-column prop="createdAt" :label="t('admin.recordTime')" width="170" />
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

async function loadData() {
  loading.value = true
  try {
    const res = await request.get('/admin/redeem-records', { params: { page: page.value, size: size.value } })
    tableData.value = res.data.records
    total.value = res.data.total
  } finally {
    loading.value = false
  }
}

async function handleExport(format) {
  const ext = format === 'xlsx' ? 'xlsx' : 'csv'
  await downloadExport('/admin/export/redeem-records', `redeem_records.${ext}`, format)
  ElMessage.success(t('admin.exportSuccess'))
}

onMounted(loadData)
</script>
