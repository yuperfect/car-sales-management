# Functional Test Suite

## Image Upload Feature (MS-IMG-01)

### STORY-IMG-01: 后端图片上传

| TC ID | Description | Source |
|-------|-------------|--------|
| IMG01-TC1 | 新增车辆时上传图片 → 确认文件和 URL 正确 | `test-cases/STORY-IMG-01.md` |
| IMG01-TC2 | 新增车辆时不传图片 → image_url 为 NULL | `test-cases/STORY-IMG-01.md` |
| IMG01-TC3 | 编辑车辆时替换图片 → 旧文件被覆盖、URL 更新 | `test-cases/STORY-IMG-01.md` |
| IMG01-TC4 | 编辑车辆时不传图片 → 原图不变 | `test-cases/STORY-IMG-01.md` |
| IMG01-TC5 | 上传非 jpg/png/webp 格式 → 返回 400 | `test-cases/STORY-IMG-01.md` |
| IMG01-TC6 | 上传超 5MB 文件 → 返回 400 | `test-cases/STORY-IMG-01.md` |
| IMG01-TC7 | 直接访问上传的图片 URL → 正常显示 | `test-cases/STORY-IMG-01.md` |
| IMG01-TC8 | 删除车辆时关联的图片文件未清理（已知限制） | `test-cases/STORY-IMG-01.md` |

### STORY-IMG-02: 管理端表单图片上传

| TC ID | Description | Source |
|-------|-------------|--------|
| IMG02-TC1 | 新增页选择图片后显示预览 | `test-cases/STORY-IMG-02.md` |
| IMG02-TC2 | 新增页带图保存 → 跳转列表 | `test-cases/STORY-IMG-02.md` |
| IMG02-TC3 | 编辑页回显当前图片 | `test-cases/STORY-IMG-02.md` |
| IMG02-TC4 | 编辑页替换图片后保存 → 更新 | `test-cases/STORY-IMG-02.md` |
| IMG02-TC5 | 编辑页移除图片后保存 → image_url 置 NULL | `test-cases/STORY-IMG-02.md` |
| IMG02-TC6 | 编辑页不动图片 → 原图保留 | `test-cases/STORY-IMG-02.md` |
| IMG02-TC7 | 选择非法格式/超大文件 → 显示错误提示 | `test-cases/STORY-IMG-02.md` |

### STORY-IMG-03: 客户端图片展示

| TC ID | Description | Source |
|-------|-------------|--------|
| IMG03-TC1 | 列表页有图片 → 显示 cover 裁剪图片 | `test-cases/STORY-IMG-03.md` |
| IMG03-TC2 | 列表页无图片 → 显示默认占位图 | `test-cases/STORY-IMG-03.md` |
| IMG03-TC3 | 详情页有图片 → 显示大图 | `test-cases/STORY-IMG-03.md` |
| IMG03-TC4 | 详情页无图片 → 显示默认占位图 | `test-cases/STORY-IMG-03.md` |
| IMG03-TC5 | 图片加载失败 → 降级显示默认占位图 | `test-cases/STORY-IMG-03.md` |
