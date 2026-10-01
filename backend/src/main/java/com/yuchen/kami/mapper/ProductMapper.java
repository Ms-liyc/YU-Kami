package com.yuchen.kami.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.yuchen.kami.entity.Product;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface ProductMapper extends BaseMapper<Product> {
}
