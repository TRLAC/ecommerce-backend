CREATE DATABASE shop;
use shop
go

CREATE TABLE users (
    user_id BIGINT IDENTITY(1,1) PRIMARY KEY,
    full_name NVARCHAR(150) NOT NULL,
    email NVARCHAR(150) NOT NULL UNIQUE,
    password_hash NVARCHAR(255) NOT NULL,
    phone NVARCHAR(20),
    created_at DATETIME2 DEFAULT SYSDATETIME(),
    updated_at DATETIME2 DEFAULT SYSDATETIME()
);

CREATE TABLE roles (
    role_id BIGINT IDENTITY(1,1) PRIMARY KEY,
    role_name NVARCHAR(50) NOT NULL UNIQUE
);

CREATE TABLE user_roles (
    user_id BIGINT NOT NULL,
    role_id BIGINT NOT NULL,
    PRIMARY KEY (user_id, role_id),
    CONSTRAINT fk_userroles_user 
        FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE,
    CONSTRAINT fk_userroles_role 
        FOREIGN KEY (role_id) REFERENCES roles(role_id) ON DELETE CASCADE
);

CREATE TABLE carts (
    cart_id BIGINT IDENTITY(1,1) PRIMARY KEY,
    user_id BIGINT NOT NULL UNIQUE,
    created_at DATETIME2 DEFAULT SYSDATETIME(),
    updated_at DATETIME2 DEFAULT SYSDATETIME(),

    CONSTRAINT fk_cart_user
        FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE
);

CREATE TABLE cart_items (
    cart_item_id BIGINT IDENTITY(1,1) PRIMARY KEY,
    cart_id BIGINT NOT NULL,
    product_id BIGINT NOT NULL,
    quantity INT NOT NULL,
    price_snapshot DECIMAL(15,2) NOT NULL,
	created_at DATETIME2 DEFAULT SYSDATETIME(),
    updated_at DATETIME2 DEFAULT SYSDATETIME(),

    CONSTRAINT fk_cartitem_cart
        FOREIGN KEY (cart_id) REFERENCES carts(cart_id) ON DELETE CASCADE,

    CONSTRAINT fk_cartitem_product
        FOREIGN KEY (product_id) REFERENCES products(product_id)
);

ALTER TABLE cart_items
ADD CONSTRAINT uq_cart_product
UNIQUE (cart_id, product_id);

CREATE TABLE brands (
    brand_id BIGINT IDENTITY(1,1) PRIMARY KEY,
    name NVARCHAR(150) NOT NULL UNIQUE
	created_at DATETIME2 NOT NULL DEFAULT SYSUTCDATETIME(),
    updated_at DATETIME2 NOT NULL DEFAULT SYSUTCDATETIME(),
);

CREATE TABLE categories (
    category_id BIGINT IDENTITY(1,1) PRIMARY KEY,
    name NVARCHAR(150) NOT NULL,
	status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    parent_id BIGINT NULL,
	created_at DATETIME2 NOT NULL DEFAULT SYSUTCDATETIME(),
    updated_at DATETIME2 NOT NULL DEFAULT SYSUTCDATETIME(),
    CONSTRAINT fk_category_parent
        FOREIGN KEY (parent_id) REFERENCES categories(category_id)
);


CREATE TABLE products (
    product_id BIGINT IDENTITY(1,1) PRIMARY KEY,
    category_id BIGINT NOT NULL,
	brand_id BIGINT NULL,
    name NVARCHAR(200) NOT NULL,
    price DECIMAL(15,2) NOT NULL,
	ram INT NOT NULL DEFAULT 8,
    storage INT NOT NULL DEFAULT 128,
    battery INT NOT NULL DEFAULT 4000,
    screen_size DECIMAL(4,2) NOT NULL DEFAULT 6.50,
    image_url NVARCHAR(500) NULL,
    stock_quantity INT NOT NULL DEFAULT 0,
    status NVARCHAR(30) NOT NULL,
    created_at DATETIME2 DEFAULT SYSDATETIME(),
    updated_at DATETIME2 NOT NULL DEFAULT SYSUTCDATETIME()
    CONSTRAINT fk_product_category
        FOREIGN KEY (category_id) REFERENCES categories(category_id),

	CONSTRAINT fk_product_brand
		FOREIGN KEY (brand_id) REFERENCES brands(brand_id)
);
    
CREATE TABLE product_images (
    image_id BIGINT IDENTITY(1,1) PRIMARY KEY,
    product_id BIGINT NOT NULL,
    image_url NVARCHAR(500) NOT NULL,
	created_at DATETIME2 NOT NULL DEFAULT SYSUTCDATETIME(),
    updated_at DATETIME2 NOT NULL DEFAULT SYSUTCDATETIME()

    CONSTRAINT fk_product_images_product
    FOREIGN KEY (product_id) REFERENCES products(product_id)
);

CREATE TABLE orders (
    order_id BIGINT IDENTITY(1,1) PRIMARY KEY,
    user_id BIGINT NOT NULL,
    total_amount DECIMAL(15,2) NOT NULL,
    status NVARCHAR(30) NOT NULL,
    created_at DATETIME2 DEFAULT SYSDATETIME(),
	updated_at DATETIME2 NOT NULL DEFAULT SYSUTCDATETIME()
    CONSTRAINT fk_order_user
        FOREIGN KEY (user_id) REFERENCES users(user_id)
);

