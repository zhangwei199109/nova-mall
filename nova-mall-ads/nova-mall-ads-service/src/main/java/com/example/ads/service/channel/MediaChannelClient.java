package com.example.ads.service.channel;

import com.example.ads.service.entity.ConversionLog;

/**
 * 媒体渠道客户端抽象：用于回传转化/事件。
 */
public interface MediaChannelClient {

    /**
     * 渠道标识（例如：douyin、xhs、google）。
     */
    String channel();

    /**
     * 回传转化事件（示例接口，实际参数可拓展）。
     * @param conversion 转化记录，含 traceId / campaignId / creativeId / orderNo 等
     * @param ip         用户 IP，可为空
     * @param ua         用户 UA，可为空
     */
    void sendConversion(ConversionLog conversion, String ip, String ua);
}

