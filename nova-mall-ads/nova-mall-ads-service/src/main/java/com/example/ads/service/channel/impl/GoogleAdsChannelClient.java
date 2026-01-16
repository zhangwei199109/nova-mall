package com.example.ads.service.channel.impl;

import com.example.ads.service.channel.ChannelConfig;
import com.example.ads.service.channel.ChannelConfigProperties;
import com.example.ads.service.entity.ConversionLog;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
public class GoogleAdsChannelClient extends AbstractRestChannelClient {

    public GoogleAdsChannelClient(ChannelConfigProperties props) {
        super(props);
    }

    @Override
    public String channel() {
        return "google";
    }

    @Override
    protected Map<String, Object> buildPayload(ChannelConfig config, ConversionLog conversion, String ip, String ua) {
        Map<String, Object> data = new HashMap<>();
        data.put("customer_id", config.getAdvertiserId());
        data.put("conversion_action", config.getConversionAction());
        data.put("conversion_date_time", nowRfc3339());
        data.put("trace_id", conversion.getTraceId());
        data.put("order_id", conversion.getOrderNo());
        data.put("currency_code", config.getCurrency());
        if (ip != null) data.put("user_ip_address", ip);
        if (ua != null) data.put("user_agent", ua);
        return data;
    }
}

