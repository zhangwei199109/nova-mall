package com.example.ads.service.channel;

import lombok.Data;

/**
 * 媒体渠道配置。
 */
@Data
public class ChannelConfig {
    /** 渠道名称，如 douyin/xhs/google */
    private String channel;
    /** 转化回传 API 地址 */
    private String conversionEndpoint;
    /** 认证 token 或 appSecret，按渠道解释 */
    private String token;
    /** 客户端/应用 ID，可选（如 Google OAuth clientId） */
    private String clientId;
    /** 客户端/应用 Secret，可选 */
    private String clientSecret;
    /** 广告主/账户 ID */
    private String advertiserId;
    /** 事件名称（部分渠道需要指定，如 purchase） */
    private String eventName = "purchase";
    /** 默认货币（如 CNY/USD） */
    private String currency = "CNY";
    /** Google Ads 等场景的 conversion_action 标识 */
    private String conversionAction;
    /** 超时毫秒 */
    private Integer timeoutMs = 3000;
}

