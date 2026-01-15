package com.example.pay.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "支付回调请求（mock）")
public class PaymentCallbackRequest {
    @Schema(description = "渠道回执码", example = "SUCCESS")
    private String channelCode;

    @Schema(description = "渠道消息", example = "ok")
    private String message;
}










