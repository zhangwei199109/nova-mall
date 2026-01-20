package com.example.pay.service.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("refund_callback_logs")
public class RefundCallbackLog {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String refundNo;
    private String callbackKey;
    private String rawMessage;
    private LocalDateTime createTime;
}













