package com.example.user.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
@Schema(description = "收货地址")
public class AddressDTO {

    @Schema(description = "地址ID")
    private Long id;

    @Schema(description = "用户ID", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "用户ID不能为空")
    private Long userId;

    @Schema(description = "收件人姓名", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "收件人姓名不能为空")
    private String receiverName;

    @Schema(description = "手机号", requiredMode = Schema.RequiredMode.REQUIRED, example = "13800000000")
    @NotBlank(message = "手机号不能为空")
    private String mobile;

    @Schema(description = "省")
    private String province;

    @Schema(description = "市")
    private String city;

    @Schema(description = "区/县")
    private String district;

    @Schema(description = "详细地址", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "详细地址不能为空")
    private String detail;

    @Schema(description = "邮编")
    private String zipCode;

    @Schema(description = "是否默认地址 1=是,0=否")
    private Integer isDefault;
}


