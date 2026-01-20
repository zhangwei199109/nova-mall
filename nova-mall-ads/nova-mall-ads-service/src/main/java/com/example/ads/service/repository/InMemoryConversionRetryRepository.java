package com.example.ads.service.repository;

import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

/**
 * 简易内存版重试仓储，示例用，生产可换 DB/Redis。
 */
@Repository
public class InMemoryConversionRetryRepository implements ConversionRetryRepository {
    private final List<ConversionRetryTask> tasks = new ArrayList<>();
    private final AtomicLong idGen = new AtomicLong(1);

    @Override
    public synchronized void save(ConversionRetryTask task) {
        if (task.getId() == null) {
            task.setId(idGen.getAndIncrement());
        }
        if (task.getCreateTime() == null) {
            task.setCreateTime(LocalDateTime.now());
        }
        tasks.add(task);
    }

    @Override
    public synchronized List<ConversionRetryTask> findPending(int limit) {
        LocalDateTime now = LocalDateTime.now();
        return tasks.stream()
                .filter(t -> t.getNextRunTime() == null || !t.getNextRunTime().isAfter(now))
                .sorted(Comparator.comparing(ConversionRetryTask::getNextRunTime, Comparator.nullsFirst(Comparator.naturalOrder()))
                        .thenComparing(ConversionRetryTask::getId))
                .limit(limit)
                .toList();
    }

    @Override
    public synchronized void delete(Long id) {
        tasks.removeIf(t -> t.getId().equals(id));
    }
}



