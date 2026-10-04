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
  <img src="https://img.shields.io/badge/MySQL-8.4-4479A1?style=flat-square&logo=mysql&logoColor=white" alt="MySQL">
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
  <sub>首次部署默认启用 <b>MOCK 模拟支付</b>，可直接体验完整购买与发卡流程；真实收款需自行配置商户密钥与回调地址。</sub><br>
  <sub>演示买家 <code>demo</code> / <code>demo123</code> · 管理端 <code>admin</code> / <code>admin123</code></sub>
</p>

<p align="center">
  <a href="https://github.com/Ms-liyc/YU-Kami"><img src="https://img.shields.io/github/stars/Ms-liyc/YU-Kami?style=social" alt="GitHub Stars"></a>
</p>

<p align="center">
  <a href="https://github.com/Ms-liyc/YU-Kami">🌟 Star 项目</a> ·
  <a href="#-快速部署指南">📖 部署文档</a> ·
  <a href="docs/PAYMENT.md">💳 支付配置</a> ·
  <a href="docs/DOCKER.md">🐳 Docker Hub</a> ·
  <a href="docs/API.md">📡 API 文档</a> ·
  <a href="CHANGELOG.md">📝 更新日志</a> ·
  <a href="#-开发路线图">📋 路线图</a> ·
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

**Docker Hub 预构建镜像（无需编译）：**

```bash
docker compose -f docker-compose.hub.yml up -d
```

镜像：`msliyc/yu-kami-backend` · `msliyc/yu-kami-frontend`（详见 [docs/DOCKER.md](docs/DOCKER.md)）

**默认访问地址：**

| 服务 | 地址 |
|------|------|
| 用户购买中心 | http://localhost/shop |
| 管理后台 | http://localhost/login |
| 后端 API | http://localhost:8080 |
| Swagger 文档 | http://localhost:8080/swagger-ui.html |
| 默认管理员 | `admin` / `admin123` |
| 演示买家 | `demo` / `demo123` |

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
| 后端 API | http://localhost:8080（或 `SERVER_PORT=8081`） |
| 演示买家 | `demo` / `demo123` |

> 本地开发若 8080 端口被占用，可设置 `SERVER_PORT=8081`，前端 `VITE_API_PROXY=http://localhost:8081`。  
> 更新商城界面截图：`cd frontend && npm run build && npx vite preview --port 5174`，另开终端执行 `npm run capture:screenshots`（查单/兑换/个人中心等页面无需后端）。

---

### 方式二：手动编译部署

适用于开发者或需要深度定制的场景。

**前提条件：** JDK 17+、Maven 3.8+、Node.js 18+、MySQL 8.0+、Redis 7+

