package com.example.ads.api.dto;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 点击埋点记录，仅用于查询/调试返回。
 */
@Data
public class ClickLogDTO {
    private Long id;
    private String traceId;
    private Long campaignId;
    private Long creativeId;
    private String ip;
    private String userAgent;
    private LocalDateTime createTime;
}

