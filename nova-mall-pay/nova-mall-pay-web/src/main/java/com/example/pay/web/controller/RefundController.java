package com.example.pay.web.controller;

import com.example.common.dto.Result;
import com.example.pay.api.RefundApi;
import com.example.pay.api.dto.RefundCallbackRequest;
import com.example.pay.api.dto.RefundCreateRequest;
import com.example.pay.api.dto.RefundDTO;
import com.example.pay.service.PayAppService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class RefundController implements RefundApi {

    private final PayAppService payAppService;

    @Override
    public Result<RefundDTO> create(String idemKey, @Valid RefundCreateRequest req) {
        return Result.success(payAppService.createRefund(idemKey, req));
    }

    @Override
    public Result<RefundDTO> callback(String refundNo, String callbackKey, @Valid RefundCallbackRequest req) {
        return Result.success(payAppService.refundCallback(refundNo, callbackKey, req));
    }

    @Override
    public Result<RefundDTO> detail(String refundNo) {
        return Result.success(payAppService.refundDetail(refundNo));
    }
}











