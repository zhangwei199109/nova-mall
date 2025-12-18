package com.example.stock.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.common.exception.BusinessException;
import com.example.stock.api.dto.StockDTO;
import com.example.stock.api.dto.StockChangeDTO;
import com.example.stock.service.StockAppService;
import com.example.stock.service.entity.Stock;
import com.example.stock.service.mapper.StockMapper;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class StockServiceImpl implements StockAppService {

    private final StockMapper stockMapper;

    public StockServiceImpl(StockMapper stockMapper) {
        this.stockMapper = stockMapper;
    }

    @Override
    public StockDTO getByProductId(Long productId) {
        Stock stock = stockMapper.selectOne(new LambdaQueryWrapper<Stock>()
                .eq(Stock::getProductId, productId));
        if (stock == null) {
            throw new BusinessException(404, "库存不存在");
        }
        return toDTO(stock);
    }

    @Override
    @Transactional
    public boolean reserve(StockChangeDTO dto) {
        Stock stock = findOrInit(dto.getProductId());
        if (stock.getAvailable() < dto.getQuantity()) {
            throw new BusinessException(400, "库存不足");
        }
        stock.setAvailable(stock.getAvailable() - dto.getQuantity());
        stock.setLocked(stock.getLocked() + dto.getQuantity());
        return stockMapper.updateById(stock) > 0;
    }

    @Override
    @Transactional
    public boolean release(StockChangeDTO dto) {
        Stock stock = findOrInit(dto.getProductId());
        if (stock.getLocked() < dto.getQuantity()) {
            throw new BusinessException(400, "锁定库存不足");
        }
        stock.setLocked(stock.getLocked() - dto.getQuantity());
        stock.setAvailable(stock.getAvailable() + dto.getQuantity());
        return stockMapper.updateById(stock) > 0;
    }

    @Override
    @Transactional
    public boolean deduct(StockChangeDTO dto) {
        Stock stock = findOrInit(dto.getProductId());
        if (stock.getLocked() < dto.getQuantity()) {
            throw new BusinessException(400, "锁定库存不足");
        }
        stock.setLocked(stock.getLocked() - dto.getQuantity());
        return stockMapper.updateById(stock) > 0;
    }

    private Stock findOrInit(Long productId) {
        Stock stock = stockMapper.selectOne(new LambdaQueryWrapper<Stock>()
                .eq(Stock::getProductId, productId));
        if (stock == null) {
            stock = new Stock();
            stock.setProductId(productId);
            stock.setAvailable(0);
            stock.setLocked(0);
            stockMapper.insert(stock);
        }
        return stock;
    }

    private StockDTO toDTO(Stock s) {
        StockDTO dto = new StockDTO();
        BeanUtils.copyProperties(s, dto);
        return dto;
    }
}



