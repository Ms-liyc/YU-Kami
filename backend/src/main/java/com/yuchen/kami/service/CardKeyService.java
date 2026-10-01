package com.yuchen.kami.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.yuchen.kami.common.BusinessException;
import com.yuchen.kami.common.PageResult;
import com.yuchen.kami.crypto.CardKeyGenerator;
import com.yuchen.kami.crypto.CryptoService;
import com.yuchen.kami.dto.GenerateBatchRequest;
import com.yuchen.kami.entity.CardBatch;
import com.yuchen.kami.entity.CardKey;
import com.yuchen.kami.entity.Product;
import com.yuchen.kami.mapper.CardBatchMapper;
import com.yuchen.kami.mapper.CardKeyMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class CardKeyService {

    private final CardKeyMapper cardKeyMapper;
    private final CardBatchMapper cardBatchMapper;
    private final ProductService productService;
    private final CardKeyGenerator cardKeyGenerator;
    private final CryptoService cryptoService;

    @Transactional
    public List<String> generateBatch(GenerateBatchRequest request, Long userId) {
        Product product = productService.getById(request.getProductId());

        CardBatch batch = new CardBatch();
        batch.setBatchNo("B" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"))
                + String.format("%04d", (int) (Math.random() * 10000)));
        batch.setProductId(product.getId());
        batch.setTotalCount(request.getCount());
        batch.setUsedCount(0);
        batch.setPrefix(request.getPrefix());
        batch.setRemark(request.getRemark());
        batch.setCreatedBy(userId);
        cardBatchMapper.insert(batch);

        List<String> plainKeys = new ArrayList<>();
        Set<String> hashSet = new HashSet<>();

        for (int i = 0; i < request.getCount(); i++) {
            String plainKey = cardKeyGenerator.generate(request.getPrefix());
            String pepper = cryptoService.generatePepper();
            String hash = cryptoService.hashCardKey(plainKey, pepper);

            if (!hashSet.add(hash)) {
                continue;
            }

            CardKey card = new CardKey();
            card.setBatchId(batch.getId());
            card.setProductId(product.getId());
            card.setKeyHash(hash);
            card.setKeyPepper(pepper);
            card.setKeyChecksum(cryptoService.computeChecksum(plainKey));
            card.setEncryptedMeta(cryptoService.encryptMeta(
                    "{\"productCode\":\"" + product.getCode() + "\",\"batchNo\":\"" + batch.getBatchNo() + "\"}"));
            card.setStatus(CardKey.STATUS_UNUSED);
            card.setExpireAt(request.getExpireAt());
            cardKeyMapper.insert(card);
            plainKeys.add(plainKey);
        }

        return plainKeys;
    }

    public PageResult<CardKey> page(int page, int size, Long batchId, Long productId, Integer status) {
        LambdaQueryWrapper<CardKey> wrapper = new LambdaQueryWrapper<>();
        if (batchId != null) {
            wrapper.eq(CardKey::getBatchId, batchId);
        }
        if (productId != null) {
            wrapper.eq(CardKey::getProductId, productId);
        }
        if (status != null) {
            wrapper.eq(CardKey::getStatus, status);
        }
        wrapper.orderByDesc(CardKey::getCreatedAt);
        Page<CardKey> result = cardKeyMapper.selectPage(new Page<>(page, size), wrapper);
        return new PageResult<>(result.getRecords(), result.getTotal(), page, size);
    }

    public PageResult<CardBatch> batchPage(int page, int size) {
        Page<CardBatch> result = cardBatchMapper.selectPage(
                new Page<>(page, size),
                new LambdaQueryWrapper<CardBatch>().orderByDesc(CardBatch::getCreatedAt));
        return new PageResult<>(result.getRecords(), result.getTotal(), page, size);
    }

    @Transactional
    public void revoke(Long cardId) {
        CardKey card = cardKeyMapper.selectById(cardId);
        if (card == null) {
            throw new BusinessException("卡密不存在");
        }
        if (card.getStatus() == CardKey.STATUS_USED) {
            throw new BusinessException("已使用的卡密无法作废");
        }
        card.setStatus(CardKey.STATUS_REVOKED);
        cardKeyMapper.updateById(card);
    }

    public CardKey findByHash(String keyHash) {
        return cardKeyMapper.selectOne(new LambdaQueryWrapper<CardKey>()
                .eq(CardKey::getKeyHash, keyHash));
    }

    @Transactional
    public String generateForOrder(Long productId, Long userId, String orderNo) {
        GenerateBatchRequest request = new GenerateBatchRequest();
        request.setProductId(productId);
        request.setCount(1);
        request.setRemark("订单发货: " + orderNo);
        List<String> keys = generateBatch(request, userId);
        if (keys.isEmpty()) {
            throw new BusinessException("卡密生成失败");
        }
        return keys.get(0);
    }
}
