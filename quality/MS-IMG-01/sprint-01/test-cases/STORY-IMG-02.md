# Test Cases: STORY-IMG-02 — 管理端表单支持图片上传

## Story Metadata
- **Story:** STORY-IMG-02
- **Points:** 2
- **Priority:** High
- **Dependencies:** STORY-IMG-01（后端 API 必须完成）

---

## AC → Test Case Mapping

| AC ID | AC Description | Test Case(s) | Type |
|-------|---------------|:------------:|:----:|
| AC-1 | 新增车辆页，选择 jpg/png/webp 图片 → 显示预览缩略图 | TC-IMG02-001 | Manual / E2E |
| AC-2 | 新增表单已选图，保存 → FormData 提交，跳转列表 | TC-IMG02-002 | Manual / E2E |
| AC-3 | 编辑已有图片的车辆 → 显示当前图片预览 | TC-IMG02-003 | Manual / E2E |
| AC-4 | 编辑页选择新图替换，保存 → 图片更新 | TC-IMG02-004 | Manual / E2E |
| AC-5 | 编辑页点"移除图片"，保存 → image_url 置 NULL | TC-IMG02-005 | Manual / E2E |
| AC-6 | 编辑页不动图片区域，保存 → 原图不变 | TC-IMG02-006 | Manual / E2E |

---

## Test Cases Detail

### TC-IMG02-001: 新增车辆页 — 图片预览
| Field | Value |
|-------|-------|
| **ID** | TC-IMG02-001 |
| **Description** | 在新增车辆页面选择图片文件后，表单区域显示预览缩略图 |
| **Precondition** | 管理员已登录管理端；打开新增车辆页面（`/admin/cars/new`）；后端 STORY-IMG-01 已完成 |
| **Steps** | 1. 打开新增车辆表单页<br>2. 填写品牌、型号等基本信息（非必填可跳过）<br>3. 点击文件选择按钮，选择 test-image.jpg<br>4. 观察表单中图片区域 |
| **Expected Result** | - 文件选择后，表单中显示所选图片的预览缩略图<br>- 缩略图比例正常，无拉伸变形<br>- 可能显示文件名或尺寸信息 |
| **Type** | Manual |
| **Status** | ⏳ pending |

---

### TC-IMG02-002: 新增车辆 — 含图片提交
| Field | Value |
|-------|-------|
| **ID** | TC-IMG02-002 |
| **Description** | 新增车辆表单含图片，保存后验证 FormData 提交成功，跳转到列表页 |
| **Precondition** | 管理员已登录；STORY-IMG-01 后端完成；TC-IMG02-001 已选图片 |
| **Steps** | 1. 在新增页填写完整车辆信息（brand, model, year, price 等）<br>2. 选择一张有效图片<br>3. 点"保存"按钮<br>4. 观察页面行为 |
| **Expected Result** | - 保存成功，自动跳转到车辆列表页<br> - 列表页中新增的车辆记录显示图片缩略图（如果有该列）<br> - 可在列表页点击该车辆查看详情，确认 imageUrl 存在 |
| **Type** | Manual / E2E |
| **Status** | ⏳ pending |

---

### TC-IMG02-003: 编辑已有图片车辆 — 显示当前图片
| Field | Value |
|-------|-------|
| **ID** | TC-IMG02-003 |
| **Description** | 编辑一辆已有图片的车辆，打开编辑页面后显示当前图片预览 |
| **Precondition** | 存在一辆已有图片的车辆（通过 TC-IMG02-002 创建）；管理员已登录 |
| **Steps** | 1. 在车辆列表页找到该车辆<br>2. 点击"编辑"按钮<br>3. 观察图片区域 |
| **Expected Result** | - 编辑页面的图片区域显示当前车辆图片预览<br>- 图片与之前上传的一致 |
| **Type** | Manual / E2E |
| **Status** | ⏳ pending |

---

