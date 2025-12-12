package com.example.product.service.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.product.service.entity.Product;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface ProductMapper extends BaseMapper<Product> {
}



