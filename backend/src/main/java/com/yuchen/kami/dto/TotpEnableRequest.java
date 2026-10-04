package com.yuchen.kami.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class TotpEnableRequest {

    @NotBlank(message = "验证码不能为空")
    private String totpCode;
}
