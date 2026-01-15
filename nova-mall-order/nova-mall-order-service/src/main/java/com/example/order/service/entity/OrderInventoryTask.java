package com.example.order.service.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 订单库存扣减任务表，用于异步/补偿扣减商品库存。
 */
@Data
@TableName("order_inventory_tasks")
public class OrderInventoryTask {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 对应订单 ID */
    private Long orderId;

    /** 商品 ID */
    private Long productId;

    /** 待扣减数量 */
    private Integer quantity;

    /** 任务状态：INIT / SUCCESS / FAILED */
    private String status;

    /** 重试次数 */
    private Integer retryCount;

    /** 最近一次失败原因 */
    private String lastError;

    /** 创建时间 */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    /** 最近更新时间 */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}



