package com.yuchen.kami.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.yuchen.kami.common.BusinessException;
import com.yuchen.kami.common.PageResult;
import com.yuchen.kami.dto.AdminProductVO;
import com.yuchen.kami.entity.CardKey;
import com.yuchen.kami.entity.Product;
import com.yuchen.kami.mapper.CardKeyMapper;
import com.yuchen.kami.mapper.ProductMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ProductService {

    private final ProductMapper productMapper;
    private final CardKeyMapper cardKeyMapper;

    public PageResult<AdminProductVO> pageWithStock(int page, int size, String keyword) {
        PageResult<Product> pageResult = page(page, size, keyword, null);
        return new PageResult<>(
                pageResult.getRecords().stream().map(this::toAdminVO).toList(),
                pageResult.getTotal(), page, size);
    }

    public long countUnusedStock(Long productId) {
        return cardKeyMapper.selectCount(new LambdaQueryWrapper<CardKey>()
                .eq(CardKey::getProductId, productId)
                .eq(CardKey::getStatus, CardKey.STATUS_UNUSED));
    }

    private AdminProductVO toAdminVO(Product product) {
        AdminProductVO vo = new AdminProductVO();
        vo.setId(product.getId());
        vo.setName(product.getName());
        vo.setCode(product.getCode());
        vo.setCategory(product.getCategory());
        vo.setCardType(product.getCardType());
        vo.setValue(product.getValue());
        vo.setDurationDays(product.getDurationDays());
        vo.setDescription(product.getDescription());
        vo.setStatus(product.getStatus());
        vo.setCreatedAt(product.getCreatedAt());
        vo.setUpdatedAt(product.getUpdatedAt());
        vo.setUnusedStock(countUnusedStock(product.getId()));
        return vo;
    }

    public PageResult<Product> page(int page, int size, String keyword, String category) {
        LambdaQueryWrapper<Product> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(keyword)) {
            wrapper.and(w -> w.like(Product::getName, keyword).or().like(Product::getCode, keyword));
        }
        if (StringUtils.hasText(category)) {
            wrapper.eq(Product::getCategory, category);
        }
        wrapper.orderByDesc(Product::getCreatedAt);
        Page<Product> result = productMapper.selectPage(new Page<>(page, size), wrapper);
        return new PageResult<>(result.getRecords(), result.getTotal(), page, size);
    }

    public List<String> listCategories() {
        return productMapper.selectList(new LambdaQueryWrapper<Product>()
                        .eq(Product::getStatus, 1)
                        .isNotNull(Product::getCategory)
                        .ne(Product::getCategory, ""))
                .stream()
                .map(Product::getCategory)
                .distinct()
                .sorted()
                .toList();
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
