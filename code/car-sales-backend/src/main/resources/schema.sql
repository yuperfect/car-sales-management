-- ============================================
-- 汽车销售管理系统 - 数据库建表脚本
-- 数据库: car_sales_db
-- 基于需求分析与设计文档
-- ============================================

CREATE DATABASE IF NOT EXISTS car_sales_db DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

USE car_sales_db;

-- ============================================
-- 清理旧表
-- ============================================
DROP TABLE IF EXISTS purchase_order;
DROP TABLE IF EXISTS appointment;
DROP TABLE IF EXISTS car;
DROP TABLE IF EXISTS customer;

-- ============================================
-- 1. 客户表 (customer)
-- 说明：客户信息由提交预约或订单时自动创建，无登录功能
-- ============================================
CREATE TABLE customer (
    customer_id INT AUTO_INCREMENT PRIMARY KEY COMMENT '客户编号',
    real_name VARCHAR(50) NOT NULL COMMENT '姓名',
    phone CHAR(11) NOT NULL COMMENT '联系电话（11位手机号）',
    first_submit_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '首次提交时间',
    UNIQUE KEY uk_phone (phone)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='客户表';

-- ============================================
-- 2. 车辆表 (car)
-- ============================================
CREATE TABLE car (
    car_id INT AUTO_INCREMENT PRIMARY KEY COMMENT '车辆编号',
    brand VARCHAR(50) NOT NULL COMMENT '品牌',
    model VARCHAR(50) NOT NULL COMMENT '型号',
    displacement VARCHAR(20) DEFAULT NULL COMMENT '排量',
    transmission VARCHAR(20) DEFAULT NULL COMMENT '变速箱',
    color VARCHAR(30) DEFAULT NULL COMMENT '颜色',
    price DECIMAL(12, 2) NOT NULL COMMENT '价格',
    stock INT NOT NULL DEFAULT 0 COMMENT '库存',
    listed_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '上架时间',
    status VARCHAR(10) NOT NULL DEFAULT 'on_sale' COMMENT '状态：on_sale在售 / sold_out停售',
    CONSTRAINT chk_car_price CHECK (price > 0),
    CONSTRAINT chk_car_stock CHECK (stock >= 0)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='车辆表';

-- 车辆表：新增图片列（DDL migration for STORY-IMG-01）
ALTER TABLE car ADD COLUMN image_url VARCHAR(500) DEFAULT NULL COMMENT '图片URL' AFTER status;

-- ============================================
-- 3. 预约表 (appointment)
-- ============================================
CREATE TABLE appointment (
    appointment_id INT AUTO_INCREMENT PRIMARY KEY COMMENT '预约编号',
    customer_id INT NOT NULL COMMENT '客户编号',
    car_id INT NOT NULL COMMENT '车辆编号',
    appointment_time DATETIME NOT NULL COMMENT '预约时间',
    status VARCHAR(10) NOT NULL DEFAULT 'pending' COMMENT '状态：pending待确认 / confirmed已确认 / cancelled已取消',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    handle_time DATETIME DEFAULT NULL COMMENT '处理时间',
    handler VARCHAR(50) DEFAULT NULL COMMENT '处理人',
    remark VARCHAR(500) DEFAULT NULL COMMENT '备注',
    CONSTRAINT fk_appointment_customer FOREIGN KEY (customer_id) REFERENCES customer(customer_id),
    CONSTRAINT fk_appointment_car FOREIGN KEY (car_id) REFERENCES car(car_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='预约表';

-- ============================================
-- 4. 订单表 (purchase_order)
-- ============================================
CREATE TABLE purchase_order (
    order_id INT AUTO_INCREMENT PRIMARY KEY COMMENT '订单编号',
    customer_id INT NOT NULL COMMENT '客户编号',
    car_id INT NOT NULL COMMENT '车辆编号',
    quantity INT NOT NULL DEFAULT 1 COMMENT '购车数量',
    unit_price DECIMAL(12, 2) NOT NULL COMMENT '单价',
    total_amount DECIMAL(12, 2) NOT NULL COMMENT '订单总金额',
    order_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '下单时间',
    status VARCHAR(10) NOT NULL DEFAULT 'pending' COMMENT '状态：pending待处理 / confirmed已确认 / cancelled已取消',
    handle_time DATETIME DEFAULT NULL COMMENT '处理时间',
    handler VARCHAR(50) DEFAULT NULL COMMENT '处理人',
    CONSTRAINT fk_order_customer FOREIGN KEY (customer_id) REFERENCES customer(customer_id),
    CONSTRAINT fk_order_car FOREIGN KEY (car_id) REFERENCES car(car_id),
    CONSTRAINT chk_order_quantity CHECK (quantity >= 1)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='订单表';

-- ============================================
-- 5. 触发器
-- ============================================

-- 触发器：库存归零后自动更新车辆状态
DELIMITER //
CREATE TRIGGER trg_car_stock_check_update_status
BEFORE UPDATE ON car
FOR EACH ROW
BEGIN
    IF NEW.stock <= 0 AND OLD.stock > 0 THEN
        SET NEW.status = 'sold_out';
    END IF;
END//
DELIMITER ;