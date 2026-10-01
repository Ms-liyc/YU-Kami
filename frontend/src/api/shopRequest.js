import axios from 'axios'
import { ElMessage } from 'element-plus'
import { useShopAuthStore } from '../stores/shopAuth'
import router from '../router'

const request = axios.create({ baseURL: '/api', timeout: 30000 })

request.interceptors.request.use(config => {
  const auth = useShopAuthStore()
  if (auth.token) config.headers.Authorization = `Bearer ${auth.token}`
  return config
})

request.interceptors.response.use(
  res => {
    if (res.data.code !== 200) {
      ElMessage.error(res.data.message || 'Request failed')
      return Promise.reject(new Error(res.data.message))
    }
    return res.data
  },
  err => {
    if (err.response?.status === 401) {
      useShopAuthStore().logout()
      router.push('/shop/login')
    }
    ElMessage.error(err.response?.data?.message || err.message)
    return Promise.reject(err)
  }
)

export default request
