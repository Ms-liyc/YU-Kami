package com.yuchen.kami.dto;

import lombok.Builder;
import lombok.Data;

/** 微信 JSAPI 调起支付参数（公众号/微信内 H5） */
@Data
@Builder
public class JsapiPayParams {

    private String appId;
    private String timeStamp;
    private String nonceStr;
    private String packageValue;
    private String signType;
    private String paySign;
}
