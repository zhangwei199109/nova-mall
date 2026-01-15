package com.example.ops.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/** 秒杀活动表，供运营配置活动信息与库存。 */
@Data
@TableName("seckill_activities")
public class OpsSeckillActivity {
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 关联商品 ID */
    private Long productId;

    /** 活动标题 */
    private String title;

    /** 秒杀价（单价） */
    private BigDecimal seckillPrice;

    /** 活动库存总量 */
    private Integer totalStock;

    /** 单用户限购数量 */
    private Integer limitPerUser;

    /** 活动开始时间 */
    private LocalDateTime startTime;

    /** 活动结束时间 */
    private LocalDateTime endTime;

    /**
     * 状态：ONLINE / OFFLINE
     */
    private String status;

    /** 创建时间 */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    /** 最近更新时间 */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    /** 逻辑删除标记 */
    @TableLogic
    private Integer deleted;
}
















