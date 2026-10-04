import { defineStore } from 'pinia'
import { ref, computed } from 'vue'

export const useAuthStore = defineStore('auth', () => {
  const token = ref(localStorage.getItem('token') || '')
  const username = ref(localStorage.getItem('username') || '')
  const nickname = ref(localStorage.getItem('nickname') || '')
  const role = ref(localStorage.getItem('role') || '')

  const isSuperAdmin = computed(() => role.value === 'SUPER_ADMIN')

  function setAuth(data) {
    token.value = data.token
    username.value = data.username
    nickname.value = data.nickname
    role.value = data.role
    localStorage.setItem('token', data.token)
    localStorage.setItem('username', data.username)
    localStorage.setItem('nickname', data.nickname)
    localStorage.setItem('role', data.role)
    if (data.warnDefaultPassword) {
      localStorage.setItem('warnDefaultPassword', '1')
      localStorage.removeItem('yukami-default-password-dismissed')
    } else {
      localStorage.removeItem('warnDefaultPassword')
    }
  }

  function logout() {
    token.value = ''
    username.value = ''
    nickname.value = ''
    role.value = ''
    localStorage.removeItem('token')
    localStorage.removeItem('username')
    localStorage.removeItem('nickname')
    localStorage.removeItem('role')
    localStorage.removeItem('warnDefaultPassword')
  }

  return { token, username, nickname, role, isSuperAdmin, setAuth, logout }
})
