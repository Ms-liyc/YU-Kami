package com.yuchen.kami.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class CardDeliveryResult {

    private String plainKey;
    private Long cardId;
}
