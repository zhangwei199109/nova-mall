package com.example.ai.retrieve;

import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Locale;

@Component
public class FaqRetriever {

    private static final List<RetrievedDoc> FAQ = List.of(
            new RetrievedDoc("支付", "支付失败/重复支付如何处理？请确认订单状态；如已扣款未出单，可稍后再查或联系客服人工处理。"),
            new RetrievedDoc("取消", "未发货订单可在订单详情页取消；支付成功后取消将同步释放库存。"),
            new RetrievedDoc("物流", "发货后可在订单详情查看物流单号，通常 24 小时内更新轨迹。"),
            new RetrievedDoc("退货", "签收后 7 天内支持申请退货，需保持商品完好并提交退货申请。"),
            new RetrievedDoc("库存", "库存以下单时为准，若提示库存不足，可尝试减少数量或选择其他仓配。"),
            new RetrievedDoc("发票", "下单时可选择是否开具电子发票，开票邮箱请确保正确。")
    );

    public List<RetrievedDoc> retrieve(String question, int topK) {
        if (question == null || question.isBlank()) {
            return List.of();
        }
        String q = question.toLowerCase(Locale.ROOT);
        return FAQ.stream()
                .filter(doc -> q.contains(doc.title().toLowerCase(Locale.ROOT)))
                .limit(topK <= 0 ? 3 : topK)
                .toList();
    }
}

