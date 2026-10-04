package com.yuchen.kami.dto;

import com.yuchen.kami.entity.Product;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class AdminProductVO extends Product {

    private Long unusedStock;
}
