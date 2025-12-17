CREATE TABLE IF NOT EXISTS products (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    description VARCHAR(1024),
    price DECIMAL(18,2) NOT NULL,
    stock INT NOT NULL,
    create_time TIMESTAMP,
    update_time TIMESTAMP,
    deleted TINYINT DEFAULT 0
);

INSERT INTO products(name, description, price, stock, create_time, update_time, deleted)
VALUES ('iPhone 15', 'Apple phone', 6999.00, 50, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0);
INSERT INTO products(name, description, price, stock, create_time, update_time, deleted)
VALUES ('MacBook Pro', 'Apple laptop', 15999.00, 20, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0);
INSERT INTO products(name, description, price, stock, create_time, update_time, deleted)
VALUES ('AirPods Pro', 'Apple earbuds', 1999.00, 100, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0);



