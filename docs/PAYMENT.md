# 支付对接配置指南

## 环境变量

```bash
PAYMENT_BASE_URL=https://your-domain.com    # 回调基础地址（公网可访问）
PAYMENT_RETURN_URL=https://your-domain.com/shop/orders  # 支付完成跳转页
```

## 支付宝（电脑网站支付）

管理后台 → 订单管理 → 支付配置 → 编辑 ALIPAY：

| 字段 | 说明 |
|------|------|
| App ID | 支付宝应用 APPID |
| Notify URL | 异步通知地址（可留空，默认 `{BASE_URL}/api/payment/alipay/notify`） |
| configJson.privateKey | 应用私钥（RSA2） |
| configJson.alipayPublicKey | 支付宝公钥 |
| configJson.sandbox | `true` 使用沙箱环境 |

```json
{
  "privateKey": "MIIEvQIBADANBg...",
  "alipayPublicKey": "MIIBIjANBgkqhkiG9w0BAQ...",
  "sandbox": true
}
```

沙箱网关：`https://openapi-sandbox.dl.alipaydev.com/gateway.do`

## 微信支付（Native 扫码支付）

| 字段 | 说明 |
|------|------|
| App ID | 微信应用 AppID |
| configJson.mchId | 商户号 |
| configJson.privateKey | 商户 API 私钥（PEM 格式） |
| configJson.merchantSerialNumber | 商户证书序列号 |
| configJson.apiV3Key | APIv3 密钥（32位） |

```json
{
  "mchId": "1900000109",
  "privateKey": "-----BEGIN PRIVATE KEY-----\n...\n-----END PRIVATE KEY-----",
  "merchantSerialNumber": "5157F09EFDC096DE15EBE81A47057A7232F1DD16",
  "apiV3Key": "your32charapiv3keyhere123456"
}
```

回调地址：`{BASE_URL}/api/payment/wechat/notify`

## 支付流程

1. 用户下单 → `POST /api/shop/orders`
2. 预支付 → `POST /api/shop/orders/prepay`
   - MOCK：即时发卡
   - ALIPAY：返回 `payUrl` 跳转
   - WECHAT：返回 `codeUrl` 展示二维码
3. 异步回调 → 验签 → 自动发卡
4. 前端轮询 → `GET /api/shop/orders/{id}/status`

## SDK 依赖

- 支付宝：`com.alipay.sdk:alipay-sdk-java`
- 微信：`com.github.wechatpay-apiv3:wechatpay-java`
