package com.example.ads.service.job;

import com.example.ads.service.channel.MediaChannelClient;
import com.example.ads.service.repository.ConversionRetryRepository;
import com.example.ads.service.repository.ConversionRetryTask;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 转化回传重试任务，简单示例：每分钟扫描待重试的任务。
 */
@Component
public class ConversionRetryJob {
    private static final Logger log = LoggerFactory.getLogger(ConversionRetryJob.class);

    private final ConversionRetryRepository repository;
    private final Map<String, MediaChannelClient> clientMap = new HashMap<>();

    public ConversionRetryJob(ConversionRetryRepository repository, List<MediaChannelClient> clients) {
        this.repository = repository;
        if (clients != null) {
            for (MediaChannelClient c : clients) {
                clientMap.put(c.channel(), c);
            }
        }
    }

    @Scheduled(fixedDelay = 60_000)
    public void retry() {
        List<ConversionRetryTask> pending = repository.findPending(50);
        for (ConversionRetryTask task : pending) {
            MediaChannelClient client = clientMap.get(task.getChannel());
            if (client == null) {
                log.warn("retry skip: no client for channel {}", task.getChannel());
                repository.delete(task.getId());
                continue;
            }
            try {
                client.sendConversion(toConversionLog(task), task.getIp(), task.getUa());
                repository.delete(task.getId());
            } catch (Exception e) {
                int nextRetry = task.getRetry() + 1;
                task.setRetry(nextRetry);
                task.setLastError(e.toString());
                task.setNextRunTime(LocalDateTime.now().plusMinutes(Math.min(nextRetry, 10)));
                repository.save(task);
                log.warn("retry failed channel={} traceId={} orderNo={} retry={} err={}",
                        task.getChannel(), task.getTraceId(), task.getOrderNo(), nextRetry, e.toString());
            }
        }
    }

    private com.example.ads.service.entity.ConversionLog toConversionLog(ConversionRetryTask task) {
        com.example.ads.service.entity.ConversionLog log = new com.example.ads.service.entity.ConversionLog();
        log.setTraceId(task.getTraceId());
        log.setOrderNo(task.getOrderNo());
        log.setCampaignId(task.getCampaignId());
        log.setCreativeId(task.getCreativeId());
        return log;
    }
}

