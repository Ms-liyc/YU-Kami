<p align="center">
  <img src="docs/logo.png" alt="YU-Kami Logo" width="160">
</p>

<h1 align="center">🚀 YU-Kami 企业级卡密系统</h1>

<h3 align="center">屿宸科技 · 开源可部署的卡密生成、兑换、商城与支付一体化方案</h3>

<p align="center">
  <img src="https://img.shields.io/github/v/tag/Ms-liyc/YU-Kami?style=flat-square&label=version" alt="Version">
  <img src="https://img.shields.io/badge/Java-17-orange?style=flat-square&logo=openjdk" alt="Java 17">
  <img src="https://img.shields.io/badge/Spring%20Boot-3.2-brightgreen?style=flat-square&logo=springboot" alt="Spring Boot">
  <img src="https://img.shields.io/badge/Vue-3-42b883?style=flat-square&logo=vue.js" alt="Vue 3">
  <img src="https://img.shields.io/badge/Vite-5-646cff?style=flat-square&logo=vite" alt="Vite">
  <img src="https://img.shields.io/badge/Element%20Plus-409eff?style=flat-square" alt="Element Plus">
  <img src="https://img.shields.io/badge/PostgreSQL-16-336791?style=flat-square&logo=postgresql&logoColor=white" alt="PostgreSQL">
  <img src="https://img.shields.io/badge/Redis-7-DC382D?style=flat-square&logo=redis&logoColor=white" alt="Redis">
  <img src="https://img.shields.io/badge/Docker-ready-2496ED?style=flat-square&logo=docker&logoColor=white" alt="Docker">
  <img src="https://img.shields.io/badge/License-MIT-blue?style=flat-square" alt="MIT License">
</p>

<p align="center">
  <b>卡密全生命周期管理</b> · 批量生成 / 导入导出 / 兑换审计 / Webhook 回调<br>
  <b>用户购买中心</b> · 商品下单 · 模拟支付开箱即用 · 支付宝 / 微信可选对接<br>
  <b>企业级安全</b> · 五重加密 · Redis 限流与分布式锁 · RBAC 权限与审计日志
</p>

<p align="center">
  <sub>首次部署默认启用 <b>MOCK 模拟支付</b>，可直接体验完整购买与发卡流程；真实收款需自行配置商户密钥与回调地址。</sub>
</p>

<p align="center">
  <a href="https://github.com/Ms-liyc/YU-Kami"><img src="https://img.shields.io/github/stars/Ms-liyc/YU-Kami?style=social" alt="GitHub Stars"></a>
</p>

<p align="center">
  <a href="https://github.com/Ms-liyc/YU-Kami">🌟 Star 项目</a> ·
  <a href="#-快速部署指南">📖 部署文档</a> ·
  <a href="docs/PAYMENT.md">💳 支付配置</a> ·
  <a href="docs/API.md">📡 API 文档</a> ·
  <a href="#-开发路线图">📋 更新日志</a> ·
  <a href="https://github.com/Ms-liyc/YU-Kami/issues">🐛 反馈问题</a>
</p>

---

## 📦 快速部署指南

本项目提供多种灵活的部署方式，满足不同场景需求。

### 方式一：Docker 全栈一键部署（推荐 🔥）

```bash
git clone https://github.com/Ms-liyc/YU-Kami.git
cd YU-Kami
docker compose up -d --build
```

**默认访问地址：**

| 服务 | 地址 |
|------|------|
| 用户购买中心 | http://localhost/shop |
| 管理后台 | http://localhost/login |
| 后端 API | http://localhost:8080 |
| 默认管理员 | `admin` / `admin123` |

> **支付说明**：首次部署默认启用**模拟支付（MOCK）**，无需配置即可体验购买与发卡。  
> 真实支付宝/微信收款为**可选功能**，需部署者自行申请商户资质、在管理后台配置密钥，并按实际环境设置回调地址。详见 [docs/PAYMENT.md](docs/PAYMENT.md)。

