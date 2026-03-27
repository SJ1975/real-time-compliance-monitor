package com.compliance.riskmonitor.dto;

import com.compliance.riskmonitor.entity.Transaction.RiskLevel;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TransactionResponse {

    private String transactionId;
    private String userId;
    private BigDecimal amount;
    private String currency;
    private String merchant;
    private String location;
    private LocalDateTime timestamp;

    // Compliance fields
    private boolean flagged;
    private Integer riskScore;
    private String flagReasons;
    private RiskLevel riskLevel;
    private LocalDateTime processedAt;
}