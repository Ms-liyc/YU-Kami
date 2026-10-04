<template>
  <el-dropdown trigger="click" @command="setMode">
    <el-button circle :title="tooltip">
      <el-icon><component :is="currentIcon" /></el-icon>
    </el-button>
    <template #dropdown>
      <el-dropdown-menu>
        <el-dropdown-item command="light" :class="{ 'is-active': mode === 'light' }">
          <el-icon><Sunny /></el-icon>
          {{ t('theme.light') }}
        </el-dropdown-item>
        <el-dropdown-item command="dark" :class="{ 'is-active': mode === 'dark' }">
          <el-icon><Moon /></el-icon>
          {{ t('theme.dark') }}
        </el-dropdown-item>
        <el-dropdown-item command="system" :class="{ 'is-active': mode === 'system' }">
          <el-icon><Monitor /></el-icon>
          {{ t('theme.system') }}
        </el-dropdown-item>
      </el-dropdown-menu>
    </template>
  </el-dropdown>
</template>

<script setup>
import { computed } from 'vue'
import { Sunny, Moon, Monitor } from '@element-plus/icons-vue'
import { useI18n } from 'vue-i18n'
import { storeToRefs } from 'pinia'
import { useThemeStore } from '../stores/theme'

const { t } = useI18n()
const theme = useThemeStore()
const { mode, isDark } = storeToRefs(theme)

const currentIcon = computed(() => {
  if (mode.value === 'system') return Monitor
  return isDark.value ? Moon : Sunny
})

const tooltip = computed(() => {
  if (mode.value === 'system') return t('theme.system')
  return isDark.value ? t('theme.dark') : t('theme.light')
})

function setMode(next) {
  theme.setMode(next)
}
</script>

<style scoped>
:deep(.el-dropdown-menu__item.is-active) {
  color: var(--el-color-primary);
  font-weight: 600;
}
:deep(.el-dropdown-menu__item .el-icon) {
  margin-right: 6px;
}
</style>
