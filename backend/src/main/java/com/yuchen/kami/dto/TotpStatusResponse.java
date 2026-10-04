package com.yuchen.kami.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class TotpStatusResponse {
    private boolean enabled;
}
