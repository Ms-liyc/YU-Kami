<template>
  <div class="page-container">
    <PageHeader title="用户管理" subtitle="管理系统管理员账号与权限">
      <template #extra>
        <el-button type="primary" :icon="Plus" @click="createDialog = true">新增用户</el-button>
      </template>
    </PageHeader>

    <div class="page-card">
      <div class="card-body">
        <el-table :data="tableData" stripe v-loading="loading">
          <el-table-column prop="username" label="用户名" width="140" />
          <el-table-column prop="nickname" label="昵称" width="140" />
          <el-table-column prop="role" label="角色" width="130">
            <template #default="{ row }">
              <el-tag :type="row.role === 'SUPER_ADMIN' ? 'danger' : 'primary'" size="small" effect="light">
                {{ row.role === 'SUPER_ADMIN' ? '超级管理员' : '管理员' }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="status" label="状态" width="90">
            <template #default="{ row }">
              <el-tag :type="row.status === 1 ? 'success' : 'danger'" size="small">
                {{ row.status === 1 ? '正常' : '禁用' }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="createdAt" label="创建时间" width="170" />
          <el-table-column label="操作" width="200" fixed="right">
            <template #default="{ row }">
              <el-button link type="primary" @click="openReset(row)">重置密码</el-button>
              <el-button link :type="row.status === 1 ? 'warning' : 'success'" @click="handleToggle(row.id)">
                {{ row.status === 1 ? '禁用' : '启用' }}
              </el-button>
            </template>
          </el-table-column>
        </el-table>
        <el-pagination v-model:current-page="page" :page-size="size" :total="total" @current-change="loadData" />
      </div>
    </div>

    <el-dialog v-model="createDialog" title="新增用户" width="440px" destroy-on-close>
      <el-form :model="form" label-width="80px">
        <el-form-item label="用户名"><el-input v-model="form.username" /></el-form-item>
        <el-form-item label="密码"><el-input v-model="form.password" type="password" show-password /></el-form-item>
        <el-form-item label="昵称"><el-input v-model="form.nickname" /></el-form-item>
        <el-form-item label="角色">
          <el-select v-model="form.role" style="width:100%">
            <el-option label="管理员" value="ADMIN" />
            <el-option label="超级管理员" value="SUPER_ADMIN" />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="createDialog = false">取消</el-button>
        <el-button type="primary" @click="handleCreate">创建</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="resetDialog" title="重置密码" width="400px">
      <el-input v-model="newPassword" type="password" placeholder="新密码（至少6位）" show-password />
      <template #footer>
        <el-button @click="resetDialog = false">取消</el-button>
        <el-button type="primary" @click="handleReset">确认</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { Plus } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import request from '../api/request'
import PageHeader from '../components/PageHeader.vue'

const tableData = ref([])
const loading = ref(false)
const page = ref(1)
const size = ref(20)
const total = ref(0)
const createDialog = ref(false)
const resetDialog = ref(false)
const resetUserId = ref(null)
const newPassword = ref('')
const form = ref({ username: '', password: '', nickname: '', role: 'ADMIN' })

async function loadData() {
  loading.value = true
  try {
    const res = await request.get('/admin/users', { params: { page: page.value, size: size.value } })
    tableData.value = res.data.records
    total.value = res.data.total
  } finally {
    loading.value = false
  }
}

async function handleCreate() {
  await request.post('/admin/users', form.value)
  ElMessage.success('创建成功')
  createDialog.value = false
  form.value = { username: '', password: '', nickname: '', role: 'ADMIN' }
  loadData()
}

function openReset(row) {
  resetUserId.value = row.id
  newPassword.value = ''
  resetDialog.value = true
}

async function handleReset() {
  await request.post(`/admin/users/${resetUserId.value}/reset-password`, { password: newPassword.value })
  ElMessage.success('密码已重置')
  resetDialog.value = false
}

async function handleToggle(id) {
  await request.post(`/admin/users/${id}/toggle`)
  ElMessage.success('操作成功')
  loadData()
}

onMounted(loadData)
</script>
