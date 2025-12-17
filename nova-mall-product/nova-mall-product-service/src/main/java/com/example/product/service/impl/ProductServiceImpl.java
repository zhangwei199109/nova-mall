package com.example.product.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.common.exception.BusinessException;
import com.example.product.api.dto.ProductDTO;
import com.example.product.service.ProductAppService;
import com.example.product.service.convert.ProductConvert;
import com.example.product.service.entity.Product;
import com.example.product.service.mapper.ProductMapper;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class ProductServiceImpl implements ProductAppService {

    private final ProductMapper productMapper;
    private final ProductConvert productConvert;

    public ProductServiceImpl(ProductMapper productMapper, ProductConvert productConvert) {
        this.productMapper = productMapper;
        this.productConvert = productConvert;
    }

    @Override
    public List<ProductDTO> listProducts() {
        List<Product> list = productMapper.selectList(new LambdaQueryWrapper<>());
        return list.stream().map(productConvert::toDTO).collect(Collectors.toList());
    }

    @Override
    public ProductDTO getById(Long id) {
        Product p = productMapper.selectById(id);
        if (p == null) {
            throw new BusinessException(404, "商品不存在");
        }
        return productConvert.toDTO(p);
    }

    @Override
    public ProductDTO create(ProductDTO dto) {
        Product entity = productConvert.toEntity(dto);
        entity.setId(null);
        productMapper.insert(entity);
        return productConvert.toDTO(productMapper.selectById(entity.getId()));
    }

    @Override
    public ProductDTO update(ProductDTO dto) {
        if (dto.getId() == null) {
            throw new BusinessException(400, "ID不能为空");
        }
        Product exist = productMapper.selectById(dto.getId());
        if (exist == null) {
            throw new BusinessException(404, "商品不存在");
        }
        productConvert.updateEntity(dto, exist);
        productMapper.updateById(exist);
        return productConvert.toDTO(productMapper.selectById(dto.getId()));
    }

    @Override
    public boolean delete(Long id) {
        if (productMapper.selectById(id) == null) {
            throw new BusinessException(404, "商品不存在");
        }
        return productMapper.deleteById(id) > 0;
    }
}



