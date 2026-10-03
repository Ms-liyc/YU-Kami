/**
 * 截取商城嵌入页截图，保存到 public/screenshots/
 * 用法：先启动前后端，再 node scripts/capture-screenshots.mjs
 */
import { chromium } from 'playwright'
import { mkdir } from 'fs/promises'
import path from 'path'
import { fileURLToPath } from 'url'

const __dirname = path.dirname(fileURLToPath(import.meta.url))
const outDir = path.join(__dirname, '../public/screenshots')
const base = process.env.PREVIEW_BASE || 'http://localhost:5174'
const api = process.env.API_BASE || 'http://localhost:8081'

function parseJsonWithBigInt(text) {
  const fields = ['id', 'productId', 'userId', 'batchId', 'orderId']
  let safe = text
  for (const field of fields) {
    safe = safe.replace(new RegExp(`"${field}"\\s*:\\s*(\\d{15,})`, 'g'), `"${field}":"$1"`)
  }
  return JSON.parse(safe)
}

async function firstProductId() {
  try {
    const res = await fetch(`${api}/api/shop/products?page=1&size=1`)
    const json = parseJsonWithBigInt(await res.text())
    const id = json?.data?.records?.[0]?.id
    if (id) return String(id)
  } catch { /* fallback below */ }
  console.warn('API unavailable, using fallback product id 1 for preview-buy')
  return '1'
}

async function main() {
  await mkdir(outDir, { recursive: true })
  const productId = await firstProductId()
  const browser = await chromium.launch()
  let page = await browser.newPage({ viewport: { width: 1024, height: 640 } })

  const shots = [
    { name: 'preview-list', url: `${base}/shop/embed/products`, viewport: { width: 1024, height: 640 } },
    { name: 'preview-buy', url: `${base}/shop/embed/buy/${productId}`, viewport: { width: 1024, height: 640 } },
    { name: 'preview-success', url: `${base}/shop/embed/success`, viewport: { width: 1024, height: 640 } },
    { name: 'preview-query', url: `${base}/shop/embed/query`, viewport: { width: 1024, height: 640 } },
    { name: 'preview-redeem', url: `${base}/shop/embed/redeem`, viewport: { width: 1024, height: 640 } },
    { name: 'preview-profile', url: `${base}/shop/embed/profile`, viewport: { width: 1024, height: 640 } },
    { name: 'preview-admin', url: `${base}/shop/embed/admin`, viewport: { width: 1024, height: 640 } },
    { name: 'preview-mobile', url: `${base}/shop/embed/mobile`, viewport: { width: 390, height: 780 } }
  ]

  const waitMap = {
    'preview-list': '.embed-products, .mini-product-card, .product-card',
    'preview-buy': '.buy-card',
    'preview-success': '.success-toast, .embed-success-page',
    'preview-query': '.embed-query-page, .query-card',
    'preview-redeem': '.embed-redeem-page, .redeem-card',
    'preview-profile': '.embed-profile-page, .profile-card',
    'preview-admin': '.embed-admin',
    'preview-mobile': '.embed-mobile'
  }

  for (const s of shots) {
    try {
      if (s.viewport) {
        await page.setViewportSize(s.viewport)
      }
      await page.goto(s.url, { waitUntil: 'domcontentloaded', timeout: 60000 })
      await page.waitForSelector(waitMap[s.name], { timeout: 30000 })
      await page.waitForTimeout(2000)
      const file = path.join(outDir, `${s.name}.png`)
      await page.screenshot({ path: file, fullPage: false })
      console.log('saved', file)
    } catch (e) {
      console.warn('skip', s.name, '-', e.message)
    }
  }

  await browser.close()
}

main().catch((e) => {
  console.error(e)
  process.exit(1)
})
