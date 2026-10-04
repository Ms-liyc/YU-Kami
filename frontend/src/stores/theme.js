import { defineStore } from 'pinia'
import { ref, computed, watch } from 'vue'

const STORAGE_KEY = 'app-theme'
const LEGACY_KEY = 'shop-theme'

function resolveDark(mode) {
  if (mode === 'dark') return true
  if (mode === 'light') return false
  return window.matchMedia('(prefers-color-scheme: dark)').matches
}

function readStoredMode() {
  const stored = localStorage.getItem(STORAGE_KEY)
  if (stored === 'light' || stored === 'dark' || stored === 'system') return stored
  const legacy = localStorage.getItem(LEGACY_KEY)
  if (legacy === 'dark') return 'dark'
  if (legacy === 'light') return 'light'
  return 'system'
}

function applyDarkClass(isDark) {
  document.documentElement.classList.toggle('dark', isDark)
  document.documentElement.style.colorScheme = isDark ? 'dark' : 'light'
}

export const useThemeStore = defineStore('theme', () => {
  const mode = ref(readStoredMode())
  const isDark = computed(() => resolveDark(mode.value))

  function setMode(next) {
    if (next !== 'light' && next !== 'dark' && next !== 'system') return
    mode.value = next
    localStorage.setItem(STORAGE_KEY, next)
    localStorage.removeItem(LEGACY_KEY)
    applyDarkClass(isDark.value)
  }

  function toggle() {
    setMode(isDark.value ? 'light' : 'dark')
  }

  function init() {
    applyDarkClass(isDark.value)
    window.matchMedia('(prefers-color-scheme: dark)').addEventListener('change', () => {
      if (mode.value === 'system') applyDarkClass(isDark.value)
    })
  }

  watch(isDark, (val) => applyDarkClass(val))

  return { mode, isDark, setMode, toggle, init }
})
