# YU-Kami

屿宸科技 - 企业级卡密系统开源站台

[![GitHub](https://img.shields.io/badge/GitHub-Ms--liyc%2FYU--Kami-blue)](https://github.com/Ms-liyc/YU-Kami)

## 技术栈

| 层级 | 技术 |
|------|------|
| 后端 | Java 17 + Spring Boot 3.2 + MyBatis-Plus |
| 前端 | Vue 3 + Vite + Element Plus + Pinia |
| 数据库 | PostgreSQL 16（高并发读写、事务可靠） |
| 缓存 | Redis 7（分布式锁、限流） |

## 多重加密体系

- **HMAC-SHA256 + Pepper**：卡密哈希存储，每张卡独立胡椒盐，数据库不存明文
- **AES-256-GCM**：卡密元数据加密
- **BCrypt**：管理员密码加密
- **RSA-SHA256**：开放 API 签名验签
- **SHA-256 校验码**：卡密格式校验，防手输错误

## 高并发设计

- Redis 分布式锁：防止同一卡密并发重复兑换
- 数据库乐观锁（version 字段）：兑换原子更新
- Redis 滑动限流：防暴力破解
- HikariCP 连接池 + PostgreSQL 索引优化

## 快速开始

### 1. 启动基础设施

```bash
docker compose up -d
```

### 2. 启动后端

```bash
cd backend
mvn spring-boot:run
```

默认端口：`8080`

### 3. 启动前端

```bash
cd frontend
npm install
npm run dev
```

默认端口：`5173`

### 4. 默认账号

- 用户名：`admin`
- 密码：`admin123`

> 首次启动会自动创建管理员账号（若数据库中不存在）。

## API 文档

### 管理端（需 JWT）

| 接口 | 方法 | 说明 |
|------|------|------|
| `/api/admin/auth/login` | POST | 管理员登录 |
| `/api/admin/dashboard` | GET | 数据概览 |
| `/api/admin/products` | CRUD | 产品管理 |
| `/api/admin/cards/generate` | POST | 批量生成卡密 |
| `/api/admin/cards` | GET | 卡密列表 |
| `/api/admin/cards/batches` | GET | 批次列表 |
| `/api/admin/cards/{id}/revoke` | POST | 作废卡密 |
| `/api/admin/redeem-records` | GET | 兑换记录 |

### 开放接口

```bash
POST /api/v1/redeem
Content-Type: application/json

{
  "cardKey": "VIP-XXXXXX-ABCDEF",
  "redeemUser": "user_12345"
}
```

## 生产环境配置

通过环境变量覆盖敏感配置：

```bash
JWT_SECRET=your-jwt-secret
HMAC_SECRET=your-hmac-secret
AES_KEY=your-32-char-aes-key-here!!!!!!
DB_HOST=localhost
DB_PORT=5432
DB_NAME=yukami
DB_USER=yukami
DB_PASSWORD=your-db-password
REDIS_HOST=localhost
REDIS_PORT=6379
```

## 版本历史

| 版本 | 说明 |
|------|------|
| v1.1.0 | API客户端管理、用户管理、审计日志、CSV导出、界面全面优化 |
| v1.0.0 | 初始版本：卡密生成/兑换/管理后台/多重加密/高并发保障 |

## License

MIT
