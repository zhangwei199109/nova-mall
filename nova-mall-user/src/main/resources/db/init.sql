-- 用户库初始化
DROP TABLE IF EXISTS `users`;
CREATE TABLE `users` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '用户ID',
  `username` varchar(50) NOT NULL COMMENT '用户名',
  `email` varchar(100) NOT NULL COMMENT '邮箱',
  `mobile` varchar(30) DEFAULT NULL COMMENT '手机号',
  `avatar` varchar(255) DEFAULT NULL COMMENT '头像URL',
  `gender` varchar(20) DEFAULT 'unknown' COMMENT '性别',
  `password` varchar(200) NOT NULL COMMENT 'BCrypt 密码',
  `age` int DEFAULT NULL COMMENT '年龄',
  `deleted` tinyint DEFAULT '0' COMMENT '逻辑删除：0-未删除，1-已删除',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_username` (`username`),
  UNIQUE KEY `uk_email` (`email`),
  UNIQUE KEY `uk_mobile` (`mobile`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户表';

-- 统一初始密码：password
INSERT INTO `users` (`username`, `email`, `mobile`, `avatar`, `gender`, `password`, `age`) VALUES
('张三', 'zhangsan@example.com', '13800000000', 'https://example.com/avatar1.png', 'male', '$2a$10$7EqJtq98hPqEX7fNZaFWoOhi5p5QWJ2YyIVeoG/aa6F/TT6YJ5b.e', 25),
('李四', 'lisi@example.com', '13900000000', 'https://example.com/avatar2.png', 'female', '$2a$10$7EqJtq98hPqEX7fNZaFWoOhi5p5QWJ2YyIVeoG/aa6F/TT6YJ5b.e', 30),
('王五', 'wangwu@example.com', '13700000000', 'https://example.com/avatar3.png', 'unknown', '$2a$10$7EqJtq98hPqEX7fNZaFWoOhi5p5QWJ2YyIVeoG/aa6F/TT6YJ5b.e', 28);

DROP TABLE IF EXISTS `user_addresses`;
CREATE TABLE `user_addresses` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '地址ID',
  `user_id` bigint NOT NULL COMMENT '用户ID',
  `receiver_name` varchar(50) NOT NULL COMMENT '收件人姓名',
  `mobile` varchar(30) NOT NULL COMMENT '手机号',
  `province` varchar(50) DEFAULT NULL COMMENT '省份',
  `city` varchar(50) DEFAULT NULL COMMENT '城市',
  `district` varchar(50) DEFAULT NULL COMMENT '区县',
  `detail` varchar(255) NOT NULL COMMENT '详细地址',
  `zip_code` varchar(20) DEFAULT NULL COMMENT '邮编',
  `is_default` tinyint DEFAULT 0 COMMENT '是否默认 1=是,0=否',
  `deleted` tinyint DEFAULT '0' COMMENT '逻辑删除：0-未删除，1-已删除',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_user_default` (`user_id`, `is_default`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户收货地址表';

INSERT INTO `user_addresses` (`user_id`, `receiver_name`, `mobile`, `province`, `city`, `district`, `detail`, `zip_code`, `is_default`)
VALUES (1, '张三', '13800000000', '广东省', '深圳市', '南山区', '科技园一路 1 号', '518000', 1);

DROP TABLE IF EXISTS `user_devices`;
CREATE TABLE `user_devices` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
  `user_id` bigint NOT NULL COMMENT '用户ID',
  `device_id` varchar(128) NOT NULL COMMENT '设备指纹',
  `platform` varchar(50) DEFAULT NULL COMMENT '平台',
  `os` varchar(50) DEFAULT NULL COMMENT '操作系统',
  `user_agent` varchar(255) DEFAULT NULL COMMENT 'UA',
  `last_ip` varchar(50) DEFAULT NULL COMMENT '最后登录IP',
  `last_login_time` datetime DEFAULT NULL COMMENT '最后登录时间',
  `status` tinyint DEFAULT 0 COMMENT '0正常 1封禁',
  `deleted` tinyint DEFAULT '0' COMMENT '逻辑删除',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_user_device` (`user_id`, `device_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户设备';

DROP TABLE IF EXISTS `user_sessions`;
CREATE TABLE `user_sessions` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
  `user_id` bigint NOT NULL COMMENT '用户ID',
  `device_id` varchar(128) DEFAULT NULL COMMENT '设备指纹',
  `refresh_hash` varchar(64) NOT NULL COMMENT 'refresh token hash',
  `expire_time` datetime NOT NULL COMMENT '过期时间',
  `active` tinyint DEFAULT 1 COMMENT '1有效 0失效',
  `deleted` tinyint DEFAULT '0' COMMENT '逻辑删除',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_refresh_hash` (`refresh_hash`),
  KEY `idx_user_device` (`user_id`, `device_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户会话';

