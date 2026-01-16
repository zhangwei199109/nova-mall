package com.example.ads.service.channel;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.List;

/**
 * 渠道配置列表，从 ads.channel 下读取。
 */
@ConfigurationProperties(prefix = "ads")
@Data
public class ChannelConfigProperties {
    /**
     * 渠道配置集合。
     */
    private List<ChannelConfig> channel;
}

