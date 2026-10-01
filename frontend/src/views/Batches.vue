<template>
  <el-card>
    <template #header>批次管理</template>
    <el-table :data="tableData" stripe>
      <el-table-column prop="batchNo" label="批次号" />
      <el-table-column prop="productId" label="产品ID" />
      <el-table-column prop="totalCount" label="总数" />
      <el-table-column prop="usedCount" label="已用" />
      <el-table-column label="使用率">
        <template #default="{ row }">
          {{ row.totalCount ? Math.round(row.usedCount / row.totalCount * 100) : 0 }}%
        </template>
      </el-table-column>
      <el-table-column prop="prefix" label="前缀" />
      <el-table-column prop="remark" label="备注" />
      <el-table-column prop="createdAt" label="创建时间" />
    </el-table>
    <el-pagination class="pagination" v-model:current-page="page" :page-size="size" :total="total" @current-change="loadData" />
  </el-card>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import request from '../api/request'

const tableData = ref([])
const page = ref(1)
const size = ref(20)
const total = ref(0)

async function loadData() {
  const res = await request.get('/admin/cards/batches', { params: { page: page.value, size: size.value } })
  tableData.value = res.data.records
  total.value = res.data.total
}

onMounted(loadData)
</script>

<style scoped>
.pagination { margin-top: 16px; justify-content: flex-end; }
</style>
