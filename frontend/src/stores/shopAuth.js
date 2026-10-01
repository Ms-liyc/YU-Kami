import { defineStore } from 'pinia'
import { ref } from 'vue'

export const useShopAuthStore = defineStore('shopAuth', () => {
  const token = ref(localStorage.getItem('shop_token') || '')
  const username = ref(localStorage.getItem('shop_username') || '')
  const nickname = ref(localStorage.getItem('shop_nickname') || '')

  function setAuth(data) {
    token.value = data.token
    username.value = data.username
    nickname.value = data.nickname
    localStorage.setItem('shop_token', data.token)
    localStorage.setItem('shop_username', data.username)
    localStorage.setItem('shop_nickname', data.nickname)
  }

  function logout() {
    token.value = ''
    username.value = ''
    nickname.value = ''
    localStorage.removeItem('shop_token')
    localStorage.removeItem('shop_username')
    localStorage.removeItem('shop_nickname')
  }

  return { token, username, nickname, setAuth, logout }
})
