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
          <el-table-column prop="name" :label="t('admin.productName')" min-width="140" />
          <el-table-column prop="code" :label="t('admin.productCode')" width="140">
            <template #default="{ row }">
              <el-tag effect="plain" size="small">{{ row.code }}</el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="category" :label="t('admin.category')" width="100" />
          <el-table-column prop="cardType" :label="t('admin.type')" width="100">
            <template #default="{ row }">
              <el-tag :type="typeMap[row.cardType]?.type" size="small" effect="light">
                {{ typeMap[row.cardType]?.label }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="value" :label="t('admin.faceValue')" width="100">
            <template #default="{ row }">¥{{ row.value || 0 }}</template>
          </el-table-column>
          <el-table-column prop="durationDays" :label="t('admin.durationDaysLabel')" width="100" />
          <el-table-column :label="t('admin.unusedStock')" width="100">
            <template #default="{ row }">
              <el-tag :type="row.unusedStock <= 10 ? 'danger' : 'success'" size="small">
                {{ row.unusedStock ?? 0 }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="status" :label="t('common.status')" width="80">
            <template #default="{ row }">
              <el-tag :type="row.status === 1 ? 'success' : 'danger'" size="small">
                {{ row.status === 1 ? t('admin.statusNormal') : t('admin.statusDisabled') }}
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

    <el-dialog v-model="dialogVisible" :title="form.id ? t('admin.editProduct') : t('admin.addProduct')" width="520px" destroy-on-close>
      <el-form :model="form" label-width="90px">
        <el-form-item :label="t('admin.productName')"><el-input v-model="form.name" /></el-form-item>
        <el-form-item :label="t('admin.productCode')"><el-input v-model="form.code" /></el-form-item>
        <el-form-item :label="t('admin.category')"><el-input v-model="form.category" :placeholder="t('admin.categoryPlaceholder')" /></el-form-item>
        <el-form-item :label="t('admin.type')">
          <el-select v-model="form.cardType" style="width:100%">
            <el-option :label="t('admin.typeDuration')" value="DURATION" />
            <el-option :label="t('admin.typeBalance')" value="BALANCE" />
            <el-option :label="t('admin.typeSingle')" value="SINGLE" />
          </el-select>
        </el-form-item>
        <el-form-item :label="t('admin.faceValue')"><el-input-number v-model="form.value" :min="0" :precision="2" style="width:100%" /></el-form-item>
        <el-form-item :label="t('admin.durationDaysLabel')"><el-input-number v-model="form.durationDays" :min="0" style="width:100%" /></el-form-item>
        <el-form-item :label="t('admin.description')"><el-input v-model="form.description" type="textarea" :rows="3" /></el-form-item>
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
import { useI18n } from 'vue-i18n'
import { Plus } from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import request from '../api/request'
import PageHeader from '../components/PageHeader.vue'

const { t } = useI18n()

const typeMap = computed(() => ({
  DURATION: { label: t('admin.typeDuration'), type: 'primary' },
  BALANCE: { label: t('admin.typeBalance'), type: 'success' },
  SINGLE: { label: t('admin.typeSingle'), type: 'warning' }
}))

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
  ElMessage.success(t('common.success'))
  dialogVisible.value = false
  loadData()
}

async function handleDelete(id) {
  await ElMessageBox.confirm(t('admin.deleteProductConfirm'), t('common.tip'), { type: 'warning' })
  await request.delete(`/admin/products/${id}`)
  ElMessage.success(t('common.success'))
  loadData()
}

onMounted(loadData)
</script>