```bash
# 1. 克隆代码
git clone https://github.com/Ms-liyc/YU-Kami.git
cd YU-Kami

# 2. 初始化数据库（MySQL 8）
mysql -u root -p yukami < backend/src/main/resources/db/schema.sql

# 3. 后端编译运行
cd backend
./mvnw clean package -DskipTests   # Windows: mvnw.cmd
java -jar target/yu-kami-*.jar

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
DB_PORT=3306
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

> 数据库结构以 `backend/src/main/resources/db/schema.sql` 为唯一来源；旧版升级请对照该文件手动补字段/表，或重建库。

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

### 🛍️ 用户发卡网（商城前台）

| 页面 | 路径 | 说明 |
|------|------|------|
| 落地页 | `/shop` | Hero、购买流程、功能展示、FAQ、热销商品 |
| 订单查询 | `/shop/query` | 凭订单号查状态，已发货可查看卡密 |
| 卡密兑换 | `/shop/redeem` | 输入卡密与标识，对接开放兑换 API |
| 登录 / 注册 | `/shop/login` | 商城买家账号 |
| 我的订单 | `/shop/orders` | 状态筛选、订单号搜索、复制卡密 |
| 购买页 | `/shop/buy/:id` | 数量选择、优惠券、余额/模拟/支付宝/微信支付 |
| 个人中心 | `/shop/profile` | 修改昵称/邮箱、修改密码、余额一览 |
| 我的钱包 | `/shop/wallet` | 余额查询、交易明细 |

* 🏠 **落地页** — Hero 区、实时统计、成交滚动、购买流程、界面一览、FAQ
* 📱 **双端预览** — PC 商品列表 + 手机端叠加展示，支持静态截图与嵌入页回退
* 🌙 **深色模式** — 全站 CSS 变量主题，顶栏一键切换
* 🛒 **商品购买** — 注册登录、数量选择、优惠券、促销价、模拟/支付宝/微信支付
* 🔍 **订单查询** — 订单号精确查询；登录后订单页筛选、搜索与一键复制卡密
* 👤 **个人中心** — 资料修改、密码修改、余额一览
* 💰 **钱包余额** — 余额支付、交易明细，演示账号 ¥200 起
* 🎫 **卡密兑换** — 前台兑换入口，对接 `/api/v1/redeem`
* 🎭 **演示数据** — 内置示例商品、促销与优惠券（`demo` / `demo123`）

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
| 🚦 限流保护 | Redis 滑动窗口限流（兑换 / 登录 / 注册） |
| 🛡️ 访问控制 | 未声明 API 默认拒绝；CORS / Swagger 可按环境配置 |
| 数据库 | MySQL 8 高并发事务 |
| 💾 连接池 | HikariCP 最大 50 连接 |

---

## 📡 API 文档

> 完整接口列表见 [docs/API.md](docs/API.md)，本地调试推荐 Swagger UI。生产部署见 [docs/SECURITY.md](docs/SECURITY.md)。

### 开放接口 — 卡密兑换

```bash
POST /api/v1/redeem
Content-Type: application/json

