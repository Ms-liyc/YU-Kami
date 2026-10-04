<template>
  <div class="page-container">
    <PageHeader :title="t('auditLog.title')" :subtitle="t('auditLog.subtitle')" />

    <div class="page-card">
      <div class="card-body table-scroll-wrap">
        <el-table :data="tableData" stripe v-loading="loading">
          <el-table-column prop="username" :label="t('auditLog.operator')" width="120" />
          <el-table-column prop="action" :label="t('auditLog.action')" width="120">
            <template #default="{ row }">
              <el-tag size="small" effect="plain">{{ row.action }}</el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="target" :label="t('auditLog.target')" width="120" />
          <el-table-column prop="detail" :label="t('auditLog.detail')" min-width="200" show-overflow-tooltip />
          <el-table-column prop="ip" :label="t('auditLog.ip')" width="140" />
          <el-table-column prop="createdAt" :label="t('auditLog.time')" width="170" />
        </el-table>
        <el-pagination v-model:current-page="page" :page-size="size" :total="total" @current-change="loadData" />
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useI18n } from 'vue-i18n'
import request from '../api/request'
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
    const res = await request.get('/admin/audit-logs', { params: { page: page.value, size: size.value } })
    tableData.value = res.data.records
    total.value = res.data.total
  } finally {
    loading.value = false
  }
}

onMounted(loadData)
</script>
