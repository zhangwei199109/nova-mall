package com.example.ai.retrieve;
import com.example.ai.api.dto.SemanticSearchResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 简易商品“语义”检索（关键词/标签匹配），可后续替换为向量检索。
 */
@Component
public class ProductRetriever {

    private static final Logger log = LoggerFactory.getLogger(ProductRetriever.class);

    private static final List<ProductDoc> CATALOG = List.of(
            new ProductDoc("sku-1001", "轻盈透气跑步鞋", "飞织鞋面，缓震中底，日常跑步与通勤皆可", List.of("运动", "跑鞋", "透气", "轻便")),
            new ProductDoc("sku-1002", "便携降噪蓝牙耳机", "主动降噪，40h 续航，支持多设备切换", List.of("耳机", "降噪", "蓝牙5.3", "长续航")),
            new ProductDoc("sku-1003", "立体裁剪西装外套", "羊毛混纺，商务休闲双场景，抗皱易打理", List.of("西装", "商务", "抗皱", "秋冬")),
            new ProductDoc("sku-1004", "户外冲锋衣三合一", "防水透气面料，可拆卸抓绒内胆，四季通用", List.of("户外", "防水", "冲锋衣", "保暖")),
            new ProductDoc("sku-1005", "原味坚果每日坚果30包", "6种坚果科学配比，独立小袋便携分享", List.of("零食", "坚果", "独立包装", "健康"))
    );
    private static final Map<String, ProductDoc> CATALOG_MAP = CATALOG.stream()
            .collect(Collectors.toMap(doc -> doc.id, doc -> doc));

    /**
     * 简单打分：查询词拆分后在名称/摘要/标签中的命中次数；若无空格（纯中文）则用整句匹配兜底，避免总是空。
     */
    public List<SemanticSearchResult> search(String query, int topK) {
        String q = normalize(query);
        if (q.isBlank()) {
            return List.of();
        }
        // 按空白/标点拆分，若全部空则使用整句作为 token（兼容中文无空格）
        String[] tokens = q.split("[\\s,;，。.!！？]+");
        boolean allBlank = true;
        for (String tk : tokens) {
            if (tk != null && !tk.isBlank()) {
                allBlank = false;
                break;
            }
        }
        if (allBlank) {
            tokens = new String[]{q};
        }
        List<SemanticSearchResult> scored = new ArrayList<>();
        for (ProductDoc doc : CATALOG) {
            int hit = 0;
            List<String> matched = new ArrayList<>();
            for (String tk : tokens) {
                if (tk.isBlank()) continue;
                // 命中规则：
                // - token 包含在名称/描述/标签
                // - 或标签包含在 token（处理中文长句包含短标签的情况，如“通勤用的轻便跑鞋”包含“轻便”）
                boolean tokenHit = doc.name.contains(tk) || doc.desc.contains(tk)
                        || doc.tags.stream().anyMatch(tag -> tag.contains(tk) || tk.contains(tag));
                if (tokenHit) {
                    hit++;
                    matched.add(tk);
                }
            }
            // 额外包含度：整句包含时给一定权重，避免完全 0 分
            double containScore = 0;
            if (doc.name.contains(q)) {
                containScore += 1.0;
            }
            if (doc.desc.contains(q)) {
                containScore += 0.5;
            }
            if (doc.tags.stream().anyMatch(tag -> tag.contains(q))) {
                containScore += 0.7;
            }
            double score = hit + jaccard(doc.tags, tokens) * 0.5 + containScore;
            if (score > 0) {
                scored.add(new SemanticSearchResult(doc.id, doc.nameRaw, doc.descRaw, score,
                        matched.isEmpty() ? "标签相似" : "匹配关键词：" + String.join("、", matched)));
            }
        }
        List<SemanticSearchResult> sorted = scored.stream()
                .sorted(Comparator.comparingDouble(SemanticSearchResult::getScore).reversed())
                .limit(topK)
                .collect(Collectors.toList());
        // 若无任何匹配，返回默认推荐，避免前端空列表体验
        if (sorted.isEmpty()) {
            List<SemanticSearchResult> fallback = CATALOG.stream()
                    .limit(topK)
                    .map(doc -> new SemanticSearchResult(doc.id, doc.nameRaw, doc.descRaw, 0.01, "默认推荐"))
                    .collect(Collectors.toList());
            log.info("product-search fallback empty-hit query={} topK={} resultCount={}", query, topK, fallback.size());
            return fallback;
        }
        log.info("product-search hit query={} topK={} resultCount={}", query, topK, sorted.size());
        return sorted;
    }

