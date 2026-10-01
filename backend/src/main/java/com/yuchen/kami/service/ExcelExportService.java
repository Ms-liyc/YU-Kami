package com.yuchen.kami.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.yuchen.kami.entity.CardBatch;
import com.yuchen.kami.entity.CardKey;
import com.yuchen.kami.entity.Product;
import com.yuchen.kami.entity.RedeemRecord;
import com.yuchen.kami.mapper.CardBatchMapper;
import com.yuchen.kami.mapper.CardKeyMapper;
import com.yuchen.kami.mapper.ProductMapper;
import com.yuchen.kami.mapper.RedeemRecordMapper;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ExcelExportService {

    private final CardKeyMapper cardKeyMapper;
    private final CardBatchMapper cardBatchMapper;
    private final RedeemRecordMapper redeemRecordMapper;
    private final ProductMapper productMapper;

    public void exportCards(HttpServletResponse response, Long batchId, Integer status) throws Exception {
        LambdaQueryWrapper<CardKey> wrapper = new LambdaQueryWrapper<>();
        if (batchId != null) wrapper.eq(CardKey::getBatchId, batchId);
        if (status != null) wrapper.eq(CardKey::getStatus, status);
        wrapper.orderByDesc(CardKey::getCreatedAt).last("LIMIT 50000");

        Map<Long, String> productMap = productMapper.selectList(null).stream()
                .collect(Collectors.toMap(Product::getId, Product::getName));

        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("卡密列表");
            CellStyle headerStyle = createHeaderStyle(workbook);
            String[] headers = {"ID", "批次ID", "产品", "校验码", "状态", "兑换用户", "兑换时间", "创建时间"};
            createHeaderRow(sheet, headers, headerStyle);

            int rowIdx = 1;
            for (CardKey card : cardKeyMapper.selectList(wrapper)) {
                Row row = sheet.createRow(rowIdx++);
                setCell(row, 0, String.valueOf(card.getId()));
                setCell(row, 1, String.valueOf(card.getBatchId()));
                setCell(row, 2, productMap.getOrDefault(card.getProductId(), ""));
                setCell(row, 3, card.getKeyChecksum());
                setCell(row, 4, statusLabel(card.getStatus()));
                setCell(row, 5, card.getRedeemUser());
                setCell(row, 6, card.getRedeemAt() != null ? card.getRedeemAt().toString() : "");
                setCell(row, 7, card.getCreatedAt() != null ? card.getCreatedAt().toString() : "");
            }
            for (int i = 0; i < headers.length; i++) sheet.autoSizeColumn(i);
            writeWorkbook(response, workbook, "cards_export.xlsx");
        }
    }

    public void exportRedeemRecords(HttpServletResponse response) throws Exception {
        List<RedeemRecord> records = redeemRecordMapper.selectList(
                new LambdaQueryWrapper<RedeemRecord>().orderByDesc(RedeemRecord::getCreatedAt).last("LIMIT 50000"));

        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("兑换记录");
            CellStyle headerStyle = createHeaderStyle(workbook);
            String[] headers = {"ID", "用户", "结果", "消息", "IP", "时间"};
            createHeaderRow(sheet, headers, headerStyle);

            int rowIdx = 1;
            for (RedeemRecord r : records) {
                Row row = sheet.createRow(rowIdx++);
                setCell(row, 0, String.valueOf(r.getId()));
                setCell(row, 1, r.getRedeemUser());
                setCell(row, 2, r.getResult());
                setCell(row, 3, r.getMessage());
                setCell(row, 4, r.getRedeemIp());
                setCell(row, 5, r.getCreatedAt() != null ? r.getCreatedAt().toString() : "");
            }
            for (int i = 0; i < headers.length; i++) sheet.autoSizeColumn(i);
            writeWorkbook(response, workbook, "redeem_records.xlsx");
        }
    }

    public void exportBatches(HttpServletResponse response) throws Exception {
        Map<Long, String> productMap = productMapper.selectList(null).stream()
                .collect(Collectors.toMap(Product::getId, Product::getName));

        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("批次列表");
            CellStyle headerStyle = createHeaderStyle(workbook);
            String[] headers = {"批次号", "产品", "总数", "已用", "使用率", "前缀", "备注", "创建时间"};
            createHeaderRow(sheet, headers, headerStyle);

            int rowIdx = 1;
            for (CardBatch batch : cardBatchMapper.selectList(
                    new LambdaQueryWrapper<CardBatch>().orderByDesc(CardBatch::getCreatedAt))) {
                Row row = sheet.createRow(rowIdx++);
                double rate = batch.getTotalCount() > 0
                        ? batch.getUsedCount() * 100.0 / batch.getTotalCount() : 0;
                setCell(row, 0, batch.getBatchNo());
                setCell(row, 1, productMap.getOrDefault(batch.getProductId(), ""));
                setCell(row, 2, String.valueOf(batch.getTotalCount()));
                setCell(row, 3, String.valueOf(batch.getUsedCount()));
                setCell(row, 4, String.format("%.1f%%", rate));
                setCell(row, 5, batch.getPrefix());
                setCell(row, 6, batch.getRemark());
                setCell(row, 7, batch.getCreatedAt() != null ? batch.getCreatedAt().toString() : "");
            }
            for (int i = 0; i < headers.length; i++) sheet.autoSizeColumn(i);
            writeWorkbook(response, workbook, "batches_export.xlsx");
        }
    }

    private CellStyle createHeaderStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        Font font = workbook.createFont();
        font.setBold(true);
        font.setColor(IndexedColors.WHITE.getIndex());
        style.setFont(font);
        style.setFillForegroundColor(IndexedColors.INDIGO.getIndex());
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        return style;
    }

    private void createHeaderRow(Sheet sheet, String[] headers, CellStyle style) {
        Row row = sheet.createRow(0);
        for (int i = 0; i < headers.length; i++) {
            Cell cell = row.createCell(i);
            cell.setCellValue(headers[i]);
            cell.setCellStyle(style);
        }
    }

    private void setCell(Row row, int col, String value) {
        Cell cell = row.createCell(col);
        cell.setCellValue(value != null ? value : "");
    }

    private void writeWorkbook(HttpServletResponse response, Workbook workbook, String filename) throws Exception {
        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response.setHeader("Content-Disposition", "attachment; filename=" + filename);
        workbook.write(response.getOutputStream());
    }

    private String statusLabel(int status) {
        return switch (status) {
            case 0 -> "未使用";
            case 1 -> "已使用";
            case 2 -> "已作废";
            case 3 -> "已过期";
            default -> "未知";
        };
    }
}
