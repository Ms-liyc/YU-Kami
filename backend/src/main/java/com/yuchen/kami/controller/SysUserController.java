package com.yuchen.kami.controller;

import com.yuchen.kami.common.PageResult;
import com.yuchen.kami.common.Result;
import com.yuchen.kami.dto.UserCreateRequest;
import com.yuchen.kami.dto.UserVO;
import com.yuchen.kami.service.AuditService;
import com.yuchen.kami.service.SysUserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/admin/users")
@RequiredArgsConstructor
@PreAuthorize("hasRole('SUPER_ADMIN')")
public class SysUserController {

    private final SysUserService sysUserService;
    private final AuditService auditService;

    @GetMapping
    public Result<PageResult<UserVO>> page(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size) {
        return Result.ok(sysUserService.page(page, size));
    }

    @PostMapping
    public Result<UserVO> create(@Valid @RequestBody UserCreateRequest request,
                                 Authentication auth, HttpServletRequest httpRequest) {
        UserVO vo = sysUserService.create(request);
        auditService.log((Long) auth.getDetails(), auth.getName(), "CREATE", "sys_user",
                "创建用户: " + request.getUsername(), httpRequest.getRemoteAddr());
        return Result.ok(vo);
    }

    @PostMapping("/{id}/toggle")
    public Result<Void> toggle(@PathVariable Long id, Authentication auth) {
        sysUserService.toggleStatus(id, (Long) auth.getDetails());
        return Result.ok();
    }

    @PostMapping("/{id}/reset-password")
    public Result<Void> resetPassword(@PathVariable Long id, @RequestBody Map<String, String> body) {
        sysUserService.resetPassword(id, body.get("password"));
        return Result.ok();
    }
}
