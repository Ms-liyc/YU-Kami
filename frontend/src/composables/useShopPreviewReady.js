import { ref, onMounted } from 'vue'
import shopHttp from '../api/shopHttp'

/** 检测商城预览所需的后端是否可用（商品 API 可访问且有数据） */
export function useShopPreviewReady() {
  const ready = ref(false)
  const productId = ref(null)
  const checking = ref(true)

  async function check() {
    checking.value = true
    try {
      const res = await shopHttp.get('/shop/products', {
        params: { page: 1, size: 1 },
        timeout: 5000
      })
      const records = res.data?.data?.records
      const ok = res.data?.code === 200 && Array.isArray(records) && records.length > 0
      ready.value = ok
      if (ok) productId.value = String(records[0].id)
    } catch {
      ready.value = false
      productId.value = null
    } finally {
      checking.value = false
    }
  }

  onMounted(check)

  return { ready, productId, checking, recheck: check }
}
