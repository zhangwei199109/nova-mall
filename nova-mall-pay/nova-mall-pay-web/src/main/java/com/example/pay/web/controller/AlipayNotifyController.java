package com.example.pay.web.controller;

import com.example.pay.service.PayAppService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/pay/notify")
@RequiredArgsConstructor
public class AlipayNotifyController {

    private final PayAppService payAppService;

    /**
     * 支付宝异步通知（form-urlencoded），Mock 场景不验签
     */
    @PostMapping(value = "/alipay", consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
    public String alipayNotify(@RequestHeader(value = "Idempotency-Key", required = false) String callbackKey,
                               @RequestParam Map<String, String> params) {
        return payAppService.alipayNotify(callbackKey, params);
    }
}







