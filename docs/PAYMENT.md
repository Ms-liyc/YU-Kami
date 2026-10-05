# 支付对接配置指南

> **开源部署说明**：支付对接为**必选配置**。部署后请在管理后台启用**支付宝**或**微信支付**，买家方可完成购买与充值。  
> 真实收款需由**部署者自行**申请商户资质、填写密钥，并按实际部署环境配置回调地址。  
> 本项目不预设固定域名或内网穿透方案，请根据你的服务器、反向代理或隧道工具自行设置。

## 默认行为

| 渠道 | 首次部署状态 | 说明 |
|------|-------------|------|
| ALIPAY | 未启用 | 需自行配置密钥后启用 |
| WECHAT | 未启用 | 需自行配置密钥后启用 |
| BALANCE | 视配置 | 买家钱包余额支付（需有余额） |

管理后台首次登录会提示支付对接说明，可在 **订单管理 → 支付配置** 中完成设置。

## 环境变量（按部署环境填写）

以下变量**无固定值**，请根据你的实际域名、端口或反向代理配置：

```bash
# 支付平台异步回调的基础地址（示例，请替换为你的公网可访问地址）
PAYMENT_BASE_URL=https://your-domain.com

# 用户支付完成后跳转的前台页面
PAYMENT_RETURN_URL=https://your-domain.com/shop/orders
```

Docker 部署可在 `docker-compose.yml` 的 `backend` 服务 `environment` 中添加上述变量。

本地开发时如未设置，默认使用 `http://localhost:8080` 与 `http://localhost:5173/shop/orders`；对接真实支付时请改为可被支付平台访问的地址。

## 支付宝（电脑网站支付）

管理后台 → 订单管理 → 支付配置 → 编辑 ALIPAY：

| 字段 | 说明 |
|------|------|
| App ID | 支付宝应用 APPID |
| Notify URL | 异步通知地址（可留空，默认 `{PAYMENT_BASE_URL}/api/payment/alipay/notify`） |
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

## 微信支付

### Native 扫码支付（PC / 非微信浏览器）

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

回调地址：`{PAYMENT_BASE_URL}/api/payment/wechat/notify`（或在 Notify URL 字段中自定义）

### JSAPI 支付（微信内浏览器 / 公众号 H5）

在微信内置浏览器中购买时，系统会自动走 JSAPI 调起支付（需先 OAuth 获取 openid）。

额外配置：

| 字段 | 说明 |
|------|------|
| App Secret | 填写在「App Secret」或 `configJson.appSecret`，用于 OAuth 换取 openid |
| 公众号网页授权域名 | 在微信公众平台配置为你的域名 |

流程：
1. 用户在微信内点击支付 → 跳转 OAuth 授权
2. 回调 `/api/shop/payment/wechat/oauth-callback` 获取 openid
3. 预支付返回 JSAPI 参数 → 调起 `WeixinJSBridge` 完成支付

## 支付流程

1. 用户下单 → `POST /api/shop/orders`
2. 预支付 → `POST /api/shop/orders/prepay`
   - BALANCE：即时扣款发卡
   - ALIPAY：返回 `payUrl` 跳转
   - WECHAT：返回 `codeUrl` 展示二维码
3. 异步回调 → 验签 → 自动发卡
4. 前端轮询 → `GET /api/shop/orders/{id}/status`

## SDK 依赖

- 支付宝：`com.alipay.sdk:alipay-sdk-java`
- 微信：`com.github.wechatpay-apiv3:wechatpay-java`
