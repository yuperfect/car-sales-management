# Bug Register

| 日期 | Bug 描述 | 修复方式 | 状态 |
|------|---------|---------|:----:|
| - | JDK 25→17 兼容性修复 | 降级 JDK 版本声明 | ✅ 已修复 |
| - | Car 实体 year 字段类型 + JSON 序列化懒加载 | 修复实体映射和 Jackson 配置 | ✅ 已修复 |
| - | API 端点 RequestBody 接收 + GET by ID + 扁平字段 + Car 关联 | 重写 Controller 层 | ✅ 已修复 |
| - | 前端字段名不匹配 (car.id→carId, item.id→orderId/appointmentId) | 统一字段命名 | ✅ 已修复 |
| - | 管理端首页统计数据 404 | 新增 CustomerController | ✅ 已修复 |
| - | 统计分析 ECharts 图表无数据 | 修复统计 API 返回值 | ✅ 已修复 |
| - | 双倍库存、预约 handler 404、车辆搜索失效 | 三项 Bug 批量修复 | ✅ 已修复 |
| - | 手机号校验、停售过滤、stop-all 端口杀进程、order表改名purchase_order | 四项 Bug 修复 | ✅ 已修复 |
| - | 停售状态/取消预约功能修复 | CarService 部分更新 + reject 端点 | ✅ 已修复 |
| - | 综合查询品牌车型字段、模糊精确查询、待确认Tab过滤 | 重写查询逻辑 | ✅ 已修复 |
| - | 启动/关闭脚本无法保证全部启停 | 重写 start.bat / stop.bat | ✅ 已修复 |
| - | E2E 测试库存不足导致失败 | 补充 RAV4/CR-V 库存数据 | ✅ 已修复 |
| - | 一键启动不自动打开浏览器 | 改用 cmd /c start | ✅ 已修复 |
| - | 下载模板文件名乱码/axios 拦截器兼容性 | 修复编码和拦截器配置 | ✅ 已修复 |
| - | emoji 在 CMD 中显示问号 | 替换为纯文本 [OK]/[FAIL]/[..] | ✅ 已修复 |
| - | 启动倒计时端口轮询检查 | 实现端口轮询 + 倒计时显示 | ✅ 已修复 |

| - | 图片上传 415 Unsupported Media Type | JSON端点加consumes约束 | ✅ 已修复 |
| - | 图片上传 413 Request Entity Too Large | Tomcat max-http-form-post-size 从2MB加到10MB | ✅ 已修复 |

> 所有已知 Bug 已在开发过程中修复，当前无未解决的 Bug。