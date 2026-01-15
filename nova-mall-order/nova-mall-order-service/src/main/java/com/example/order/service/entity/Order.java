package com.example.order.service.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.Version;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 订单主表，记录订单编号、用户、金额、状态以及关键时间戳。
 * 搭配 {@link com.example.order.service.entity.OrderItem} 维护订单明细。
 */
@Data
@TableName("orders")
public class Order {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 业务订单号（外显与对账用） */
    private String orderNo;

    /** 幂等键，防重复下单/支付 */
    @TableField("idem_key")
    private String idemKey;

    /** 下单用户 ID */
    private Long userId;

    /** 订单总金额（优惠后应付） */
    private BigDecimal amount;

    /** 订单状态，取值见 OrderStatus */
    private String status;

    /**
     * 发货时间，用于自动收货。
     */
    private LocalDateTime shipTime;

    /**
     * 完成时间。
     */
    private LocalDateTime finishTime;

    /** 乐观锁版本号，防并发覆盖 */
    @Version
    private Integer version;

    /** 逻辑删除标记 */
    @TableLogic
    private Integer deleted;

    /** 创建时间 */
    @TableField(value = "create_time", fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    /** 最近更新时间 */
    @TableField(value = "update_time", fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getOrderNo() {
        return orderNo;
    }

    public void setOrderNo(String orderNo) {
        this.orderNo = orderNo;
    }

    public String getIdemKey() {
        return idemKey;
    }

    public void setIdemKey(String idemKey) {
        this.idemKey = idemKey;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Integer getVersion() {
        return version;
    }

    public void setVersion(Integer version) {
        this.version = version;
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



