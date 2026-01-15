package com.example.order.service.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/** 订单明细表，记录每个订单行的商品与数量价格信息。 */
@Data
@TableName("order_items")
public class OrderItem {
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 关联的订单主表 ID */
    private Long orderId;

    /** 商品 ID */
    private Long productId;

    /** 商品名称（下单快照） */
    private String productName;

    /** 成交单价（下单快照） */
    private BigDecimal price;

    /** 购买数量 */
    private Integer quantity;

    /** 逻辑删除标记 */
    @TableLogic
    private Integer deleted;

    /** 创建时间 */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    /** 最近更新时间 */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

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

    public Long getProductId() {
        return productId;
    }

    public void setProductId(Long productId) {
        this.productId = productId;
    }

    public String getProductName() {
        return productName;
    }

    public void setProductName(String productName) {
        this.productName = productName;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }

    public Integer getQuantity() {
        return quantity;
    }

    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
    }

    public Integer getDeleted() {
        return deleted;
    }

    public void setDeleted(Integer deleted) {
        this.deleted = deleted;
    }

    public LocalDateTime getCreateTime() {
        return createTime;
    }

    public void setCreateTime(LocalDateTime createTime) {
        this.createTime = createTime;
    }

    public LocalDateTime getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(LocalDateTime updateTime) {
        this.updateTime = updateTime;
    }
}



