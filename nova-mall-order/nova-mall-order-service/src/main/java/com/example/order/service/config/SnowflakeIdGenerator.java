package com.example.order.service.config;

import org.springframework.stereotype.Component;

/**
 * 简单雪花算法实现，用于生成全局唯一ID。
 */
@Component
public class SnowflakeIdGenerator {

    private static final long START_STAMP = 1704067200000L; // 2024-01-01

    private static final long SEQUENCE_BIT = 12;      // 序列号占用位数
    private static final long MACHINE_BIT = 5;        // 机器标识占用位数
    private static final long DATACENTER_BIT = 5;     // 数据中心占用位数

    private static final long MAX_DATACENTER_NUM = ~(-1L << DATACENTER_BIT);
    private static final long MAX_MACHINE_NUM = ~(-1L << MACHINE_BIT);
    private static final long MAX_SEQUENCE = ~(-1L << SEQUENCE_BIT);

    private static final long MACHINE_LEFT = SEQUENCE_BIT;
    private static final long DATACENTER_LEFT = SEQUENCE_BIT + MACHINE_BIT;
    private static final long TIMESTAMP_LEFT = DATACENTER_LEFT + DATACENTER_BIT;

    private final long datacenterId;
    private final long machineId;
    private long sequence = 0L;
    private long lastStamp = -1L;

    public SnowflakeIdGenerator() {
        this(1, 1);
    }

    public SnowflakeIdGenerator(long datacenterId, long machineId) {
        if (datacenterId > MAX_DATACENTER_NUM || datacenterId < 0) {
            throw new IllegalArgumentException("datacenterId out of range");
        }
        if (machineId > MAX_MACHINE_NUM || machineId < 0) {
            throw new IllegalArgumentException("machineId out of range");
        }
        this.datacenterId = datacenterId;
        this.machineId = machineId;
    }

    /**
     * 生成下一个ID（线程安全）。
     */
    public synchronized long nextId() {
        long currStamp = System.currentTimeMillis();
        if (currStamp < lastStamp) {
            throw new IllegalStateException("Clock moved backwards. Refusing to generate id");
        }

        if (currStamp == lastStamp) {
            sequence = (sequence + 1) & MAX_SEQUENCE;
            if (sequence == 0L) {
                currStamp = waitNextMillis();
            }
        } else {
            sequence = 0L;
        }

        lastStamp = currStamp;

        return (currStamp - START_STAMP) << TIMESTAMP_LEFT
                | datacenterId << DATACENTER_LEFT
                | machineId << MACHINE_LEFT
                | sequence;
    }

    public String nextIdStr() {
        return String.valueOf(nextId());
    }

    private long waitNextMillis() {
        long mill = System.currentTimeMillis();
        while (mill <= lastStamp) {
            mill = System.currentTimeMillis();
        }
        return mill;
    }
}




















