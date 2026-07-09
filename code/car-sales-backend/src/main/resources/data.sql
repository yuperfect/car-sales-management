-- ============================================
-- 汽车销售管理系统 - 初始测试数据
-- ============================================

USE car_sales_db;

-- ============================================
-- 1. 客户数据
-- ============================================
INSERT INTO customer (real_name, phone, first_submit_time) VALUES
('张三', '13900000001', '2026-06-15 10:00:00'),
('李四', '13900000002', '2026-06-20 14:30:00'),
('王五', '13900000003', '2026-06-25 09:15:00');

-- ============================================
-- 2. 车辆数据
-- ============================================
INSERT INTO car (brand, model, displacement, transmission, color, price, stock, status, listed_time) VALUES
('丰田', 'RAV4 荣放', '2.0L', 'CVT无级变速', '白色', 185800.00, 5, 'on_sale', '2026-06-01 08:00:00'),
('本田', 'CR-V', '1.5T', 'CVT无级变速', '黑色', 195900.00, 3, 'on_sale', '2026-06-01 08:00:00'),
('大众', '途观L', '2.0T', '7档双离合', '银色', 215800.00, 4, 'on_sale', '2026-06-01 08:00:00'),
('宝马', '3系', '2.0T', '8档手自一体', '蓝色', 329900.00, 2, 'on_sale', '2026-06-01 08:00:00'),
('丰田', '凯美瑞', '2.0L', 'CVT无级变速', '白色', 179800.00, 6, 'on_sale', '2026-06-01 08:00:00'),
('特斯拉', 'Model Y', '纯电动', '单速变速箱', '红色', 249900.00, 3, 'on_sale', '2026-06-01 08:00:00'),
('比亚迪', '汉EV', '纯电动', '单速变速箱', '黑色', 209800.00, 4, 'on_sale', '2026-06-01 08:00:00'),
('保时捷', '718', '2.0T', '7档双离合', '黄色', 565000.00, 1, 'on_sale', '2026-06-01 08:00:00');

-- ============================================
-- 3. 预约数据
-- ============================================
INSERT INTO appointment (customer_id, car_id, appointment_time, status, create_time, handle_time, remark) VALUES
(1, 1, '2026-07-21 10:00:00', 'confirmed', '2026-07-15 09:00:00', '2026-07-15 14:00:00', '希望试驾白色RAV4'),
(1, 5, '2026-07-22 14:30:00', 'confirmed', '2026-07-16 10:00:00', '2026-07-16 16:00:00', NULL),
(2, 2, '2026-07-23 09:00:00', 'pending',   '2026-07-17 08:00:00', NULL, NULL),
(3, 4, '2026-07-24 15:00:00', 'cancelled', '2026-07-18 11:00:00', '2026-07-18 15:30:00', '临时有事取消'),
(2, 7, '2026-07-25 11:00:00', 'pending',   '2026-07-19 09:30:00', NULL, NULL);

-- ============================================
-- 4. 订单数据
-- ============================================
-- 先插入 pending 订单，再 UPDATE 为 confirmed 以触发库存扣减触发器
INSERT INTO `order` (customer_id, car_id, quantity, unit_price, total_amount, order_time, status, handle_time, handler) VALUES
(1, 1, 1, 185800.00, 185800.00, '2026-07-15 10:00:00', 'pending', NULL, NULL),
(1, 5, 1, 179800.00, 179800.00, '2026-07-16 11:00:00', 'pending', NULL, NULL),
(2, 2, 1, 195900.00, 195900.00, '2026-07-17 14:00:00', 'pending', NULL, NULL),
(3, 4, 2, 329900.00, 659800.00, '2026-07-18 09:00:00', 'pending', NULL, NULL),
(2, 7, 1, 209800.00, 209800.00, '2026-07-19 16:00:00', 'pending', NULL, NULL);

-- 确认部分订单以产生统计数据（同时触发库存扣减）
UPDATE `order` SET status = 'confirmed', handle_time = '2026-07-16 09:00:00', handler = '管理员' WHERE order_id = 1;
UPDATE `order` SET status = 'confirmed', handle_time = '2026-07-17 10:00:00', handler = '管理员' WHERE order_id = 2;
UPDATE `order` SET status = 'confirmed', handle_time = '2026-07-18 11:00:00', handler = '管理员' WHERE order_id = 4;
UPDATE `order` SET status = 'cancelled', handle_time = '2026-07-19 15:00:00', handler = '管理员' WHERE order_id = 3;

-- 确认部分预约
UPDATE appointment SET status = 'confirmed', handle_time = '2026-07-15 14:00:00' WHERE appointment_id = 1;
UPDATE appointment SET status = 'confirmed', handle_time = '2026-07-16 16:00:00' WHERE appointment_id = 2;