package com.yuchen.kami.controller;

import com.yuchen.kami.common.PageResult;
import com.yuchen.kami.common.Result;
import com.yuchen.kami.config.YuKamiProperties;
import com.yuchen.kami.dto.GenerateBatchRequest;
import com.yuchen.kami.entity.CardBatch;
import com.yuchen.kami.entity.CardKey;
import com.yuchen.kami.service.AuditService;
import com.yuchen.kami.service.CardKeyService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin/cards")
@RequiredArgsConstructor
public class CardKeyController {

    private final CardKeyService cardKeyService;
    private final AuditService auditService;
    private final YuKamiProperties properties;

    @PostMapping("/generate")
    public Result<List<String>> generate(@Valid @RequestBody GenerateBatchRequest request,
                                        Authentication auth, HttpServletRequest httpRequest) {
        Long userId = (Long) auth.getDetails();
        List<String> keys = cardKeyService.generateBatch(request, userId);
        auditService.log(userId, auth.getName(), "GENERATE", "card_batch",
                "生成卡密 " + request.getCount() + " 张", httpRequest.getRemoteAddr());
        return Result.ok(keys);
    }

    @GetMapping
    public Result<PageResult<CardKey>> page(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) Long batchId,
            @RequestParam(required = false) Long productId,
            @RequestParam(required = false) Integer status) {
        return Result.ok(cardKeyService.page(page, size, batchId, productId, status));
    }

    @GetMapping("/batches")
    public Result<PageResult<CardBatch>> batches(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size) {
        return Result.ok(cardKeyService.batchPage(page, size));
    }

    @PostMapping("/{id}/revoke")
    public Result<Void> revoke(@PathVariable Long id, Authentication auth, HttpServletRequest httpRequest) {
        cardKeyService.revoke(id);
        auditService.log((Long) auth.getDetails(), auth.getName(), "REVOKE", "card_key",
                "作废卡密 ID=" + id, httpRequest.getRemoteAddr());
        return Result.ok();
    }

    @GetMapping("/stats")
    public Result<Map<String, Object>> encryptionInfo() {
        int hashRounds = properties.getCrypto().getHashRounds();
        int aesRounds = properties.getCrypto().getAesRounds();
        return Result.ok(Map.of(
                "hashVersion", "v3",
                "hashRounds", hashRounds,
                "aesRounds", aesRounds,
                "storage", hashRounds + "x HMAC-SHA256 + HKDF + Pepper",
                "meta", aesRounds + "x AES-256-GCM (HKDF)",
                "cache", aesRounds + "x AES-256-GCM",
                "password", "BCrypt",
                "api", "RSA-SHA256",
                "checksum", hashRounds + "x HMAC-SHA256 (8 chars)"
        ));
    }
}
