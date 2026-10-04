# 安全加固指南

本文说明 YU-Kami 生产部署时的安全配置与 v1.12.1 起内置的安全机制。

## 启动自检

应用启动后会通过 `ProductionSecurityChecker` 在日志中输出警告（不阻断启动），检测项包括：

- `JWT_SECRET` / `HMAC_SECRET` / `AES_KEY` 仍为仓库默认值
- `DB_PASSWORD` 为空或使用已知弱默认值
- `SWAGGER_ENABLED=true`（生产建议关闭）
- `CORS_ALLOWED_ORIGINS` 为 `*` 或未限制

## 必改环境变量

| 变量 | 说明 |
|------|------|
| `JWT_SECRET` | JWT 签名密钥，至少 32 位随机字符串 |
| `HMAC_SECRET` | Webhook / API 签名密钥 |
| `AES_KEY` | 卡密等敏感字段 AES 密钥（32 字符） |
| `DB_PASSWORD` | 数据库强密码，勿使用 `yukami123` 等默认值 |
| `MAIL_PASSWORD` | QQ 邮箱 **16 位授权码**（非 QQ 密码），仅放环境变量，勿提交仓库 |
| `STOCK_ALERT_EMAIL` | 库存告警收件人，仅服务端使用，API 不返回明文 |

完整示例见项目根目录 [.env.example](../.env.example)。邮件配置详见 [MAIL.md](MAIL.md)。

## 推荐生产配置

```bash
SWAGGER_ENABLED=false
CORS_ALLOWED_ORIGINS=https://your-shop.com,https://your-admin.com
LOGIN_RATE_LIMIT=20
REGISTER_RATE_LIMIT=5
```

## 内置防护（v1.12.1+）

### HTTP 安全

- Spring Security 默认拒绝未声明路径（`denyAll`），不再对未知 API 放行
- 响应头：`X-Frame-Options: DENY`、`X-Content-Type-Options: nosniff`、`Referrer-Policy`、`Permissions-Policy`
- 前端 Nginx 同步添加上述安全头

### CORS

- 默认仅允许本地开发域名（5173 / 80）
- 生产通过 `CORS_ALLOWED_ORIGINS` 指定逗号分隔的域名列表
- 设为 `*` 时允许任意来源，但不携带凭证 Cookie（JWT 仍通过 Header 传递）

### 速率限制

基于 Redis，按 IP / 用户名限制：

| 场景 | 默认限额 | 环境变量 |
|------|----------|----------|
| 管理端/商城登录 | 20 次/分钟 | `LOGIN_RATE_LIMIT` |
| 商城注册 | 5 次/分钟 | `REGISTER_RATE_LIMIT` |
| 卡密兑换 | 30 次/分钟 | `yukami.redeem.rate-limit-per-minute` |

超限返回 HTTP 429。

### Swagger

- 开发默认开启；生产设置 `SWAGGER_ENABLED=false` 关闭文档与 UI
- Security 层同步拒绝未启用时的 `/swagger-ui/**` 访问

## HTTPS

- 生产环境请在 Nginx / 负载均衡层终止 TLS
- 配置 `X-Forwarded-Proto` 以便后端正确识别客户端 IP（已通过 `ClientIpUtils` 解析 `X-Forwarded-For` / `X-Real-IP`）

## Docker Compose

`docker-compose.yml` 中 MySQL 默认密码 `yukami123` 仅用于本地演示。生产部署请修改 compose 环境变量或使用外部托管数据库，并同步更新 `DB_PASSWORD`。

## 演示账号

默认管理员 `admin/admin123`、买家 `demo/demo123` 仅供本地体验，上线前请修改或禁用。
