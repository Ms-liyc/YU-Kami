package com.yuchen.kami.controller;

import com.yuchen.kami.common.Result;
import com.yuchen.kami.dto.SetupStatusDTO;
import com.yuchen.kami.service.SetupService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/setup")
@RequiredArgsConstructor
public class SetupController {

    private final SetupService setupService;

    @GetMapping("/status")
    public Result<SetupStatusDTO> status() {
        return Result.ok(setupService.getStatus());
    }
}
