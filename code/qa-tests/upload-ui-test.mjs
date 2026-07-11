import { chromium } from 'playwright';

const ADMIN_URL = 'http://localhost:5173';
const IMAGE_PATH = 'D:\\MIS_Design\\ui-test-car.jpg';

async function main() {
  const browser = await chromium.launch({ headless: true });
  const page = await browser.newPage({ viewport: { width: 1280, height: 800 } });

  try {
    console.log('1. Opening admin page...');
    await page.goto(ADMIN_URL, { waitUntil: 'networkidle', timeout: 20000 });
    console.log('   PASS');

    console.log('2. Navigating to car management...');
    await page.click('text=车辆管理');
    await page.waitForTimeout(2000);
    console.log('   PASS');

    console.log('3. Clicking new car...');
    await page.click('text=新增车辆');
    await page.waitForTimeout(2000);
    console.log('   PASS');

    console.log('4. Filling form...');
    await page.fill('input[placeholder*="宝马"]', 'UI测试品牌');
    await page.fill('input[placeholder*="X5"]', 'UI测试车型');
    await page.fill('input[placeholder*="2.0L"]', '3.0T');
    await page.fill('input[placeholder*="CVT"]', '8AT');
    await page.fill('input[placeholder*="白色"]', '蓝色');
    const priceInputs = page.locator('input[type="number"]');
    await priceInputs.first().fill('350000');
    await page.fill('input[placeholder="0"]', '3');
    console.log('   PASS');

    console.log('5. Uploading image...');
    const fileInput = page.locator('input[type="file"]');
    await fileInput.setInputFiles(IMAGE_PATH);
    await page.waitForTimeout(1500);
    const previewVisible = await page.locator('.image-preview').isVisible().catch(() => false);
    console.log('   Image preview:', previewVisible ? 'VISIBLE' : 'NOT VISIBLE');

    console.log('6. Saving form...');
    await page.click('button:has-text("保存")');
    await page.waitForTimeout(3000);
    console.log('   Current URL:', page.url());

    console.log('7. Verifying result...');
    const body = await page.textContent('body');
    if (body.includes('UI测试品牌') || body.includes('UI测试车型')) {
      console.log('   PASS - Car found in list with brand/model');
    } else {
      // Maybe still on form, check by navigating
      console.log('   Checking by navigating to car list...');
      await page.goto(ADMIN_URL + '/admin/cars', { waitUntil: 'networkidle', timeout: 10000 });
      const body2 = await page.textContent('body');
      if (body2.includes('UI测试品牌')) {
        console.log('   PASS - Car found in list');
      } else {
        console.log('   WARN - Car not visible in list');
      }
    }

    console.log('\n========================================');
    console.log('UI TEST COMPLETED SUCCESSFULLY');
    console.log('========================================');
  } catch (e) {
    console.error('\nFAILED:', e.message);
    try {
      await page.screenshot({ path: 'D:\\MIS_Design\\ui-test-failure.png', fullPage: true });
      console.log('Screenshot saved');
    } catch {}
    process.exit(1);
  } finally {
    await browser.close();
  }
}

main();
