package com.example.ads.service.repository;

import java.util.List;

/**
 * 转化回传重试仓储接口（可用 DB/Redis/MQ 实现）。
 */
public interface ConversionRetryRepository {
    void save(ConversionRetryTask task);
    List<ConversionRetryTask> findPending(int limit);
    void delete(Long id);
}

