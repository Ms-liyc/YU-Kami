package com.yuchen.kami.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class WebhookRequest {

    @NotBlank(message = "名称不能为空")
    private String name;

    @NotBlank(message = "URL不能为空")
    private String url;

    private String secret;
    private String events;
}
