package com.compliance.riskmonitor.dto;

import lombok.*;
import java.math.BigDecimal;
import java.util.Map;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DashboardSummary {

    // Overall counts
    private long totalTransactions;
    private long flaggedTransactions;
    private long cleanTransactions;
    private double flaggedPercentage;

    // Amount stats
    private BigDecimal totalVolume;
    private BigDecimal averageTransactionAmount;

    // Risk breakdown
    private Map<String, Long> transactionsByRiskLevel;

    // Top flagged users
    private Map<String, Long> topFlaggedUsers;
}