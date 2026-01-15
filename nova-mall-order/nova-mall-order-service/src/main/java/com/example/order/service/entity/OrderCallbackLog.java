package com.example.order.service.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/** 订单回调日志，记录幂等回调键与创建时间，用于防重。 */
@Data
@TableName("order_callback_log")
public class OrderCallbackLog {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 关联订单 ID */
    private Long orderId;

    /** 回调幂等键（如支付/库存回调标识） */
    private String callbackKey;

    /** 创建时间 */
    private LocalDateTime createTime;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getOrderId() {
        return orderId;
    }

    public void setOrderId(Long orderId) {
        this.orderId = orderId;
    }

    public String getCallbackKey() {
        return callbackKey;
    }

    public void setCallbackKey(String callbackKey) {
        this.callbackKey = callbackKey;
    }

    public LocalDateTime getCreateTime() {
        return createTime;
    }

    public void setCreateTime(LocalDateTime createTime) {
        this.createTime = createTime;
    }
}



