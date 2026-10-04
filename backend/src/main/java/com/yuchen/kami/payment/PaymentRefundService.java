package com.yuchen.kami.payment;

import com.alipay.api.AlipayApiException;
import com.alipay.api.AlipayClient;
import com.alipay.api.DefaultAlipayClient;
import com.alipay.api.request.AlipayTradeRefundRequest;
import com.wechat.pay.java.core.Config;
import com.wechat.pay.java.core.RSAAutoCertificateConfig;
import com.wechat.pay.java.service.refund.RefundService;
import com.wechat.pay.java.service.refund.model.AmountReq;
import com.wechat.pay.java.service.refund.model.CreateRequest;
import com.yuchen.kami.entity.PaymentConfig;
import com.yuchen.kami.entity.ShopOrder;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.RoundingMode;

@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentRefundService {

    private static final String PROD_GATEWAY = "https://openapi.alipay.com/gateway.do";
    private static final String SANDBOX_GATEWAY = "https://openapi-sandbox.dl.alipaydev.com/gateway.do";

    private final PaymentChannelHelper helper;

    public boolean refundAlipay(PaymentConfig config, ShopOrder order, String reason) {
        if (config == null) {
            return false;
        }
        try {
            String gateway = helper.isSandbox(config) ? SANDBOX_GATEWAY : PROD_GATEWAY;
            AlipayClient client = new DefaultAlipayClient(
                    gateway,
                    config.getAppId(),
                    getPrivateKey(config),
                    "json",
                    "UTF-8",
                    getAlipayPublicKey(config),
                    "RSA2"
            );
            AlipayTradeRefundRequest request = new AlipayTradeRefundRequest();
            String amount = order.getAmount().setScale(2, RoundingMode.HALF_UP).toPlainString();
            String refundReason = reason != null && !reason.isBlank() ? reason : "订单退款";
            request.setBizContent(String.format(
                    "{\"out_trade_no\":\"%s\",\"refund_amount\":\"%s\",\"refund_reason\":\"%s\"}",
                    order.getOrderNo(), amount, refundReason));
            var response = client.execute(request);
            return response.isSuccess();
        } catch (AlipayApiException e) {
            log.warn("支付宝退款失败: {} - {}", order.getOrderNo(), e.getMessage());
            return false;
        }
    }

    public boolean refundWechat(PaymentConfig config, ShopOrder order, String reason) {
        if (config == null) {
            return false;
        }
        try {
            Config wxConfig = buildWechatConfig(config);
            RefundService service = new RefundService.Builder().config(wxConfig).build();
            CreateRequest request = new CreateRequest();
            request.setOutTradeNo(order.getOrderNo());
            request.setOutRefundNo("REF" + order.getOrderNo());
            request.setReason(reason != null && !reason.isBlank() ? reason : "订单退款");
            AmountReq amountReq = new AmountReq();
            long fen = order.getAmount().multiply(java.math.BigDecimal.valueOf(100)).longValue();
            amountReq.setRefund(fen);
            amountReq.setTotal(fen);
            amountReq.setCurrency("CNY");
            request.setAmount(amountReq);
            service.create(request);
            return true;
        } catch (Exception e) {
            log.warn("微信退款失败: {} - {}", order.getOrderNo(), e.getMessage());
            return false;
        }
    }

    private Config buildWechatConfig(PaymentConfig config) {
        return new RSAAutoCertificateConfig.Builder()
                .merchantId(getMchId(config))
                .privateKey(getPrivateKey(config))
                .merchantSerialNumber(getSerialNo(config))
                .apiV3Key(getApiV3Key(config))
                .build();
    }

    private String getPrivateKey(PaymentConfig config) {
        String key = helper.getJsonField(config, "privateKey");
        if (key == null) {
            key = config.getAppSecret();
        }
        return key;
    }

    private String getAlipayPublicKey(PaymentConfig config) {
        return helper.getJsonField(config, "alipayPublicKey");
    }

    private String getMchId(PaymentConfig config) {
        String mchId = helper.getJsonField(config, "mchId");
        if (mchId == null) {
            mchId = helper.getJsonField(config, "merchantId");
        }
        return mchId;
    }

    private String getSerialNo(PaymentConfig config) {
        String sn = helper.getJsonField(config, "merchantSerialNumber");
        if (sn == null) {
            sn = helper.getJsonField(config, "serialNo");
        }
        return sn;
    }

    private String getApiV3Key(PaymentConfig config) {
        String key = helper.getJsonField(config, "apiV3Key");
        if (key == null) {
            key = helper.getJsonField(config, "apiV3key");
        }
        return key;
    }
}
