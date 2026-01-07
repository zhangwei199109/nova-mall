CREATE TABLE IF NOT EXISTS stocks (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '库存ID',
    product_id BIGINT NOT NULL COMMENT '商品ID，对应 products.id',
    available INT NOT NULL DEFAULT 0 COMMENT '可售库存（可直接销售）',
    locked INT NOT NULL DEFAULT 0 COMMENT '锁定库存（占用待支付/待出库，不可售）',
    create_time TIMESTAMP COMMENT '创建时间',
    update_time TIMESTAMP COMMENT '更新时间',
    deleted TINYINT DEFAULT 0 COMMENT '逻辑删除：0-未删，1-已删'
) COMMENT='商品库存表';

INSERT INTO stocks(product_id, available, locked, create_time, update_time, deleted)
VALUES (1, 100, 0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0);
INSERT INTO stocks(product_id, available, locked, create_time, update_time, deleted)
VALUES (2, 50, 0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0);
INSERT INTO stocks(product_id, available, locked, create_time, update_time, deleted)
VALUES (3, 200, 0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0);

