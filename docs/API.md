# API 文档

## Swagger UI

后端启动后访问：

| 环境 | 地址 |
|------|------|
| 本地开发 | http://localhost:8080/swagger-ui.html |
| Docker（后端端口映射） | http://localhost:8080/swagger-ui.html |

## 认证方式

大部分接口需要在请求头携带 JWT：

```
Authorization: Bearer <token>
```

| 端 | 登录接口 | 角色 |
|----|---------|------|
| 管理后台 | `POST /api/admin/auth/login` | ADMIN / SUPER_ADMIN |
| 用户商城 | `POST /api/shop/auth/login` | SHOP_USER |

在 Swagger UI 右上角点击 **Authorize**，输入 `Bearer <你的token>` 即可调试需鉴权的接口。

---

## 公开接口（无需 Token）

| 接口 | 方法 | 说明 |
|------|------|------|
| `/api/v1/redeem` | POST | 卡密兑换 |
| `/api/shop/products` | GET | 商品列表 |
| `/api/shop/products/{id}` | GET | 商品详情 |
| `/api/shop/stats` | GET | 商城公开统计 |
| `/api/shop/orders/recent` | GET | 最近成交（脱敏） |
| `/api/shop/auth/register` | POST | 商城注册 |
| `/api/shop/auth/login` | POST | 商城登录 |
| `/api/payment/alipay/notify` | POST | 支付宝异步回调 |
| `/api/payment/wechat/notify` | POST | 微信异步回调 |
| `/api/admin/auth/login` | POST | 管理端登录 |

### 卡密兑换示例

```bash
POST /api/v1/redeem
Content-Type: application/json

{
  "cardKey": "VIP-XXXXXX-ABCDEF",
  "redeemUser": "user_12345"
}
```

---

## 商城接口（需 SHOP_USER JWT）

### 账号

| 接口 | 方法 | 说明 |
|------|------|------|
| `/api/shop/auth/me` | GET | 当前用户资料 |
| `/api/shop/auth/profile` | PUT | 更新昵称、邮箱 |
| `/api/shop/auth/password` | PUT | 修改密码 |

**更新资料请求体：**

```json
{
  "nickname": "新昵称",
  "email": "user@example.com"
}
```

**修改密码请求体：**

```json
{
  "oldPassword": "当前密码",
  "newPassword": "新密码"
}
```

### 订单

| 接口 | 方法 | 说明 |
|------|------|------|
| `/api/shop/orders` | GET | 我的订单，支持 `status`、`orderNo` 查询参数 |
| `/api/shop/orders/lookup` | GET | 按订单号查询，`?orderNo=O2026...` |
| `/api/shop/orders` | POST | 创建订单 |
| `/api/shop/orders/pricing/preview` | POST | 价格预览（促销 + 优惠券） |
| `/api/shop/orders/pay` | POST | 模拟支付 |
| `/api/shop/orders/prepay` | POST | 预支付（支付宝/微信） |
| `/api/shop/orders/{id}/status` | GET | 支付状态轮询 |
| `/api/shop/orders/{id}/card` | GET | 查看卡密（已发货） |
| `/api/shop/orders/{id}/cancel` | POST | 取消待支付订单 |
| `/api/shop/orders/payment-channels` | GET | 可用支付渠道（需登录，含 BALANCE） |

### 钱包

| 接口 | 方法 | 说明 |
|------|------|------|
| `/api/shop/wallet` | GET | 当前余额 |
| `/api/shop/wallet/transactions` | GET | 交易明细（分页） |

余额支付：创建订单后调用 `POST /api/shop/orders/prepay`，`paymentMethod` 设为 `BALANCE`。

---

## 管理端接口（需 ADMIN JWT）

| 模块 | 前缀 | 说明 |
|------|------|------|
| 数据概览 | `/api/admin/dashboard` | 统计面板 |
| 产品 | `/api/admin/products` | CRUD |
| 卡密 | `/api/admin/cards/**` | 生成、导入、作废、批次 |
| 兑换记录 | `/api/admin/redeem-records` | 审计明细 |
| 订单 | `/api/admin/orders/**` | 列表、取消、支付配置 |
| 促销 | `/api/admin/promotions` | 满减/折扣/特价 |
| 优惠券 | `/api/admin/coupons` | 券码管理 |
| API 客户端 | `/api/admin/api-clients` | 开放接入 |
| 用户 | `/api/admin/users` | 管理员 RBAC |
| 审计 | `/api/admin/audit-logs` | 操作日志 |
| Webhook | `/api/admin/webhooks` | 回调配置 |
| 导出 | `/api/admin/export/**` | CSV / Excel |

---

## OpenAPI JSON

```
GET /v3/api-docs
```

可用于导入 Postman、Apifox 等工具。

---

## Webhook 回调

兑换成功后，系统向配置的 URL 发送 POST 请求：

**请求头：**

- `X-YK-Event`: 事件类型（如 `REDEEM_SUCCESS`）
- `X-YK-Signature`: HMAC-SHA256 签名

**请求体示例：**

```json
{
  "event": "REDEEM_SUCCESS",
  "timestamp": 1727798400000,
  "data": {
    "cardId": 123,
    "productCode": "VIP_MONTH",
    "productName": "月度会员",
    "redeemUser": "user_001"
  }
}
```
