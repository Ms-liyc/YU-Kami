<template>
  <div class="shop-layout shop-embed-root" :class="{ dark: isDark }">
    <div class="embed-toolbar">
      <ThemeSwitcher />
      <LanguageSwitcher />
    </div>
    <router-view />
  </div>
</template>

<script setup>
import { storeToRefs } from 'pinia'
import { useThemeStore } from '../stores/theme'
import ThemeSwitcher from '../components/ThemeSwitcher.vue'
import LanguageSwitcher from '../components/LanguageSwitcher.vue'

const theme = useThemeStore()
const { isDark } = storeToRefs(theme)
</script>

<style scoped>
.shop-embed-root {
  min-height: 100vh;
  position: relative;
}
.embed-toolbar {
  position: fixed;
  top: 12px;
  right: 12px;
  z-index: 200;
  display: flex;
  align-items: center;
  gap: 8px;
}
</style>
