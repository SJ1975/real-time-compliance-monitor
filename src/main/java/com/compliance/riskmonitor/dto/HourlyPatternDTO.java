package com.compliance.riskmonitor.dto;

import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class HourlyPatternDTO {
    private int hour;
    private String timeLabel;   // e.g. "14:00 - 15:00"
    private long transactionCount;
    private BigDecimal avgAmount;
    private long flaggedCount;
}
