import axios from 'axios'
import { parseJsonWithBigInt } from '../utils/jsonBigInt'

/** 商城公开 API（商品列表等），自动处理雪花 ID 精度 */
const shopHttp = axios.create({
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

export default shopHttp
