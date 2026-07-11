# 车辆图片支持 — 功能增强设计

## Source
- **Feature/Module:** 车辆管理（后端 + 管理端 + 客户端）
- **Requestor:** 用户（在售车辆网页界面放入真实车辆图片）

## Impact Analysis

### Affected Modules

| Layer | Module | Change Type |
|-------|--------|-------------|
| DB | `car` 表 | 新增 `image_url` 列 |
| Backend Entity | `Car.java` | 新增 `imageUrl` 字段 |
| Backend Controller | `CarController.java` | 新增/修改接口改为接收 `MultipartFile` |
| Backend Service | `CarService.java` | 新增图片文件保存/删除逻辑 |
| Backend Config | `application.yml` | 新增 multipart 配置 + 静态资源映射 |
| Admin API | `car-sales-admin/src/api/index.js` | 新增/修改接口用 FormData |
| Admin Form | `CarForm.vue` | 新增文件上传 input + 预览 |
| Admin List | `CarManage.vue` | 可选新增图片缩略图列 |
| Client API | `car-sales-client/src/api/index.js` | 无需改动（只读接口不变） |
| Client List | `CarList.vue` | 🚗 → `<img :src>` |
| Client Detail | `CarDetail.vue` | 🚗 → `<img :src>` |
| Client CSS | `main.css` | `.card-image` / `.detail-image` 改为图片样式 |

### Affected APIs

| Method | Current | New |
|--------|---------|-----|
| `POST /api/cars` | `@RequestBody Car` | `@RequestParam` + `@RequestPart("image", required=false) MultipartFile` |
| `PUT /api/cars/{id}` | `@RequestBody Car` | 同上 |
| `GET /api/cars` | 返回 Car 列表（无 imageUrl） | 返回含 imageUrl |
| `GET /api/cars/{id}` | 返回 Car（无 imageUrl） | 返回含 imageUrl |
| 新增 `GET /uploads/images/{filename}` | — | 静态资源直接访问 |

### Affected DB Schema

```
ALTER TABLE car ADD COLUMN image_url VARCHAR(500) DEFAULT NULL COMMENT '图片URL';
```

### Breaking Change Risk
- **Yes — API 接口签名变更**：`POST` 和 `PUT` 从 JSON body 改为 `multipart/form-data`
- 前端管理端调用侧必须同步更新
- 客户端（只读）不受影响

## Design Summary

### Architecture

```
┌─────────────────────────────────────────────────────────┐
│                  新增/编辑车辆流程                          │
│                                                          │
│  CarForm.vue                                              │
│   ├─ 字段 (brand, model, ...)                             │
│   └─ 图片文件 (选填)                                      │
│       │                                                   │
│       └─ FormData ──→ POST/PUT /api/cars ──→ Controller   │
│                                                    │      │
│                                                    ▼      │
│                                              CarService   │
│                                               ├─ 保存文件到 │
│                                               │  uploads/  │
│                                               │  images/   │
│                                               ├─ 写入 DB   │
│                                               └─ 返回 Car  │
│                                                           │
│  CarList.vue / CarDetail.vue                              │
│    └─ <img :src="car.imageUrl">                           │
│         └─ 404 时 v-if 显示默认占位图                        │
└─────────────────────────────────────────────────────────┘
```

### Storage Strategy
- 图片存储路径：`uploads/images/{carId}.{ext}`（如 `uploads/images/1.jpg`）
- 数据库 `image_url` 存储相对路径 `/uploads/images/1.jpg`
- 后端通过 `spring.web.resources.static-locations` 映射为可直接访问的 URL
- 无图片时 `image_url` = `NULL`，前端兜底显示默认占位图

### Image Constraints
- 格式：jpg / png / webp
- 大小上限：5MB
- 新增时图片选填，编辑时可选替换或移除

## Stories

### Story: STORY-IMG-01 - 后端：数据库加图片列 + 文件上传接口

- **Persona:** 系统
- **Story Statement:**
  As a 系统管理员，I want 车辆数据支持图片字段且后端能接收/保存图片，So that 前台可以展示真实车辆图片。
