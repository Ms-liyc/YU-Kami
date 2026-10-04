package com.yuchen.kami.controller;

import com.yuchen.kami.common.ClientIpUtils;
import com.yuchen.kami.common.Result;
import com.yuchen.kami.dto.MailStatusDTO;
import com.yuchen.kami.dto.MailTestRequest;
import com.yuchen.kami.service.EmailService;
import com.yuchen.kami.service.RateLimitService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/mail")
@RequiredArgsConstructor
@PreAuthorize("hasRole('SUPER_ADMIN')")
public class MailController {

    private final EmailService emailService;
    private final RateLimitService rateLimitService;

    @GetMapping("/status")
    public Result<MailStatusDTO> status() {
        return Result.ok(emailService.getStatus());
    }

    @PostMapping("/test")
    public Result<Void> sendTest(@Valid @RequestBody MailTestRequest request,
                                 Authentication auth,
                                 HttpServletRequest httpRequest) {
        Long userId = (Long) auth.getDetails();
        rateLimitService.checkMailTestLimit(userId, ClientIpUtils.resolve(httpRequest));
        emailService.sendTestEmail(request.getTo());
        return Result.ok();
    }
}
