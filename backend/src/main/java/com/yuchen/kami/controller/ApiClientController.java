package com.yuchen.kami.controller;

import com.yuchen.kami.common.PageResult;
import com.yuchen.kami.common.Result;
import com.yuchen.kami.dto.ApiClientVO;
import com.yuchen.kami.service.ApiClientService;
import com.yuchen.kami.service.AuditService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/admin/api-clients")
@RequiredArgsConstructor
@PreAuthorize("hasRole('SUPER_ADMIN')")
public class ApiClientController {

    private final ApiClientService apiClientService;
    private final AuditService auditService;

    @GetMapping
    public Result<PageResult<ApiClientVO>> page(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size) {
        return Result.ok(apiClientService.page(page, size));
    }

    @PostMapping
    public Result<ApiClientVO> create(@RequestBody Map<String, String> body,
                                      Authentication auth, HttpServletRequest request) {
        ApiClientVO vo = apiClientService.create(body.get("name"));
        auditService.log((Long) auth.getDetails(), auth.getName(), "CREATE", "api_client",
                "创建API客户端: " + body.get("name"), request.getRemoteAddr());
        return Result.ok(vo);
    }

    @PostMapping("/{id}/toggle")
    public Result<Void> toggle(@PathVariable Long id) {
        apiClientService.toggleStatus(id);
        return Result.ok();
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        apiClientService.delete(id);
        return Result.ok();
    }
}
