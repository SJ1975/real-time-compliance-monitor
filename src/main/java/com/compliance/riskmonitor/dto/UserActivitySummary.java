package com.compliance.riskmonitor.dto;

import lombok.*;
import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserActivitySummary {

    private String userId;
    private long totalTransactions;
    private BigDecimal totalAmount;
    private BigDecimal averageAmount;
    private long flaggedTransactions;
    private String riskProfile;   // LOW / MEDIUM / HIGH based on flagged ratio
}