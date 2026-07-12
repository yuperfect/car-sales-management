-- ============================================
-- 汽车销售管理系统 - 初始测试数据（扩充版）
-- 仅在表为空时插入（支持幂等重启）
-- ============================================

USE car_sales_db;

-- ============================================
-- 1. 客户数据（依赖 customer.phone 的唯一索引去重）
-- ============================================
INSERT IGNORE INTO customer (real_name, phone, first_submit_time) VALUES
-- 原有 3 个客户
('张三', '13900000001', '2026-06-15 10:00:00'),
('李四', '13900000002', '2026-06-20 14:30:00'),
('王五', '13900000003', '2026-06-25 09:15:00'),
-- 新增 7 个客户
('陈六', '13900000004', '2026-07-01 10:00:00'),
('刘七', '13900000005', '2026-07-02 11:30:00'),
('赵八', '13900000006', '2026-07-03 09:00:00'),
('孙九', '13900000007', '2026-07-05 14:00:00'),
('周十', '13900000008', '2026-07-06 16:00:00'),
('林小姐', '13900000009', '2026-07-08 10:30:00'),
('黄先生', '13900000010', '2026-07-10 15:00:00');

-- ============================================
-- 2. 车辆数据（仅在表为空时插入）
-- ============================================
INSERT INTO car (brand, model, displacement, transmission, color, price, stock, status, listed_time, image_url)
SELECT * FROM (
  -- 原有 8 辆车（ID 1~8）
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
  SELECT '特斯拉', 'Model Y', '纯电动', '单速变速箱', '蓝色', 249900.00, 3, 'on_sale', '2026-06-01 08:00:00', '/images/6.jpg'
  UNION ALL
  SELECT '比亚迪', '汉EV', '纯电动', '单速变速箱', '黑色', 209800.00, 4, 'on_sale', '2026-06-01 08:00:00', '/images/7.jpg'
  UNION ALL
  SELECT '保时捷', '718', '2.0T', '7档双离合', '黄色', 565000.00, 1, 'on_sale', '2026-06-01 08:00:00', '/images/8.jpg'
  -- ============================================
  -- 新增 12 辆车（ID 9~20）
  -- ============================================
  UNION ALL
  SELECT '奔驰', 'GLC 300', '2.0T', '9档手自一体', '黑色', 428800.00, 3, 'on_sale', '2026-06-15 08:00:00', '/images/9.jpg'
  UNION ALL
  SELECT '奥迪', 'A4L', '2.0T', '7档双离合', '白色', 321800.00, 4, 'on_sale', '2026-06-15 08:00:00', '/images/10.jpg'
  UNION ALL
  SELECT '日产', '轩逸', '1.6L', 'CVT无级变速', '白色', 119900.00, 6, 'on_sale', '2026-06-15 08:00:00', '/images/11.jpg'
  UNION ALL
  SELECT '哈弗', 'H6', '1.5T', '7档双离合', '银色', 119800.00, 5, 'on_sale', '2026-06-15 08:00:00', '/images/12.jpg'
  UNION ALL
  SELECT '蔚来', 'ET5', '纯电动', '单速变速箱', '黑色', 298000.00, 2, 'on_sale', '2026-06-20 08:00:00', '/images/13.jpg'
  UNION ALL
  SELECT '小鹏', 'P7', '纯电动', '单速变速箱', '白色', 239900.00, 3, 'on_sale', '2026-06-20 08:00:00', '/images/14.jpg'
  UNION ALL
  SELECT '理想', 'L7', '增程式', '单速变速箱', '白色', 319800.00, 2, 'on_sale', '2026-06-20 08:00:00', '/images/15.jpg'
  UNION ALL
  SELECT '福特', '蒙迪欧', '2.0T', '8档手自一体', '黄色', 199800.00, 4, 'on_sale', '2026-06-20 08:00:00', '/images/16.jpg'
  UNION ALL
  SELECT '奔驰', 'E级', '2.0T', '9档手自一体', '黑色', 498800.00, 2, 'on_sale', '2026-06-25 08:00:00', '/images/17.jpg'
  UNION ALL
  SELECT '奥迪', 'Q5', '2.0T', '7档双离合', '白色', 398800.00, 3, 'on_sale', '2026-06-25 08:00:00', '/images/18.jpg'
  UNION ALL
  SELECT '日产', '天籁', '2.0L', 'CVT无级变速', '银色', 179800.00, 5, 'on_sale', '2026-06-25 08:00:00', '/images/19.jpg'
  UNION ALL
  SELECT '理想', 'L9', '增程式', '单速变速箱', '黑色', 429800.00, 2, 'on_sale', '2026-06-25 08:00:00', '/images/20.jpg'
) tmp
WHERE (SELECT COUNT(*) FROM car) = 0;

