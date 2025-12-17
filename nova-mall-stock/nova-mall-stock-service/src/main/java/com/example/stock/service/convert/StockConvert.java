package com.example.stock.service.convert;

import com.example.stock.api.dto.StockDTO;
import com.example.stock.service.entity.Stock;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface StockConvert {

    StockDTO toDTO(Stock entity);

    Stock toEntity(StockDTO dto);
}

