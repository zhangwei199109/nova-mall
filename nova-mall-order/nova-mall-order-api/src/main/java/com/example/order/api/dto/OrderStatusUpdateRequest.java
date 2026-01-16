package com.example.order.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
@Schema(description = "订单状态更新请求（内部）")
public class OrderStatusUpdateRequest {
    @NotBlank
    @Schema(description = "新状态，如 PAID/REFUNDED")
    private String status;

    @Schema(description = "可选原因或备注")
    private String reason;
}











