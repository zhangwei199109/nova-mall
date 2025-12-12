package com.example.stock.api;

import com.example.common.dto.Result;
import com.example.stock.api.dto.StockChangeDTO;
import com.example.stock.api.dto.StockDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

@Tag(name = "库存接口")
@RequestMapping("/stock")
@FeignClient(name = "stock-service", url = "${service.stock.base-url:http://localhost:8087}")
public interface StockApi {

    @Operation(summary = "查询商品库存")
    @GetMapping("/{productId}")
    Result<StockDTO> get(@PathVariable("productId") Long productId);

    @Operation(summary = "预占库存")
    @PostMapping("/reserve")
    Result<Boolean> reserve(@Valid @RequestBody StockChangeDTO dto);

    @Operation(summary = "释放预占库存")
    @PostMapping("/release")
    Result<Boolean> release(@Valid @RequestBody StockChangeDTO dto);

    @Operation(summary = "扣减库存（从预占转实际扣减）")
    @PostMapping("/deduct")
    Result<Boolean> deduct(@Valid @RequestBody StockChangeDTO dto);
}



