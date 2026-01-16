package com.example.ads.api.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 生成的投放追踪链接，包含 traceId 与最终落地页 URL（已附加追踪参数）。
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TrackingLinkResponse {
    private String traceId;
    private String url;
}