{
  "cardKey": "VIP-XXXXXX-ABCDEF",
  "redeemUser": "user_12345"
}
```

### 商城接口（买家 JWT）

| 接口 | 方法 | 说明 |
|------|------|------|
| `/api/shop/auth/register` | POST | 注册 |
| `/api/shop/auth/login` | POST | 登录 |
| `/api/shop/auth/me` | GET | 当前用户资料 |
| `/api/shop/auth/profile` | PUT | 更新昵称/邮箱 |
| `/api/shop/auth/password` | PUT | 修改密码 |
| `/api/shop/products` | GET | 商品列表 |
| `/api/shop/products/{id}` | GET | 商品详情 |
| `/api/shop/orders` | GET | 我的订单（支持 `status`、`orderNo` 筛选） |
| `/api/shop/orders/lookup` | GET | 按订单号查询（`?orderNo=`） |
| `/api/shop/orders` | POST | 创建订单 |
| `/api/shop/orders/pay` | POST | 模拟支付 |
| `/api/shop/orders/prepay` | POST | 预支付（支付宝/微信） |
| `/api/shop/orders/{id}/card` | GET | 查看卡密 |
| `/api/shop/orders/{id}/cancel` | POST | 取消订单 |
| `/api/shop/orders/pricing/preview` | POST | 价格预览（促销+优惠券） |

### 钱包接口（需 SHOP_USER JWT）

| 接口 | 方法 | 说明 |
|------|------|------|
| `/api/shop/wallet` | GET | 当前余额 |
| `/api/shop/wallet/transactions` | GET | 交易明细（分页） |

> 余额支付：创建订单后 `POST /api/shop/orders/prepay`，`paymentMethod` 传 `BALANCE`。

### 商城公开接口（无需 Token）

| 接口 | 方法 | 说明 |
|------|------|------|
| `/api/shop/stats` | GET | 落地页统计（商品数、成交数等） |
| `/api/shop/orders/recent` | GET | 最近成交滚动（脱敏） |
| `/api/shop/products/**` | GET | 商品浏览 |

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
> 支持事件：`REDEEM_SUCCESS` · `ORDER_DELIVERED` · `RECHARGE_SUCCESS` · `ORDER_REFUNDED` · `LOW_STOCK`

---

## 🛣️ 开发路线图

### 📅 更新日志

#### v1.16.0

* 📂 **商城分类筛选**：商品列表按 category 标签过滤
* ✉️ **邮箱验证**：个人中心展示验证状态，支持重发验证邮件
* 📧 **库存邮件告警**：`STOCK_ALERT_EMAIL` 低库存邮件通知
* 🐛 **修复**：密码重置链接、邮箱验证跳转页
* 🌐 **i18n**：登录页与仪表盘统计卡片中英双语

#### v1.15.0

* 🔐 **登录验证码**：管理端/商城登录图形验证码，防暴力破解
* 📧 **找回密码**：邮件重置链接（需配置 SMTP）
* 🛡️ **2FA**：管理端 TOTP 双因素认证
* ↩️ **原路退款**：订单/充值支持退至余额或支付宝/微信原路退款
* 📂 **产品分类**：产品管理增加 category 字段
* 📈 **趋势图表**：仪表盘近 7 日订单与充值柱状图
* 🔔 **库存 Webhook**：定时检测低库存并推送 `LOW_STOCK`
* 📤 **订单导出**：管理端 CSV 导出
* 🔒 **HTTPS 部署**：`docker-compose.https.yml` + [docs/HTTPS.md](docs/HTTPS.md)

#### v1.14.0

* 📦 **库存告警**：仪表盘低库存提醒，产品页显示可用卡密数
* 🔔 **订单 Webhook**：发货/充值/退款事件回调
* 💳 **买家流水**：管理端查看商城买家钱包交易明细
* 📊 **今日订单**：仪表盘新增订单统计

#### v1.13.0

* 👤 **买家管理增强**：管理端启用/禁用商城买家账号
* 💰 **自助充值**：钱包页支持 MOCK/支付宝/微信充值余额
* ↩️ **订单退款**：已支付/已发货订单退款至余额，自动作废卡密
* 🐳 **Hub 部署**：compose 默认 `SWAGGER_ENABLED=false` 与 CORS 限制

#### v1.12.1

* 🔒 **安全加固**：默认密码移除、未匹配 API 拒绝访问、CORS/Swagger 可配置
* 🛡️ **速率限制**：管理端/商城登录与注册防暴力破解（Redis）
* 📋 **安全响应头**：后端 + Nginx 添加 X-Frame-Options 等
* 📖 **安全文档**：新增 [docs/SECURITY.md](docs/SECURITY.md)

#### v1.12.0

* 👥 **商城买家管理**：管理端查看买家列表，充值/扣减钱包余额
* 🐳 **Docker Hub 镜像**：`msliyc/yu-kami-backend` / `frontend`，CI 自动推送
* 📱 **管理端移动优化**：页头/表格/对话框/仪表盘响应式布局

#### v1.11.0

* 💰 **用户钱包**：账户余额、交易明细页（`/shop/wallet`）
* 💳 **余额支付**：购买页选择「余额支付」，即时扣款并自动发货
* 🎭 **演示余额**：`demo` 账号初始 ¥200，可直接体验余额购卡
* 🔄 **自动迁移**：启动时补全 `shop_user.balance` 与钱包流水表

#### v1.10.2

* 📦 **版本统一**：Maven / npm / Swagger / 管理端侧栏同步 1.10.2，构建时自动注入
* 🌐 **商城 i18n**：登录页、商品筛选、订单查询提示中英双语
* 📱 **管理端移动适配**：后台表格小屏横向滚动，筛选栏自适应
* 🧹 **仓库清理**：`.gitignore` 忽略 Redis dump 文件

#### v1.10.1

* 🖼️ **界面截图**：查单、兑换、个人中心嵌入页 + 轮播图扩展
* 🌐 **商城 i18n**：顶栏、底栏、页脚、移动端导航中英双语
* 📝 **CHANGELOG.md**：独立更新日志文件
* 🔧 **截图脚本**：无后端时可降级，单页失败不中断

#### v1.10.0

* 🔍 **订单号查询**：凭订单号查询状态，已发货订单可直接查看卡密
* 📋 **订单筛选**：我的订单支持状态筛选与订单号搜索
* 👤 **个人中心**：修改昵称/邮箱、修改密码
* 🎫 **卡密兑换页**：商城前台接入 `/api/v1/redeem` 兑换入口
* 🔢 **购买数量**：购买页支持选择数量（1–99）
* 📊 **真实数据**：落地页统计与成交滚动条对接后端 API
* 🖼️ **界面截图**：新增查单、兑换、个人中心预览与轮播
* 🌐 **商城 i18n**：顶栏、底栏、页脚导航中英双语
* 📦 **版本统一**：Maven / npm 版本号同步为 1.10.0

> 详细变更见 [CHANGELOG.md](CHANGELOG.md)

#### v1.9.0

* 🗄️ **数据库迁移**：PostgreSQL → MySQL 8（`schema.sql`、Docker、配置统一）
* 🏠 **发卡网首页**：落地页重构，真实系统界面预览（PC + 手机双端）
* 🌙 **深色模式**：商城全站主题变量，Element Plus 组件适配
* 🖼️ **界面截图**：Playwright 自动截取嵌入页，`npm run capture:screenshots`
* 🔢 **雪花 ID 修复**：前端大整数 JSON 解析，购买页不再空白
* 🎨 **自定义图标**：移除 emoji，统一 SVG 图标组件

#### v1.8.0

* 🎁 **优惠券**：发布券码、限定适用商品、发行量与每人限用
* 🎉 **促销活动**：满减、折扣、特价/节日价，支持时间窗口与优先级
* 💰 **智能定价**：商城自动展示促销价，下单时优惠券与活动取最优
* 📋 数据表：活动与优惠券共用 `promotion` 表（`kind` 区分），商品范围存 `product_ids` 字段

#### v1.7.0

* 💚 **微信 JSAPI 支付**：微信内浏览器自动 OAuth + 调起支付（公众号 H5）
* 🧪 **单元测试**：支付发货、订单取消等核心逻辑测试
* 🐳 **Docker 健康检查**：Actuator + 后端就绪后再启动前端
* 🌐 **管理后台 i18n**：主导航、登录页、核心页面中英双语

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
* 🗄️ **Webhook 数据表**：并入 `schema.sql` 统一管理

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

* [x] 微信 JSAPI 支付（公众号 H5 内浏览器）
* [ ] 微信小程序支付
* [x] 用户钱包与余额支付
* [x] 商城落地页与移动端预览
* [x] 商城深色模式
* [x] 订单号查询与个人中心
* [x] 卡密兑换前台入口
* [x] 管理后台表格移动端基础适配
* [x] 管理端买家钱包充值/调整
* [x] 买家账号启用/禁用
* [x] 买家钱包自助充值
* [x] 管理端订单退款
* [x] 库存告警与产品可用库存
* [x] 订单/充值 Webhook 事件
* [x] Docker Hub 官方镜像
* [x] 管理端移动端深度优化（页头/表格/对话框/仪表盘）
* [x] 登录验证码 / 找回密码 / 2FA
* [x] 原路退款 / 充值退款 / 订单导出
* [x] 产品分类 / 趋势图表 / 库存 Webhook
* [x] HTTPS Docker 部署示例
* [x] 商城分类筛选 / 邮箱验证 / 库存邮件告警

---

## 🏗️ 技术架构

```
┌─────────────┐     ┌─────────────┐     ┌──────────────┐
│  Vue 3 前端  │────▶│ Spring Boot │────▶│   MySQL 8   │
│ Element Plus│     │  MyBatis-Plus│     └──────────────┘
└─────────────┘     │  Spring Sec  │     ┌──────────────┐
                    │              │────▶│   Redis 7    │
                    └─────────────┘     └──────────────┘
```

| 层级 | 技术 |
|------|------|
| 后端 | Java 17 + Spring Boot 3.2 + MyBatis-Plus |
| 前端 | Vue 3 + Vite + Element Plus + Pinia |
| 数据库 | MySQL 8 |
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