-- ============================================
-- 3. 预约数据（仅在预约表为空时插入）
-- ============================================
INSERT INTO appointment (customer_id, car_id, appointment_time, status, create_time, handle_time, handler, remark)
SELECT * FROM (
  -- 原有 5 条
  SELECT 1 AS customer_id, 1 AS car_id, '2026-07-21 10:00:00' AS appointment_time, 'confirmed' AS status, '2026-07-15 09:00:00' AS create_time, '2026-07-15 14:00:00' AS handle_time, '管理员' AS handler, '希望试驾白色RAV4' AS remark
  UNION ALL
  SELECT 1, 5, '2026-07-22 14:30:00', 'confirmed', '2026-07-16 10:00:00', '2026-07-16 16:00:00', '管理员', NULL
  UNION ALL
  SELECT 2, 2, '2026-07-23 09:00:00', 'pending',   '2026-07-17 08:00:00', NULL, NULL, NULL
  UNION ALL
  SELECT 3, 4, '2026-07-24 15:00:00', 'cancelled', '2026-07-18 11:00:00', '2026-07-18 15:30:00', NULL, '临时有事取消'
  UNION ALL
  SELECT 2, 7, '2026-07-25 11:00:00', 'pending',   '2026-07-19 09:30:00', NULL, NULL, NULL
  -- 新增 7 条
  UNION ALL
  SELECT 4, 9, '2026-08-01 10:00:00', 'pending',   '2026-07-25 09:00:00', NULL, NULL, '试驾奔驰GLC'
  UNION ALL
  SELECT 5, 10, '2026-08-02 14:00:00', 'pending',   '2026-07-26 10:00:00', NULL, NULL, NULL
  UNION ALL
  SELECT 6, 12, '2026-08-03 09:00:00', 'confirmed', '2026-07-27 08:00:00', '2026-07-28 10:00:00', '管理员', '哈弗H6银色'
  UNION ALL
  SELECT 7, 14, '2026-08-04 15:00:00', 'pending',   '2026-07-28 11:00:00', NULL, NULL, '小鹏P7银色'
  UNION ALL
  SELECT 8, 17, '2026-08-05 11:00:00', 'confirmed', '2026-07-29 09:30:00', '2026-07-30 14:00:00', '管理员', '奔驰E级'
  UNION ALL
  SELECT 9, 13, '2026-08-06 10:30:00', 'cancelled', '2026-07-30 10:00:00', '2026-07-31 09:00:00', NULL, '暂时不考虑了'
  UNION ALL
  SELECT 10, 20, '2026-08-07 14:00:00', 'pending',  '2026-07-31 15:00:00', NULL, NULL, '理想L9金色'
) tmp
WHERE (SELECT COUNT(*) FROM appointment) = 0;

-- ============================================
-- 4. 订单数据（仅在订单表为空时插入）
-- ============================================
INSERT INTO purchase_order (customer_id, car_id, quantity, unit_price, total_amount, order_time, status, handle_time, handler)
SELECT * FROM (
  -- 原有 5 条
  SELECT 1 AS customer_id, 1 AS car_id, 1 AS quantity, 185800.00 AS unit_price, 185800.00 AS total_amount, '2026-07-15 10:00:00' AS order_time, 'confirmed' AS status, '2026-07-16 09:00:00' AS handle_time, '管理员' AS handler
  UNION ALL
  SELECT 1, 5, 1, 179800.00, 179800.00, '2026-07-16 11:00:00', 'confirmed', '2026-07-17 10:00:00', '管理员'
  UNION ALL
  SELECT 2, 2, 1, 195900.00, 195900.00, '2026-07-17 14:00:00', 'cancelled', '2026-07-19 15:00:00', '管理员'
  UNION ALL
  SELECT 3, 4, 2, 329900.00, 659800.00, '2026-07-18 09:00:00', 'confirmed', '2026-07-18 11:00:00', '管理员'
  UNION ALL
  SELECT 2, 7, 1, 209800.00, 209800.00, '2026-07-19 16:00:00', 'pending', NULL, NULL
  -- 新增 7 条
  UNION ALL
  SELECT 4, 9, 1, 428800.00, 428800.00, '2026-07-25 10:00:00', 'pending', NULL, NULL
  UNION ALL
  SELECT 4, 11, 1, 119900.00, 119900.00, '2026-07-26 11:00:00', 'confirmed', '2026-07-27 10:00:00', '管理员'
  UNION ALL
  SELECT 5, 10, 1, 321800.00, 321800.00, '2026-07-27 14:00:00', 'pending', NULL, NULL
  UNION ALL
  SELECT 6, 12, 1, 119800.00, 119800.00, '2026-07-28 09:00:00', 'confirmed', '2026-07-29 10:00:00', '管理员'
  UNION ALL
  SELECT 7, 16, 1, 199800.00, 199800.00, '2026-07-29 15:00:00', 'pending', NULL, NULL
  UNION ALL
  SELECT 8, 17, 1, 498800.00, 498800.00, '2026-07-30 10:00:00', 'confirmed', '2026-07-31 09:00:00', '管理员'
  UNION ALL
  SELECT 10, 18, 1, 398800.00, 398800.00, '2026-07-31 16:00:00', 'pending', NULL, NULL
) tmp
WHERE (SELECT COUNT(*) FROM purchase_order) = 0;
