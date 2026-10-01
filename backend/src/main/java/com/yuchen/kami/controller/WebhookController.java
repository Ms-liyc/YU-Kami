package com.yuchen.kami.controller;

import com.yuchen.kami.common.PageResult;
import com.yuchen.kami.common.Result;
import com.yuchen.kami.dto.WebhookRequest;
import com.yuchen.kami.entity.WebhookConfig;
import com.yuchen.kami.entity.WebhookLog;
import com.yuchen.kami.service.AuditService;
import com.yuchen.kami.service.WebhookConfigService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/webhooks")
@RequiredArgsConstructor
@PreAuthorize("hasRole('SUPER_ADMIN')")
public class WebhookController {

    private final WebhookConfigService webhookConfigService;
    private final AuditService auditService;

    @GetMapping
    public Result<PageResult<WebhookConfig>> page(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size) {
        return Result.ok(webhookConfigService.page(page, size));
    }

    @PostMapping
    public Result<WebhookConfig> create(@Valid @RequestBody WebhookRequest request,
                                        Authentication auth, HttpServletRequest httpRequest) {
        WebhookConfig config = webhookConfigService.create(request);
        auditService.log((Long) auth.getDetails(), auth.getName(), "CREATE", "webhook",
                "创建Webhook: " + request.getName(), httpRequest.getRemoteAddr());
        return Result.ok(config);
    }

    @PutMapping("/{id}")
    public Result<WebhookConfig> update(@PathVariable Long id, @Valid @RequestBody WebhookRequest request) {
        return Result.ok(webhookConfigService.update(id, request));
    }

    @PostMapping("/{id}/toggle")
    public Result<Void> toggle(@PathVariable Long id) {
        webhookConfigService.toggleStatus(id);
        return Result.ok();
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        webhookConfigService.delete(id);
        return Result.ok();
    }

    @GetMapping("/logs")
    public Result<PageResult<WebhookLog>> logs(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) Long webhookId) {
        return Result.ok(webhookConfigService.logs(page, size, webhookId));
    }
}
