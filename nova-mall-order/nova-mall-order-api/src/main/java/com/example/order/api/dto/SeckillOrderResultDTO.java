package com.example.order.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Schema(description = "秒杀下单结果")
public class SeckillOrderResultDTO {

    @Schema(description = "活动ID")
    private Long activityId;

    @Schema(description = "订单ID")
    private Long orderId;

    @Schema(description = "订单号")
    private String orderNo;

    @Schema(description = "下单状态，SUCCESS/REPEAT/LIMIT/FAILED/PENDING")
    private String status;

    @Schema(description = "提示信息")
    private String message;

    @Schema(description = "购买数量")
    private Integer quantity;

    @Schema(description = "应付金额（秒杀价*数量）")
    private BigDecimal payAmount;
}



















