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

## 公开接口（无需 Token）

- `POST /api/v1/redeem` — 卡密兑换
- `GET /api/shop/products/**` — 商品浏览
- `POST /api/shop/auth/register` / `login` — 商城注册登录
- `POST /api/payment/**` — 支付回调（支付宝/微信服务器调用）
- `POST /api/admin/auth/login` — 管理端登录

## OpenAPI JSON

```
GET /v3/api-docs
```

可用于导入 Postman、Apifox 等工具。
