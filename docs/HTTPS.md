# HTTPS 部署指南

生产环境建议全程使用 HTTPS，以保护登录凭证、支付回调与 Webhook 签名。

## Docker Compose（推荐）

项目提供 `docker-compose.https.yml`，在 Hub 镜像基础上由 Nginx 终止 TLS。

### 1. 准备证书

将证书文件放入项目根目录 `certs/`：

```
certs/
  fullchain.pem   # 证书链
  privkey.pem     # 私钥
```

可使用 Let's Encrypt（certbot）、云厂商负载均衡证书，或自签证书（仅测试）：

```bash
mkdir -p certs
openssl req -x509 -nodes -days 365 -newkey rsa:2048 \
  -keyout certs/privkey.pem -out certs/fullchain.pem \
  -subj "/CN=localhost"
```

### 2. 启动

```bash
docker compose -f docker-compose.https.yml up -d
```

访问：

- 商城：`https://你的域名/shop`
- 管理端：`https://你的域名/login`

HTTP 80 会自动 301 跳转到 HTTPS。

### 3. 必改环境变量

在 `docker-compose.https.yml` 的 `backend.environment` 中，将示例域名改为实际值：

| 变量 | 说明 |
|------|------|
| `CORS_ALLOWED_ORIGINS` | 前端 https 域名，逗号分隔 |
| `APP_URL` | 用于密码重置邮件链接 |
| `PAYMENT_BASE_URL` | 支付异步通知基础 URL |
| `PAYMENT_RETURN_URL` | 支付完成跳转页 |
| `JWT_SECRET` / `HMAC_SECRET` / `AES_KEY` | 随机密钥 |

邮件找回密码需配置 SMTP（见 `.env.example` 中 `MAIL_*`）。

## 自有 Nginx 反向代理

若已有 Nginx / Caddy / Traefik，可继续使用 `docker-compose.hub.yml` 或源码 compose，仅暴露 80 端口，由外层代理处理 TLS：

```nginx
server {
    listen 443 ssl http2;
    server_name shop.example.com;

    ssl_certificate     /path/to/fullchain.pem;
    ssl_certificate_key /path/to/privkey.pem;

    location / {
        proxy_pass http://127.0.0.1:80;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto https;
    }
}
```

后端需同步设置 `CORS_ALLOWED_ORIGINS=https://shop.example.com` 与 `PAYMENT_BASE_URL`。

## 安全提示

- 自签证书仅用于本地测试，浏览器会提示不受信任
- 微信支付、支付宝回调 URL 必须为公网可访问的 HTTPS 地址
- 启用 HTTPS 后请检查 Webhook 与邮件中的链接是否使用 `https://`

更多安全项见 [SECURITY.md](SECURITY.md)。
