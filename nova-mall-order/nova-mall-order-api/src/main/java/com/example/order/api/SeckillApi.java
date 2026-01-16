package com.example.order.api;

import com.example.common.dto.Result;
import com.example.order.api.dto.SeckillActivityDTO;
import com.example.order.api.dto.SeckillOrderResultDTO;
import com.example.order.api.dto.SeckillPlaceRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;

@Tag(name = "秒杀服务", description = "热点商品秒杀（示例实现）")
@RequestMapping("/seckill")
public interface SeckillApi {

    @Operation(summary = "查询正在进行/即将开始的秒杀活动")
    @GetMapping("/activities")
    Result<List<SeckillActivityDTO>> activities();

    @Operation(summary = "提交秒杀下单（默认数量1，可配合 Idempotency-Key 幂等）")
    @PostMapping("/order")
    Result<SeckillOrderResultDTO> place(@RequestHeader(value = "Idempotency-Key", required = false) String idemKey,
                                        @Valid @RequestBody SeckillPlaceRequest req);

    @Operation(summary = "查询当前用户在某个活动下的秒杀结果")
    @GetMapping("/result/{activityId}")
    Result<SeckillOrderResultDTO> result(@PathVariable Long activityId);
}

















