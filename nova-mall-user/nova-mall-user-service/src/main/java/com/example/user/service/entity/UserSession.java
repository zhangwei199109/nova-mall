package com.example.user.service.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("user_sessions")
public class UserSession {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long userId;

    private String deviceId;

    private String refreshHash;

    private LocalDateTime expireTime;

    /**
     * 1=有效,0=已注销
     */
    private Integer active;

    @TableLogic
    private Integer deleted;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}



