package com.yuchen.kami.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.yuchen.kami.common.BusinessException;
import com.yuchen.kami.common.PageResult;
import com.yuchen.kami.dto.WalletTransactionVO;
import com.yuchen.kami.dto.WalletVO;
import com.yuchen.kami.entity.ShopOrder;
import com.yuchen.kami.entity.ShopUser;
import com.yuchen.kami.entity.WalletTransaction;
import com.yuchen.kami.mapper.ShopUserMapper;
import com.yuchen.kami.mapper.WalletTransactionMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class WalletService {

    private static final Map<String, String> TYPE_LABELS = Map.of(
            WalletTransaction.TYPE_PAY, "订单支付",
            WalletTransaction.TYPE_RECHARGE, "余额充值",
            WalletTransaction.TYPE_REFUND, "订单退款",
            WalletTransaction.TYPE_ADJUST, "余额调整"
    );

    private final ShopUserMapper shopUserMapper;
    private final WalletTransactionMapper walletTransactionMapper;

    public WalletVO getWallet(Long userId) {
        ShopUser user = requireUser(userId);
        return WalletVO.builder()
                .balance(normalizeBalance(user.getBalance()))
                .build();
    }

    public PageResult<WalletTransactionVO> listTransactions(Long userId, int page, int size) {
        Page<WalletTransaction> result = walletTransactionMapper.selectPage(new Page<>(page, size),
                new LambdaQueryWrapper<WalletTransaction>()
                        .eq(WalletTransaction::getUserId, userId)
                        .orderByDesc(WalletTransaction::getCreatedAt));
        List<WalletTransactionVO> vos = result.getRecords().stream().map(this::toVO).toList();
        return new PageResult<>(vos, result.getTotal(), page, size);
    }

    @Transactional
    public String payOrder(Long userId, ShopOrder order) {
        BigDecimal amount = order.getAmount();
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new BusinessException("订单金额无效");
        }
        ShopUser user = requireUser(userId);
        BigDecimal balance = normalizeBalance(user.getBalance());
        if (balance.compareTo(amount) < 0) {
            throw new BusinessException("余额不足，当前余额 ¥" + balance);
        }
        BigDecimal after = balance.subtract(amount);
        int rows = shopUserMapper.update(null, new LambdaUpdateWrapper<ShopUser>()
                .eq(ShopUser::getId, userId)
                .ge(ShopUser::getBalance, amount)
                .set(ShopUser::getBalance, after));
        if (rows == 0) {
            throw new BusinessException("余额不足");
        }
        recordTransaction(userId, WalletTransaction.TYPE_PAY, amount.negate(), after,
                order.getId(), order.getOrderNo(), "订单支付");
        return "BAL" + order.getOrderNo();
    }

    @Transactional
    public void credit(Long userId, BigDecimal amount, String type, String remark) {
        credit(userId, amount, type, remark, null, null);
    }

    @Transactional
    public void credit(Long userId, BigDecimal amount, String type, String remark, Long orderId, String orderNo) {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new BusinessException("充值金额必须大于 0");
        }
        ShopUser user = requireUser(userId);
        BigDecimal after = normalizeBalance(user.getBalance()).add(amount);
        shopUserMapper.update(null, new LambdaUpdateWrapper<ShopUser>()
                .eq(ShopUser::getId, userId)
                .set(ShopUser::getBalance, after));
        recordTransaction(userId, type, amount, after, orderId, orderNo, remark);
    }

    @Transactional
    public void refundOrder(Long userId, BigDecimal amount, Long orderId, String orderNo, String remark) {
        String detail = remark != null && !remark.isBlank() ? remark : "订单退款";
        credit(userId, amount, WalletTransaction.TYPE_REFUND, detail, orderId, orderNo);
    }

    @Transactional
    public BigDecimal adjustBalance(Long userId, BigDecimal delta, String remark) {
        if (delta == null || delta.compareTo(BigDecimal.ZERO) == 0) {
            throw new BusinessException("调整金额不能为 0");
        }
        ShopUser user = requireUser(userId);
        BigDecimal after = normalizeBalance(user.getBalance()).add(delta);
        if (after.compareTo(BigDecimal.ZERO) < 0) {
            throw new BusinessException("余额不足，无法扣减");
        }
        shopUserMapper.update(null, new LambdaUpdateWrapper<ShopUser>()
                .eq(ShopUser::getId, userId)
                .set(ShopUser::getBalance, after));
        String detail = remark != null && !remark.isBlank() ? remark : "管理员调整余额";
        recordTransaction(userId, WalletTransaction.TYPE_ADJUST, delta, after, null, null, detail);
        return after;
    }

    private void recordTransaction(Long userId, String type, BigDecimal amount, BigDecimal balanceAfter,
                                   Long orderId, String orderNo, String remark) {
        WalletTransaction tx = new WalletTransaction();
        tx.setUserId(userId);
        tx.setType(type);
        tx.setAmount(amount);
        tx.setBalanceAfter(balanceAfter);
        tx.setOrderId(orderId);
        tx.setOrderNo(orderNo);
        tx.setRemark(remark);
        walletTransactionMapper.insert(tx);
    }

    private ShopUser requireUser(Long userId) {
        ShopUser user = shopUserMapper.selectById(userId);
        if (user == null || user.getStatus() != 1) {
            throw new BusinessException("用户不存在");
        }
        if (user.getBalance() == null) {
            user.setBalance(BigDecimal.ZERO);
        }
        return user;
    }

    private BigDecimal normalizeBalance(BigDecimal balance) {
        return balance == null ? BigDecimal.ZERO : balance;
    }

    private WalletTransactionVO toVO(WalletTransaction tx) {
        return WalletTransactionVO.builder()
                .id(tx.getId())
                .type(tx.getType())
                .typeLabel(TYPE_LABELS.getOrDefault(tx.getType(), tx.getType()))
                .amount(tx.getAmount())
                .balanceAfter(tx.getBalanceAfter())
                .orderNo(tx.getOrderNo())
                .remark(tx.getRemark())
                .createdAt(tx.getCreatedAt())
                .build();
    }
}
