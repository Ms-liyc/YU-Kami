package com.yuchen.kami.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.yuchen.kami.common.BusinessException;
import com.yuchen.kami.common.PageResult;
import com.yuchen.kami.dto.AdminShopUserVO;
import com.yuchen.kami.entity.ShopUser;
import com.yuchen.kami.mapper.ShopUserMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
public class AdminShopUserService {

    private final ShopUserMapper shopUserMapper;
    private final WalletService walletService;

    public PageResult<AdminShopUserVO> page(int page, int size, String keyword) {
        LambdaQueryWrapper<ShopUser> wrapper = new LambdaQueryWrapper<>();
        if (keyword != null && !keyword.isBlank()) {
            String q = keyword.trim();
            wrapper.and(w -> w.like(ShopUser::getUsername, q)
                    .or().like(ShopUser::getNickname, q)
                    .or().like(ShopUser::getEmail, q));
        }
        wrapper.orderByDesc(ShopUser::getCreatedAt);
        Page<ShopUser> result = shopUserMapper.selectPage(new Page<>(page, size), wrapper);
        return new PageResult<>(result.getRecords().stream().map(this::toVO).toList(),
                result.getTotal(), page, size);
    }

    public AdminShopUserVO adjustWallet(Long userId, BigDecimal amount, String remark) {
        walletService.adjustBalance(userId, amount, remark);
        ShopUser user = shopUserMapper.selectById(userId);
        return toVO(user);
    }

    public AdminShopUserVO updateStatus(Long userId, Integer status) {
        ShopUser user = requireUser(userId);
        if (status == null || (status != 0 && status != 1)) {
            throw new BusinessException("状态值无效");
        }
        user.setStatus(status);
        shopUserMapper.updateById(user);
        return toVO(user);
    }

    public ShopUser requireUser(Long userId) {
        ShopUser user = shopUserMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException("用户不存在");
        }
        return user;
    }

    private AdminShopUserVO toVO(ShopUser user) {
        return AdminShopUserVO.builder()
                .id(user.getId())
                .username(user.getUsername())
                .nickname(user.getNickname())
                .email(user.getEmail())
                .balance(user.getBalance() != null ? user.getBalance() : BigDecimal.ZERO)
                .status(user.getStatus())
                .createdAt(user.getCreatedAt())
                .build();
    }
}
