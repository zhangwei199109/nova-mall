package com.example.order.web.client.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class OpsActivityDTO {
    private Long id;
    private Long productId;
    private String title;
    private BigDecimal seckillPrice;
    private Integer totalStock;
    private Integer limitPerUser;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private String status;
}



















