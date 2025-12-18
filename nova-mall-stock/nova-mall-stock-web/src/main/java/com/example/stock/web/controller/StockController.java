package com.example.stock.web.controller;

import com.example.common.dto.Result;
import com.example.stock.api.StockApi;
import com.example.stock.api.dto.StockDTO;
import com.example.stock.api.dto.StockChangeDTO;
import com.example.stock.service.StockAppService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class StockController implements StockApi {

    private final StockAppService stockAppService;

    public StockController(StockAppService stockAppService) {
        this.stockAppService = stockAppService;
    }

    @Override
    public Result<StockDTO> get(Long productId) {
        return Result.success(stockAppService.getByProductId(productId));
    }

    @Override
    public Result<Boolean> reserve(@Valid StockChangeDTO dto) {
        return Result.success(stockAppService.reserve(dto));
    }

    @Override
    public Result<Boolean> release(@Valid StockChangeDTO dto) {
        return Result.success(stockAppService.release(dto));
    }

    @Override
    public Result<Boolean> deduct(@Valid StockChangeDTO dto) {
        return Result.success(stockAppService.deduct(dto));
    }
}



