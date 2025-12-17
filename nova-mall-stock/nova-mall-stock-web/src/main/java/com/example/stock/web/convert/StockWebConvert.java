package com.example.stock.web.convert;

import com.example.stock.api.dto.StockChangeDTO;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface StockWebConvert {

    StockChangeDTO toChangeDto(StockChangeDTO dto);
}

