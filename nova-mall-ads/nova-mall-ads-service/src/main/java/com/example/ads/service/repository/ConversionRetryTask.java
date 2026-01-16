package com.example.ads.service.repository;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 转化回传重试任务。
 */
@Data
public class ConversionRetryTask {
    private Long id;
    private String channel;
    private String traceId;
    private String orderNo;
    private String ip;
    private String ua;
    private int retry;
    private String lastError;
    private LocalDateTime nextRunTime;
    private LocalDateTime createTime;
}

