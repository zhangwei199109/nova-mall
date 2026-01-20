package com.example.ai.api.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

/**
 * 价格/竞争情报输入参数。
 */
@Data
public class PriceIntelRequest {

    /** 当前售价 */
    @NotNull
    @DecimalMin(value = "0.01", message = "售价需大于0")
    private BigDecimal currentPrice;

    /** 基线转化率（历史或品类均值），0-1 */
    @NotNull
    private BigDecimal baselineConversion;

    /** 当前观测转化率，0-1 */
    @NotNull
    private BigDecimal conversionRate;

    /** 竞品价格列表（同品类/同档位），至少1条 */
    @NotEmpty
    private List<BigDecimal> competitorPrices;

    /** 成本/安全底价，可选 */
    private BigDecimal floorPrice;
}



