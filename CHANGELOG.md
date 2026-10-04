# 更新日志

本文件记录 YU-Kami 各版本的主要变更。完整历史亦见 [README.md](README.md#-开发路线图)。

## [1.17.0] — 2026-01

### 新增

- 商城独立商品详情页 `/shop/product/:id`
- Webhook 管理页完整 i18n（中英双语）
- 订单支付配置弹窗 i18n
- 密码重置链接单元测试

### 改进

- 商品卡片点击跳转详情页，再进入购买流程
- `docker-compose.hub.yml` 补充邮件/验证码/库存告警环境变量示例

## [1.16.0] — 2026-01

### 新增

- 商城商品分类 API 与前台分类筛选
- 买家邮箱验证状态展示、重发验证邮件、验证结果页
- 低库存邮件告警（`STOCK_ALERT_EMAIL`）
- 管理端 2FA 启用状态查询

### 修复

- 管理端密码重置邮件链接路径错误（`/reset-password`）
- 邮箱验证链接改为跳转商城前端页面

### 改进

- 登录页、仪表盘统计卡片 i18n 补全
- 安全设置页区分已启用/未启用 2FA 的操作流程

## [1.15.0] — 2026-01

### 新增

- 图形验证码（管理端/商城登录与注册，`GET /api/captcha`）
- 邮件找回密码（管理端与商城买家）
- 管理端 TOTP 双因素认证（2FA）
- 充值订单退款、原路退款（支付宝/微信，需已配置支付渠道）
- 产品分类字段与筛选
- 仪表盘近 7 日订单/充值趋势 API
- 定时低库存 Webhook（`LOW_STOCK`）
- 管理端订单 CSV 导出
- `docker-compose.https.yml` 与 [docs/HTTPS.md](docs/HTTPS.md)

### 改进

- 订单退款支持选择「退至余额」或「原路退款」
- 登录页增加验证码、2FA 与忘记密码入口
- 管理端新增「安全设置」菜单页

## [1.14.0] — 2026-01

### 新增

- 仪表盘库存告警（可配置 `STOCK_LOW_THRESHOLD`）
- 产品管理页展示各商品可用卡密库存
- 管理端查看买家钱包流水
- Webhook 支持 `ORDER_DELIVERED` / `RECHARGE_SUCCESS` / `ORDER_REFUNDED`

### 改进

- 仪表盘新增「今日订单」统计
- Webhook 管理页扩展可订阅事件

## [1.13.0] — 2026-01

### 新增

- 管理端买家账号启用/禁用
- 买家钱包自助充值（MOCK/支付宝/微信）
- 管理端订单退款（退回余额、作废卡密）
- Hub Compose 默认关闭 Swagger 并限制 CORS

### 改进

- 发货订单关联 `card_id`，支持退款时回收卡密
- `shop_order.order_type` 区分商品订单与充值订单
- Docker 文档补充生产环境变量说明

## [1.12.1] — 2026-01

### 安全

- 移除 `application.yml` 中泄露的真实数据库默认密码
- Spring Security 未匹配路径改为 `denyAll`，不再默认放行
- 可配置 CORS 来源（`CORS_ALLOWED_ORIGINS`），生产环境限制域名
- 生产环境可关闭 Swagger（`SWAGGER_ENABLED=false`）
- 管理端/商城登录与注册增加 Redis 速率限制
- 添加 HTTP 安全响应头（后端 + Nginx）
- 启动自检扩展：弱密码、Swagger、CORS 警告
- 新增 [docs/SECURITY.md](docs/SECURITY.md)

## [1.12.0] — 2026-01

### 新增

- 管理端商城买家列表与钱包余额调整（`/api/admin/shop-users`）
- Docker Hub 官方镜像与 `docker-compose.hub.yml`
- GitHub Actions Docker Publish 工作流

### 改进

- 管理后台移动端：页头、筛选栏、对话框、仪表盘布局优化
- 余额调整写入审计日志

## [1.11.0] — 2026-01

### 新增

- 用户钱包余额与交易明细（`/shop/wallet`）
- 余额支付渠道（`BALANCE`），购买页可直接扣款发货
- 演示账号 `demo` 自动获得 ¥200 初始余额
- 启动时自动迁移 `shop_user.balance` 与 `wallet_transaction` 表

### 改进

- 个人中心展示余额并跳转钱包页
- 支付渠道接口需登录，动态包含余额支付

## [1.10.2] — 2026-01

### 改进

- 版本号统一为 1.10.2（Maven / npm / Swagger / 管理端侧栏自动读取）
- 商城登录页、商品列表、订单查询 i18n 补全
- 管理后台表格移动端横向滚动适配
- 订单管理页筛选与取消按钮 i18n
- `.gitignore` 忽略 Redis dump 文件

## [1.10.1] — 2026-01

### 改进

- 新增查单、兑换、个人中心嵌入预览页与界面截图
- 商城顶栏/底栏/页脚导航中英双语（i18n）
- 截图脚本支持无后端降级与容错
- 新增 [CHANGELOG.md](CHANGELOG.md)
- `.env.example` 移除示例真实密码

## [1.10.0] — 2026-01

### 新增

- 订单号查询页（`/shop/query`），支持查看卡密与继续支付
- 个人中心（`/shop/profile`），资料修改与密码修改
- 卡密兑换页（`/shop/redeem`），对接 `POST /api/v1/redeem`
- 商城公开统计 API（`/api/shop/stats`）与最近成交 API（`/api/shop/orders/recent`）
- 购买页数量选择（1–99）
- 我的订单状态筛选与订单号搜索

### 改进

- 落地页统计条与成交滚动对接真实数据
- 商城布局导航、底栏、页脚中英双语
- 界面截图扩展至查单、兑换、个人中心
- 版本号统一为 1.10.0（Maven / npm）

## [1.9.0] — 2026-01

- 数据库迁移：PostgreSQL → MySQL 8
- 发卡网落地页重构，PC + 手机双端预览
- 全站深色模式与 CSS 变量主题
- Playwright 自动截图脚本
- 雪花 ID 前端 JSON 大整数修复
- 自定义 SVG 图标替代 emoji

## [1.8.0]

- 优惠券与促销活动
- 智能定价引擎（促销 + 优惠券取最优）

## [1.7.0]

- 微信 JSAPI 支付
- 单元测试、Docker 健康检查
- 管理后台 i18n

## [1.6.0] 及更早

详见 [README 更新日志](README.md#-开发路线图)。

[1.17.0]: https://github.com/Ms-liyc/YU-Kami/compare/v1.16.0...v1.17.0
[1.16.0]: https://github.com/Ms-liyc/YU-Kami/compare/v1.15.0...v1.16.0
[1.15.0]: https://github.com/Ms-liyc/YU-Kami/compare/v1.14.0...v1.15.0
[1.14.0]: https://github.com/Ms-liyc/YU-Kami/compare/v1.13.0...v1.14.0
[1.13.0]: https://github.com/Ms-liyc/YU-Kami/compare/v1.12.1...v1.13.0
[1.12.1]: https://github.com/Ms-liyc/YU-Kami/compare/v1.12.0...v1.12.1
[1.12.0]: https://github.com/Ms-liyc/YU-Kami/compare/v1.11.0...v1.12.0
[1.11.0]: https://github.com/Ms-liyc/YU-Kami/compare/v1.10.2...v1.11.0
[1.10.2]: https://github.com/Ms-liyc/YU-Kami/compare/v1.10.1...v1.10.2
[1.10.1]: https://github.com/Ms-liyc/YU-Kami/compare/v1.10.0...v1.10.1
[1.10.0]: https://github.com/Ms-liyc/YU-Kami/compare/v1.9.0...v1.10.0
[1.9.0]: https://github.com/Ms-liyc/YU-Kami/compare/v1.8.0...v1.9.0
