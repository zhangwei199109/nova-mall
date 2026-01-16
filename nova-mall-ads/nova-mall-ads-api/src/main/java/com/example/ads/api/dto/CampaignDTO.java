package com.example.ads.api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 广告活动（campaign）基础信息，承载前后台的创建/查询请求响应。
 * 仅包含轻量字段，不含统计指标。
 */
@Data
public class CampaignDTO {
    private Long id;

    @NotBlank
    private String name;

    /** 投放渠道或分组标签（自定义，如 douyin/xhs/sem） */
    private String channel;

    /** 预算（可选，单位元） */
    private Integer budget;

    /** 是否启用 */
    @NotNull
    private Boolean enabled;

    private LocalDateTime startTime;
    private LocalDateTime endTime;
}

