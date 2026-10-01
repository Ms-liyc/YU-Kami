package com.yuchen.kami.controller;

import com.yuchen.kami.service.ExportService;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/export")
@RequiredArgsConstructor
public class ExportController {

    private final ExportService exportService;

    @GetMapping("/cards")
    public void exportCards(HttpServletResponse response,
                            @RequestParam(required = false) Long batchId,
                            @RequestParam(required = false) Integer status) throws Exception {
        exportService.exportCards(response, batchId, status);
    }

    @GetMapping("/redeem-records")
    public void exportRedeemRecords(HttpServletResponse response) throws Exception {
        exportService.exportRedeemRecords(response);
    }

    @GetMapping("/batches")
    public void exportBatches(HttpServletResponse response) throws Exception {
        exportService.exportBatches(response);
    }
}
