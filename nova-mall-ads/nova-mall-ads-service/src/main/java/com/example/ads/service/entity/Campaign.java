package com.example.ads.service.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 广告活动表，承载投放渠道、预算、有效期等活动维度信息。
 */
@Data
@TableName("ad_campaign")
public class Campaign {
    @TableId(type = IdType.AUTO)
    /** 主键ID */
    private Long id;
    /** 活动名称 */
    private String name;
    /** 投放渠道/分组标签，如 douyin/xhs/sem */
    private String channel;
    /** 预算（元，选填） */
    private Integer budget;
    /** 是否启用 */
    private Boolean enabled;
    /** 生效开始时间 */
    private LocalDateTime startTime;
    /** 生效结束时间 */
    private LocalDateTime endTime;
    /** 创建时间 */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
    /** 最近更新时间 */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}

