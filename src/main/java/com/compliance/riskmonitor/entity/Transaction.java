package com.compliance.riskmonitor.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "transactions", indexes = {
        @Index(name = "idx_user_id", columnList = "userId"),
        @Index(name = "idx_flagged", columnList = "flagged"),
        @Index(name = "idx_timestamp", columnList = "timestamp")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Transaction {

    @Id
    @Column(name = "transaction_id", nullable = false, unique = true)
    private String transactionId;

    @Column(name = "user_id", nullable = false)
    private String userId;

    @Column(name = "amount", nullable = false, precision = 15, scale = 2)
    private BigDecimal amount;

    @Column(name = "currency", length = 3)
    private String currency;

    @Column(name = "merchant", nullable = false)
    private String merchant;

    @Column(name = "location", nullable = false)
    private String location;

    @Column(name = "timestamp", nullable = false)
    private LocalDateTime timestamp;

    // --- Compliance fields ---

    @Column(name = "flagged", nullable = false)
    private boolean flagged = false;

    @Column(name = "risk_score")
    private Integer riskScore = 0;

    // Stores which rules were triggered, e.g. "HIGH_AMOUNT,VELOCITY"
    @Column(name = "flag_reasons", length = 500)
    private String flagReasons;

    @Enumerated(EnumType.STRING)
    @Column(name = "risk_level", length = 20)
    private RiskLevel riskLevel = RiskLevel.LOW;

    @CreationTimestamp
    @Column(name = "processed_at", updatable = false)
    private LocalDateTime processedAt;

    // --- Enum for risk classification ---
    public enum RiskLevel {
        LOW,        // score 0–30
        MEDIUM,     // score 31–60
        HIGH,       // score 61–85
        CRITICAL    // score 86–100
    }
}