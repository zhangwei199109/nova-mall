-- 订单库初始化
DROP TABLE IF EXISTS `orders`;
CREATE TABLE `orders` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '订单ID',
  `order_no` varchar(64) NOT NULL COMMENT '订单号',
  `user_id` bigint NOT NULL COMMENT '用户ID',
  `amount` decimal(18,2) NOT NULL COMMENT '订单金额',
  `status` varchar(32) DEFAULT 'CREATED' COMMENT '订单状态',
  `deleted` tinyint DEFAULT '0' COMMENT '逻辑删除：0-未删除，1-已删除',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_order_no` (`order_no`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='订单表';

INSERT INTO `orders` (`order_no`, `user_id`, `amount`, `status`) VALUES
('ORD-1001', 1, 199.99, 'CREATED'),
('ORD-1002', 2, 88.50, 'PAID'),
('ORD-1003', 3, 45.00, 'CREATED');

