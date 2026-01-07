CREATE TABLE IF NOT EXISTS payments (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '支付记录ID',
    pay_no VARCHAR(64) NOT NULL UNIQUE COMMENT '商户支付单号',
    order_id BIGINT NOT NULL COMMENT '关联订单ID',
    user_id BIGINT COMMENT '用户ID',
    amount DECIMAL(18,2) NOT NULL COMMENT '支付金额',
    channel VARCHAR(32) COMMENT '支付渠道',
    channel_trade_no VARCHAR(64) COMMENT '渠道交易号',
    currency VARCHAR(8) COMMENT '币种',
    extra VARCHAR(512) COMMENT '扩展字段',
    status VARCHAR(32) NOT NULL COMMENT '状态',
    idem_key VARCHAR(64) UNIQUE COMMENT '幂等键',
    version INT DEFAULT 0 COMMENT '乐观锁',
    deleted TINYINT DEFAULT 0 COMMENT '逻辑删除：0未删 1已删',
    create_time TIMESTAMP COMMENT '创建时间',
    update_time TIMESTAMP COMMENT '更新时间'
) COMMENT='支付记录';

CREATE TABLE IF NOT EXISTS refunds (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '退款记录ID',
    refund_no VARCHAR(64) NOT NULL UNIQUE COMMENT '商户退款单号',
    pay_no VARCHAR(64) NOT NULL COMMENT '关联支付单号',
    order_id BIGINT COMMENT '关联订单ID',
    user_id BIGINT COMMENT '用户ID',
    amount DECIMAL(18,2) NOT NULL COMMENT '退款金额',
    channel_refund_no VARCHAR(64) COMMENT '渠道退款号',
    currency VARCHAR(8) COMMENT '币种',
    extra VARCHAR(512) COMMENT '扩展字段',
    status VARCHAR(32) NOT NULL COMMENT '状态',
    reason VARCHAR(255) COMMENT '退款原因',
    idem_key VARCHAR(64) UNIQUE COMMENT '幂等键',
    version INT DEFAULT 0 COMMENT '乐观锁',
    deleted TINYINT DEFAULT 0 COMMENT '逻辑删除',
    create_time TIMESTAMP COMMENT '创建时间',
    update_time TIMESTAMP COMMENT '更新时间'
) COMMENT='退款记录';

CREATE TABLE IF NOT EXISTS payment_callback_logs (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键',
    pay_no VARCHAR(64) NOT NULL COMMENT '支付单号',
    callback_key VARCHAR(64) NOT NULL COMMENT '回调幂等键',
    raw_message VARCHAR(512) COMMENT '原始报文',
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    UNIQUE KEY uk_pay_cb (pay_no, callback_key)
) COMMENT='支付回调日志';

CREATE TABLE IF NOT EXISTS refund_callback_logs (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键',
    refund_no VARCHAR(64) NOT NULL COMMENT '退款单号',
    callback_key VARCHAR(64) NOT NULL COMMENT '回调幂等键',
    raw_message VARCHAR(512) COMMENT '原始报文',
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    UNIQUE KEY uk_ref_cb (refund_no, callback_key)
) COMMENT='退款回调日志';

