package com.yuchen.kami.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class MailTestRequest {

    @NotBlank(message = "收件邮箱不能为空")
    @Email(message = "邮箱格式不正确")
    private String to;
}
