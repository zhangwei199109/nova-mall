package com.example.product.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.common.dto.PageParam;
import com.example.common.dto.PageResult;
import com.example.common.exception.BusinessException;
import com.example.product.api.dto.ProductAdjustRequest;
import com.example.product.api.dto.ProductDTO;
import com.example.product.api.dto.ProductQuery;
import com.example.product.api.dto.ProductRecDTO;
import com.example.product.service.ProductAppService;
import com.example.product.service.entity.Product;
import com.example.product.service.mapper.ProductMapper;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class ProductServiceImpl implements ProductAppService {

    private final ProductMapper productMapper;

    public ProductServiceImpl(ProductMapper productMapper) {
        this.productMapper = productMapper;
    }

    @Override
    public List<ProductDTO> listProducts() {
        List<Product> list = productMapper.selectList(new LambdaQueryWrapper<Product>()
                .eq(Product::getDeleted, 0));
        return list.stream().map(this::toDTO).collect(Collectors.toList());
    }

    @Override
    public PageResult<ProductDTO> page(PageParam pageParam, ProductQuery query) {
        PageParam norm = pageParam == null ? new PageParam(1, 10).normalized(1, 10, 100)
                : pageParam.normalized(1, 10, 100);
        LambdaQueryWrapper<Product> wrapper = new LambdaQueryWrapper<Product>()
                .eq(Product::getDeleted, 0);
        Integer status = query == null ? null : query.getStatus();
        if (status == null) {
            wrapper.eq(Product::getStatus, 1); // 默认筛选上架
        } else {
            wrapper.eq(Product::getStatus, status);
        }
        if (query != null && StringUtils.hasText(query.getKeyword())) {
            wrapper.like(Product::getName, query.getKeyword());
        }
        if (query != null && query.getCategoryId() != null) {
            wrapper.eq(Product::getCategoryId, query.getCategoryId());
        }
        if (query != null && StringUtils.hasText(query.getBrand())) {
            wrapper.eq(Product::getBrand, query.getBrand());
        }
        if (query != null && query.getMinPrice() != null) {
            wrapper.ge(Product::getPrice, query.getMinPrice());
        }
        if (query != null && query.getMaxPrice() != null) {
            wrapper.le(Product::getPrice, query.getMaxPrice());
        }
        if (query != null && query.getMinPrice() != null && query.getMaxPrice() != null
                && query.getMinPrice().compareTo(query.getMaxPrice()) > 0) {
            throw new BusinessException(400, "价格区间不合法");
        }
        // 排序
        String sortBy = query == null ? null : query.getSortBy();
        String order = query == null ? null : query.getOrder();
        boolean asc = "asc".equalsIgnoreCase(order);
        if ("price".equalsIgnoreCase(sortBy)) {
            wrapper.orderBy(true, asc, Product::getPrice);
        } else if ("sales".equalsIgnoreCase(sortBy)) {
            wrapper.orderBy(true, !asc, Product::getSoldCount); // 默认销量高在前
        } else if ("newest".equalsIgnoreCase(sortBy)) {
            wrapper.orderBy(true, false, Product::getCreateTime);
        } else {
            wrapper.orderByDesc(Product::getUpdateTime);
        }
        Page<Product> page = productMapper.selectPage(
                new Page<>(norm.getPageNo(), norm.getPageSize()), wrapper);
        List<ProductDTO> records = page.getRecords().stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
        return new PageResult<>(records, page.getTotal(), page.getCurrent(), page.getSize());
    }

    @Override
    public ProductDTO getById(Long id) {
        Product p = productMapper.selectById(id);
        if (p == null) {
            throw new BusinessException(404, "商品不存在");
        }
        // 浏览量+1（简单累加，可后续接入异步或埋点）
        productMapper.update(null, new com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper<Product>()
                .eq(Product::getId, id)
                .set(Product::getViewCount, (p.getViewCount() == null ? 0 : p.getViewCount()) + 1));
        return toDTO(p);
    }

    @Override
    public ProductDTO create(ProductDTO dto) {
        Product entity = new Product();
        BeanUtils.copyProperties(dto, entity);
        entity.setId(null);
        applyDefaults(entity);
        validateForSave(entity);
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
        applyDefaults(exist);
        validateForSave(exist);
        productMapper.updateById(exist);
        return toDTO(productMapper.selectById(dto.getId()));
    }

    @Override
    public boolean onShelf(Long id) {
        Product exist = productMapper.selectById(id);
        if (exist == null) {
            throw new BusinessException(404, "商品不存在");
        }
        if (exist.getStock() == null || exist.getStock() <= 0) {
            throw new BusinessException(400, "库存需大于0才能上架");
        }
        exist.setStatus(1);
        return productMapper.updateById(exist) > 0;
    }

    @Override
    public boolean offShelf(Long id) {
        Product exist = productMapper.selectById(id);
        if (exist == null) {
            throw new BusinessException(404, "商品不存在");
        }
        exist.setStatus(0);
        return productMapper.updateById(exist) > 0;
    }

    @Override
    public boolean delete(Long id) {
        if (productMapper.selectById(id) == null) {
            throw new BusinessException(404, "商品不存在");
        }
        return productMapper.deleteById(id) > 0;
    }

    @Override
    public List<ProductDTO> topBySales(Integer limit) {
        int realLimit = (limit == null || limit < 1) ? 10 : Math.min(limit, 50);
        List<Product> list = productMapper.selectList(new LambdaQueryWrapper<Product>()
                .eq(Product::getDeleted, 0)
                .eq(Product::getStatus, 1)
                .orderByDesc(Product::getSoldCount)
                .last("limit " + realLimit));
        return list.stream().map(this::toDTO).collect(Collectors.toList());
    }

    @Override
    public boolean adjustAfterPay(List<ProductAdjustRequest> items) {
        if (items == null || items.isEmpty()) {
            return true;
        }
        // 汇总相同商品数量，减少多次更新
        Map<Long, Integer> aggregated = new HashMap<>();
        for (ProductAdjustRequest req : items) {
            if (req.getProductId() == null || req.getQuantity() == null || req.getQuantity() < 1) {
                throw new BusinessException(400, "商品ID与数量不能为空");
            }
            aggregated.merge(req.getProductId(), req.getQuantity(), Integer::sum);
        }
        for (Map.Entry<Long, Integer> entry : aggregated.entrySet()) {
            Long productId = entry.getKey();
            Integer qty = entry.getValue();
            int updated = productMapper.update(null, new com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper<Product>()
                    .eq(Product::getId, productId)
                    .eq(Product::getDeleted, 0)
                    .ge(Product::getStock, qty)
                    .setSql("stock = stock - " + qty)
                    .setSql("sold_count = sold_count + " + qty));
            if (updated == 0) {
                throw new BusinessException(409, "商品库存不足或已下架，productId=" + productId);
            }
        }
        return true;
    }

    @Override
    public List<ProductRecDTO> recommendByProduct(Long productId, Integer limit) {
        int realLimit = (limit == null || limit < 1) ? 6 : Math.min(limit, 20);
        Product base = productId == null ? null : productMapper.selectById(productId);
        if (productId != null && base == null) {
            throw new BusinessException(404, "商品不存在");
        }
        final Product baseRef = base;
        LambdaQueryWrapper<Product> wrapper = new LambdaQueryWrapper<Product>()
                .eq(Product::getDeleted, 0)
                .eq(Product::getStatus, 1)
                .ne(productId != null, Product::getId, productId);
        List<Product> candidates = productMapper.selectList(wrapper);
        return candidates.stream()
                .sorted(buildComparator(baseRef))
                .limit(realLimit)
                .map(p -> {
                    ProductRecDTO rec = new ProductRecDTO();
                    rec.setProductId(p.getId());
                    rec.setReason(reason(baseRef, p));
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

    private void applyDefaults(Product entity) {
        if (entity.getStatus() == null) {
            entity.setStatus(1);
        }
        if (entity.getSoldCount() == null) {
            entity.setSoldCount(0);
        }
        if (entity.getViewCount() == null) {
            entity.setViewCount(0);
        }
    }

    private void validateForSave(Product entity) {
        if (entity.getPrice() == null || entity.getPrice().compareTo(BigDecimal.ZERO) <= 0) {
            throw new BusinessException(400, "价格必须大于0");
        }
        if (entity.getStock() == null || entity.getStock() < 0) {
            throw new BusinessException(400, "库存不能为负");
        }
        if (entity.getStatus() != null && entity.getStatus() == 1
                && (entity.getStock() == null || entity.getStock() <= 0)) {
            throw new BusinessException(400, "上架商品库存必须大于0");
        }
    }
}



