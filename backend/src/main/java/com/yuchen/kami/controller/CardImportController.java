package com.yuchen.kami.controller;

import com.yuchen.kami.common.Result;
import com.yuchen.kami.dto.ImportResult;
import com.yuchen.kami.service.AuditService;
import com.yuchen.kami.service.CardImportService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/admin/cards")
@RequiredArgsConstructor
public class CardImportController {

    private final CardImportService cardImportService;
    private final AuditService auditService;

    @PostMapping("/import")
    public Result<ImportResult> importCards(
            @RequestParam("file") MultipartFile file,
            @RequestParam Long productId,
            @RequestParam(required = false) String remark,
            Authentication auth, HttpServletRequest request) throws Exception {
        Long userId = (Long) auth.getDetails();
        ImportResult result = cardImportService.importCards(file, productId, remark, userId);
        auditService.log(userId, auth.getName(), "IMPORT", "card_batch",
                "导入卡密 " + result.getSuccess() + " 张，批次 " + result.getBatchNo(),
                request.getRemoteAddr());
        return Result.ok(result);
    }
}
