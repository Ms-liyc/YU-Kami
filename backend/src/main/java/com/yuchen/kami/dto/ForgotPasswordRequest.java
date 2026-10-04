package com.yuchen.kami.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class ForgotPasswordRequest {

    @NotBlank(message = "用户名不能为空")
    private String username;
}
