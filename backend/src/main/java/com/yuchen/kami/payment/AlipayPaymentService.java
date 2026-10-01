package com.yuchen.kami.payment;

import com.alipay.api.AlipayApiException;
import com.alipay.api.AlipayClient;
import com.alipay.api.DefaultAlipayClient;
import com.alipay.api.internal.util.AlipaySignature;
import com.alipay.api.request.AlipayTradePagePayRequest;
import com.alipay.api.request.AlipayTradeQueryRequest;
import com.yuchen.kami.common.BusinessException;
import com.yuchen.kami.config.YuKamiProperties;
import com.yuchen.kami.entity.PaymentConfig;
import com.yuchen.kami.entity.ShopOrder;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.RoundingMode;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class AlipayPaymentService {

    private static final String PROD_GATEWAY = "https://openapi.alipay.com/gateway.do";
    private static final String SANDBOX_GATEWAY = "https://openapi-sandbox.dl.alipaydev.com/gateway.do";

    private final PaymentChannelHelper helper;
    private final YuKamiProperties properties;

    public String createPagePay(PaymentConfig config, ShopOrder order) {
        validateConfig(config);
        try {
            AlipayClient client = buildClient(config);
            AlipayTradePagePayRequest request = new AlipayTradePagePayRequest();
            request.setNotifyUrl(notifyUrl(config));
            request.setReturnUrl(properties.getPayment().getReturnUrl() + "?orderId=" + order.getId());
            String amount = order.getAmount().setScale(2, RoundingMode.HALF_UP).toPlainString();
            request.setBizContent(String.format(
                    "{\"out_trade_no\":\"%s\",\"product_code\":\"FAST_INSTANT_TRADE_PAY\","
                            + "\"total_amount\":\"%s\",\"subject\":\"%s\"}",
                    order.getOrderNo(), amount, order.getProductName()));
            return client.pageExecute(request, "GET").getBody();
        } catch (AlipayApiException e) {
            log.error("支付宝下单失败", e);
            throw new BusinessException("支付宝下单失败: " + e.getMessage());
        }
    }

    public boolean verifyNotify(PaymentConfig config, Map<String, String> params) {
        try {
            String alipayPublicKey = getAlipayPublicKey(config);
            return AlipaySignature.rsaCheckV1(params, alipayPublicKey, "UTF-8", "RSA2");
        } catch (AlipayApiException e) {
            log.error("支付宝验签失败", e);
            return false;
        }
    }

    public boolean isTradeSuccess(PaymentConfig config, String orderNo) {
        try {
            AlipayClient client = buildClient(config);
            AlipayTradeQueryRequest request = new AlipayTradeQueryRequest();
            request.setBizContent("{\"out_trade_no\":\"" + orderNo + "\"}");
            var response = client.execute(request);
            return response.isSuccess()
                    && ("TRADE_SUCCESS".equals(response.getTradeStatus())
                    || "TRADE_FINISHED".equals(response.getTradeStatus()));
        } catch (AlipayApiException e) {
            return false;
        }
    }

    private AlipayClient buildClient(PaymentConfig config) {
        String gateway = helper.isSandbox(config) ? SANDBOX_GATEWAY : PROD_GATEWAY;
        return new DefaultAlipayClient(
                gateway,
                config.getAppId(),
                getPrivateKey(config),
                "json",
                "UTF-8",
                getAlipayPublicKey(config),
                "RSA2"
        );
    }

    private void validateConfig(PaymentConfig config) {
        if (config.getAppId() == null || config.getAppId().isBlank()) {
            throw new BusinessException("请配置支付宝 AppID");
        }
        if (getPrivateKey(config) == null) {
            throw new BusinessException("请配置支付宝应用私钥 (configJson.privateKey)");
        }
        if (getAlipayPublicKey(config) == null) {
            throw new BusinessException("请配置支付宝公钥 (configJson.alipayPublicKey)");
        }
    }

    private String notifyUrl(PaymentConfig config) {
        if (config.getNotifyUrl() != null && !config.getNotifyUrl().isBlank()) {
            return config.getNotifyUrl();
        }
        return properties.getPayment().getBaseUrl() + "/api/payment/alipay/notify";
    }

    private String getPrivateKey(PaymentConfig config) {
        String key = helper.getJsonField(config, "privateKey");
        if (key == null) key = config.getAppSecret();
        return key;
    }

    private String getAlipayPublicKey(PaymentConfig config) {
        return helper.getJsonField(config, "alipayPublicKey");
    }
}
