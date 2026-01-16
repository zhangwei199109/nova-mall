package com.example.ai.controller;

import com.example.ai.api.AiApi;
import com.example.ai.api.dto.QaRequest;
import com.example.ai.api.dto.ProductCopyRequest;
import com.example.ai.api.dto.ProductCopyResponse;
import com.example.ai.api.dto.SemanticSearchRequest;
import com.example.ai.api.dto.SemanticSearchResult;
import com.example.ai.api.dto.RecommendRequest;
import com.example.ai.service.AiProductService;
import com.example.ai.service.AiQaService;
import com.example.ai.retrieve.RetrievedDoc;
import com.example.common.dto.Result;
import jakarta.validation.Valid;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyEmitter;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;

@RestController
@RequestMapping("/ai")
public class AiAssistantController implements AiApi {

    private final AiQaService aiQaService;
    private final AiProductService aiProductService;

    public AiAssistantController(AiQaService aiQaService, AiProductService aiProductService) {
        this.aiQaService = aiQaService;
        this.aiProductService = aiProductService;
    }

    @PostMapping("/qa")
    @Override
    public Result<String> qa(@Valid @RequestBody QaRequest req) {
        return Result.success(aiQaService.answer(req.getQuestion()));
    }

    @PostMapping(value = "/qa/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    @Override
    public SseEmitter qaStream(@Valid @RequestBody QaRequest req) {
        SseEmitter emitter = new SseEmitter(60_000L);
        CompletableFuture.runAsync(() -> {
            AtomicBoolean done = new AtomicBoolean(false);
            String requestId = UUID.randomUUID().toString();
            AtomicLong seq = new AtomicLong(0);
            try {
                trySendSse(emitter, done, seq, requestId, "status", "processing", false, false);
                List<RetrievedDoc> contexts = aiQaService.retrieveContexts(req.getQuestion());
                boolean streamed = aiQaService.streamAnswer(req.getQuestion(), contexts,
                        chunk -> sendChunkStreaming(emitter, done, seq, requestId, "answer", chunk));
                if (!streamed) {
                    String answer = aiQaService.answerWithContexts(req.getQuestion(), contexts);
                    streamLocalChunks(emitter, done, seq, requestId, answer);
                }
                sendDoneSse(emitter, done, seq, requestId);
            } catch (Exception ex) {
                sendDoneSse(emitter, done, seq, requestId);
            }
        });
        return emitter;
    }

    @GetMapping(value = "/qa/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    @Override
    public SseEmitter qaStreamGet(@RequestParam("question") String question) {
        SseEmitter emitter = new SseEmitter(60_000L);
        CompletableFuture.runAsync(() -> {
            AtomicBoolean done = new AtomicBoolean(false);
            String requestId = UUID.randomUUID().toString();
            AtomicLong seq = new AtomicLong(0);
            try {
                trySendSse(emitter, done, seq, requestId, "status", "processing", false, false);
                List<RetrievedDoc> contexts = aiQaService.retrieveContexts(question);
                boolean streamed = aiQaService.streamAnswer(question, contexts,
                        chunk -> sendChunkStreaming(emitter, done, seq, requestId, "answer", chunk));
                if (!streamed) {
                    String answer = aiQaService.answerWithContexts(question, contexts);
                    streamLocalChunks(emitter, done, seq, requestId, answer);
                }
                sendDoneSse(emitter, done, seq, requestId);
            } catch (Exception ex) {
                sendDoneSse(emitter, done, seq, requestId);
            }
        });
        return emitter;
    }

    @PostMapping(value = "/qa/stream-chunk", produces = MediaType.APPLICATION_JSON_VALUE)
    @Override
    public ResponseBodyEmitter qaStreamChunk(@Valid @RequestBody QaRequest req) {
        ResponseBodyEmitter emitter = new ResponseBodyEmitter(60_000L);
        CompletableFuture.runAsync(() -> {
            AtomicBoolean done = new AtomicBoolean(false);
            String requestId = UUID.randomUUID().toString();
            AtomicLong seq = new AtomicLong(0);
            try {
                trySendChunk(emitter, done, seq, requestId, "status", "processing", false, false);
                List<RetrievedDoc> contexts = aiQaService.retrieveContexts(req.getQuestion());
                boolean streamed = aiQaService.streamAnswer(req.getQuestion(), contexts,
                        chunk -> sendChunkStreaming(emitter, done, seq, requestId, "answer", chunk));
                if (!streamed) {
                    String answer = aiQaService.answerWithContexts(req.getQuestion(), contexts);
                    streamLocalChunks(emitter, done, seq, requestId, answer);
                }
                sendDoneChunk(emitter, done, seq, requestId);
            } catch (Exception ex) {
                sendDoneChunk(emitter, done, seq, requestId);
            }
        });
        return emitter;
    }

    @PostMapping(value = "/product/copy", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    @Override
    public Result<ProductCopyResponse> generateProductCopy(@Valid @RequestBody ProductCopyRequest req) {
        return Result.success(aiProductService.generateCopy(req));
    }

    @PostMapping(value = "/product/search", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    @Override
    public Result<java.util.List<SemanticSearchResult>> semanticSearch(@Valid @RequestBody SemanticSearchRequest req) {
        return Result.success(aiProductService.semanticSearch(req));
    }

    @PostMapping(value = "/product/recommend", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    @Override
    public Result<java.util.List<SemanticSearchResult>> recommend(@Valid @RequestBody RecommendRequest req) {
        return Result.success(aiProductService.recommend(req));
    }

    private boolean trySendSse(SseEmitter emitter, AtomicBoolean done, AtomicLong seq, String requestId,
                               String event, String data, boolean partial, boolean doneFlag) {
        if (done.get()) return false;
        try {
            long curSeq = seq.incrementAndGet();
            emitter.send(SseEmitter.event().name(event).data(asJson(requestId, curSeq, event, data, partial, doneFlag)));
            return true;
        } catch (Exception e) {
            done.set(true);
            tryCompleteSse(emitter, done);
            return false;
        }
    }

    private boolean trySendChunk(ResponseBodyEmitter emitter, AtomicBoolean done, AtomicLong seq, String requestId,
                                 String event, String data, boolean partial, boolean doneFlag) {
        if (done.get()) return false;
        try {
            long curSeq = seq.incrementAndGet();
            emitter.send(asJson(requestId, curSeq, event, data, partial, doneFlag) + "\n");
            return true;
        } catch (Exception e) {
            done.set(true);
            tryCompleteChunk(emitter, done);
            return false;
        }
    }

    private void sendChunkStreaming(SseEmitter emitter, AtomicBoolean done, AtomicLong seq, String requestId,
                                    String event, String data) {
        if (data == null) return;
        int size = 1;
        for (int i = 0; i < data.length(); i += size) {
            String part = data.substring(i, Math.min(data.length(), i + size));
            if (!trySendSse(emitter, done, seq, requestId, event, part, true, false)) {
                break;
            }
            try {
                TimeUnit.MILLISECONDS.sleep(20);
            } catch (InterruptedException ignored) {
                Thread.currentThread().interrupt();
                break;
            }
        }
    }

    private void sendChunkStreaming(ResponseBodyEmitter emitter, AtomicBoolean done, AtomicLong seq, String requestId,
                                    String event, String data) {
        if (data == null) return;
        int size = 1;
        for (int i = 0; i < data.length(); i += size) {
            String part = data.substring(i, Math.min(data.length(), i + size));
            if (!trySendChunk(emitter, done, seq, requestId, event, part, true, false)) {
                break;
            }
            try {
                TimeUnit.MILLISECONDS.sleep(20);
            } catch (InterruptedException ignored) {
                Thread.currentThread().interrupt();
                break;
            }
        }
    }

    private String asJson(String requestId, long seq, String event, String data, boolean partial, boolean done) {
        return "{\"requestId\":\"" + escape(requestId) + "\",\"seq\":" + seq
                + ",\"event\":\"" + escape(event) + "\",\"data\":\"" + escape(data)
                + "\",\"partial\":" + partial + ",\"done\":" + done + "}";
    }

    private String escape(String s) {
        if (s == null) return "";
        return s.replace("\\", "\\\\").replace("\"", "\\\"").replace("\n", "\\n").replace("\r", "");
    }

    private void streamLocalChunks(SseEmitter emitter, AtomicBoolean done, AtomicLong seq, String requestId, String answer) {
        if (answer == null) {
            return;
        }
        int size = 1;
        for (int i = 0; i < answer.length(); i += size) {
            String part = answer.substring(i, Math.min(answer.length(), i + size));
            if (!trySendSse(emitter, done, seq, requestId, "answer", part, false, false)) {
                break;
            }
            try {
                TimeUnit.MILLISECONDS.sleep(30);
            } catch (InterruptedException ignored) {
                Thread.currentThread().interrupt();
                break;
            }
        }
    }

    private void streamLocalChunks(ResponseBodyEmitter emitter, AtomicBoolean done, AtomicLong seq, String requestId, String answer) {
        if (answer == null) {
            return;
        }
        int size = 1;
        for (int i = 0; i < answer.length(); i += size) {
            String part = answer.substring(i, Math.min(answer.length(), i + size));
            if (!trySendChunk(emitter, done, seq, requestId, "answer", part, false, false)) {
                break;
            }
            try {
                TimeUnit.MILLISECONDS.sleep(30);
            } catch (InterruptedException ignored) {
                Thread.currentThread().interrupt();
                break;
            }
        }
    }

    private void tryCompleteSse(SseEmitter emitter, AtomicBoolean done) {
        if (done.getAndSet(true)) return;
        try {
            emitter.complete();
        } catch (Exception ignored) {
        }
    }

    private void tryCompleteChunk(ResponseBodyEmitter emitter, AtomicBoolean done) {
        if (done.getAndSet(true)) return;
        try {
            emitter.complete();
        } catch (Exception ignored) {
        }
    }

    private void sendDoneSse(SseEmitter emitter, AtomicBoolean done, AtomicLong seq, String requestId) {
        trySendSse(emitter, done, seq, requestId, "done", "ok", false, true);
        tryCompleteSse(emitter, done);
    }

    private void sendDoneChunk(ResponseBodyEmitter emitter, AtomicBoolean done, AtomicLong seq, String requestId) {
        trySendChunk(emitter, done, seq, requestId, "done", "ok", false, true);
        tryCompleteChunk(emitter, done);
    }
}

