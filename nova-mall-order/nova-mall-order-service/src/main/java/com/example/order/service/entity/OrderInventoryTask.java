package com.example.order.service.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("order_inventory_tasks")
public class OrderInventoryTask {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long orderId;

    private Long productId;

    private Integer quantity;

    /**
     * INIT / SUCCESS / FAILED
     */
    private String status;

    /**
     * 重试次数
     */
    private Integer retryCount;

    private String lastError;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}


