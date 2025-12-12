package com.example.gateway.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
@ConfigurationProperties(prefix = "spring.cloud.gateway.local-rate-limit")
public class LocalRateLimitProperties {

    /**
     * 是否启用本地内存限流；使用 Redis 限流时建议关闭。
     */
    private boolean enabled = true;

    private RouteLimit defaults = new RouteLimit();
    private List<RouteLimit> routes = new ArrayList<>();

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public RouteLimit getDefaults() {
        return defaults;
    }

    public void setDefaults(RouteLimit defaults) {
        this.defaults = defaults;
    }

    public List<RouteLimit> getRoutes() {
        return routes;
    }

    public void setRoutes(List<RouteLimit> routes) {
        this.routes = routes;
    }

    public static class RouteLimit {
        /**
         * 标识，用于缓存桶。
         */
        private String id;
        /**
         * 前缀匹配路径，如 /user/ 或 /order/
         */
        private String matchPrefix;
        /**
         * 每秒填充令牌数。
         */
        private long replenishRate = 50;
        /**
         * 桶容量（突发上限）。
         */
        private long burstCapacity = 100;

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        public String getMatchPrefix() {
            return matchPrefix;
        }

        public void setMatchPrefix(String matchPrefix) {
            this.matchPrefix = matchPrefix;
        }

        public long getReplenishRate() {
            return replenishRate;
        }

        public void setReplenishRate(long replenishRate) {
            this.replenishRate = replenishRate;
        }

        public long getBurstCapacity() {
            return burstCapacity;
        }

        public void setBurstCapacity(long burstCapacity) {
            this.burstCapacity = burstCapacity;
        }
    }
}