    /**
     * 基于商品ID或标签偏好做相似/猜你喜欢推荐。
     */
    public List<SemanticSearchResult> recommendSimilar(String productId, List<String> preferTags, int topK) {
        ProductDoc seed = CATALOG_MAP.get(productId);
        Set<String> tagSet = normalizeTags(preferTags);
        if (seed != null) {
            if (tagSet.isEmpty() && seed.tags != null) {
                tagSet = seed.tags.stream().map(t -> t.toLowerCase(Locale.ROOT)).collect(Collectors.toSet());
            }
        }
        List<SemanticSearchResult> scored = new ArrayList<>();
        for (ProductDoc doc : CATALOG) {
            if (seed != null && doc.id.equals(seed.id)) {
                continue; // 不推荐自身
            }
            double score = 0;
            StringBuilder reason = new StringBuilder();
            if (seed != null) {
                double tagScore = jaccardTags(seed.tags, doc.tags);
                if (tagScore > 0) {
                    score += tagScore * 2.0;
                    reason.append("与参考商品标签相似;");
                }
                if (doc.desc.contains(seed.name) || doc.name.contains(seed.name)) {
                    score += 0.5;
                }
            }
            if (!tagSet.isEmpty()) {
                double preferScore = jaccard(tagSet, doc.tags);
                if (preferScore > 0) {
                    score += preferScore * 1.5;
                    reason.append("匹配偏好标签;");
                }
            }
            if (score > 0) {
                scored.add(new SemanticSearchResult(doc.id, doc.nameRaw, doc.descRaw, score,
                        reason.isEmpty() ? "相似推荐" : reason.toString()));
            }
        }
        List<SemanticSearchResult> sorted = scored.stream()
                .sorted(Comparator.comparingDouble(SemanticSearchResult::getScore).reversed())
                .limit(topK <= 0 ? 5 : topK)
                .collect(Collectors.toList());
        if (sorted.isEmpty()) {
            return CATALOG.stream()
                    .filter(doc -> seed == null || !doc.id.equals(seed.id))
                    .limit(topK <= 0 ? 5 : topK)
                    .map(doc -> new SemanticSearchResult(doc.id, doc.nameRaw, doc.descRaw, 0.01, "默认推荐"))
                    .collect(Collectors.toList());
        }
        return sorted;
    }

    private double jaccard(List<String> tags, String[] tokens) {
        if (tags == null || tokens == null || tags.isEmpty()) return 0;
        List<String> tokenList = new ArrayList<>();
        for (String tk : tokens) {
            if (tk != null && !tk.isBlank()) {
                tokenList.add(tk.toLowerCase(Locale.ROOT));
            }
        }
        if (tokenList.isEmpty()) return 0;
        var tagSet = tags.stream().map(t -> t.toLowerCase(Locale.ROOT)).collect(Collectors.toSet());
        var tokenSet = tokenList.stream().collect(Collectors.toSet());
        var union = new java.util.HashSet<>(tagSet);
        union.addAll(tokenSet);
        var inter = new java.util.HashSet<>(tagSet);
        inter.retainAll(tokenSet);
        if (union.isEmpty()) return 0;
        return (double) inter.size() / union.size();
    }

    private double jaccard(Set<String> prefer, List<String> tags) {
        if (prefer == null || prefer.isEmpty() || tags == null || tags.isEmpty()) return 0;
        var tagSet = tags.stream().map(t -> t.toLowerCase(Locale.ROOT)).collect(Collectors.toSet());
        var union = new java.util.HashSet<>(tagSet);
        union.addAll(prefer);
        var inter = new java.util.HashSet<>(tagSet);
        inter.retainAll(prefer);
        if (union.isEmpty()) return 0;
        return (double) inter.size() / union.size();
    }

    private double jaccardTags(List<String> a, List<String> b) {
        if (a == null || b == null || a.isEmpty() || b.isEmpty()) return 0;
        var sa = a.stream().map(t -> t.toLowerCase(Locale.ROOT)).collect(Collectors.toSet());
        var sb = b.stream().map(t -> t.toLowerCase(Locale.ROOT)).collect(Collectors.toSet());
        var union = new java.util.HashSet<>(sa);
        union.addAll(sb);
        var inter = new java.util.HashSet<>(sa);
        inter.retainAll(sb);
        if (union.isEmpty()) return 0;
        return (double) inter.size() / union.size();
    }

    private Set<String> normalizeTags(List<String> tags) {
        if (tags == null || tags.isEmpty()) return Set.of();
        return tags.stream()
                .filter(t -> t != null && !t.isBlank())
                .map(t -> t.toLowerCase(Locale.ROOT).trim())
                .collect(Collectors.toSet());
    }

    private String normalize(String q) {
        if (q == null) return "";
        return q.toLowerCase(Locale.ROOT).trim();
    }

    private static class ProductDoc {
        private final String id;
        private final String nameRaw;
        private final String descRaw;
        private final List<String> tags;
        private final String name;
        private final String desc;

        private ProductDoc(String id, String nameRaw, String descRaw, List<String> tags) {
            this.id = id;
            this.nameRaw = nameRaw;
            this.descRaw = descRaw;
            this.tags = tags;
            this.name = nameRaw.toLowerCase(Locale.ROOT);
            this.desc = descRaw.toLowerCase(Locale.ROOT);
        }
    }
}

