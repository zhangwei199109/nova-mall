package com.example.order.service.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("order_callback_log")
public class OrderCallbackLog {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long orderId;

    private String callbackKey;

    private LocalDateTime createTime;
}

