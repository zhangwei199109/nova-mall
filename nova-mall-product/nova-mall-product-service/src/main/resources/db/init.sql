CREATE TABLE IF NOT EXISTS products (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    description VARCHAR(1024),
    price DECIMAL(18,2) NOT NULL,
    stock INT NOT NULL,
    category_id BIGINT NULL,
    brand VARCHAR(255) NULL,
    tags VARCHAR(512) NULL,
    sold_count INT NOT NULL DEFAULT 0,
    view_count INT NOT NULL DEFAULT 0,
    status TINYINT NOT NULL DEFAULT 1,
    create_time TIMESTAMP,
    update_time TIMESTAMP,
    deleted TINYINT DEFAULT 0
);

INSERT INTO products(name, description, price, stock, category_id, brand, tags, sold_count, view_count, status, create_time, update_time, deleted)
VALUES ('iPhone 15', 'Apple phone', 6999.00, 50, 101, 'Apple', '手机,旗舰', 1200, 8000, 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0);
INSERT INTO products(name, description, price, stock, category_id, brand, tags, sold_count, view_count, status, create_time, update_time, deleted)
VALUES ('MacBook Pro', 'Apple laptop', 15999.00, 20, 102, 'Apple', '电脑,高端', 500, 5000, 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0);
INSERT INTO products(name, description, price, stock, category_id, brand, tags, sold_count, view_count, status, create_time, update_time, deleted)
VALUES ('AirPods Pro', 'Apple earbuds', 1999.00, 100, 101, 'Apple', '耳机,降噪', 2000, 12000, 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0);

