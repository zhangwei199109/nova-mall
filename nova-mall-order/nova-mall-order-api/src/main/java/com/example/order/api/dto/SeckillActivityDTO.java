package com.example.order.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Schema(description = "秒杀活动信息")
public class SeckillActivityDTO {

    @Schema(description = "活动ID")
    private Long id;

    @Schema(description = "商品ID")
    private Long productId;

    @Schema(description = "活动标题")
    private String title;

    @Schema(description = "秒杀价")
    private BigDecimal seckillPrice;

    @Schema(description = "总秒杀库存")
    private Integer totalStock;

    @Schema(description = "剩余秒杀库存")
    private Integer stockLeft;

    @Schema(description = "单用户限购")
    private Integer limitPerUser;

    @Schema(description = "开始时间")
    private LocalDateTime startTime;

    @Schema(description = "结束时间")
    private LocalDateTime endTime;

    @Schema(description = "活动状态: NOT_STARTED/ONGOING/ENDED")
    private String status;
}
















