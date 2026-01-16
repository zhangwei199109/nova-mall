package com.example.pay.service.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "pay.channel")
@Data
public class PayChannelProperties {
    private Alipay alipay = new Alipay();
    private Wechat wechat = new Wechat();
    private Unionpay unionpay = new Unionpay();

    @Data
    public static class Alipay {
        private boolean enabled = false;
        private String gateway = "https://openapi.alipay.com/gateway.do";
        private String appId;
        private String privateKey;
        private String alipayPublicKey;
        private String signType = "RSA2";
        private String charset = "UTF-8";
        private String notifyUrl;
        private String returnUrl;
    }

    @Data
    public static class Wechat {
        private boolean enabled = false;
        private String mchId;
        private String appId;
        private String apiV3Key;
        private String notifyUrl;
        private String h5ReturnUrl;
    }

    @Data
    public static class Unionpay {
        private boolean enabled = false;
        private String merchantId;
        private String frontUrl;
        private String backUrl;
        private String certPath;
        private String certPwd;
    }
}

