package com.example.order.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
@Schema(description = "秒杀下单请求")
public class SeckillPlaceRequest {

    @Schema(description = "活动ID", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "活动ID不能为空")
    private Long activityId;

    @Schema(description = "购买数量，默认为1", example = "1")
    @Min(value = 1, message = "数量至少为1")
    private Integer quantity = 1;
}



















