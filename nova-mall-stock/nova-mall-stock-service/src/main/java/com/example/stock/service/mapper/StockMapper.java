package com.example.stock.service.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.stock.service.entity.Stock;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface StockMapper extends BaseMapper<Stock> {
}



