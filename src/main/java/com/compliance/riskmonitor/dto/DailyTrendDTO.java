package com.compliance.riskmonitor.dto;

import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class DailyTrendDTO {
    private String date;
    private long totalTransactions;
    private BigDecimal totalAmount;
    private BigDecimal averageAmount;
    private long flaggedCount;
}
