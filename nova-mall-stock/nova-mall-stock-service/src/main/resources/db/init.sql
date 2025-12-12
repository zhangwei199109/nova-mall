CREATE TABLE IF NOT EXISTS stocks (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    product_id BIGINT NOT NULL,
    available INT NOT NULL DEFAULT 0,
    locked INT NOT NULL DEFAULT 0,
    create_time TIMESTAMP,
    update_time TIMESTAMP,
    deleted TINYINT DEFAULT 0
);

INSERT INTO stocks(product_id, available, locked, create_time, update_time, deleted)
VALUES (1, 100, 0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0);
INSERT INTO stocks(product_id, available, locked, create_time, update_time, deleted)
VALUES (2, 50, 0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0);
INSERT INTO stocks(product_id, available, locked, create_time, update_time, deleted)
VALUES (3, 200, 0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0);

