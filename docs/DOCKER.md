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
