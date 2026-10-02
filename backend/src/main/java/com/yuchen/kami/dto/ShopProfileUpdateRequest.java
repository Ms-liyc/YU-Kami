package com.yuchen.kami.dto;

import jakarta.validation.constraints.Email;
import lombok.Data;

@Data
public class ShopProfileUpdateRequest {
    private String nickname;

    @Email(message = "邮箱格式不正确")
    private String email;
}
