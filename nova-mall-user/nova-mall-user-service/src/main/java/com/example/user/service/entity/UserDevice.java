package com.example.user.service.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("user_devices")
public class UserDevice {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long userId;

    /**
     * 设备指纹/ID
     */
    private String deviceId;

    private String platform;

    private String os;

    private String userAgent;

    private String lastIp;

    private LocalDateTime lastLoginTime;

    /**
     * 0=正常，1=封禁
     */
    private Integer status;

    @TableLogic
    private Integer deleted;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}




