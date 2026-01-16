package com.example.pay.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Schema(description = "支付发起请求")
public class PaymentCreateRequest {
    @NotNull
    @Schema(description = "订单ID", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long orderId;

    @NotNull
    @DecimalMin(value = "0.01", message = "金额必须大于0")
    @Schema(description = "支付金额", example = "99.99")
    private BigDecimal amount;

    @NotBlank
    @Schema(description = "支付渠道", example = "ALIPAY/WECHAT/UNIONPAY")
    private String channel;
}











