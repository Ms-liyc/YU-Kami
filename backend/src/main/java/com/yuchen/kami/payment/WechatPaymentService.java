package com.yuchen.kami.payment;

import com.wechat.pay.java.core.Config;
import com.wechat.pay.java.core.RSAAutoCertificateConfig;
import com.wechat.pay.java.core.notification.NotificationConfig;
import com.wechat.pay.java.core.notification.NotificationParser;
import com.wechat.pay.java.core.notification.RequestParam;
import com.wechat.pay.java.service.payments.model.Transaction;
import com.wechat.pay.java.service.payments.jsapi.JsapiServiceExtension;
import com.wechat.pay.java.service.payments.jsapi.model.Payer;
import com.wechat.pay.java.service.payments.jsapi.model.PrepayWithRequestPaymentResponse;
import com.wechat.pay.java.service.payments.nativepay.NativePayService;
import com.wechat.pay.java.service.payments.nativepay.model.Amount;
import com.wechat.pay.java.service.payments.nativepay.model.PrepayRequest;
import com.wechat.pay.java.service.payments.nativepay.model.PrepayResponse;
import com.yuchen.kami.common.BusinessException;
import com.yuchen.kami.dto.JsapiPayParams;
import com.yuchen.kami.config.YuKamiProperties;
import com.yuchen.kami.entity.PaymentConfig;
import com.yuchen.kami.entity.ShopOrder;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Slf4j
@Service
@RequiredArgsConstructor
public class WechatPaymentService {

    private final PaymentChannelHelper helper;
    private final YuKamiProperties properties;

    public JsapiPayParams createJsapiPay(PaymentConfig config, ShopOrder order, String openid) {
        validateConfig(config);
        if (openid == null || openid.isBlank()) {
            throw new BusinessException("微信 JSAPI 支付需要 openid，请先完成微信授权");
        }
        try {
            Config wxConfig = buildConfig(config);
            JsapiServiceExtension service = new JsapiServiceExtension.Builder().config(wxConfig).build();

            com.wechat.pay.java.service.payments.jsapi.model.PrepayRequest request =
                    new com.wechat.pay.java.service.payments.jsapi.model.PrepayRequest();
            request.setAppid(config.getAppId());
            request.setMchid(getMchId(config));
            request.setDescription(order.getProductName());
            request.setOutTradeNo(order.getOrderNo());
            request.setNotifyUrl(notifyUrl(config));

            Payer payer = new Payer();
            payer.setOpenid(openid);
            request.setPayer(payer);

            com.wechat.pay.java.service.payments.jsapi.model.Amount amount =
                    new com.wechat.pay.java.service.payments.jsapi.model.Amount();
            amount.setTotal(toFen(order.getAmount()));
            amount.setCurrency("CNY");
            request.setAmount(amount);

            PrepayWithRequestPaymentResponse response = service.prepayWithRequestPayment(request);
            return JsapiPayParams.builder()
                    .appId(response.getAppId())
                    .timeStamp(response.getTimeStamp())
                    .nonceStr(response.getNonceStr())
                    .packageValue(response.getPackageVal())
                    .signType(response.getSignType())
                    .paySign(response.getPaySign())
                    .build();
        } catch (Exception e) {
            log.error("微信 JSAPI 下单失败", e);
            throw new BusinessException("微信 JSAPI 下单失败: " + e.getMessage());
        }
    }

    public String createNativePay(PaymentConfig config, ShopOrder order) {
        validateConfig(config);
        try {
            Config wxConfig = buildConfig(config);
            NativePayService service = new NativePayService.Builder().config(wxConfig).build();

            PrepayRequest request = new PrepayRequest();
            request.setAppid(config.getAppId());
            request.setMchid(getMchId(config));
            request.setDescription(order.getProductName());
            request.setOutTradeNo(order.getOrderNo());
            request.setNotifyUrl(notifyUrl(config));

            Amount amount = new Amount();
            amount.setTotal(toFen(order.getAmount()));
            amount.setCurrency("CNY");
            request.setAmount(amount);

            PrepayResponse response = service.prepay(request);
            return response.getCodeUrl();
        } catch (Exception e) {
            log.error("微信下单失败", e);
            throw new BusinessException("微信下单失败: " + e.getMessage());
        }
    }

    public Transaction parseNotify(PaymentConfig config, String body, String serial,
                                   String nonce, String timestamp, String signature) {
        try {
            NotificationConfig notificationConfig = (NotificationConfig) buildConfig(config);
            NotificationParser parser = new NotificationParser(notificationConfig);
            RequestParam param = new RequestParam.Builder()
                    .serialNumber(serial)
                    .nonce(nonce)
                    .signature(signature)
                    .timestamp(timestamp)
                    .body(body)
                    .build();
            return parser.parse(param, Transaction.class);
        } catch (Exception e) {
            log.error("微信回调解析失败", e);
            throw new BusinessException("微信回调验签失败");
        }
    }

    private Config buildConfig(PaymentConfig config) {
        return new RSAAutoCertificateConfig.Builder()
                .merchantId(getMchId(config))
                .privateKey(getPrivateKey(config))
                .merchantSerialNumber(getSerialNo(config))
                .apiV3Key(getApiV3Key(config))
                .build();
    }

    private void validateConfig(PaymentConfig config) {
        if (config.getAppId() == null || config.getAppId().isBlank()) {
            throw new BusinessException("请配置微信 AppID");
        }
        if (getMchId(config) == null) {
            throw new BusinessException("请配置微信商户号 (configJson.mchId)");
        }
        if (getPrivateKey(config) == null) {
            throw new BusinessException("请配置微信商户私钥 (configJson.privateKey)");
        }
        if (getSerialNo(config) == null) {
            throw new BusinessException("请配置微信证书序列号 (configJson.merchantSerialNumber)");
        }
        if (getApiV3Key(config) == null) {
            throw new BusinessException("请配置微信 APIv3 密钥 (configJson.apiV3Key)");
        }
    }

    private int toFen(BigDecimal yuan) {
        return yuan.multiply(BigDecimal.valueOf(100)).intValue();
    }

    private String notifyUrl(PaymentConfig config) {
        if (config.getNotifyUrl() != null && !config.getNotifyUrl().isBlank()) {
            return config.getNotifyUrl();
        }
        return properties.getPayment().getBaseUrl() + "/api/payment/wechat/notify";
    }

    private String getMchId(PaymentConfig config) {
        String mchId = helper.getJsonField(config, "mchId");
        if (mchId == null) mchId = helper.getJsonField(config, "merchantId");
        return mchId;
    }

    private String getPrivateKey(PaymentConfig config) {
        String key = helper.getJsonField(config, "privateKey");
        if (key == null) key = config.getAppSecret();
        return key;
    }

    private String getSerialNo(PaymentConfig config) {
        String sn = helper.getJsonField(config, "merchantSerialNumber");
        if (sn == null) sn = helper.getJsonField(config, "serialNo");
        return sn;
    }

    private String getApiV3Key(PaymentConfig config) {
        String key = helper.getJsonField(config, "apiV3Key");
        if (key == null) key = helper.getJsonField(config, "apiV3key");
        return key;
    }
}
