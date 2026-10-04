package com.yuchen.kami.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.yuchen.kami.entity.CardBatch;
import com.yuchen.kami.entity.CardKey;
import com.yuchen.kami.entity.Product;
import com.yuchen.kami.entity.RedeemRecord;
import com.yuchen.kami.entity.ShopOrder;
import com.yuchen.kami.entity.ShopUser;
import com.yuchen.kami.mapper.CardBatchMapper;
import com.yuchen.kami.mapper.CardKeyMapper;
import com.yuchen.kami.mapper.ProductMapper;
import com.yuchen.kami.mapper.RedeemRecordMapper;
import com.yuchen.kami.mapper.ShopOrderMapper;
import com.yuchen.kami.mapper.ShopUserMapper;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ExportService {

    private final CardKeyMapper cardKeyMapper;
    private final CardBatchMapper cardBatchMapper;
    private final RedeemRecordMapper redeemRecordMapper;
    private final ProductMapper productMapper;
    private final ShopOrderMapper shopOrderMapper;
    private final ShopUserMapper shopUserMapper;

    public void exportOrders(HttpServletResponse response, String status) throws Exception {
        LambdaQueryWrapper<ShopOrder> wrapper = new LambdaQueryWrapper<>();
        if (status != null && !status.isBlank()) {
            wrapper.eq(ShopOrder::getStatus, status);
        }
        wrapper.orderByDesc(ShopOrder::getCreatedAt).last("LIMIT 50000");

        Map<Long, String> userMap = shopUserMapper.selectList(null).stream()
                .collect(Collectors.toMap(ShopUser::getId, ShopUser::getUsername));

        response.setContentType("text/csv;charset=UTF-8");
        response.setHeader("Content-Disposition", "attachment; filename=orders_export.csv");
        response.getOutputStream().write(new byte[]{(byte) 0xEF, (byte) 0xBB, (byte) 0xBF});

        PrintWriter writer = new PrintWriter(response.getOutputStream(), true, StandardCharsets.UTF_8);
        writer.println("订单号,用户,类型,产品,金额,状态,支付方式,创建时间,支付时间");
        for (ShopOrder order : shopOrderMapper.selectList(wrapper)) {
            writer.printf("%s,%s,%s,%s,%s,%s,%s,%s,%s%n",
                    order.getOrderNo(),
                    userMap.getOrDefault(order.getUserId(), ""),
                    nullSafe(order.getOrderType()),
                    nullSafe(order.getProductName()),
                    order.getAmount(),
                    nullSafe(order.getStatus()),
                    nullSafe(order.getPaymentMethod()),
                    nullSafe(order.getCreatedAt()),
                    nullSafe(order.getPaidAt()));
        }
        writer.flush();
    }

    public void exportCards(HttpServletResponse response, Long batchId, Integer status) throws Exception {
        LambdaQueryWrapper<CardKey> wrapper = new LambdaQueryWrapper<>();
        if (batchId != null) wrapper.eq(CardKey::getBatchId, batchId);
        if (status != null) wrapper.eq(CardKey::getStatus, status);
        wrapper.orderByDesc(CardKey::getCreatedAt).last("LIMIT 50000");

        Map<Long, String> productMap = productMapper.selectList(null).stream()
                .collect(Collectors.toMap(Product::getId, Product::getName));

        response.setContentType("text/csv;charset=UTF-8");
        response.setHeader("Content-Disposition", "attachment; filename=cards_export.csv");
        response.getOutputStream().write(new byte[]{(byte) 0xEF, (byte) 0xBB, (byte) 0xBF});

        PrintWriter writer = new PrintWriter(response.getOutputStream(), true, StandardCharsets.UTF_8);
        writer.println("ID,批次ID,产品,校验码,状态,兑换用户,兑换时间,创建时间");
        for (CardKey card : cardKeyMapper.selectList(wrapper)) {
            writer.printf("%s,%s,%s,%s,%s,%s,%s,%s%n",
                    card.getId(), card.getBatchId(),
                    productMap.getOrDefault(card.getProductId(), ""),
                    card.getKeyChecksum(), statusLabel(card.getStatus()),
                    nullSafe(card.getRedeemUser()), nullSafe(card.getRedeemAt()),
                    card.getCreatedAt());
        }
        writer.flush();
    }

    public void exportRedeemRecords(HttpServletResponse response) throws Exception {
        response.setContentType("text/csv;charset=UTF-8");
        response.setHeader("Content-Disposition", "attachment; filename=redeem_records.csv");
        response.getOutputStream().write(new byte[]{(byte) 0xEF, (byte) 0xBB, (byte) 0xBF});

        PrintWriter writer = new PrintWriter(response.getOutputStream(), true, StandardCharsets.UTF_8);
        writer.println("ID,用户,结果,消息,IP,时间");
        redeemRecordMapper.selectList(new LambdaQueryWrapper<RedeemRecord>()
                .orderByDesc(RedeemRecord::getCreatedAt).last("LIMIT 50000"))
                .forEach(r -> writer.printf("%s,%s,%s,%s,%s,%s%n",
                        r.getId(), r.getRedeemUser(), r.getResult(),
                        nullSafe(r.getMessage()), nullSafe(r.getRedeemIp()), r.getCreatedAt()));
        writer.flush();
    }

    public void exportBatches(HttpServletResponse response) throws Exception {
        Map<Long, String> productMap = productMapper.selectList(null).stream()
                .collect(Collectors.toMap(Product::getId, Product::getName));

        response.setContentType("text/csv;charset=UTF-8");
        response.setHeader("Content-Disposition", "attachment; filename=batches_export.csv");
        response.getOutputStream().write(new byte[]{(byte) 0xEF, (byte) 0xBB, (byte) 0xBF});

        PrintWriter writer = new PrintWriter(response.getOutputStream(), true, StandardCharsets.UTF_8);
        writer.println("批次号,产品,总数,已用,使用率,前缀,备注,创建时间");
        for (CardBatch batch : cardBatchMapper.selectList(
                new LambdaQueryWrapper<CardBatch>().orderByDesc(CardBatch::getCreatedAt))) {
            double rate = batch.getTotalCount() > 0
                    ? batch.getUsedCount() * 100.0 / batch.getTotalCount() : 0;
            writer.printf("%s,%s,%d,%d,%.1f%%,%s,%s,%s%n",
                    batch.getBatchNo(), productMap.getOrDefault(batch.getProductId(), ""),
                    batch.getTotalCount(), batch.getUsedCount(), rate,
                    nullSafe(batch.getPrefix()), nullSafe(batch.getRemark()), batch.getCreatedAt());
        }
        writer.flush();
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

    private String nullSafe(Object val) {
        return val == null ? "" : val.toString().replace(",", " ");
    }
}
