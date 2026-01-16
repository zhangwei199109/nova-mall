package com.example.ads.service.channel.impl;

import com.example.ads.service.channel.ChannelConfig;
import com.example.ads.service.channel.ChannelConfigProperties;
import com.example.ads.service.entity.ConversionLog;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
public class XhsChannelClient extends AbstractRestChannelClient {

    public XhsChannelClient(ChannelConfigProperties props) {
        super(props);
    }

    @Override
    public String channel() {
        return "xhs";
    }

    @Override
    protected Map<String, Object> buildPayload(ChannelConfig config, ConversionLog conversion, String ip, String ua) {
        Map<String, Object> data = new HashMap<>();
        data.put("advertiser_id", config.getAdvertiserId());
        data.put("event", config.getEventName() != null ? config.getEventName() : "purchase");
        data.put("time", nowRfc3339());
        data.put("trace_id", conversion.getTraceId());
        data.put("order_no", conversion.getOrderNo());
        if (ip != null) data.put("ip", ip);
        if (ua != null) data.put("ua", ua);
        data.put("currency", config.getCurrency());
        return data;
    }
}

