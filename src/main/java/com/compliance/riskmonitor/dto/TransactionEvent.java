package com.compliance.riskmonitor.dto;

import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TransactionEvent {

    private String transactionId;
    private String userId;
    private BigDecimal amount;
    private String currency;
    private String merchant;
    private String location;
    private LocalDateTime timestamp;
}