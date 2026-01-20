package com.example.pay.web.controller;

import com.example.common.dto.Result;
import com.example.pay.api.PaymentApi;
import com.example.pay.api.dto.PaymentCallbackRequest;
import com.example.pay.api.dto.PaymentCreateRequest;
import com.example.pay.api.dto.PaymentDTO;
import com.example.pay.service.PayAppService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class PaymentController implements PaymentApi {

    private final PayAppService payAppService;

    @Override
    public Result<PaymentDTO> create(String idemKey, @Valid PaymentCreateRequest req) {
        return Result.success(payAppService.createPayment(idemKey, req));
    }

    @Override
    public Result<PaymentDTO> callback(String payNo, String callbackKey, @Valid PaymentCallbackRequest req) {
        return Result.success(payAppService.callback(payNo, callbackKey, req));
    }

    @Override
    public Result<PaymentDTO> detail(String payNo) {
        return Result.success(payAppService.detail(payNo));
    }
}













