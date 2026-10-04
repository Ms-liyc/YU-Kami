package com.yuchen.kami.dto;

import lombok.Data;

@Data
public class OrderRefundRequest {

    /** BALANCE | ORIGINAL，默认 BALANCE */
    private String refundMode;
    private String remark;
}
