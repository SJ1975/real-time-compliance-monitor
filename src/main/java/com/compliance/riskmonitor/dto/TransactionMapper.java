package com.compliance.riskmonitor.dto;

import com.compliance.riskmonitor.entity.Transaction;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.UUID;

@Component
public class TransactionMapper {

    // Convert Request DTO → Entity
    public Transaction toEntity(TransactionRequest request) {
        return Transaction.builder()
                .transactionId(UUID.randomUUID().toString())
                .userId(request.getUserId())
                .amount(request.getAmount())
                .currency(request.getCurrency())
                .merchant(request.getMerchant())
                .location(request.getLocation())
                .timestamp(request.getTimestamp() != null
                        ? request.getTimestamp()
                        : LocalDateTime.now())
                .flagged(false)
                .riskScore(0)
                .riskLevel(Transaction.RiskLevel.LOW)
                .build();
    }

    // Convert Entity → Response DTO
    public TransactionResponse toResponse(Transaction transaction) {
        return TransactionResponse.builder()
                .transactionId(transaction.getTransactionId())
                .userId(transaction.getUserId())
                .amount(transaction.getAmount())
                .currency(transaction.getCurrency())
                .merchant(transaction.getMerchant())
                .location(transaction.getLocation())
                .timestamp(transaction.getTimestamp())
                .flagged(transaction.isFlagged())
                .riskScore(transaction.getRiskScore())
                .flagReasons(transaction.getFlagReasons())
                .riskLevel(transaction.getRiskLevel())
                .processedAt(transaction.getProcessedAt())
                .build();
    }
}