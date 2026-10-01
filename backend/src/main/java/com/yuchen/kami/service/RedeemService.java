package com.yuchen.kami.service;

import com.yuchen.kami.common.BusinessException;
import com.yuchen.kami.crypto.CryptoService;
import com.yuchen.kami.dto.RedeemRequest;
import com.yuchen.kami.dto.RedeemResponse;
import com.yuchen.kami.entity.CardKey;
import com.yuchen.kami.entity.Product;
import com.yuchen.kami.entity.RedeemRecord;
import com.yuchen.kami.mapper.CardBatchMapper;
import com.yuchen.kami.mapper.CardKeyMapper;
import com.yuchen.kami.mapper.RedeemRecordMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
public class RedeemService {

    private final CardKeyMapper cardKeyMapper;
    private final CardBatchMapper cardBatchMapper;
    private final RedeemRecordMapper redeemRecordMapper;
    private final ProductService productService;
    private final CryptoService cryptoService;
    private final RateLimitService rateLimitService;
    private final DistributedLockService lockService;

    public RedeemResponse redeem(RedeemRequest request, String ip, String userAgent) {
        rateLimitService.checkRedeemLimit(ip + ":" + request.getRedeemUser());

        String plainKey = request.getCardKey().trim().toUpperCase();
        validateFormat(plainKey);

        String lockKey = cryptoService.sha256(plainKey);
        return lockService.executeWithLock(lockKey, () -> doRedeem(request, plainKey, ip, userAgent));
    }

    @Transactional
    protected RedeemResponse doRedeem(RedeemRequest request, String plainKey, String ip, String userAgent) {
        int lastDash = plainKey.lastIndexOf('-');
        String dataPart = plainKey.substring(0, lastDash);
        String checksum = plainKey.substring(lastDash + 1);
        if (!cryptoService.verifyChecksum(dataPart, checksum)) {
            recordFailure(null, null, null, request.getRedeemUser(), ip, userAgent, "INVALID", "卡密校验失败");
            throw new BusinessException("卡密无效");
        }

        // 暴力搜索需配合索引；生产可用 key_prefix 分片优化
        CardKey card = findCardByPlainKey(plainKey);
        if (card == null) {
            recordFailure(null, null, null, request.getRedeemUser(), ip, userAgent, "NOT_FOUND", "卡密不存在");
            throw new BusinessException("卡密无效或已使用");
        }

        if (card.getStatus() == CardKey.STATUS_REVOKED) {
            recordFailure(card, request.getRedeemUser(), ip, userAgent, "REVOKED", "卡密已作废");
            throw new BusinessException("卡密已作废");
        }
        if (card.getStatus() == CardKey.STATUS_USED) {
            recordFailure(card, request.getRedeemUser(), ip, userAgent, "USED", "卡密已使用");
            throw new BusinessException("卡密已被使用");
        }
        if (card.getExpireAt() != null && card.getExpireAt().isBefore(LocalDateTime.now())) {
            card.setStatus(CardKey.STATUS_EXPIRED);
            cardKeyMapper.updateById(card);
            recordFailure(card, request.getRedeemUser(), ip, userAgent, "EXPIRED", "卡密已过期");
            throw new BusinessException("卡密已过期");
        }

        int updated = cardKeyMapper.redeem(card.getId(), card.getVersion(),
                request.getRedeemUser(), ip);
        if (updated == 0) {
            throw new BusinessException(409, "卡密已被使用");
        }
        cardBatchMapper.incrementUsedCount(card.getBatchId());

        Product product = productService.getById(card.getProductId());
        recordSuccess(card, product, request.getRedeemUser(), ip, userAgent);

        return RedeemResponse.builder()
                .productName(product.getName())
                .productCode(product.getCode())
                .cardType(product.getCardType())
                .value(product.getValue())
                .durationDays(product.getDurationDays())
                .message("兑换成功")
                .build();
    }

    private CardKey findCardByPlainKey(String plainKey) {
        // 通过 checksum 缩小范围后逐条验证（企业级可改为 Bloom Filter + 前缀索引）
        String checksum = plainKey.substring(plainKey.length() - 6);
        var candidates = cardKeyMapper.selectList(
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<CardKey>()
                        .eq(CardKey::getKeyChecksum, checksum)
                        .eq(CardKey::getStatus, CardKey.STATUS_UNUSED)
                        .last("LIMIT 10"));
        for (CardKey candidate : candidates) {
            String hash = cryptoService.hashCardKey(plainKey, candidate.getKeyPepper());
            if (hash.equals(candidate.getKeyHash())) {
                return candidate;
            }
        }
        return null;
    }

    private void validateFormat(String plainKey) {
        if (plainKey.length() < 10 || !plainKey.contains("-")) {
            throw new BusinessException("卡密格式错误");
        }
    }

    private void recordSuccess(CardKey card, Product product, String redeemUser, String ip, String userAgent) {
        RedeemRecord record = new RedeemRecord();
        record.setCardId(card.getId());
        record.setProductId(product.getId());
        record.setBatchId(card.getBatchId());
        record.setRedeemUser(redeemUser);
        record.setRedeemIp(ip);
        record.setUserAgent(userAgent);
        record.setResult("SUCCESS");
        record.setMessage("兑换成功");
        redeemRecordMapper.insert(record);
    }

    private void recordFailure(CardKey card, String redeemUser, String ip, String userAgent,
                               String result, String message) {
        recordFailure(card, card != null ? card.getProductId() : null,
                card != null ? card.getBatchId() : null, redeemUser, ip, userAgent, result, message);
    }

    private void recordFailure(CardKey card, Long productId, Long batchId, String redeemUser,
                               String ip, String userAgent, String result, String message) {
        RedeemRecord record = new RedeemRecord();
        if (card != null) {
            record.setCardId(card.getId());
        }
        record.setProductId(productId != null ? productId : 0L);
        record.setBatchId(batchId != null ? batchId : 0L);
        record.setRedeemUser(redeemUser);
        record.setRedeemIp(ip);
        record.setUserAgent(userAgent);
        record.setResult(result);
        record.setMessage(message);
        redeemRecordMapper.insert(record);
    }
}
