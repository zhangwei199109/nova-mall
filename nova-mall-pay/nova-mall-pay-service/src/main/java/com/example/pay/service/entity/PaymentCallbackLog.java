package com.example.pay.service.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("payment_callback_logs")
public class PaymentCallbackLog {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String payNo;
    private String callbackKey;
    private String rawMessage;
    private LocalDateTime createTime;
}













