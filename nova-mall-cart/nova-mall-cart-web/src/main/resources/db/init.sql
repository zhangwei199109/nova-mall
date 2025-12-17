CREATE TABLE IF NOT EXISTS cart_items (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id VARCHAR(64) NOT NULL,
    product_id BIGINT NOT NULL,
    product_name VARCHAR(255),
    price DECIMAL(18,2),
    quantity INT NOT NULL,
    create_time TIMESTAMP,
    update_time TIMESTAMP,
    deleted TINYINT DEFAULT 0
);

INSERT INTO cart_items(user_id, product_id, product_name, price, quantity, create_time, update_time, deleted)
VALUES ('guest', 1, 'iPhone 15', 6999.00, 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0);



