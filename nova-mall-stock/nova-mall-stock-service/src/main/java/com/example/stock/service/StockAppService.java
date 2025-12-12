package com.example.stock.service;

import com.example.stock.api.dto.StockDTO;
import com.example.stock.api.dto.StockChangeDTO;

public interface StockAppService {
    StockDTO getByProductId(Long productId);

    boolean reserve(StockChangeDTO dto);

    boolean release(StockChangeDTO dto);

    boolean deduct(StockChangeDTO dto);
}




