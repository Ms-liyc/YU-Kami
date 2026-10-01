package com.yuchen.kami.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.yuchen.kami.common.BusinessException;
import com.yuchen.kami.common.PageResult;
import com.yuchen.kami.entity.Product;
import com.yuchen.kami.mapper.ProductMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
@RequiredArgsConstructor
public class ProductService {

    private final ProductMapper productMapper;

    public PageResult<Product> page(int page, int size, String keyword) {
        LambdaQueryWrapper<Product> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(keyword)) {
            wrapper.and(w -> w.like(Product::getName, keyword).or().like(Product::getCode, keyword));
        }
        wrapper.orderByDesc(Product::getCreatedAt);
        Page<Product> result = productMapper.selectPage(new Page<>(page, size), wrapper);
        return new PageResult<>(result.getRecords(), result.getTotal(), page, size);
    }

    public Product getById(Long id) {
        Product product = productMapper.selectById(id);
        if (product == null) {
            throw new BusinessException("产品不存在");
        }
        return product;
    }

    public Product create(Product product) {
        Long count = productMapper.selectCount(new LambdaQueryWrapper<Product>()
                .eq(Product::getCode, product.getCode()));
        if (count > 0) {
            throw new BusinessException("产品编码已存在");
        }
        productMapper.insert(product);
        return product;
    }

    public Product update(Long id, Product product) {
        Product existing = getById(id);
        product.setId(existing.getId());
        productMapper.updateById(product);
        return productMapper.selectById(id);
    }

    public void delete(Long id) {
        productMapper.deleteById(id);
    }
}
