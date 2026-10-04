package com.yuchen.kami.service;

import com.yuchen.kami.common.BusinessException;
import com.yuchen.kami.crypto.CryptoService;
import com.yuchen.kami.dto.ImportResult;
import com.yuchen.kami.entity.CardBatch;
import com.yuchen.kami.entity.CardKey;
import com.yuchen.kami.entity.Product;
import com.yuchen.kami.mapper.CardBatchMapper;
import com.yuchen.kami.mapper.CardKeyMapper;
import lombok.RequiredArgsConstructor;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class CardImportService {

    private final CardKeyMapper cardKeyMapper;
    private final CardBatchMapper cardBatchMapper;
    private final ProductService productService;
    private final CryptoService cryptoService;

    @Transactional
    public ImportResult importCards(MultipartFile file, Long productId, String remark, Long userId) throws Exception {
        Product product = productService.getById(productId);
        List<String> keys = parseFile(file);
        if (keys.isEmpty()) {
            throw new BusinessException("文件中没有有效的卡密");
        }
        if (keys.size() > 10000) {
            throw new BusinessException("单次导入最多10000条");
        }

        CardBatch batch = new CardBatch();
        batch.setBatchNo("I" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"))
                + String.format("%04d", (int) (Math.random() * 10000)));
        batch.setProductId(product.getId());
        batch.setTotalCount(keys.size());
        batch.setUsedCount(0);
        batch.setRemark(remark != null ? remark : "批量导入");
        batch.setCreatedBy(userId);
        cardBatchMapper.insert(batch);

        int success = 0, skipped = 0, failed = 0;
        Set<String> hashSet = new HashSet<>();

        for (String plainKey : keys) {
            try {
                String normalized = plainKey.trim().toUpperCase();
                if (normalized.isEmpty()) {
                    skipped++;
                    continue;
                }
                String pepper = cryptoService.generatePepper();
                String hash = cryptoService.hashCardKey(normalized, pepper);
                if (!hashSet.add(hash)) {
                    skipped++;
                    continue;
                }
                if (cardKeyMapper.selectCount(new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<CardKey>()
                        .eq(CardKey::getKeyHash, hash)) > 0) {
                    skipped++;
                    continue;
                }

                CardKey card = new CardKey();
                card.setBatchId(batch.getId());
                card.setProductId(product.getId());
                card.setKeyHash(hash);
                card.setKeyPepper(pepper);
                card.setKeyChecksum(cryptoService.resolveChecksumForStorage(normalized));
                card.setEncryptedMeta(cryptoService.encryptMeta(
                        "{\"productCode\":\"" + product.getCode() + "\",\"batchNo\":\"" + batch.getBatchNo() + "\",\"imported\":true}"));
                card.setStatus(CardKey.STATUS_UNUSED);
                cardKeyMapper.insert(card);
                success++;
            } catch (Exception e) {
                failed++;
            }
        }

        batch.setTotalCount(success);
        cardBatchMapper.updateById(batch);

        return ImportResult.builder()
                .batchNo(batch.getBatchNo())
                .total(keys.size())
                .success(success)
                .skipped(skipped)
                .failed(failed)
                .build();
    }

    private List<String> parseFile(MultipartFile file) throws Exception {
        String filename = file.getOriginalFilename() != null ? file.getOriginalFilename().toLowerCase() : "";
        if (filename.endsWith(".xlsx") || filename.endsWith(".xls")) {
            return parseExcel(file);
        }
        return parseText(file);
    }

    private List<String> parseText(MultipartFile file) throws Exception {
        List<String> keys = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(file.getInputStream(), StandardCharsets.UTF_8))) {
            String line;
            boolean first = true;
            while ((line = reader.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty()) continue;
                if (first && (line.toLowerCase().contains("卡密") || line.toLowerCase().contains("card"))) {
                    first = false;
                    continue;
                }
                first = false;
                if (line.contains(",")) {
                    keys.add(line.split(",")[0].trim());
                } else {
                    keys.add(line);
                }
            }
        }
        return keys;
    }

    private List<String> parseExcel(MultipartFile file) throws Exception {
        List<String> keys = new ArrayList<>();
        DataFormatter formatter = new DataFormatter();
        try (Workbook workbook = WorkbookFactory.create(file.getInputStream())) {
            Sheet sheet = workbook.getSheetAt(0);
            for (int i = 0; i <= sheet.getLastRowNum(); i++) {
                Row row = sheet.getRow(i);
                if (row == null) continue;
                Cell cell = row.getCell(0);
                if (cell == null) continue;
                String value = formatter.formatCellValue(cell).trim();
                if (value.isEmpty()) continue;
                if (i == 0 && (value.contains("卡密") || value.toLowerCase().contains("card"))) {
                    continue;
                }
                keys.add(value);
            }
        }
        return keys;
    }
}
