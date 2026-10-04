<template>
  <div class="page-container">
    <PageHeader :title="t('admin.productsTitle')" :subtitle="t('admin.productsSubtitle')">
      <template #extra>
        <el-button type="primary" :icon="Plus" @click="openDialog()">{{ t('common.add') }}</el-button>
      </template>
    </PageHeader>

    <div class="page-card">
      <div class="card-body">
        <el-table :data="tableData" stripe v-loading="loading">
          <el-table-column prop="name" label="产品名称" min-width="140" />
          <el-table-column prop="code" label="编码" width="140">
            <template #default="{ row }">
              <el-tag effect="plain" size="small">{{ row.code }}</el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="category" :label="t('admin.category')" width="100" />
          <el-table-column prop="cardType" label="类型" width="100">
            <template #default="{ row }">
              <el-tag :type="typeMap[row.cardType]?.type" size="small" effect="light">
                {{ typeMap[row.cardType]?.label }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="value" label="面值" width="100">
            <template #default="{ row }">¥{{ row.value || 0 }}</template>
          </el-table-column>
          <el-table-column prop="durationDays" label="时长(天)" width="100" />
          <el-table-column :label="t('admin.unusedStock')" width="100">
            <template #default="{ row }">
              <el-tag :type="row.unusedStock <= 10 ? 'danger' : 'success'" size="small">
                {{ row.unusedStock ?? 0 }}
              </el-tag>
            </template>
          </el-table-column>
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

    <el-dialog v-model="dialogVisible" :title="form.id ? '编辑产品' : '新增产品'" width="520px" destroy-on-close>
      <el-form :model="form" label-width="90px">
        <el-form-item label="名称"><el-input v-model="form.name" /></el-form-item>
        <el-form-item label="编码"><el-input v-model="form.code" /></el-form-item>
        <el-form-item :label="t('admin.category')"><el-input v-model="form.category" :placeholder="t('admin.categoryPlaceholder')" /></el-form-item>
        <el-form-item label="类型">
          <el-select v-model="form.cardType" style="width:100%">
            <el-option label="时长卡" value="DURATION" />
            <el-option label="余额卡" value="BALANCE" />
            <el-option label="单次卡" value="SINGLE" />
          </el-select>
        </el-form-item>
        <el-form-item label="面值"><el-input-number v-model="form.value" :min="0" :precision="2" style="width:100%" /></el-form-item>
        <el-form-item label="时长(天)"><el-input-number v-model="form.durationDays" :min="0" style="width:100%" /></el-form-item>
        <el-form-item label="描述"><el-input v-model="form.description" type="textarea" :rows="3" /></el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSave">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useI18n } from 'vue-i18n'

const { t } = useI18n()
import { Plus } from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import request from '../api/request'
import PageHeader from '../components/PageHeader.vue'

const typeMap = {
  DURATION: { label: '时长卡', type: 'primary' },
  BALANCE: { label: '余额卡', type: 'success' },
  SINGLE: { label: '单次卡', type: 'warning' }
}

const tableData = ref([])
const loading = ref(false)
const page = ref(1)
const size = ref(20)
const total = ref(0)
const dialogVisible = ref(false)
const form = ref({})

async function loadData() {
  loading.value = true
  try {
    const res = await request.get('/admin/products', { params: { page: page.value, size: size.value } })
    tableData.value = res.data.records
    total.value = res.data.total
  } finally {
    loading.value = false
  }
}

function openDialog(row) {
  form.value = row ? { ...row } : { name: '', code: '', category: '', cardType: 'DURATION', value: 0, durationDays: 30, status: 1 }
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
  await ElMessageBox.confirm('确定删除该产品？', '提示', { type: 'warning' })
  await request.delete(`/admin/products/${id}`)
  ElMessage.success('删除成功')
  loadData()
}

onMounted(loadData)
</script>
