package com.yuchen.kami.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class GenerateBatchRequest {

    @NotNull(message = "产品ID不能为空")
    private Long productId;

    @NotNull(message = "生成数量不能为空")
    @Min(value = 1, message = "至少生成1张")
    @Max(value = 10000, message = "单次最多生成10000张")
    private Integer count;

    private String prefix;
    private String remark;
    private LocalDateTime expireAt;
}
