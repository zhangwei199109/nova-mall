package com.example.pay.api;

import com.example.common.dto.Result;
import com.example.pay.api.dto.PaymentCreateRequest;
import com.example.pay.api.dto.PaymentDTO;
import com.example.pay.api.dto.PaymentCallbackRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

@Tag(name = "支付", description = "支付发起与回调")
@RequestMapping("/pay")
public interface PaymentApi {

    @Operation(summary = "发起支付")
    @PostMapping
    Result<PaymentDTO> create(@RequestHeader(value = "Idempotency-Key", required = false) String idemKey,
                              @Valid @RequestBody PaymentCreateRequest req);

    @Operation(summary = "支付回调（幂等）")
    @PostMapping("/{payNo}/callback")
    Result<PaymentDTO> callback(@PathVariable("payNo") String payNo,
                                @RequestHeader(value = "Idempotency-Key", required = false) String callbackKey,
                                @Valid @RequestBody PaymentCallbackRequest req);

    @Operation(summary = "查询支付单")
    @GetMapping("/{payNo}")
    Result<PaymentDTO> detail(@PathVariable("payNo") String payNo);
}

