package com.example.ops.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Schema(description = "运营后台-秒杀活动配置")
public class ActivityAdminDTO {
    @Schema(description = "活动ID，更新时必填")
    private Long id;

    @Schema(description = "商品ID", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "商品ID不能为空")
    private Long productId;

    @Schema(description = "标题", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "标题不能为空")
    private String title;

    @Schema(description = "秒杀价", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "秒杀价不能为空")
    private BigDecimal seckillPrice;

    @Schema(description = "总库存", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "总库存不能为空")
    @Min(value = 1, message = "总库存至少为1")
    private Integer totalStock;

    @Schema(description = "单用户限购", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "限购不能为空")
    @Min(value = 1, message = "限购至少为1")
    private Integer limitPerUser;

    @Schema(description = "开始时间", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "开始时间不能为空")
    private LocalDateTime startTime;

    @Schema(description = "结束时间", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "结束时间不能为空")
    private LocalDateTime endTime;

    @Schema(description = "状态 ONLINE/OFFLINE，创建时默认 OFFLINE")
    private String status;
}

















