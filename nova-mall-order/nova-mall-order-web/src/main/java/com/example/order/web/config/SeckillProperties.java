package com.example.order.web.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * 秒杀活动配置（可由配置中心或本地 YAML 提供）。
 */
@Data
@Component
@ConfigurationProperties(prefix = "seckill")
public class SeckillProperties {

    /**
     * 活动列表，未配置时由业务代码提供默认示例。
     */
    private List<Activity> activities = new ArrayList<>();

    @Data
    public static class Activity {
        private Long id;
        private Long productId;
        private String title;
        private BigDecimal seckillPrice;
        private Integer totalStock;
        private Integer limitPerUser;
        private LocalDateTime startTime;
        private LocalDateTime endTime;
    }
}



















