package com.yuchen.kami.controller;

import com.yuchen.kami.common.Result;
import com.yuchen.kami.dto.RedeemRequest;
import com.yuchen.kami.dto.RedeemResponse;
import com.yuchen.kami.service.RedeemService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class RedeemController {

    private final RedeemService redeemService;

    @PostMapping("/redeem")
    public Result<RedeemResponse> redeem(@Valid @RequestBody RedeemRequest request,
                                         HttpServletRequest httpRequest) {
        String ip = httpRequest.getRemoteAddr();
        String userAgent = httpRequest.getHeader("User-Agent");
        return Result.ok(redeemService.redeem(request, ip, userAgent));
    }
}
