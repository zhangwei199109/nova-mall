package com.example.pay.service.channel;

import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * 支付渠道路由器，根据 channel 名称选择具体实现。
 */
@Component
public class PayChannelRouter {

    private final Map<String, PayChannel> map = new HashMap<>();

    public PayChannelRouter(List<PayChannel> channels) {
        if (channels != null) {
            for (PayChannel ch : channels) {
                map.put(ch.channel().toUpperCase(Locale.ROOT), ch);
            }
        }
    }

    public PayChannel route(String channel) {
        if (channel == null) return null;
        return map.get(channel.toUpperCase(Locale.ROOT));
    }
}



