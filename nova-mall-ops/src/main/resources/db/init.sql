CREATE TABLE IF NOT EXISTS seckill_activities (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '活动ID',
    product_id BIGINT NOT NULL COMMENT '商品ID',
    title VARCHAR(255) NOT NULL COMMENT '活动标题',
    seckill_price DECIMAL(18,2) NOT NULL COMMENT '秒杀价',
    total_stock INT NOT NULL COMMENT '活动库存总数',
    limit_per_user INT NOT NULL COMMENT '每人限购数量',
    start_time TIMESTAMP NOT NULL COMMENT '开始时间',
    end_time TIMESTAMP NOT NULL COMMENT '结束时间',
    status VARCHAR(32) NOT NULL DEFAULT 'OFFLINE' COMMENT '状态：ONLINE/OFFLINE',
    create_time TIMESTAMP COMMENT '创建时间',
    update_time TIMESTAMP COMMENT '更新时间',
    deleted TINYINT DEFAULT 0 COMMENT '逻辑删除'
) COMMENT='秒杀活动';

-- 示例数据（默认下线）
INSERT INTO seckill_activities(product_id, title, seckill_price, total_stock, limit_per_user, start_time, end_time, status, create_time, update_time, deleted)
VALUES (1, 'iPhone 15 运营配置', 5899.00, 20, 1, TIMESTAMP '2025-12-24 00:00:00', TIMESTAMP '2025-12-31 23:59:59', 'OFFLINE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0);














