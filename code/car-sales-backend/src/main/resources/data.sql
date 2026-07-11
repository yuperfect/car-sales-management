-- ============================================
-- 汽车销售管理系统 - 初始测试数据
-- 仅在表为空时插入（支持幂等重启）
-- ============================================

USE car_sales_db;

-- ============================================
-- 1. 客户数据（依赖 customer.phone 的唯一索引去重）
-- ============================================
INSERT IGNORE INTO customer (real_name, phone, first_submit_time) VALUES
('张三', '13900000001', '2026-06-15 10:00:00'),
('李四', '13900000002', '2026-06-20 14:30:00'),
('王五', '13900000003', '2026-06-25 09:15:00');

-- ============================================
-- 2. 车辆数据（仅在表为空时插入）
-- ============================================
INSERT INTO car (brand, model, displacement, transmission, color, price, stock, status, listed_time, image_url)
SELECT * FROM (
  SELECT '丰田' AS brand, 'RAV4 荣放' AS model, '2.0L' AS displacement, 'CVT无级变速' AS transmission, '白色' AS color, 185800.00 AS price, 4 AS stock, 'on_sale' AS status, '2026-06-01 08:00:00' AS listed_time, '/images/1.jpg' AS image_url
  UNION ALL
  SELECT '本田', 'CR-V', '1.5T', 'CVT无级变速', '黑色', 195900.00, 3, 'on_sale', '2026-06-01 08:00:00', '/images/2.jpg'
  UNION ALL
  SELECT '大众', '途观L', '2.0T', '7档双离合', '银色', 215800.00, 4, 'on_sale', '2026-06-01 08:00:00', '/images/3.jpg'
  UNION ALL
  SELECT '宝马', '3系', '2.0T', '8档手自一体', '蓝色', 329900.00, 0, 'sold_out', '2026-06-01 08:00:00', '/images/4.jpg'
  UNION ALL
  SELECT '丰田', '凯美瑞', '2.0L', 'CVT无级变速', '白色', 179800.00, 5, 'on_sale', '2026-06-01 08:00:00', '/images/5.jpg'
  UNION ALL
  SELECT '特斯拉', 'Model Y', '纯电动', '单速变速箱', '红色', 249900.00, 3, 'on_sale', '2026-06-01 08:00:00', '/images/6.jpg'
  UNION ALL
  SELECT '比亚迪', '汉EV', '纯电动', '单速变速箱', '黑色', 209800.00, 4, 'on_sale', '2026-06-01 08:00:00', '/images/7.jpg'
  UNION ALL
  SELECT '保时捷', '718', '2.0T', '7档双离合', '黄色', 565000.00, 1, 'on_sale', '2026-06-01 08:00:00', '/images/8.jpg'
) tmp
WHERE (SELECT COUNT(*) FROM car) = 0;

-- ============================================
-- 3. 预约数据（仅在预约表为空时插入）
-- ============================================
INSERT INTO appointment (customer_id, car_id, appointment_time, status, create_time, handle_time, handler, remark)
SELECT * FROM (
  SELECT 1 AS customer_id, 1 AS car_id, '2026-07-21 10:00:00' AS appointment_time, 'confirmed' AS status, '2026-07-15 09:00:00' AS create_time, '2026-07-15 14:00:00' AS handle_time, '管理员' AS handler, '希望试驾白色RAV4' AS remark
  UNION ALL
  SELECT 1, 5, '2026-07-22 14:30:00', 'confirmed', '2026-07-16 10:00:00', '2026-07-16 16:00:00', '管理员', NULL
  UNION ALL
  SELECT 2, 2, '2026-07-23 09:00:00', 'pending',   '2026-07-17 08:00:00', NULL, NULL, NULL
  UNION ALL
  SELECT 3, 4, '2026-07-24 15:00:00', 'cancelled', '2026-07-18 11:00:00', '2026-07-18 15:30:00', NULL, '临时有事取消'
  UNION ALL
  SELECT 2, 7, '2026-07-25 11:00:00', 'pending',   '2026-07-19 09:30:00', NULL, NULL, NULL
) tmp
WHERE (SELECT COUNT(*) FROM appointment) = 0;

-- ============================================
-- 4. 订单数据（仅在订单表为空时插入）
-- ============================================
INSERT INTO purchase_order (customer_id, car_id, quantity, unit_price, total_amount, order_time, status, handle_time, handler)
SELECT * FROM (
  SELECT 1 AS customer_id, 1 AS car_id, 1 AS quantity, 185800.00 AS unit_price, 185800.00 AS total_amount, '2026-07-15 10:00:00' AS order_time, 'confirmed' AS status, '2026-07-16 09:00:00' AS handle_time, '管理员' AS handler
  UNION ALL
  SELECT 1, 5, 1, 179800.00, 179800.00, '2026-07-16 11:00:00', 'confirmed', '2026-07-17 10:00:00', '管理员'
  UNION ALL
  SELECT 2, 2, 1, 195900.00, 195900.00, '2026-07-17 14:00:00', 'cancelled', '2026-07-19 15:00:00', '管理员'
  UNION ALL
  SELECT 3, 4, 2, 329900.00, 659800.00, '2026-07-18 09:00:00', 'confirmed', '2026-07-18 11:00:00', '管理员'
  UNION ALL
  SELECT 2, 7, 1, 209800.00, 209800.00, '2026-07-19 16:00:00', 'pending', NULL, NULL
) tmp
WHERE (SELECT COUNT(*) FROM purchase_order) = 0;
