package com.yuchen.kami.controller;

import com.yuchen.kami.common.PageResult;
import com.yuchen.kami.common.Result;
import com.yuchen.kami.dto.ShopProductVO;
import com.yuchen.kami.entity.Product;
import com.yuchen.kami.service.ProductService;
import com.yuchen.kami.service.PromotionService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/shop/products")
@RequiredArgsConstructor
public class ShopProductController {

    private final ProductService productService;
    private final PromotionService promotionService;

    @GetMapping
    public Result<PageResult<ShopProductVO>> list(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String category) {
        PageResult<Product> products = productService.page(page, size, null, category);
        var vos = products.getRecords().stream()
                .filter(p -> p.getStatus() == 1)
                .map(promotionService::toShopVO)
                .toList();
        return Result.ok(new PageResult<>(vos, products.getTotal(), page, size));
    }

    @GetMapping("/categories")
    public Result<List<String>> categories() {
        return Result.ok(productService.listCategories());
    }

    @GetMapping("/{id}")
    public Result<ShopProductVO> detail(@PathVariable Long id) {
        Product product = productService.getById(id);
        return Result.ok(promotionService.toShopVO(product));
    }
}
