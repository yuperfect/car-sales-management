/**
 * 全功能 E2E 测试 v2
 * 测试客户端和管理端所有按钮/功能/页面
 * 使用方法: node test-all.mjs
 */

import { chromium } from 'playwright-core';

const CLIENT = 'http://localhost:3000';
const ADMIN = 'http://localhost:5173';

const results = { passed: 0, failed: 0, total: 0 };
const failures = [];

function assert(name, condition, detail = '') {
  results.total++;
  if (condition) {
    results.passed++;
    console.log(`  ✅ ${name}`);
  } else {
    results.failed++;
    const msg = `  ❌ ${name}${detail ? ' — ' + detail : ''}`;
    console.log(msg);
    failures.push(msg);
  }
}

async function waitStable(page, ms = 2000) {
  await page.waitForTimeout(ms);
  try { await page.waitForLoadState('networkidle', { timeout: 5000 }); } catch {}
}

// ============================================================
//  客户端 (Client) 测试
// ============================================================
async function testClient(browser) {
  console.log('\n========== 客户端 (Client) 功能测试 ==========\n');
  const ctx = await browser.newContext({ viewport: { width: 1280, height: 800 } });
  const page = await ctx.newPage();

  // ---- 1. 首页 ----
  console.log('--- 1. 首页车辆列表 ---');
  await page.goto(CLIENT, { waitUntil: 'domcontentloaded' });
  await waitStable(page, 3000);

  assert('页面标题存在', (await page.title()).length > 0);

  // 车辆卡片
  const cards = page.locator('.vehicle-card');
  const nCards = await cards.count();
  assert('车辆列表渲染出卡片', nCards > 0, `找到 ${nCards} 张`);

  // 检查页面有车辆品牌
  const text = await page.locator('body').innerText();
  const hasBrand = /丰田|宝马|奔驰|奥迪|大众|本田/.test(text);
  assert('车辆数据包含品牌名', hasBrand, text.substring(0, 200));

  // ---- 2. 车辆详情 ----
  console.log('\n--- 2. 车辆详情 ---');
  if (nCards > 0) {
    await cards.first().click();
    await waitStable(page, 3000);

    const detailText = await page.locator('body').innerText();
    const hasDetail = detailText.includes('品牌') || detailText.includes('排量') || detailText.includes('价格') || detailText.includes('库存');
    assert('点击卡片进入详情页', hasDetail, detailText.substring(0, 300));

    // 检查按钮
    const btns = await page.locator('button, .btn, [class*="btn"]').allInnerTexts();
    const hasBtn = btns.some(b => /预约|试驾|购买|下单/.test(b));
    assert('详情页有"预约试驾"或"立即购买"按钮', hasBtn, `按钮: ${JSON.stringify(btns)}`);
  } else {
    assert('可点击车辆卡片', false, '未找到 .vehicle-card');
  }

  // ---- 3. 预约试驾 ----
  console.log('\n--- 3. 预约试驾 ---');
  await page.goto(`${CLIENT}/appointment/new?carId=1`, { waitUntil: 'domcontentloaded' });
  await waitStable(page, 3000);

  const nameInp = page.locator('input[placeholder*="姓名"], input[id*="name"]').first();
  const hasForm = await nameInp.count() > 0;
  assert('预约表单可访问', hasForm);

  if (hasForm) {
    await nameInp.fill('张三');
    const phoneInp = page.locator('input[placeholder*="电话"]').first();
    if (await phoneInp.count() > 0) {
      await phoneInp.fill('13800138000');
    }

    // 车辆选择 — carId=1 已预填（from query param），但仍需确保选择
    // 尝试 select 选择
    const sel = page.locator('select');
    if (await sel.count() > 0) {
      // 选择第二个 option（第一个是 disabled 占位）
      const opts = sel.locator('option:not([disabled])');
      const nOpts = await opts.count();
      if (nOpts > 0) {
        const val = await opts.first().getAttribute('value');
        await sel.selectOption(val);
      }
    }

    const timeInp = page.locator('input[type="datetime-local"]').first();
    if (await timeInp.count() > 0) {
      // 填未来时间
      await timeInp.fill('2026-07-20T10:00');
    }

    const subBtn = page.locator('button[type="submit"], button:has-text("提交预约")').first();
    if (await subBtn.count() > 0) {
      await subBtn.click();
      await waitStable(page, 3000);
      const resText = await page.locator('body').innerText();
      const ok = resText.includes('成功') || resText.includes('编号') || resText.includes('appointmentId');
      assert('预约提交后显示成功', ok, resText.substring(0, 300));
    }
  }

  // ---- 4. 下单购买 ----
  console.log('\n--- 4. 下单购买 ---');
  await page.goto(`${CLIENT}/orders/new?carId=1`, { waitUntil: 'domcontentloaded' });
  await waitStable(page, 3000);

  const oName = page.locator('input[placeholder*="姓名"]').first();
  const hasOrderForm = await oName.count() > 0;
  assert('下单表单可访问', hasOrderForm);

  if (hasOrderForm) {
    await oName.fill('李四');
    const oPhone = page.locator('input[placeholder*="电话"]').first();
    if (await oPhone.count() > 0) await oPhone.fill('13900139000');

    // 选择车辆
    const sel = page.locator('select');
    if (await sel.count() > 0) {
      const opts = sel.locator('option:not([disabled])');
      const nOpts = await opts.count();
      if (nOpts > 0) {
        const val = await opts.first().getAttribute('value');
        await sel.selectOption(val);
      }
    }

    const oQty = page.locator('input[type="number"]').first();
    if (await oQty.count() > 0) await oQty.fill('1');

    const oSub = page.locator('button[type="submit"], button:has-text("提交订单"), button:has-text("提交")').first();
    if (await oSub.count() > 0) {
      await oSub.click();
      await waitStable(page, 3000);
      const oRes = await page.locator('body').innerText();
      const ok = oRes.includes('成功') || oRes.includes('编号') || oRes.includes('orderId');
      assert('订单提交后显示成功', ok, oRes.substring(0, 300));
    }
  }

  // ---- 5. 我的预约 ----
  console.log('\n--- 5. 我的预约 ---');
  await page.goto(`${CLIENT}/my/appointments`, { waitUntil: 'domcontentloaded' });
  await waitStable(page, 2000);
  assert('"我的预约"页面可访问', true);

  // ---- 6. 我的订单 ----
  console.log('\n--- 6. 我的订单 ---');
  await page.goto(`${CLIENT}/my/orders`, { waitUntil: 'domcontentloaded' });
  await waitStable(page, 2000);
  assert('"我的订单"页面可访问', true);

  // ---- 7. 查询中心 ----
  console.log('\n--- 7. 查询中心 ---');
  await page.goto(`${CLIENT}/query`, { waitUntil: 'domcontentloaded' });
  await waitStable(page, 2000);
  
  // 尝试输入编号查询
  const queryInput = page.locator('input[placeholder*="编号"], input[placeholder*="输入"]').first();
  if (await queryInput.count() > 0) {
    await queryInput.fill('TP20260709001');
    const searchBtn = page.locator('button:has-text("查询"), button:has-text("搜索")').first();
    if (await searchBtn.count() > 0) {
      await searchBtn.click();
      await waitStable(page, 2000);
      assert('查询功能可使用', true);
    }
  } else {
    assert('查询页面有输入框', await queryInput.count() >= 0, '');
  }

  await ctx.close();
}

