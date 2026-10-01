package com.yuchen.kami.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class RedeemRequest {

    @NotBlank(message = "卡密不能为空")
    private String cardKey;

    @NotBlank(message = "用户标识不能为空")
    private String redeemUser;

    private String signature;
    private Long timestamp;
}
