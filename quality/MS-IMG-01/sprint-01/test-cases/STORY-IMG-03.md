# Test Cases: STORY-IMG-03 — 客户端展示真实图片

## Story Metadata
- **Story:** STORY-IMG-03
- **Points:** 1
- **Priority:** High
- **Dependencies:** STORY-IMG-01（后端必须提供 imageUrl 字段）

---

## AC → Test Case Mapping

| AC ID | AC Description | Test Case(s) | Type |
|-------|---------------|:------------:|:----:|
| AC-1 | 车辆列表页，车辆有 imageUrl → 卡片显示该图片（cover 裁剪） | TC-IMG03-001 | Manual / E2E |
| AC-2 | 车辆列表页，车辆无 imageUrl → 显示默认占位图 | TC-IMG03-002 | Manual / E2E |
| AC-3 | 车辆详情页，车辆有 imageUrl → 左侧大图显示该图片 | TC-IMG03-003 | Manual / E2E |
| AC-4 | 车辆详情页，车辆无 imageUrl → 显示默认占位图 | TC-IMG03-004 | Manual / E2E |
| AC-5 | 图片加载失败 → 降级显示默认占位图 | TC-IMG03-005 | Manual |

---

## Test Cases Detail

### TC-IMG03-001: 列表页 — 有图片车辆显示真实图片
| Field | Value |
|-------|-------|
| **ID** | TC-IMG03-001 |
| **Description** | 在在售车辆列表页，有 imageUrl 的车辆卡片显示真实图片，以 cover 模式裁剪 |
| **Precondition** | 至少有一辆车辆含有 `imageUrl`（通过 STORY-IMG-02 创建）；客户端页面运行 |
| **Steps** | 1. 打开在售车辆列表页（`/cars`）<br>2. 找到有图片的车辆卡片<br>3. 观察图片区域 |
| **Expected Result** | - 车辆卡片图片区域显示真实图片，而非 🚗 emoji<br>- 图片以 `object-fit: cover` 裁剪，不变形<br>- 图片尺寸符合设计（约 160px 高或按卡片比例）<br>- 图片加载正常，无断裂图标 |
| **Type** | Manual |
| **Status** | ⏳ pending |

---

### TC-IMG03-002: 列表页 — 无图片车辆显示默认占位
| Field | Value |
|-------|-------|
| **ID** | TC-IMG03-002 |
| **Description** | 在在售车辆列表页，无 imageUrl 的车辆卡片显示默认占位图 |
| **Precondition** | 至少有一辆车辆 `image_url` 为 NULL；客户端页面运行 |
| **Steps** | 1. 打开在售车辆列表页<br>2. 找到无图片的车辆卡片（image_url 为 NULL）<br>3. 观察图片区域 |
| **Expected Result** | - 卡片图片区域显示默认占位图（如渐变背景 + 🚗 图标）<br>- 占位图样式整洁，无布局错位<br>- 与有图片的卡片高度一致 |
| **Type** | Manual |
| **Status** | ⏳ pending |

---

### TC-IMG03-003: 详情页 — 有图片车辆显示大图
| Field | Value |
|-------|-------|
| **ID** | TC-IMG03-003 |
| **Description** | 在车辆详情页，有 imageUrl 的车辆左侧大图显示真实图片 |
| **Precondition** | 有一辆有图片的车辆；客户端页面运行 |
| **Steps** | 1. 打开在售车辆列表页<br>2. 点击有图片的车辆，进入详情页<br>3. 观察左侧大图区域 |
| **Expected Result** | - 左侧大图区域显示真实图片（非 emoji）<br>- 图片尺寸约 360x240 或按设计<br>- `object-fit: cover` 裁剪，不变形<br>- 图片清晰，比例合适 |
| **Type** | Manual |
| **Status** | ⏳ pending |

---

### TC-IMG03-004: 详情页 — 无图片车辆显示默认占位
| Field | Value |
|-------|-------|
| **ID** | TC-IMG03-004 |
| **Description** | 在车辆详情页，无 imageUrl 的车辆左侧大图显示默认占位图 |
| **Precondition** | 有一辆无图片的车辆；客户端页面运行 |
| **Steps** | 1. 打开在售车辆列表页<br>2. 点击无图片的车辆，进入详情页<br>3. 观察左侧大图区域 |
| **Expected Result** | - 大图区域显示默认占位图（如渐变背景 + 🚗 图标）<br>- 占位图样式整洁，与有图片详情页布局一致<br>- 无空白区域或布局偏移 |
| **Type** | Manual |
| **Status** | ⏳ pending |

---

### TC-IMG03-005: 图片加载失败 — 降级显示占位图
| Field | Value |
|-------|-------|
| **ID** | TC-IMG03-005 |
| **Description** | 车辆的 imageUrl 指向不可访问的图片（如删除的文件或错误的 URL），页面应降级显示默认占位图 |
| **Precondition** | 管理端可手动将某车辆 `image_url` 改为无效 URL（如 `/uploads/images/nonexistent.jpg`）；客户端页面运行 |
| **Steps** | **方法 A — 修改 DB：**<br>1. 将某车辆的 `image_url` 改为 `/uploads/images/nonexistent.jpg`<br>2. 打开列表页和详情页<br>3. 观察该车辆图片区域<br><br>**方法 B — 网络模拟（DevTools）：**<br>1. 打开列表页<br>2. 打开 Chrome DevTools → Network → 拦截图片请求（Offline 或 Block URL）<br>3. 观察有图片车辆的卡片 |
| **Expected Result** | - 图片加载失败时，图片区域降级显示默认占位图<br>- 不显示断裂的图片图标（alt 文本图标）<br>- 占位图与 TC-IMG03-002/004 中的样式一致<br>- 布局不受影响（无布局偏移）<br>- 列表页和详情页都正常工作 |
| **Type** | Manual |
| **Status** | ⏳ pending |

---

## Reverse Index: TC → AC Mapping

| TC ID | AC ID(s) | Description |
|-------|:--------:|-------------|
| TC-IMG03-001 | AC-1 | List — imageUrl exists → real image (cover) |
| TC-IMG03-002 | AC-2 | List — no imageUrl → placeholder |
| TC-IMG03-003 | AC-3 | Detail — imageUrl exists → real image |
| TC-IMG03-004 | AC-4 | Detail — no imageUrl → placeholder |
| TC-IMG03-005 | AC-5 | Image load failure → fallback placeholder |

---

## Test Data Requirements

| Data Item | Value | Purpose |
|-----------|-------|---------|
| Vehicle with image | car with imageUrl set | Positive display tests |
| Vehicle without image | car with image_url = NULL | Placeholder tests |
| Invalid image URL | e.g., `/uploads/images/nonexistent.jpg` | Fallback test |
| Client URL | `http://localhost:5174` or client port | Base URL for client pages |
