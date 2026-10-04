# Docker Hub 部署

## 官方镜像

| 镜像 | 说明 |
|------|------|
| `msliyc/yu-kami-backend` | Spring Boot 后端 |
| `msliyc/yu-kami-frontend` | Nginx + Vue 静态资源 |

拉取示例：

```bash
docker pull msliyc/yu-kami-backend:latest
docker pull msliyc/yu-kami-frontend:latest
```

## 一键部署（推荐）

使用预构建镜像，无需本地编译：

```bash
git clone https://github.com/Ms-liyc/YU-Kami.git
cd YU-Kami
docker compose -f docker-compose.hub.yml up -d
```

访问地址与 `docker compose up` 相同：商城 `http://localhost/shop`，管理端 `http://localhost/login`。

### 生产环境建议

`docker-compose.hub.yml` 已默认关闭 Swagger 并限制 CORS。上线前请修改 backend 环境变量：

| 变量 | 说明 |
|------|------|
| `JWT_SECRET` / `HMAC_SECRET` / `AES_KEY` | 必改随机密钥 |
| `SWAGGER_ENABLED` | 保持 `false` |
| `CORS_ALLOWED_ORIGINS` | 改为实际前端域名 |

详见 [docs/SECURITY.md](SECURITY.md)。

## HTTPS 部署

生产环境建议使用 TLS。将证书放入 `certs/` 后执行：

```bash
docker compose -f docker-compose.https.yml up -d
```

配置说明见 [docs/HTTPS.md](HTTPS.md)。

## 发布新镜像（维护者）

GitHub Actions 工作流 **Docker Publish** 会在推送 `v*` 标签时自动构建并推送到 Docker Hub。

### 配置 Secrets

在 GitHub 仓库 Settings → Secrets and variables → Actions 中添加：

| Secret | 说明 |
|--------|------|
| `DOCKERHUB_USERNAME` | Docker Hub 用户名 |
| `DOCKERHUB_TOKEN` | Docker Hub Access Token |

### 触发发布

```bash
git tag v1.12.0
git push origin v1.12.0
```

将同时推送 `latest` 与版本号标签（如 `1.12.0`）。

也可在 GitHub Actions 页面手动 **Run workflow**（推送 `latest`）。

## 与源码构建对比

| 方式 | 命令 | 适用场景 |
|------|------|----------|
| 源码构建 | `docker compose up -d --build` | 开发、定制 Dockerfile |
| Hub 镜像 | `docker compose -f docker-compose.hub.yml up -d` | 生产快速部署 |
