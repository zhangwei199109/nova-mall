package com.example.stock.web.controller;

import com.example.common.dto.Result;
import com.example.stock.api.StockApi;
import com.example.stock.service.StockAppService;
import com.example.stock.api.dto.StockDTO;
import com.example.stock.api.dto.StockChangeDTO;
import com.example.stock.web.convert.StockWebConvert;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class StockController implements StockApi {

    private final StockAppService stockAppService;
    private final StockWebConvert stockWebConvert;

    public StockController(StockAppService stockAppService, StockWebConvert stockWebConvert) {
        this.stockAppService = stockAppService;
        this.stockWebConvert = stockWebConvert;
    }

    @Override
    public Result<StockDTO> get(Long productId) {
        return Result.success(stockAppService.getByProductId(productId));
    }

    @Override
    public Result<Boolean> reserve(@Valid StockChangeDTO dto) {
        return Result.success(stockAppService.reserve(stockWebConvert.toChangeDto(dto)));
    }

    @Override
    public Result<Boolean> release(@Valid StockChangeDTO dto) {
        return Result.success(stockAppService.release(stockWebConvert.toChangeDto(dto)));
    }

    @Override
    public Result<Boolean> deduct(@Valid StockChangeDTO dto) {
        return Result.success(stockAppService.deduct(stockWebConvert.toChangeDto(dto)));
    }
}



