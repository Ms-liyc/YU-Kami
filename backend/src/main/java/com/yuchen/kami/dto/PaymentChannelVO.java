package com.yuchen.kami.dto;

import com.yuchen.kami.entity.PaymentConfig;
import lombok.Data;

@Data
public class PaymentChannelVO {

    private String channel;
    private String label;

    public static PaymentChannelVO from(PaymentConfig config) {
        PaymentChannelVO vo = new PaymentChannelVO();
        vo.setChannel(config.getChannel());
        vo.setLabel(channelLabel(config.getChannel()));
        return vo;
    }

    private static String channelLabel(String channel) {
        return switch (channel) {
            case "ALIPAY" -> "支付宝";
            case "WECHAT" -> "微信支付";
            case "BALANCE" -> "余额支付";
            default -> channel;
        };
    }

    public static PaymentChannelVO balance() {
        PaymentChannelVO vo = new PaymentChannelVO();
        vo.setChannel("BALANCE");
        vo.setLabel("余额支付");
        return vo;
    }
}
