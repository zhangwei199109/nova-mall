package com.example.pay.api;

import com.example.common.dto.Result;
import com.example.pay.api.dto.RefundCreateRequest;
import com.example.pay.api.dto.RefundDTO;
import com.example.pay.api.dto.RefundCallbackRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

@Tag(name = "退款", description = "退款发起与回调")
@RequestMapping("/refund")
public interface RefundApi {

    @Operation(summary = "发起退款")
    @PostMapping
    Result<RefundDTO> create(@RequestHeader(value = "Idempotency-Key", required = false) String idemKey,
                             @Valid @RequestBody RefundCreateRequest req);

    @Operation(summary = "退款回调（幂等）")
    @PostMapping("/{refundNo}/callback")
    Result<RefundDTO> callback(@PathVariable("refundNo") String refundNo,
                               @RequestHeader(value = "Idempotency-Key", required = false) String callbackKey,
                               @Valid @RequestBody RefundCallbackRequest req);

    @Operation(summary = "查询退款单")
    @GetMapping("/{refundNo}")
    Result<RefundDTO> detail(@PathVariable("refundNo") String refundNo);
}