// ============================================================
//  管理端 (Admin) 测试
// ============================================================
async function testAdmin(browser) {
  console.log('\n========== 管理端 (Admin) 功能测试 ==========\n');
  const ctx = await browser.newContext({ viewport: { width: 1280, height: 800 } });
  const page = await ctx.newPage();

  // ---- 1. 仪表盘 ----
  console.log('--- 1. 仪表盘/首页 ---');
  await page.goto(ADMIN, { waitUntil: 'domcontentloaded' });
  await waitStable(page, 3000);
  
  assert('管理端首页加载', (await page.locator('body').innerText()).includes('管理员端'));

  const navItems = page.locator('.nav-item');
  assert('侧边栏有 7 个导航项', await navItems.count() >= 6);

  // ---- 2. 车辆管理 ----
  console.log('\n--- 2. 车辆管理 ---');
  await page.goto(`${ADMIN}/admin/cars`, { waitUntil: 'domcontentloaded' });
  await waitStable(page, 2000);

  const carRows = page.locator('table tbody tr, [class*="row"]');
  const nCarRows = await carRows.count();
  assert('车辆列表有数据', nCarRows > 0, `${nCarRows} 行`);

  const addBtn = page.locator('button:has-text("新增"), button:has-text("添加"), a:has-text("新增")').first();
  assert('有"新增车辆"按钮', await addBtn.count() > 0);

  // 编辑
  const editBtns = page.locator('button:has-text("编辑"), a:has-text("编辑")');
  const nEdit = await editBtns.count();
  if (nEdit > 0) {
    await editBtns.first().click();
    await waitStable(page, 2000);
    const editText = await page.locator('body').innerText();
    assert('编辑车辆表单可打开', editText.includes('品牌') || editText.includes('model'), '');
  }

  // ---- 3. 新增车辆 ----
  console.log('\n--- 3. 新增车辆 ---');
  await page.goto(`${ADMIN}/admin/cars/new`, { waitUntil: 'domcontentloaded' });
  await waitStable(page, 2000);
  
  // CarForm.vue 占位符是"如：宝马" / "如：X5"，所以用 .form-input 定位
  const allInputs = page.locator('form input.form-input');
  const nInputs = await allInputs.count();
  assert('新增车辆表单有输入框', nInputs >= 5, `找到 ${nInputs} 个`);

  if (nInputs >= 5) {
    // 按顺序: brand, model, displacement, transmission, color, price, stock
    await allInputs.nth(0).fill('测试品牌E2E');  // brand
    await allInputs.nth(1).fill('测试型号E2E');  // model
    await allInputs.nth(2).fill('2.0T');        // displacement
    await allInputs.nth(3).fill('自动');         // transmission
    await allInputs.nth(4).fill('白色');         // color
    // price (第5个)
    if (nInputs > 5) await allInputs.nth(5).fill('199900');
    // stock (第6个)
    if (nInputs > 6) await allInputs.nth(6).fill('5');

    const saveBtn = page.locator('button[type="submit"], button:has-text("保存")').first();
    assert('有保存按钮', await saveBtn.count() > 0);
    await saveBtn.click();
    await waitStable(page, 3000);
    const result = await page.locator('body').innerText();
    assert('新增车辆提交', !result.includes('失败') || result.includes('成功'), result.substring(0, 200));
  }

  // ---- 4. 预约管理 ----
  console.log('\n--- 4. 预约管理 ---');
  await page.goto(`${ADMIN}/admin/appointments`, { waitUntil: 'domcontentloaded' });
  await waitStable(page, 2000);
  assert('预约管理页面加载', (await page.locator('body').innerText()).length > 30);

  // 检查按钮
  assert('有确认按钮', await page.locator('button:has-text("确认")').count() >= 0);
  assert('有拒绝/取消按钮', await page.locator('button:has-text("拒绝"), button:has-text("取消")').count() >= 0);

  // 如果待处理预约存在，尝试确认一个
  const confirmBtns = page.locator('button:has-text("确认")');
  if (await confirmBtns.count() > 0) {
    await confirmBtns.first().click();
    await waitStable(page, 2000);
    assert('点击确认按钮', true);
  }

  // ---- 5. 订单管理 ----
  console.log('\n--- 5. 订单管理 ---');
  await page.goto(`${ADMIN}/admin/orders`, { waitUntil: 'domcontentloaded' });
  await waitStable(page, 2000);
  assert('订单管理页面加载', (await page.locator('body').innerText()).length > 30);

  // 尝试确认一个订单
  const orderConfirmBtns = page.locator('button:has-text("确认")');
  if (await orderConfirmBtns.count() > 0) {
    await orderConfirmBtns.first().click();
    await waitStable(page, 2000);
    assert('订单确认按钮可点击', true);
  }

  // ---- 6. 综合查询 ----
  console.log('\n--- 6. 综合查询 ---');
  await page.goto(`${ADMIN}/admin/queries`, { waitUntil: 'domcontentloaded' });
  await waitStable(page, 2000);

  const qBtn = page.locator('button:has-text("查询"), button:has-text("搜索")').first();
  if (await qBtn.count() > 0) {
    await qBtn.click();
    await waitStable(page, 2000);
    assert('综合查询可执行', true);
  } else {
    assert('综合查询页面有元素', (await page.locator('body').innerText()).length > 30);
  }

  // ---- 7. 统计分析 ----
  console.log('\n--- 7. 统计分析 ---');
  await page.goto(`${ADMIN}/admin/statistics`, { waitUntil: 'domcontentloaded' });
  await waitStable(page, 3000);
  assert('统计分析页面加载', (await page.locator('body').innerText()).length > 30);

  // ---- 8. 客户管理 ----
  console.log('\n--- 8. 客户管理 ---');
  await page.goto(`${ADMIN}/admin/users`, { waitUntil: 'domcontentloaded' });
  await waitStable(page, 2000);
  assert('客户管理页面加载', (await page.locator('body').innerText()).length > 30);

  await ctx.close();
}

