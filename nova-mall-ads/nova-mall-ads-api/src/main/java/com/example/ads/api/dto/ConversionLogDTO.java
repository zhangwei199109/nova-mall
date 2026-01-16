package com.example.ads.api.dto;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 转化埋点记录，存储 traceId 与订单号关联。
 */
@Data
public class ConversionLogDTO {
    private Long id;
    private String traceId;
    private Long campaignId;
    private Long creativeId;
    private String orderNo;
    private LocalDateTime createTime;
}

