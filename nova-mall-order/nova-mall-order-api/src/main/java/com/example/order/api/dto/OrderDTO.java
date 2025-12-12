package com.example.order.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
@Schema(description = "订单数据传输对象")
public class OrderDTO {

    @Schema(description = "订单ID", example = "1")
    private Long id;

    @Schema(description = "订单号", example = "ORD-20240101-0001")
    private String orderNo;

    @Schema(description = "用户ID", example = "1")
    private Long userId;

    @Schema(description = "订单金额", example = "199.99")
    private BigDecimal amount;

    @Schema(description = "订单状态", example = "CREATED")
    private String status;

    @Schema(description = "订单项列表")
    private List<OrderItemDTO> items;
}