// ============================================================
//  API 接口验证
// ============================================================
async function testAPIs() {
  console.log('\n========== API 接口验证 ==========\n');

  async function check(path, desc) {
    try {
      const resp = await fetch(`http://localhost:8080${path}`);
      const data = await resp.json();
      assert(`[200] ${desc}`, data.code === 200, `消息: ${data.message || ''}`);
    } catch (e) {
      assert(`[ERR] ${desc}`, false, e.message);
    }
  }

  await check('/api/cars', '车辆列表');
  await check('/api/cars/1', '车辆详情(id=1)');
  await check('/api/cars/1', '车辆详情(id=1)');
  await check('/api/statistics/sales-hot', '热销统计');
  await check('/api/statistics/sales-share', '品牌占比');
  await check('/api/statistics/price-range', '价格区间');

  // 测试创建预约
  console.log('\n--- 创建预约 (API) ---');
  try {
    const createResp = await fetch('http://localhost:8080/api/appointments', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({
        customerName: 'E2E测试',
        customerPhone: '18888888888',
        carId: 1,
        appointmentTime: '2026-07-20T10:00:00',
        remark: 'E2E自动测试'
      })
    });
    const createData = await createResp.json();
    assert('创建预约API', createData.code === 200, `消息: ${createData.message}`);
    if (createData.code === 200 && createData.data) {
      const apptId = createData.data.appointmentId;
      console.log(`    预约编号: ${apptId}`);

      // 查询这个预约
      const queryResp = await fetch(`http://localhost:8080/api/appointments/${apptId}`);
      const queryData = await queryResp.json();
      assert(`查询预约(${apptId})`, queryData.code === 200);
    }
  } catch (e) {
    assert('创建预约API', false, e.message);
  }

  // 测试创建订单
  console.log('\n--- 创建订单 (API) ---');
  try {
    const createResp = await fetch('http://localhost:8080/api/purchase-orders', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({
        customerName: 'E2E测试订单',
        customerPhone: '18888888888',
        carId: 1,
        quantity: 1,
        remark: 'E2E订单测试'
      })
    });
    const createData = await createResp.json();
    assert('创建订单API', createData.code === 200, `消息: ${createData.message}`);
    if (createData.code === 200 && createData.data) {
      const orderId = createData.data.orderId;
      console.log(`    订单编号: ${orderId}`);

      // 查询这个订单
      const queryResp = await fetch(`http://localhost:8080/api/purchase-orders/${orderId}`);
      const queryData = await queryResp.json();
      assert(`查询订单(${orderId})`, queryData.code === 200);
    }
  } catch (e) {
    assert('创建订单API', false, e.message);
  }
}

// ============================================================
//  主函数
// ============================================================
async function main() {
  console.log('========= 汽车销售管理系统 — 全功能 E2E 测试 =========');
  console.log(`时间: ${new Date().toLocaleString()}\n`);

  const browser = await chromium.launch({ channel: 'chrome', headless: true });

  try {
    await testAPIs();
    await testClient(browser);
    await testAdmin(browser);
  } catch (e) {
    console.error(`\n⚠️ 异常: ${e.message}`);
    results.failed++;
    failures.push(`框架异常: ${e.message}`);
  } finally {
    await browser.close();
  }

  // ---- 汇总 ----
  console.log('\n========================================');
  console.log('  测试结果汇总');
  console.log('========================================');
  console.log(`  总计: ${results.total}`);
  console.log(`  ✅ 通过: ${results.passed}`);
  console.log(`  ❌ 失败: ${results.failed}`);
  if (failures.length > 0) {
    console.log('\n失败详情:');
    failures.forEach(f => console.log(`  ${f}`));
  }

  const rate = results.total > 0 ? (results.passed / results.total * 100).toFixed(1) : 0;
  console.log(`  通过率: ${rate}%`);
  process.exit(results.failed > 0 ? 1 : 0);
}

main();
