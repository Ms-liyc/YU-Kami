/** 将 JSON 中的大整数 ID 字段保留为字符串，避免精度丢失 */
const ID_FIELDS = ['id', 'productId', 'userId', 'batchId', 'orderId', 'cardId', 'promotionId', 'webhookId']

export function parseJsonWithBigInt(text) {
  let safe = text
  for (const field of ID_FIELDS) {
    const re = new RegExp(`"${field}"\\s*:\\s*(\\d{15,})`, 'g')
    safe = safe.replace(re, `"${field}":"$1"`)
  }
  return JSON.parse(safe)
}
