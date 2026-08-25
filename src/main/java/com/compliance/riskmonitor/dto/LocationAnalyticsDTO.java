package com.compliance.riskmonitor.dto;

import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class LocationAnalyticsDTO {
    private String location;
    private long totalTransactions;
    private long flaggedCount;
    private BigDecimal flaggedPercentage;
    private BigDecimal avgRiskScore;
    private String riskLabel;    // SAFE / MODERATE / DANGEROUS
}

