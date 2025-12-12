package com.example.product.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.common.exception.BusinessException;
import com.example.product.service.ProductAppService;
import com.example.product.api.dto.ProductDTO;
import com.example.product.service.entity.Product;
import com.example.product.service.mapper.ProductMapper;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class ProductServiceImpl implements ProductAppService {

    private final ProductMapper productMapper;

    public ProductServiceImpl(ProductMapper productMapper) {
        this.productMapper = productMapper;
    }

    @Override
    public List<ProductDTO> listProducts() {
        List<Product> list = productMapper.selectList(new LambdaQueryWrapper<>());
        return list.stream().map(this::toDTO).collect(Collectors.toList());
    }

    @Override
    public ProductDTO getById(Long id) {
        Product p = productMapper.selectById(id);
        if (p == null) {
            throw new BusinessException(404, "商品不存在");
        }
        return toDTO(p);
    }

    @Override
    public ProductDTO create(ProductDTO dto) {
        Product entity = new Product();
        BeanUtils.copyProperties(dto, entity);
        entity.setId(null);
        productMapper.insert(entity);
        return toDTO(productMapper.selectById(entity.getId()));
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
        BeanUtils.copyProperties(dto, exist);
        productMapper.updateById(exist);
        return toDTO(productMapper.selectById(dto.getId()));
    }

    @Override
    public boolean delete(Long id) {
        if (productMapper.selectById(id) == null) {
            throw new BusinessException(404, "商品不存在");
        }
        return productMapper.deleteById(id) > 0;
    }

    private ProductDTO toDTO(Product p) {
        ProductDTO dto = new ProductDTO();
        BeanUtils.copyProperties(p, dto);
        return dto;
    }
}



