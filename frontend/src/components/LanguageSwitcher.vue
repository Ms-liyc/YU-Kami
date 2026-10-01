<template>
  <el-dropdown @command="switchLang">
    <el-button link>
      <el-icon><Globe /></el-icon>
      {{ currentLabel }}
    </el-button>
    <template #dropdown>
      <el-dropdown-menu>
        <el-dropdown-item command="zh-CN">简体中文</el-dropdown-item>
        <el-dropdown-item command="en-US">English</el-dropdown-item>
      </el-dropdown-menu>
    </template>
  </el-dropdown>
</template>

<script setup>
import { computed } from 'vue'
import { useI18n } from 'vue-i18n'
import zhCn from 'element-plus/dist/locale/zh-cn.mjs'
import en from 'element-plus/dist/locale/en.mjs'

const { locale } = useI18n()

const currentLabel = computed(() => locale.value === 'zh-CN' ? '中文' : 'EN')

function switchLang(lang) {
  locale.value = lang
  localStorage.setItem('locale', lang)
  const elLocale = lang === 'zh-CN' ? zhCn : en
  import('element-plus').then(({ ElConfigProvider }) => {
    document.documentElement.lang = lang === 'zh-CN' ? 'zh' : 'en'
  })
  window.location.reload()
}
</script>
