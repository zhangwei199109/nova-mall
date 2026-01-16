package com.example.ads.service.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 广告创意表，记录素材/落地页以及追踪参数。
 */
@Data
@TableName("ad_creative")
public class Creative {
    @TableId(type = IdType.AUTO)
    /** 主键ID */
    private Long id;
    /** 所属活动ID */
    private Long campaignId;
    /** 创意标题 */
    private String title;
    /** 素材链接（图片/视频 URL） */
    private String mediaUrl;
    /** 落地页 URL */
    private String landingUrl;
    /** 追踪参数（utm 等），会拼接到落地页 */
    private String utm;
    /** 是否启用 */
    private Boolean enabled;
    /** 创建时间 */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
    /** 最近更新时间 */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}