---

### 方式一-B：Linux 一键安装脚本

```bash
curl -O https://raw.githubusercontent.com/Ms-liyc/YU-Kami/main/install.sh
chmod +x install.sh
sudo ./install.sh
```

自动安装 Docker、克隆代码、构建镜像并启动服务。

---

### 方式一-C：开发模式（仅数据库）

```bash
docker compose -f docker-compose.dev.yml up -d
cd backend && mvn spring-boot:run
cd frontend && npm install && npm run dev
```

| 服务 | 地址 |
|------|------|
| 用户前台 | http://localhost:5173/shop |
| 管理后台 | http://localhost:5173/login |

---

### 方式二：手动编译部署

适用于开发者或需要深度定制的场景。

**前提条件：** JDK 17+、Maven 3.8+、Node.js 18+、PostgreSQL 16+、Redis 7+

```bash
# 1. 克隆代码
git clone https://github.com/Ms-liyc/YU-Kami.git
cd YU-Kami

# 2. 初始化数据库
psql -U yukami -d yukami -f backend/src/main/resources/db/schema.sql

# 3. 后端编译运行
cd backend
./mvnw clean package -DskipTests   # Windows: mvnw.cmd
java -jar target/yu-kami-1.6.0.jar

# 4. 前端编译
cd ../frontend
npm install
npm run build
# 构建产物位于 dist/ 目录，使用 Nginx 托管
```

**Nginx 反向代理示例：**

```nginx
location /api {
    proxy_pass http://localhost:8080/api;
}
```

---

### 方式三：生产环境变量配置

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

# 完整模板见 .env.example

# 支付对接（可选，按你的部署环境填写，无固定值）
# PAYMENT_BASE_URL=https://your-domain.com
# PAYMENT_RETURN_URL=https://your-domain.com/shop/orders
```

> 从 v1.1.0 升级至 v1.2.0，请额外执行：
> `psql -U yukami -d yukami -f backend/src/main/resources/db/migration-v1.2.0.sql`

---

## 📊 功能模块

### 🎛️ 管理员后台

* 📈 **数据概览** — 实时统计面板、使用率分析、最近兑换记录
* 📦 **产品管理** — 时长卡 / 余额卡 / 单次卡，灵活配置权益
* 🔑 **卡密管理** — 批量生成、批量导入、作废、状态查询
* 📁 **批次管理** — 批次追踪、使用率进度条、导入导出
* 📋 **兑换记录** — 全链路兑换审计，成功/失败明细
* 🔌 **API 客户端** — AppKey / AppSecret 接入管理
* 👥 **用户管理** — 多管理员 RBAC 权限控制
* 📝 **审计日志** — 操作行为全记录
* 🔔 **Webhook** — 兑换成功实时回调第三方系统
* 📤 **数据导出** — CSV / Excel 双格式导出

### 🔌 开发者接口

* ✅ **卡密兑换 API** — 标准化 RESTful 接口，轻松集成
* 🛡️ **多重加密** — HMAC + AES + RSA + BCrypt + SHA-256 五重防护
* 🔔 **WebHook 回调** — HMAC-SHA256 签名，兑换成功实时推送
* 🔐 **JWT 认证** — 管理端安全鉴权
* ⏱️ **限流保护** — Redis 滑动窗口，防暴力破解

---

## 🎯 使用场景

### 💼 软件授权

* 🖥️ 桌面软件激活码
* 📱 移动应用授权
* 🎮 游戏道具 / 会员验证
* 🔧 插件功能解锁

### 🎓 在线教育

* 📚 课程访问控制
* 🎥 视频观看权限
* 📝 考试系统验证
* 🏆 证书颁发管理

### 💰 虚拟商品

* 🎁 优惠券 / 兑换码
* 🎪 会员权益管理
* 🛍️ 卡密自动发货
* 💎 VIP 服务激活

---

## 🔐 安全与性能

| 功能特性 | 说明 |
|---------|------|
| 🔒 卡密存储 | HMAC-SHA256 + 独立 Pepper，数据库不存明文 |
| 🔐 元数据加密 | AES-256-GCM 加密敏感信息 |
| 🔑 密码安全 | BCrypt 哈希存储管理员密码 |
| ✍️ API 签名 | RSA-SHA256 开放接口验签 |
| ✅ 格式校验 | SHA-256 校验码防手输错误 |
| ⚡ 分布式锁 | Redis 锁防止并发重复兑换 |
| 🔄 乐观锁 | 数据库 version 字段原子更新 |
| 🚦 限流保护 | Redis 滑动窗口限流 |
| 🗄️ 数据库 | PostgreSQL 16 高并发事务 |
| 💾 连接池 | HikariCP 最大 50 连接 |

---

## 📡 API 文档

### 开放接口 — 卡密兑换

```bash
POST /api/v1/redeem
Content-Type: application/json