### TC-IMG02-004: 编辑车辆 — 替换图片
| Field | Value |
|-------|-------|
| **ID** | TC-IMG02-004 |
| **Description** | 编辑已有图片的车辆，选择新图片替换，保存后验证图片更新 |
| **Precondition** | TC-IMG02-003 已确认显示原图 |
| **Steps** | 1. 在编辑页点击"选择图片"或类似按钮<br>2. 选择一张新图片（不同内容）<br>3. 预览变为新图片<br>4. 点"保存"<br>5. 跳转到列表页后，重新打开编辑<br>6. 观察图片预览 |
| **Expected Result** | - 重新打开编辑后，图片预览为新图片<br>- 旧图片已被替换<br>- 可在数据库中确认 `image_url` 未变（同一 carId 覆盖）或按规范更新 |
| **Type** | Manual / E2E |
| **Status** | ⏳ pending |

---

### TC-IMG02-005: 编辑车辆 — 移除图片
| Field | Value |
|-------|-------|
| **ID** | TC-IMG02-005 |
| **Description** | 编辑已有图片的车辆，点击"移除图片"，保存后验证 image_url 置 NULL |
| **Precondition** | TC-IMG02-003 已确认显示原图 |
| **Steps** | 1. 在编辑页面找到"移除图片"或"删除图片"按钮<br>2. 点击移除<br>3. 预览区域变为无图状态（显示默认占位）<br>4. 点"保存"<br>5. 跳转到列表后重新打开编辑 |
| **Expected Result** | - 重新打开编辑后，图片区域显示默认占位（无图片）<br>- 可查询数据库确认该车辆 `image_url` 为 NULL |
| **Type** | Manual / E2E |
| **Status** | ⏳ pending |

---

### TC-IMG02-006: 编辑车辆 — 不动图片区域
| Field | Value |
|-------|-------|
| **ID** | TC-IMG02-006 |
| **Description** | 编辑车辆时仅修改文本字段，不操作图片区域，保存后原图不变 |
| **Precondition** | TC-IMG02-003 已确认显示原图 |
| **Steps** | 1. 在编辑页面不点击图片区域的任何按钮<br>2. 修改 price 字段（如 250000 → 260000）<br>3. 点"保存"<br>4. 成功跳转后重新打开编辑<br>5. 观察图片预览 |
| **Expected Result** | - 图片预览与编辑前一致<br>- 可查询数据库确认 `image_url` 未改变<br>- price 等其他字段已更新 |
| **Type** | Manual / E2E |
| **Status** | ⏳ pending |

---

## Additional Checks

### TC-IMG02-007: 文件选择过滤
| Field | Value |
|-------|-------|
| **ID** | TC-IMG02-007 |
| **Description** | 验证文件选择框的 accept 属性限制为图片格式 (jpg/png/webp) |
| **Precondition** | 管理员已登录；打开新增或编辑表单 |
| **Steps** | 1. 点击文件选择按钮<br>2. 观察文件选择对话框的文件类型过滤 |
| **Expected Result** | 文件选择器只允许选择 .jpg/.jpeg/.png/.webp 文件，其他类型文件被过滤或灰显 |
| **Type** | Manual |
| **Status** | ⏳ pending |

---

## Reverse Index: TC → AC Mapping

| TC ID | AC ID(s) | Description |
|-------|:--------:|-------------|
| TC-IMG02-001 | AC-1 | Select image → preview |
| TC-IMG02-002 | AC-2 | Save with image → FormData → redirect |
| TC-IMG02-003 | AC-3 | Edit shows existing image preview |
| TC-IMG02-004 | AC-4 | Replace image on edit |
| TC-IMG02-005 | AC-5 | Remove image → NULL |
| TC-IMG02-006 | AC-6 | Keep image unchanged |
| TC-IMG02-007 | (AC-1 补充) | File picker accept filter |

---

## Test Data Requirements

| Data Item | Value | Purpose |
|-----------|-------|---------|
| test-image.jpg | 500x375, valid JPEG, <1MB | Positive upload/preview |
| test-image.png | 500x375, valid PNG, <1MB | PNG format test |
| new-test-image.jpg | 300x200, valid JPEG, <500KB | Replace test (different content) |
| admin-credentials | admin / admin123 (or as per seed) | Login for admin pages |
