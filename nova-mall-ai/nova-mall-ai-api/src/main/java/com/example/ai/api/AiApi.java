package com.example.ai.api;

import com.example.ai.api.dto.QaRequest;
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
}

