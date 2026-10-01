<template>
  <el-card>
    <template #header>
      <div class="card-header">
        <span>产品管理</span>
        <el-button type="primary" @click="openDialog()">新增产品</el-button>
      </div>
    </template>
    <el-table :data="tableData" stripe>
      <el-table-column prop="name" label="产品名称" />
      <el-table-column prop="code" label="编码" />
      <el-table-column prop="cardType" label="类型" />
      <el-table-column prop="value" label="面值" />
      <el-table-column prop="durationDays" label="时长(天)" />
      <el-table-column prop="status" label="状态">
        <template #default="{ row }">
          <el-tag :type="row.status === 1 ? 'success' : 'danger'">{{ row.status === 1 ? '启用' : '禁用' }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="160">
        <template #default="{ row }">
          <el-button link type="primary" @click="openDialog(row)">编辑</el-button>
          <el-button link type="danger" @click="handleDelete(row.id)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>
    <el-pagination class="pagination" v-model:current-page="page" :page-size="size" :total="total" @current-change="loadData" />

    <el-dialog v-model="dialogVisible" :title="form.id ? '编辑产品' : '新增产品'" width="500px">
      <el-form :model="form" label-width="100px">
        <el-form-item label="名称"><el-input v-model="form.name" /></el-form-item>
        <el-form-item label="编码"><el-input v-model="form.code" /></el-form-item>
        <el-form-item label="类型">
          <el-select v-model="form.cardType" style="width:100%">
            <el-option label="时长卡" value="DURATION" />
            <el-option label="余额卡" value="BALANCE" />
            <el-option label="单次卡" value="SINGLE" />
          </el-select>
        </el-form-item>
        <el-form-item label="面值"><el-input-number v-model="form.value" :min="0" :precision="2" /></el-form-item>
        <el-form-item label="时长(天)"><el-input-number v-model="form.durationDays" :min="0" /></el-form-item>
        <el-form-item label="描述"><el-input v-model="form.description" type="textarea" /></el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSave">保存</el-button>
      </template>
    </el-dialog>
  </el-card>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import request from '../api/request'

const tableData = ref([])
const page = ref(1)
const size = ref(20)
const total = ref(0)
const dialogVisible = ref(false)
const form = ref({})

async function loadData() {
  const res = await request.get('/admin/products', { params: { page: page.value, size: size.value } })
  tableData.value = res.data.records
  total.value = res.data.total
}

function openDialog(row) {
  form.value = row ? { ...row } : { name: '', code: '', cardType: 'DURATION', value: 0, durationDays: 30, status: 1 }
  dialogVisible.value = true
}

async function handleSave() {
  if (form.value.id) {
    await request.put(`/admin/products/${form.value.id}`, form.value)
  } else {
    await request.post('/admin/products', form.value)
  }
  ElMessage.success('保存成功')
  dialogVisible.value = false
  loadData()
}

async function handleDelete(id) {
  await ElMessageBox.confirm('确定删除该产品？', '提示')
  await request.delete(`/admin/products/${id}`)
  ElMessage.success('删除成功')
  loadData()
}

onMounted(loadData)
</script>

<style scoped>
.card-header { display: flex; justify-content: space-between; align-items: center; }
.pagination { margin-top: 16px; justify-content: flex-end; }
</style>
