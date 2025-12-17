package com.example.stock.service;

import com.example.stock.api.dto.StockChangeDTO;
import com.example.stock.api.dto.StockDTO;

/**
 * 库存领域对外（RPC）接口。
 */
public interface StockAppService {
    StockDTO getByProductId(Long productId);

    boolean reserve(StockChangeDTO dto);

    boolean release(StockChangeDTO dto);

    boolean deduct(StockChangeDTO dto);
}







