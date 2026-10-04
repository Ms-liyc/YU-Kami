package com.yuchen.kami.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
public class SmsTestRequest {

    @NotBlank(message = "手机号不能为空")
    @Pattern(regexp = "^(1\\d{10}|\\+?\\d{8,15})$", message = "手机号格式不正确")
    private String phone;
}
