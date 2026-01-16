package com.example.ai.service;

import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;

/**
 * 价格/竞争情报智能体（轻量规则版）：
 * - 监控同类商品价格与自身转化
 * - 给出调价建议（降价/持平/小幅提价）并限制安全幅度
 *
 * 仅为示例，可替换为线上监控与 A/B 反馈驱动的模型。
 */
@Component
public class PriceIntelAgent {

    /** 最大降价幅度 8%，最大涨价幅度 5%（避免过度波动）。 */
    private static final BigDecimal MAX_DISCOUNT = new BigDecimal("0.08");
    private static final BigDecimal MAX_MARKUP = new BigDecimal("0.05");
    /** 当转化率低于基线 10% 且价高于竞品均价时，优先降价。 */
    private static final BigDecimal CONVERSION_DROP = new BigDecimal("0.10");
    /** 当转化率高于基线 8% 且价明显低于竞品时，可尝试小幅提价。 */
    private static final BigDecimal CONVERSION_GAIN = new BigDecimal("0.08");
    /** “明显低/高于竞品均价”的阈值比例。 */
    private static final BigDecimal GAP_THRESHOLD = new BigDecimal("0.03");

    public PriceAdvice advise(PriceContext ctx) {
        List<String> reasons = new ArrayList<>();
        BigDecimal cur = ctx.currentPrice();
        BigDecimal avgComp = average(ctx.competitorPrices());

        BigDecimal convDelta = ctx.conversionRate().subtract(ctx.baselineConversion());
        BigDecimal suggested = cur;
        Action action = Action.HOLD;

        // 情况 1：转化显著低于基线，且当前价高于竞品均价 -> 建议降价
        if (convDelta.compareTo(CONVERSION_DROP.negate()) < 0 && cur.compareTo(avgComp) > 0) {
            BigDecimal gapPct = pctGap(cur, avgComp);
            BigDecimal cut = gapPct.min(MAX_DISCOUNT);
            suggested = applyDelta(cur, cut.negate());
            reasons.add("转化低于基线且价格高于竞品均价，建议小幅降价");
            action = Action.DECREASE;
        }

        // 情况 2：转化高于基线，且明显低于竞品，可小幅提价
        if (action == Action.HOLD && convDelta.compareTo(CONVERSION_GAIN) > 0 && pctGap(avgComp, cur).compareTo(GAP_THRESHOLD) > 0) {
            BigDecimal gapPct = pctGap(avgComp, cur); // 当前价低于竞品多少
            BigDecimal up = gapPct.min(MAX_MARKUP);
            suggested = applyDelta(cur, up);
            reasons.add("转化高于基线且价格明显低于竞品，建议尝试小幅提价");
            action = Action.INCREASE;
        }

        // 情况 3：转化下滑但价与竞品相近，建议保持或做非价格优化
        if (action == Action.HOLD && convDelta.compareTo(CONVERSION_DROP.negate()) < 0) {
            reasons.add("转化下滑但价格与竞品接近，优先排查流量/货描/服务");
        }

        // 情况 4：默认保持
        if (reasons.isEmpty()) {
            reasons.add("价格与竞品相当，转化稳定，保持不变");
        }

        // 保证不低于成本底价（如提供）
        if (ctx.floorPrice() != null && suggested.compareTo(ctx.floorPrice()) < 0) {
            suggested = ctx.floorPrice();
            reasons.add("已触达底价，避免穿底");
            action = Action.HOLD;
        }

        BigDecimal pctChange = pctGap(cur, suggested);
        return new PriceAdvice(action, suggested, pctChange, reasons);
    }

    private BigDecimal average(List<BigDecimal> prices) {
        if (prices == null || prices.isEmpty()) return BigDecimal.ZERO;
        BigDecimal sum = BigDecimal.ZERO;
        for (BigDecimal p : prices) {
            if (p != null) {
                sum = sum.add(p);
            }
        }
        return sum.divide(new BigDecimal(prices.size()), 4, RoundingMode.HALF_UP);
    }

    /**
     * pct gap = (a - b) / b ，用于评估相对差异（可为负）。
     */
    private BigDecimal pctGap(BigDecimal a, BigDecimal b) {
        if (b == null || b.compareTo(BigDecimal.ZERO) == 0 || a == null) {
            return BigDecimal.ZERO;
        }
        return a.subtract(b).divide(b, 4, RoundingMode.HALF_UP).abs();
    }

    /**
     * 按比例增减，比例可正可负。
     */
    private BigDecimal applyDelta(BigDecimal base, BigDecimal deltaPct) {
        if (base == null) return BigDecimal.ZERO;
        return base.multiply(BigDecimal.ONE.add(deltaPct)).setScale(2, RoundingMode.HALF_UP);
    }

    public enum Action {
        HOLD, DECREASE, INCREASE
    }

    /**
     * 输入上下文。
     * @param currentPrice 当前售价
     * @param baselineConversion 基线转化率（历史或同品类均值），0-1
     * @param conversionRate 当前观测转化率，0-1
     * @param competitorPrices 竞品价格列表
     * @param floorPrice 成本/安全底价，可选
     */
    public record PriceContext(
            BigDecimal currentPrice,
            BigDecimal baselineConversion,
            BigDecimal conversionRate,
            List<BigDecimal> competitorPrices,
            BigDecimal floorPrice
    ) {}

    /**
     * 输出建议。
     * @param action 调价动作
     * @param suggestedPrice 建议价格
     * @param pctChange 相对原价变化比例（正为涨，负为降），绝对值已取 abs
     * @param reasons 决策原因
     */
    public record PriceAdvice(
            Action action,
            BigDecimal suggestedPrice,
            BigDecimal pctChange,
            List<String> reasons
    ) {}
}