{
  "cardKey": "VIP-XXXXXX-ABCDEF",
  "redeemUser": "user_12345"
}
```

### 管理端接口（需 JWT）

| 接口 | 方法 | 说明 |
|------|------|------|
| `/api/admin/auth/login` | POST | 管理员登录 |
| `/api/admin/dashboard` | GET | 数据概览 |
| `/api/admin/products` | CRUD | 产品管理 |
| `/api/admin/cards/generate` | POST | 批量生成卡密 |
| `/api/admin/cards/import` | POST | 批量导入卡密 |
| `/api/admin/cards` | GET | 卡密列表 |
| `/api/admin/cards/batches` | GET | 批次列表 |
| `/api/admin/cards/{id}/revoke` | POST | 作废卡密 |
| `/api/admin/redeem-records` | GET | 兑换记录 |
| `/api/admin/api-clients` | CRUD | API 客户端 |
| `/api/admin/users` | CRUD | 用户管理 |
| `/api/admin/audit-logs` | GET | 审计日志 |
| `/api/admin/webhooks` | CRUD | Webhook 配置 |
| `/api/admin/export/*` | GET | CSV / Excel 导出 |

### Webhook 回调格式

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

> 签名头：`X-YK-Signature`（HMAC-SHA256） · 事件头：`X-YK-Event`

---

## 🛣️ 开发路线图

### 📅 更新日志

#### v1.6.0

* 📡 **Swagger API 文档**：`http://localhost:8080/swagger-ui.html`，支持 JWT 在线调试
* ⚠️ **生产安全告警**：启动时检测默认 JWT/HMAC/AES/数据库密码并输出警告
* 📱 **移动端适配**：管理端侧栏抽屉菜单、商城头部响应式优化
* 📖 **贡献指南**：`CONTRIBUTING.md` + `docs/API.md`

#### v1.5.0

* 📄 **开源基础完善**：MIT LICENSE、`.env.example`、GitHub Actions CI、Maven Wrapper
* 🔒 **默认密码安全**：登录检测初始密码，后台一键修改
* 🎨 **品牌统一**：Logo / Favicon 应用于登录页、管理端、商城
* 📱 **微信扫码优化**：二维码本地生成，不再依赖外网服务
* 🐛 **体验修复**：订单管理增加「已支付」筛选

#### v1.4.1

* 📢 **支付对接可选引导**：首次部署提示自行配置，默认 MOCK 即可体验
* 🔒 **安全加固**：管理端/商城端 JWT 角色隔离，支付渠道接口脱敏
* 🐛 **支付修复**：回调并发防重复发卡、渠道禁用后仍可验签、待支付订单续付

#### v1.4.0

* 💳 **支付宝正式 SDK**：电脑网站支付（Page Pay）+ 异步回调验签
* 💚 **微信支付正式 SDK**：Native 扫码支付（APIv3）+ 回调解密
* 🔄 **预支付流程**：跳转支付 / 扫码支付 / 状态轮询
* 📖 支付配置文档：[docs/PAYMENT.md](docs/PAYMENT.md)

#### v1.3.0

* 🛒 **用户前台购买中心**：商品浏览、注册登录、在线购买
* 📋 **订单管理**：管理端订单列表、取消、支付配置
* 💳 **支付对接**：模拟支付 / 支付宝 / 微信（可配置启用）
* 🐳 **Docker 一键部署**：`docker compose up -d` 全栈启动
* 📜 **Linux 安装脚本**：`curl ... | sudo ./install.sh`
* 🌐 **多语言支持**：中文 / English（vue-i18n）

#### v1.2.0

* 📤 **Excel 导出**：卡密、批次、兑换记录支持 `.xlsx` 格式
* 📥 **批量导入**：支持 TXT / CSV / Excel 批量导入外部卡密
* 🔔 **Webhook**：兑换成功异步回调，HMAC 签名验签
* 🗄️ **数据库升级脚本**：`migration-v1.2.0.sql`

#### v1.1.0

* 🔌 API 客户端管理、用户管理、审计日志
* 📤 CSV 导出功能
* 🎨 管理后台界面全面优化

#### v1.0.0

* 🎉 初始版本发布
* 🔑 卡密生成 / 兑换 / 管理后台
* 🔐 五重加密体系
* ⚡ Redis 高并发保障

### 🚀 后续计划

* [ ] 微信 JSAPI 支付（公众号/小程序内）
* [ ] 用户钱包与余额支付
* [ ] 移动端适配优化
* [ ] Docker Hub 官方镜像

---

## 🏗️ 技术架构

```
┌─────────────┐     ┌─────────────┐     ┌──────────────┐
│  Vue 3 前端  │────▶│ Spring Boot │────▶│ PostgreSQL 16│
│ Element Plus│     │  MyBatis-Plus│     └──────────────┘
└─────────────┘     │  Spring Sec  │     ┌──────────────┐
                    │              │────▶│   Redis 7    │
                    └─────────────┘     └──────────────┘
```

| 层级 | 技术 |
|------|------|
| 后端 | Java 17 + Spring Boot 3.2 + MyBatis-Plus |
| 前端 | Vue 3 + Vite + Element Plus + Pinia |
| 数据库 | PostgreSQL 16 |
| 缓存 | Redis 7 |

---

## 🤝 参与贡献

我们欢迎所有形式的贡献！

1. 🍴 Fork 本仓库
2. 🌿 创建特性分支（`git checkout -b feature/AmazingFeature`）
3. 💾 提交更改（`git commit -m 'Add some AmazingFeature'`）
4. 📤 推送分支（`git push origin feature/AmazingFeature`）
5. 🔄 创建 Pull Request

---

## 📞 联系我们

### 🌐 开源地址

* 🐙 **GitHub**: [https://github.com/Ms-liyc/YU-Kami](https://github.com/Ms-liyc/YU-Kami)

### 💬 问题反馈

* 🐛 [提交 Issue](https://github.com/Ms-liyc/YU-Kami/issues)
* 💡 [功能建议](https://github.com/Ms-liyc/YU-Kami/issues)

---

## 📄 开源协议

本项目基于 [MIT License](LICENSE) 开源协议发布。

---

## ⭐ Star 历史

[![Star History Chart](https://api.star-history.com/svg?repos=Ms-liyc/YU-Kami&type=Date)](https://star-history.com/#Ms-liyc/YU-Kami&Date)

---

<p align="center">
  <b>🎉 如果这个项目对您有帮助，请给我们一个 ⭐ Star！</b>
</p>

<p align="center">
  <b>屿宸科技 · 让我们一起构建更好的卡密系统！</b> 🚀
</p>

<p align="center">
  <i>© 2026 屿宸科技 YU-Kami. All rights reserved.</i>
</p>
