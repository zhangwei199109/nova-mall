CREATE TABLE IF NOT EXISTS products (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '商品ID',
    name VARCHAR(255) NOT NULL COMMENT '商品名称',
    description VARCHAR(1024) COMMENT '商品描述',
    price DECIMAL(18,2) NOT NULL COMMENT '价格',
    stock INT NOT NULL COMMENT '库存',
    category_id BIGINT NULL COMMENT '类目ID',
    brand VARCHAR(255) NULL COMMENT '品牌',
    tags VARCHAR(512) NULL COMMENT '标签，逗号分隔',
    sold_count INT NOT NULL DEFAULT 0 COMMENT '累计销量',
    view_count INT NOT NULL DEFAULT 0 COMMENT '浏览量',
    status TINYINT NOT NULL DEFAULT 1 COMMENT '状态：1上架，0下架',
    create_time TIMESTAMP COMMENT '创建时间',
    update_time TIMESTAMP COMMENT '更新时间',
    deleted TINYINT DEFAULT 0 COMMENT '逻辑删除：0未删，1已删'
) COMMENT='商品表';

INSERT INTO products(name, description, price, stock, category_id, brand, tags, sold_count, view_count, status, create_time, update_time, deleted)
VALUES ('iPhone 15', 'Apple phone', 6999.00, 50, 101, 'Apple', '手机,旗舰', 1200, 8000, 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0);
INSERT INTO products(name, description, price, stock, category_id, brand, tags, sold_count, view_count, status, create_time, update_time, deleted)
VALUES ('MacBook Pro', 'Apple laptop', 15999.00, 20, 102, 'Apple', '电脑,高端', 500, 5000, 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0);
INSERT INTO products(name, description, price, stock, category_id, brand, tags, sold_count, view_count, status, create_time, update_time, deleted)
VALUES ('AirPods Pro', 'Apple earbuds', 1999.00, 100, 101, 'Apple', '耳机,降噪', 2000, 12000, 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0);

