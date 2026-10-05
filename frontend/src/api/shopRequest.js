import axios from 'axios'
import { ElMessage } from 'element-plus'
import { useShopAuthStore } from '../stores/shopAuth'
import router from '../router'
import { parseJsonWithBigInt } from '../utils/jsonBigInt'

const request = axios.create({
  baseURL: '/api',
  timeout: 30000,
  transformResponse: [(data) => {
    if (typeof data !== 'string' || !data) return data
    try {
      return parseJsonWithBigInt(data)
    } catch {
      return JSON.parse(data)
    }
  }]
})

request.interceptors.request.use(config => {
  const auth = useShopAuthStore()
  if (auth.token) config.headers.Authorization = `Bearer ${auth.token}`
  return config
})

function isEmbedPage() {
  return window.location.pathname.startsWith('/shop/embed')
}

request.interceptors.response.use(
  res => {
    if (res.data.code !== 200) {
      if (!isEmbedPage()) ElMessage.error(res.data.message || 'Request failed')
      return Promise.reject(new Error(res.data.message))
    }
    return res.data
  },
  err => {
    const status = err.response?.status
    if (status === 401 || status === 403) {
      const auth = useShopAuthStore()
      if (auth.token) {
        auth.logout()
        if (!isEmbedPage()) router.push('/shop/login')
      }
    }
    if (!isEmbedPage()) ElMessage.error(err.response?.data?.message || err.message)
    return Promise.reject(err)
  }
)

export default request
