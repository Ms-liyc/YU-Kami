package com.yuchen.kami.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class ApiClientVO {

    private Long id;
    private String name;
    private String appKey;
    private String appSecret;
    private Integer status;
    private LocalDateTime createdAt;
}
