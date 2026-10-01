package com.yuchen.kami.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ImportResult {

    private String batchNo;
    private int total;
    private int success;
    private int skipped;
    private int failed;
}
