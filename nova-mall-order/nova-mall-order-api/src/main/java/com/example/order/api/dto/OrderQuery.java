package com.example.order.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "订单查询参数")
public class OrderQuery {
    @Schema(description = "订单状态，例：CREATED/PAID/CANCELLED")
    private String status;
}