- **Priority:** High
- **Story Points:** 3
- **Acceptance Criteria:**
  - [ ] Given `car` 表存在，when 执行增量 DDL，then `image_url VARCHAR(500) DEFAULT NULL` 列被成功添加
  - [ ] Given 新增车辆请求含图片文件，when 调用 `POST /api/cars`（multipart/form-data），then 图片保存至 `uploads/images/{carId}.{ext}`、`image_url` 写入数据库、返回 Car 含 `imageUrl`
  - [ ] Given 新增车辆请求不含图片，when 调用 `POST /api/cars`，then `image_url` 为 `NULL`，正常创建车辆
  - [ ] Given 编辑车辆请求含新图片，when 调用 `PUT /api/cars/{id}`（multipart/form-data），then 旧图片被覆盖、`image_url` 更新
  - [ ] Given 编辑车辆请求不含图片字段，when 调用 `PUT /api/cars/{id}`，then 原有图片不变
  - [ ] Given 图片格式为 jpg/png/webp 且 ≤5MB，when 上传，then 成功
  - [ ] Given 图片格式不正确或超 5MB，when 上传，then 返回 400 错误
  - [ ] Given 直接访问 `/uploads/images/{filename}`，when 图片存在，then 可正常显示

### Story: STORY-IMG-02 - 管理端：车辆新增/编辑表单支持图片上传与展示

- **Persona:** 系统管理员
- **Story Statement:**
  As a 系统管理员，I want 在新增和编辑车辆时能上传/更换/移除图片，So that 车辆信息可附带真实图片。
- **Priority:** High
- **Story Points:** 2
- **Acceptance Criteria:**
  - [ ] Given 进入新增车辆页面，when 选择图片文件（jpg/png/webp），then 显示预览缩略图
  - [ ] Given 新增车辆表单已选择图片，when 点保存，then 以 FormData 提交、保存后跳转列表
  - [ ] Given 编辑车辆（已有图片），when 打开编辑页，then 显示当前图片预览
  - [ ] Given 编辑页面选择新图片替换，when 保存，then 图片被更新
  - [ ] Given 编辑页面点了"移除图片"，when 保存，then `image_url` 置为 `NULL`
  - [ ] Given 编辑页面不操作图片区域，when 保存，then 原图片不变
  - [ ] Given 车辆列表页，when 车辆有图片，then 显示缩略图（可选列）

### Story: STORY-IMG-03 - 客户端：在售车辆列表和详情页展示真实图片

- **Persona:** 客户
- **Story Statement:**
  As a 客户，I want 在浏览在售车辆时看到真实车辆图片，So that 可以直观了解车辆外观。
- **Priority:** High
- **Story Points:** 1
- **Acceptance Criteria:**
  - [ ] Given 车辆列表页，when 车辆有 `imageUrl`，then 卡片显示该图片（160px 高，cover 裁剪）
  - [ ] Given 车辆列表页，when 车辆无 `imageUrl`，then 显示默认占位图（保留渐变背景+🚗）
  - [ ] Given 车辆详情页，when 车辆有 `imageUrl`，then 左侧大图显示该图片（360x240）
  - [ ] Given 车辆详情页，when 车辆无 `imageUrl`，then 显示默认占位图

## Risk Assessment

| Risk | Likelihood | Impact | Mitigation |
|------|:----------:|:------:|------------|
| API 从 JSON 改为 multipart，破坏现有调用 | Medium | High | 同步更新管理端前端所有调用处；客户端只读接口不变 |
| 用户上传超大/恶意文件 | Low | Medium | 配置 max-file-size、限制格式 jpg/png/webp |
| 图片文件随车辆删除未清理 | Low | Low | 后续可补充定时清理任务，当前接受 |
| 编辑时并发覆盖图片 | Low | Low | 当前为单用户管理，可接受 |

## Regression Risk Areas
- 车辆 CRUD 功能回归（新增/编辑/查询/删除）
- Excel 导入不受影响（不涉及图片）
- 客户端的订单/预约流程涉及车辆信息展示，需回归确认图片不破坏布局

## Escalation
- DB schema 和 API 签名变更 → 已按完整设计处理
