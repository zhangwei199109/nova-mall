package com.example.product.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.common.exception.BusinessException;
import com.example.product.api.dto.ProductDTO;
import com.example.product.api.dto.ProductRecDTO;
import com.example.product.service.ProductAppService;
import com.example.product.service.entity.Product;
import com.example.product.service.mapper.ProductMapper;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.util.Comparator;
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

    @Override
    public List<ProductRecDTO> recommendByProduct(Long productId, Integer limit) {
        int realLimit = (limit == null || limit < 1) ? 6 : Math.min(limit, 20);
        // 简单规则：价格相近 + ID排序；若无基准则随机排序
        Product base = productId == null ? null : productMapper.selectById(productId);
        List<Product> candidates = productMapper.selectList(new LambdaQueryWrapper<>());
        return candidates.stream()
                .filter(p -> !p.getId().equals(productId))
                .sorted(buildComparator(base))
                .limit(realLimit)
                .map(p -> {
                    ProductRecDTO rec = new ProductRecDTO();
                    rec.setProductId(p.getId());
                    rec.setReason(reason(base, p));
                    rec.setScore(Math.random());
                    return rec;
                })
                .collect(Collectors.toList());
    }

    private Comparator<Product> buildComparator(Product base) {
        if (base == null || base.getPrice() == null) {
            return Comparator.comparing(Product::getId);
        }
        return Comparator.<Product>comparingDouble(p -> priceDiff(base, p))
                .thenComparing(Product::getId);
    }

    private double priceDiff(Product base, Product other) {
        if (base.getPrice() == null || other.getPrice() == null) {
            return Double.MAX_VALUE;
        }
        return base.getPrice().subtract(other.getPrice()).abs().doubleValue();
    }

    private String reason(Product base, Product other) {
        if (base == null) {
            return "热度推荐";
        }
        return "价格相近推荐";
    }

    private ProductDTO toDTO(Product p) {
        ProductDTO dto = new ProductDTO();
        BeanUtils.copyProperties(p, dto);
        return dto;
    }
}



