# 车辆图片支持 — 产品待办列表

## Initiative
在售车辆图片展示功能增强

## Design Inputs
- Enhancement Design: `workspace/car-sales-management/design/car-image-support-enhancement-design.md`

## Background
当前系统的在售车辆列表和详情页均使用 🚗 emoji 作为车辆图片占位，无法展示真实车辆外观。设计文档虽已规划 `image_url` 字段，但从未实现。本次增强需要完成从数据库到前端展示的完整链路。

## Product Goals
1. 管理员能在新增/编辑车辆时上传、替换、移除图片
2. 客户能在在售车辆列表和详情页看到真实图片
3. 图片为选填，不影响现有业务流程

## Milestones

| Milestone | Name | Scope | Status |
|-----------|------|-------|:------:|
| MS-IMG-01 | 车辆图片支持（全链路） | STORY-IMG-01, STORY-IMG-02, STORY-IMG-03 | ✅ 设计中 |

---

## Identifier Standard
- Milestone ID: `MS-IMG-01`
- Story ID: `STORY-IMG-01` 起

---

### Epic: EPIC-IMG - 车辆图片展示支持

- **Objective:** 为车辆信息添加图片字段，实现上传、存储、展示全链路
- **Business Value:** 提升在售车辆页面的直观性和用户体验
- **Scope Boundary:** 仅涉及 car 表的图片列；不涉及多图、视频、PDF等
- **Dependencies:** 后端 API 从 JSON 改为 multipart，前端调用方需同步更新
- **NFR Considerations:** 图片 ≤5MB，格式限 jpg/png/webp
- **Milestone Mapping:** MS-IMG-01
- **Epic Acceptance Criteria:**
  - [ ] 数据库 car 表存在 `image_url` 列
  - [ ] 后端支持图片上传/替换/移除
  - [ ] 管理端表单支持图片上传预览
  - [ ] 客户端列表/详情页展示真实图片，无图片时显示默认占位

---

### Story: STORY-IMG-01 - 后端加图片列 + 文件上传接口

- **Parent Epic:** EPIC-IMG
- **Persona:** 系统
- **Story Statement:** As a 系统管理员，I want 车辆数据支持图片字段且后端能接收/保存图片，So that 前台可以展示真实车辆图片。
- **Priority:** High
- **Story Points:** 3
- **Dependencies:** 无
- **Acceptance Criteria (Given-When-Then):**
  - [ ] Given `car` 表存在，when 执行增量 DDL，then `image_url VARCHAR(500) DEFAULT NULL` 列被成功添加
  - [ ] Given 新增车辆请求含图片文件，when 以 multipart/form-data 调用 `POST /api/cars`，then 图片保存至 `uploads/images/{carId}.{ext}`、`image_url` 写入 DB、返回 Car 含 imageUrl
  - [ ] Given 新增车辆请求不含图片，when 调用 `POST /api/cars`，then `image_url` 为 NULL，正常创建
  - [ ] Given 编辑车辆请求含新图片，when 调用 `PUT /api/cars/{id}`，then 旧图被覆盖、image_url 更新
  - [ ] Given 编辑请求不含图片字段，when 调用，then 原图片不变
  - [ ] Given 格式非 jpg/png/webp 或 >5MB，when 上传，then 返回 400
  - [ ] Given 直接访问 `/uploads/images/{filename}`，when 图片存在，then 正常显示

- **Ready Checklist:**
  - [x] AC testable
  - [x] Points estimated
  - [x] Design references linked
  - [x] Known dependencies recorded
  - [x] Scope boundary defined
- **Done Definition:**
  - [ ] Functional acceptance passed
  - [ ] NFR checks passed
  - [ ] Test evidence attached

---

### Story: STORY-IMG-02 - 管理端表单支持图片上传

- **Parent Epic:** EPIC-IMG
- **Persona:** 系统管理员
- **Story Statement:** As a 系统管理员，I want 在新增和编辑车辆时能上传/更换/移除图片，So that 车辆信息可附带真实图片。
- **Priority:** High
- **Story Points:** 2
- **Dependencies:** STORY-IMG-01
- **Acceptance Criteria (Given-When-Then):**
  - [ ] Given 新增车辆页，when 选择 jpg/png/webp 图片，then 显示预览缩略图
  - [ ] Given 新增表单已选图，when 保存，then 以 FormData 提交，保存后跳转列表
  - [ ] Given 编辑已有图片的车辆，when 打开编辑页，then 显示当前图片预览
  - [ ] Given 编辑页选择新图替换，when 保存，then 图片更新
  - [ ] Given 编辑页点"移除图片"，when 保存，then `image_url` 置 NULL
  - [ ] Given 编辑页不动图片区域，when 保存，then 原图不变

- **Ready Checklist:**
  - [x] AC testable
  - [x] Points estimated
  - [x] Design references linked
  - [x] Known dependencies recorded
  - [x] Scope boundary defined
- **Done Definition:**
  - [ ] Functional acceptance passed
  - [ ] NFR checks passed
  - [ ] Test evidence attached

---

### Story: STORY-IMG-03 - 客户端展示真实图片

- **Parent Epic:** EPIC-IMG
- **Persona:** 客户
- **Story Statement:** As a 客户，I want 浏览在售车辆时看到真实车辆图片，So that 直观了解车辆外观。
- **Priority:** High
- **Story Points:** 1
- **Dependencies:** STORY-IMG-01
- **Acceptance Criteria (Given-When-Then):**
  - [ ] Given 车辆列表页，when 车辆有 imageUrl，then 卡片显示该图片（cover 裁剪）
  - [ ] Given 车辆列表页，when 车辆无 imageUrl，then 显示默认占位图
  - [ ] Given 车辆详情页，when 车辆有 imageUrl，then 左侧大图显示该图片
  - [ ] Given 车辆详情页，when 车辆无 imageUrl，then 显示默认占位图
  - [ ] Given 图片加载失败，then 降级显示默认占位图

- **Ready Checklist:**
  - [x] AC testable
  - [x] Points estimated
  - [x] Design references linked
  - [x] Known dependencies recorded
  - [x] Scope boundary defined
- **Done Definition:**
  - [ ] Functional acceptance passed
  - [ ] NFR checks passed
  - [ ] Test evidence attached

---

## Story Acceptance Criteria Summary

| Story | AC Count | Priority | Points |
|-------|:--------:|:--------:|:------:|
| STORY-IMG-01 | 7 | High | 3 |
| STORY-IMG-02 | 6 | High | 2 |
| STORY-IMG-03 | 5 | High | 1 |
| **Total** | **18** | | **6** |

## Dependencies and Risks

| ID | Description | Type | Mitigation |
|----|-------------|------|-----------|
| RSK-001 | API 从 JSON 改为 multipart 破坏管理端现有调用 | Breaking Change | 同步更新 CarForm.vue + api/index.js |
| RSK-002 | 用户上传恶意文件 | Security | 限制格式 + 大小 |
| RSK-003 | 客户端图片样式与新布局不兼容 | Regression | AC 中硬性要求 cover 裁剪 + 占位回退 |

- Ref: `workspace/car-sales-management/quality/risk-register.md`

## Prioritization Rationale
3 个 Story 按依赖链排列：后端先（IMG-01）→ 管理端（IMG-02）→ 客户端（IMG-03），后两个依赖第一个完成。同一 Sprint 实施，总 6 点。
