package com.yuchen.kami.controller;

import com.yuchen.kami.service.ExcelExportService;
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
    private final ExcelExportService excelExportService;

    @GetMapping("/cards")
    public void exportCards(HttpServletResponse response,
                            @RequestParam(required = false) Long batchId,
                            @RequestParam(required = false) Integer status,
                            @RequestParam(defaultValue = "csv") String format) throws Exception {
        if ("xlsx".equalsIgnoreCase(format)) {
            excelExportService.exportCards(response, batchId, status);
        } else {
            exportService.exportCards(response, batchId, status);
        }
    }

    @GetMapping("/redeem-records")
    public void exportRedeemRecords(HttpServletResponse response,
                                    @RequestParam(defaultValue = "csv") String format) throws Exception {
        if ("xlsx".equalsIgnoreCase(format)) {
            excelExportService.exportRedeemRecords(response);
        } else {
            exportService.exportRedeemRecords(response);
        }
    }

    @GetMapping("/batches")
    public void exportBatches(HttpServletResponse response,
                              @RequestParam(defaultValue = "csv") String format) throws Exception {
        if ("xlsx".equalsIgnoreCase(format)) {
            excelExportService.exportBatches(response);
        } else {
            exportService.exportBatches(response);
        }
    }
}
