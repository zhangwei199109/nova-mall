package com.example.user.service.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("user_addresses")
public class Address {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long userId;

    private String receiverName;

    private String mobile;

    private String province;

    private String city;

    private String district;

    private String detail;

    private String zipCode;

    /**
     * 1=默认，0=非默认
     */
    private Integer isDefault;

    @TableLogic
    private Integer deleted;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}



