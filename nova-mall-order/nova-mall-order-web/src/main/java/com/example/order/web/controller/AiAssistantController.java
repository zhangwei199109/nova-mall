package com.example.order.web.controller;

import com.example.common.dto.Result;
import com.example.order.web.dto.QaRequest;
import com.example.order.web.service.AiQaService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/ai")
@Tag(name = "智能客服/问答", description = "检索 + LLM 风格回答（当前内置轻量实现，可替换真实大模型）")
public class AiAssistantController {

    private final AiQaService aiQaService;

    public AiAssistantController(AiQaService aiQaService) {
        this.aiQaService = aiQaService;
    }

    @Operation(summary = "问答", description = "输入问题，检索 FAQ 并通过 LLM 风格生成回答")
    @PostMapping("/qa")
    public Result<String> qa(@Valid @RequestBody QaRequest req) {
        return Result.success(aiQaService.answer(req.getQuestion()));
    }
}

