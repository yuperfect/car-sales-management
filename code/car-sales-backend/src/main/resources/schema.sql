-- ============================================
-- 汽车销售管理系统 - 数据库建表脚本
-- 数据库: car_sales_db
-- ============================================

CREATE DATABASE IF NOT EXISTS car_sales_db DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

USE car_sales_db;

-- ============================================
-- 1. 用户表 (user)
-- ============================================
DROP TABLE IF EXISTS purchase_order;
DROP TABLE IF EXISTS test_drive;
DROP TABLE IF EXISTS car;
DROP TABLE IF EXISTS category;
DROP TABLE IF EXISTS user;

CREATE TABLE user (
    user_id INT AUTO_INCREMENT PRIMARY KEY COMMENT '用户ID',
    username VARCHAR(50) NOT NULL UNIQUE COMMENT '用户名',
    password VARCHAR(100) NOT NULL COMMENT '密码',
    role ENUM('customer', 'admin') NOT NULL DEFAULT 'customer' COMMENT '角色：客户/管理员',
    real_name VARCHAR(50) DEFAULT NULL COMMENT '真实姓名',
    phone VARCHAR(20) DEFAULT NULL COMMENT '联系电话',
    email VARCHAR(100) DEFAULT NULL COMMENT '电子邮箱',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户表';

-- ============================================
-- 2. 车型分类表 (category)
-- ============================================
CREATE TABLE category (
    category_id INT AUTO_INCREMENT PRIMARY KEY COMMENT '分类ID',
    category_name VARCHAR(50) NOT NULL UNIQUE COMMENT '分类名称',
    description VARCHAR(200) DEFAULT NULL COMMENT '分类描述'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='车型分类表';

-- ============================================
-- 3. 在售车辆表 (car)
-- ============================================
CREATE TABLE car (
    car_id INT AUTO_INCREMENT PRIMARY KEY COMMENT '车辆ID',
    category_id INT NOT NULL COMMENT '分类ID',
    brand VARCHAR(50) NOT NULL COMMENT '品牌',
    model VARCHAR(100) NOT NULL COMMENT '车型名称',
    year YEAR DEFAULT NULL COMMENT '出厂年份',
    color VARCHAR(30) DEFAULT NULL COMMENT '颜色',
    price DECIMAL(10, 2) NOT NULL COMMENT '售价',
    stock INT NOT NULL DEFAULT 0 COMMENT '库存数量',
    status ENUM('on_sale', 'sold_out') NOT NULL DEFAULT 'on_sale' COMMENT '销售状态',
    description TEXT DEFAULT NULL COMMENT '车辆描述',
    image_url VARCHAR(200) DEFAULT NULL COMMENT '图片路径',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '录入时间',
    CONSTRAINT fk_car_category FOREIGN KEY (category_id) REFERENCES category(category_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='在售车辆表';

-- ============================================
-- 4. 试驾预约表 (test_drive)
-- ============================================
CREATE TABLE test_drive (
    drive_id INT AUTO_INCREMENT PRIMARY KEY COMMENT '预约ID',
    customer_id INT NOT NULL COMMENT '客户ID',
    car_id INT NOT NULL COMMENT '车辆ID',
    drive_date DATE NOT NULL COMMENT '预约试驾日期',
    drive_time TIME NOT NULL COMMENT '预约试驾时间',
    status ENUM('pending', 'confirmed', 'cancelled') NOT NULL DEFAULT 'pending' COMMENT '状态：待确认/已确认/已取消',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '申请时间',
    confirmed_at DATETIME DEFAULT NULL COMMENT '确认时间',
    CONSTRAINT fk_testdrive_customer FOREIGN KEY (customer_id) REFERENCES user(user_id),
    CONSTRAINT fk_testdrive_car FOREIGN KEY (car_id) REFERENCES car(car_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='试驾预约表';

-- ============================================
-- 5. 购车订单表 (purchase_order)
-- ============================================
CREATE TABLE purchase_order (
    order_id INT AUTO_INCREMENT PRIMARY KEY COMMENT '订单ID',
    customer_id INT NOT NULL COMMENT '客户ID',
    car_id INT NOT NULL COMMENT '车辆ID',
    quantity INT NOT NULL DEFAULT 1 COMMENT '购买数量',
    total_price DECIMAL(10, 2) NOT NULL COMMENT '总价',
    status ENUM('pending', 'confirmed', 'cancelled') NOT NULL DEFAULT 'pending' COMMENT '状态：待确认/已确认/已取消',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '下单时间',
    confirmed_at DATETIME DEFAULT NULL COMMENT '确认时间',
    CONSTRAINT fk_order_customer FOREIGN KEY (customer_id) REFERENCES user(user_id),
    CONSTRAINT fk_order_car FOREIGN KEY (car_id) REFERENCES car(car_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='购车订单表';

-- ============================================
-- 6. 触发器
-- ============================================

-- 触发器1：订单确认后自动扣减库存
DELIMITER //
CREATE TRIGGER trg_order_confirm_deduct_stock
AFTER UPDATE ON purchase_order
FOR EACH ROW
BEGIN
    IF NEW.status = 'confirmed' AND OLD.status = 'pending' THEN
        UPDATE car
        SET stock = stock - NEW.quantity
        WHERE car_id = NEW.car_id;
    END IF;
END//
DELIMITER ;

-- 触发器2：订单取消后自动恢复库存
DELIMITER //
CREATE TRIGGER trg_order_cancel_restore_stock
AFTER UPDATE ON purchase_order
FOR EACH ROW
BEGIN
    IF NEW.status = 'cancelled' AND OLD.status = 'confirmed' THEN
        UPDATE car
        SET stock = stock + NEW.quantity
        WHERE car_id = NEW.car_id;
    END IF;
END//
DELIMITER ;

-- 触发器3：库存归零后自动更新车辆状态
-- 使用 BEFORE UPDATE，直接修改 NEW.status 避免同一表递归更新
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