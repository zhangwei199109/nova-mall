package com.example.pay.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Schema(description = "退款发起请求")
public class RefundCreateRequest {
    @NotBlank
    @Schema(description = "支付单号", requiredMode = Schema.RequiredMode.REQUIRED)
    private String payNo;

    @NotNull
    @DecimalMin(value = "0.01", message = "退款金额必须大于0")
    @Schema(description = "退款金额", example = "9.99")
    private BigDecimal amount;

    @Schema(description = "退款原因", example = "用户申请")
    private String reason;
}













