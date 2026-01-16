package com.example.ads.service.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 点击埋点表，记录 traceId 与访问端信息，便于后续归因。
 */
@Data
@TableName("ad_click_log")
public class ClickLog {
    @TableId(type = IdType.AUTO)
    /** 主键ID */
    private Long id;
    /** 追踪ID（来源于链接 traceId） */
    private String traceId;
    /** 关联活动ID（可为空） */
    private Long campaignId;
    /** 关联创意ID */
    private Long creativeId;
    /** 访问 IP */
    private String ip;
    /** 访问 UA */
    private String userAgent;
    /** 记录时间 */
    private LocalDateTime createTime;
}

