CREATE TABLE IF NOT EXISTS cart_items (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '购物车项ID',
    user_id VARCHAR(64) NOT NULL COMMENT '用户ID',
    product_id BIGINT NOT NULL COMMENT '商品ID',
    product_name VARCHAR(255) COMMENT '商品名称快照',
    price DECIMAL(18,2) COMMENT '价格快照',
    quantity INT NOT NULL COMMENT '数量',
    create_time TIMESTAMP COMMENT '创建时间',
    update_time TIMESTAMP COMMENT '更新时间',
    deleted TINYINT DEFAULT 0 COMMENT '逻辑删除：0未删 1已删'
) COMMENT='购物车表';

INSERT INTO cart_items(user_id, product_id, product_name, price, quantity, create_time, update_time, deleted)
VALUES ('guest', 1, 'iPhone 15', 6999.00, 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0);

