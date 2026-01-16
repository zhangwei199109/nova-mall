package com.example.ads.service.channel.impl;

import com.example.ads.service.channel.ChannelConfig;
import com.example.ads.service.channel.ChannelConfigProperties;
import com.example.ads.service.channel.MediaChannelClient;
import com.example.ads.service.entity.ConversionLog;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.client.RestTemplate;

import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;

/**
 * 基于 RestTemplate 的简易渠道客户端抽象，实际调用可替换为官方 SDK。
 */
public abstract class AbstractRestChannelClient implements MediaChannelClient {
    protected final Logger log = LoggerFactory.getLogger(getClass());
    private final ChannelConfigProperties props;
    protected final RestTemplate restTemplate = new RestTemplate();

    protected AbstractRestChannelClient(ChannelConfigProperties props) {
        this.props = props;
    }

    @Override
    public void sendConversion(ConversionLog conversion, String ip, String ua) {
        ChannelConfig config = resolveConfig();
        if (config == null || config.getConversionEndpoint() == null) {
            log.warn("{} conversion skipped: endpoint not configured", channel());
            return;
        }
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            if (config.getToken() != null) {
                headers.set("Authorization", "Bearer " + config.getToken());
            }
            Map<String, Object> body = buildPayload(config, conversion, ip, ua);
            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(body, headers);
            restTemplate.postForEntity(config.getConversionEndpoint(), entity, String.class);
            log.info("{} conversion sent traceId={} orderNo={}", channel(), conversion.getTraceId(), conversion.getOrderNo());
        } catch (Exception e) {
            log.warn("{} conversion send failed traceId={} orderNo={}, err={}",
                    channel(), conversion.getTraceId(), conversion.getOrderNo(), e.toString());
        }
    }

    /**
     * 渠道特定的转化上报 payload。
     */
    protected abstract Map<String, Object> buildPayload(ChannelConfig config, ConversionLog conversion, String ip, String ua);

    private ChannelConfig resolveConfig() {
        if (props == null || props.getChannel() == null) return null;
        return props.getChannel().stream()
                .filter(c -> channel().equalsIgnoreCase(c.getChannel()))
                .findFirst()
                .orElse(null);
    }

    /**
     * RFC3339 时间串，部分渠道需要。
     */
    protected String nowRfc3339() {
        return OffsetDateTime.now().format(DateTimeFormatter.ISO_OFFSET_DATE_TIME);
    }
}

