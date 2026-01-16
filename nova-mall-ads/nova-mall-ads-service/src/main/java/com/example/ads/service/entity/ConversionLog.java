package com.example.ads.service.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 转化埋点表，按 traceId 与订单号落库，用于点击到订单的闭环归因。
 */
@Data
@TableName("ad_conversion_log")
public class ConversionLog {
    @TableId(type = IdType.AUTO)
    /** 主键ID */
    private Long id;
    /** 追踪ID（点击链路 traceId） */
    private String traceId;
    /** 关联活动ID（若能关联到点击） */
    private Long campaignId;
    /** 关联创意ID（若能关联到点击） */
    private Long creativeId;
    /** 关联订单号 */
    private String orderNo;
    /** 记录时间 */
    private LocalDateTime createTime;
}

