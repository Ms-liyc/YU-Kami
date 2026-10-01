<template>
  <div class="page-container">
    <PageHeader title="批次管理" subtitle="查看卡密批次生成与使用统计">
      <template #extra>
        <el-button :icon="Download" @click="handleExport">导出 CSV</el-button>
      </template>
    </PageHeader>

    <div class="page-card">
      <div class="card-body">
        <el-table :data="tableData" stripe v-loading="loading">
          <el-table-column prop="batchNo" label="批次号" min-width="180">
            <template #default="{ row }">
              <span class="batch-no">{{ row.batchNo }}</span>
            </template>
          </el-table-column>
          <el-table-column prop="productId" label="产品ID" width="180" show-overflow-tooltip />
          <el-table-column prop="totalCount" label="总数" width="80" align="center" />
          <el-table-column prop="usedCount" label="已用" width="80" align="center" />
          <el-table-column label="使用率" width="160">
            <template #default="{ row }">
              <el-progress :percentage="usageRate(row)" :stroke-width="8"
                :color="usageRate(row) > 80 ? '#10b981' : '#4f6ef7'" />
            </template>
          </el-table-column>
          <el-table-column prop="prefix" label="前缀" width="80" />
          <el-table-column prop="remark" label="备注" show-overflow-tooltip />
          <el-table-column prop="createdAt" label="创建时间" width="170" />
        </el-table>
        <el-pagination v-model:current-page="page" :page-size="size" :total="total" @current-change="loadData" />
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { Download } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import request from '../api/request'
import { downloadExport } from '../api/export'
import PageHeader from '../components/PageHeader.vue'

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

async function handleExport() {
  await downloadExport('/admin/export/batches', 'batches_export.csv')
  ElMessage.success('导出成功')
}

onMounted(loadData)
</script>

<style scoped>
.batch-no { font-family: 'Consolas', monospace; font-weight: 600; color: var(--primary); }
</style>
