-- ============================================
-- 汽车销售管理系统 - 数据库建表脚本
-- 数据库: car_sales_db
-- 基于需求分析与设计文档
-- ============================================

CREATE DATABASE IF NOT EXISTS car_sales_db DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

USE car_sales_db;

-- ============================================
-- 1. 客户表 (customer)
-- 说明：客户信息由提交预约或订单时自动创建，支持用户名密码绑定
-- ============================================
CREATE TABLE IF NOT EXISTS customer (
    customer_id INT AUTO_INCREMENT PRIMARY KEY COMMENT '客户编号',
    username VARCHAR(50) DEFAULT NULL COMMENT '用户名（唯一标识，NULL表示未绑定）',
    password VARCHAR(255) DEFAULT NULL COMMENT '密码（SHA-256加密，NULL表示未绑定）',
    real_name VARCHAR(50) NOT NULL COMMENT '姓名',
    phone CHAR(11) NOT NULL COMMENT '联系电话（11位手机号）',
    email VARCHAR(100) DEFAULT NULL COMMENT '邮箱',
    address VARCHAR(200) DEFAULT NULL COMMENT '地址',
    gender VARCHAR(10) DEFAULT '保密' COMMENT '性别：男/女/保密',
    birthday DATE DEFAULT NULL COMMENT '生日',
    avatar_url VARCHAR(500) DEFAULT NULL COMMENT '头像URL',
    first_submit_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '首次提交时间',
    update_time DATETIME DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP COMMENT '最后修改时间',
    UNIQUE KEY uk_phone (phone),
    UNIQUE KEY uk_username (username)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='客户表';

-- ============================================
-- 2. 车辆表 (car)
-- ============================================
CREATE TABLE IF NOT EXISTS car (
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
    image_url VARCHAR(500) DEFAULT NULL COMMENT '车辆图片URL',
    CONSTRAINT chk_car_price CHECK (price > 0),
    CONSTRAINT chk_car_stock CHECK (stock >= 0)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='车辆表';

-- ============================================
-- 3. 预约表 (appointment)
-- ============================================
CREATE TABLE IF NOT EXISTS appointment (
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
CREATE TABLE IF NOT EXISTS purchase_order (
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