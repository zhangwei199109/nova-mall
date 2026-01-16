package com.example.ads.api.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 生成投放追踪链接请求。
 * 可传入自定义 traceId，未传则自动生成 UUID。
 */
@Data
public class TrackingLinkRequest {
    @NotNull
    private Long creativeId;

    /** 可选自定义 traceId（不传则生成 UUID） */
    private String traceId;
}

