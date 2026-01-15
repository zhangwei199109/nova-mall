package com.example.pay.service.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("refunds")
public class Refund {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String refundNo;

    private String payNo;

    private Long orderId;

    private Long userId;

    private BigDecimal amount;

    private String channelRefundNo;

    private String currency;

    private String extra;

    private String status;

    private String reason;

    @TableField("idem_key")
    private String idemKey;

    @Version
    private Integer version;

    @TableLogic
    private Integer deleted;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}

