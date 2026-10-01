import { useAuthStore } from '../stores/auth'

export function downloadExport(url, filename, format = 'csv') {
  const auth = useAuthStore()
  const separator = url.includes('?') ? '&' : '?'
  const fullUrl = '/api' + url + separator + 'format=' + format
  return fetch(fullUrl, {
    headers: { Authorization: `Bearer ${auth.token}` }
  }).then(res => {
    if (!res.ok) throw new Error('导出失败')
    return res.blob()
  }).then(blob => {
    const a = document.createElement('a')
    a.href = URL.createObjectURL(blob)
    a.download = filename
    a.click()
    URL.revokeObjectURL(a.href)
  })
}
