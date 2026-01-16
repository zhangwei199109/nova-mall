package com.example.ads.api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 广告素材（创意）信息，包含标题、素材链接、落地页与追踪参数。
 */
@Data
public class CreativeDTO {
    private Long id;

    @NotNull
    private Long campaignId;

    @NotBlank
    private String title;

    /** 图片/视频 URL */
    private String mediaUrl;

    /** 落地页 URL */
    @NotBlank
    private String landingUrl;

    /** UTM/追踪参数，会拼接到落地页 */
    private String utm;

    /** 是否启用 */
    @NotNull
    private Boolean enabled;
}

