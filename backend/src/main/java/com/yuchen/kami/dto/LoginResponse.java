package com.yuchen.kami.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class LoginResponse {

    private String token;
    private String username;
    private String nickname;
    private String role;
    /** 是否仍在使用默认密码，提示尽快修改 */
    private boolean warnDefaultPassword;
}
