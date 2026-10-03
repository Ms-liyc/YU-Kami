# 更新日志

本文件记录 YU-Kami 各版本的主要变更。完整历史亦见 [README.md](README.md#-开发路线图)。

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

[1.10.2]: https://github.com/Ms-liyc/YU-Kami/compare/v1.10.1...v1.10.2
[1.10.1]: https://github.com/Ms-liyc/YU-Kami/compare/v1.10.0...v1.10.1
[1.10.0]: https://github.com/Ms-liyc/YU-Kami/compare/v1.9.0...v1.10.0
[1.9.0]: https://github.com/Ms-liyc/YU-Kami/compare/v1.8.0...v1.9.0