CREATE TABLE order_items (
    order_item_id BIGINT IDENTITY(1,1) PRIMARY KEY,
    order_id BIGINT NOT NULL,
    product_id BIGINT NOT NULL,
    quantity INT NOT NULL,
    price_snapshot DECIMAL(15,2) NOT NULL,
	created_at DATETIME2 DEFAULT SYSDATETIME(),
	updated_at DATETIME2 NOT NULL DEFAULT SYSUTCDATETIME()
    CONSTRAINT fk_orderitem_order
        FOREIGN KEY (order_id) REFERENCES orders(order_id) ON DELETE CASCADE,
    CONSTRAINT fk_orderitem_product
        FOREIGN KEY (product_id) REFERENCES products(product_id)
);

CREATE TABLE payments (
    payment_id BIGINT IDENTITY(1,1) PRIMARY KEY,
    order_id BIGINT NOT NULL UNIQUE,
    method NVARCHAR(50) NOT NULL,
    amount DECIMAL(15,2) NOT NULL,
    status NVARCHAR(30) NOT NULL,
    transaction_id NVARCHAR(150),
    paid_at DATETIME2 NULL,
	created_at DATETIME2 DEFAULT SYSDATETIME(),
	updated_at DATETIME2 NOT NULL DEFAULT SYSUTCDATETIME()
    CONSTRAINT fk_payment_order
        FOREIGN KEY (order_id) REFERENCES orders(order_id) ON DELETE CASCADE
);

CREATE TABLE shippings (
    shipping_id BIGINT IDENTITY(1,1) PRIMARY KEY,
    order_id BIGINT NOT NULL UNIQUE,
    address NVARCHAR(255) NOT NULL,
    shipping_status NVARCHAR(30) NOT NULL,
    tracking_code NVARCHAR(100),
	created_at DATETIME2 DEFAULT SYSDATETIME(),
	updated_at DATETIME2 NOT NULL DEFAULT SYSUTCDATETIME()
    CONSTRAINT fk_shipping_order
        FOREIGN KEY (order_id) REFERENCES orders(order_id) ON DELETE CASCADE
);

CREATE TABLE refunds (
    refund_id BIGINT IDENTITY(1,1) PRIMARY KEY,
    payment_id BIGINT NOT NULL,
    amount DECIMAL(15,2) NOT NULL,
    reason NVARCHAR(255),
    status NVARCHAR(30) NOT NULL,
    created_at DATETIME2 DEFAULT SYSDATETIME(),
	updated_at DATETIME2 NOT NULL DEFAULT SYSUTCDATETIME()
    CONSTRAINT fk_refund_payment
        FOREIGN KEY (payment_id) REFERENCES payments(payment_id)
);

CREATE TABLE order_status_logs (
    log_id BIGINT IDENTITY(1,1) PRIMARY KEY,
    order_id BIGINT NOT NULL,
    old_status NVARCHAR(30),
    new_status NVARCHAR(30) NOT NULL,
    changed_by BIGINT NULL,
    changed_at DATETIME2 DEFAULT SYSDATETIME(),
    CONSTRAINT fk_log_order
        FOREIGN KEY (order_id) REFERENCES orders(order_id) ON DELETE CASCADE,
    CONSTRAINT fk_log_user
        FOREIGN KEY (changed_by) REFERENCES users(user_id)
);

INSERT INTO users (full_name, email, password_hash, phone)
VALUES 
(N'Admin System', 'admin@gmail.com', 'hashed_admin_pw', '0900000001'),
(N'Nguyen Van A', 'user1@gmail.com', 'hashed_user_pw', '0900000002');

-- Admin = role_id 2
INSERT INTO user_roles (user_id, role_id)
VALUES (1, 2);

-- User = role_id 1
INSERT INTO user_roles (user_id, role_id)
VALUES (2, 1);

INSERT INTO categories (name)
VALUES (N'Điện thoại'), (N'Laptop');

-- Category con
INSERT INTO categories (name, parent_id)
VALUES (N'Android', 1), (N'iOS', 1);

INSERT INTO products (category_id, name, price, stock_quantity, status)
VALUES
(3, N'Samsung Galaxy S24', 22000000, 20, 'ACTIVE'),
(4, N'iPhone 15 Pro', 28000000, 15, 'ACTIVE'),
(2, N'MacBook Air M2', 30000000, 10, 'ACTIVE');

INSERT INTO orders (user_id, total_amount, status)
VALUES (2, 50000000, 'PAID');

INSERT INTO order_items (order_id, product_id, quantity, price_snapshot)
VALUES
(1, 1, 1, 22000000),
(1, 2, 1, 28000000);

INSERT INTO payments (order_id, method, amount, status, transaction_id, paid_at)
VALUES
(1, 'VNPAY', 50000000, 'SUCCESS', 'TXN123456', SYSDATETIME());

INSERT INTO shippings (order_id, address, shipping_status, tracking_code)
VALUES
(1, N'123 Le Loi, Quan 1, TP.HCM', 'SHIPPING', 'GHN999888');

INSERT INTO order_status_logs (order_id, old_status, new_status, changed_by)
VALUES
(1, 'PENDING', 'PAID', 2),
(1, 'PAID', 'SHIPPING', 1);

INSERT INTO refunds (payment_id, amount, reason, status)
VALUES
(1, 5000000, N'Khách yêu cầu hoàn tiền 1 phần', 'PENDING');

CREATE INDEX idx_orders_user_id ON orders(user_id);
CREATE INDEX idx_order_items_order_id ON order_items(order_id);
CREATE INDEX idx_products_category_id ON products(category_id);
CREATE INDEX idx_logs_order_id ON order_status_logs(order_id);
CREATE INDEX idx_cart_user_id ON carts(user_id);
CREATE INDEX idx_cart_items_cart_id ON cart_items(cart_id);