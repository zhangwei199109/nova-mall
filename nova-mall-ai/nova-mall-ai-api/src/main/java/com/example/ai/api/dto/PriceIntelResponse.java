package com.example.ai.api.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

/**
 * 价格智能体调价建议输出。
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PriceIntelResponse {
    /** 动作：HOLD / DECREASE / INCREASE */
    private String action;
    /** 建议价格（四舍五入到分） */
    private BigDecimal suggestedPrice;
    /** 相对原价变化比例，正为涨，负为降，已取绝对值 */
    private BigDecimal pctChange;
    /** 决策原因 */
    private List<String> reasons;
}



