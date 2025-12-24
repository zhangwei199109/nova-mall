package com.example.ai.api;

import com.example.ai.api.dto.QaRequest;
import com.example.ai.api.dto.ProductCopyRequest;
import com.example.ai.api.dto.ProductCopyResponse;
import com.example.ai.api.dto.SemanticSearchRequest;
import com.example.ai.api.dto.SemanticSearchResult;
import com.example.common.dto.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyEmitter;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.List;

/**
 * AI 客服/问答对外接口契约（文档定义）。实现由 web 模块提供。
 */
@Tag(name = "AI 接口")
@RequestMapping("/ai")
public interface AiApi {

    @Operation(summary = "问答", description = "同步返回完整回答")
    @PostMapping(value = "/qa", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    Result<String> qa(@Valid @RequestBody QaRequest req);

    @Operation(summary = "问答-流式(POST SSE)", description = "以 SSE(text/event-stream) 流式返回答案增量")
    @PostMapping(value = "/qa/stream", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    SseEmitter qaStream(@Valid @RequestBody QaRequest req);

    @Operation(summary = "问答-流式(GET SSE)", description = "GET SSE(text/event-stream)，便于浏览器原生 EventSource 使用")
    @GetMapping(value = "/qa/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    SseEmitter qaStreamGet(@RequestParam("question") String question);

    @Operation(summary = "问答-流式(POST chunked)", description = "chunked JSON 行流，兼容 fetch/ReadableStream")
    @PostMapping(value = "/qa/stream-chunk", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    ResponseBodyEmitter qaStreamChunk(@Valid @RequestBody QaRequest req);

    @Operation(summary = "商品文案生成", description = "输入商品要素，生成标题/卖点/描述/SEO 关键词")
    @PostMapping(value = "/product/copy", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    Result<ProductCopyResponse> generateProductCopy(@Valid @RequestBody ProductCopyRequest req);

    @Operation(summary = "商品语义搜索/推荐", description = "自然语言找商品，返回 TopK 语义相关结果")
    @PostMapping(value = "/product/search", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    Result<List<SemanticSearchResult>> semanticSearch(@Valid @RequestBody SemanticSearchRequest req);
}

