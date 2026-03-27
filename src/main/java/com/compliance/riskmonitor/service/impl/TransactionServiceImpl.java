package com.compliance.riskmonitor.service.impl;

import com.compliance.riskmonitor.dto.*;
import com.compliance.riskmonitor.entity.Transaction;
import com.compliance.riskmonitor.exception.TransactionNotFoundException;
import com.compliance.riskmonitor.kafka.producer.TransactionProducer;
import com.compliance.riskmonitor.repository.TransactionRepository;
import com.compliance.riskmonitor.service.TransactionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class TransactionServiceImpl implements TransactionService {

    private final TransactionRepository transactionRepository;
    private final TransactionMapper transactionMapper;
    private final TransactionProducer transactionProducer;

    @Override
//    @Transactional
    public TransactionResponse createTransaction(TransactionRequest request) {
        log.info("Creating transaction for user: {}", request.getUserId());

        // Build the entity to generate transactionId and timestamp
        Transaction transaction = transactionMapper.toEntity(request);

        // Build Kafka event from entity
        TransactionEvent event = TransactionEvent.builder()
                .transactionId(transaction.getTransactionId())
                .userId(transaction.getUserId())
                .amount(transaction.getAmount())
                .currency(transaction.getCurrency())
                .merchant(transaction.getMerchant())
                .location(transaction.getLocation())
                .timestamp(transaction.getTimestamp())
                .build();

        // Send to Kafka — consumer will save to DB
        transactionProducer.sendTransaction(event);

        log.info("Transaction {} sent to Kafka", transaction.getTransactionId());

        // Return immediately — DB save happens async via consumer
        return transactionMapper.toResponse(transaction);
    }

    @Override
    @Transactional(readOnly = true)
    public TransactionResponse getTransactionById(String transactionId) {
        log.debug("Fetching transaction: {}", transactionId);

        Transaction transaction = transactionRepository.findById(transactionId)
                .orElseThrow(() -> new TransactionNotFoundException(transactionId));

        return transactionMapper.toResponse(transaction);
    }

    @Override
    @Transactional(readOnly = true)
    public List<TransactionResponse> getAllTransactions() {
        log.debug("Fetching all transactions");

        return transactionRepository.findAll()
                .stream()
                .map(transactionMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<TransactionResponse> getFlaggedTransactions() {
        log.debug("Fetching all flagged transactions");

        return transactionRepository.findByFlaggedTrueOrderByRiskScoreDesc()
                .stream()
                .map(transactionMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<TransactionResponse> getTransactionsByUser(String userId) {
        log.debug("Fetching transactions for user: {}", userId);

        return transactionRepository.findByUserIdOrderByTimestampDesc(userId)
                .stream()
                .map(transactionMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public UserActivitySummary getUserActivitySummary(String userId) {
        log.debug("Building activity summary for user: {}", userId);

        List<Object[]> results = transactionRepository.getUserActivitySummary(userId);

        // Handle case where user has no transactions at all
        if (results == null || results.isEmpty()) {
            return UserActivitySummary.builder()
                    .userId(userId)
                    .totalTransactions(0)
                    .totalAmount(BigDecimal.ZERO)
                    .averageAmount(BigDecimal.ZERO)
                    .flaggedTransactions(0)
                    .riskProfile("LOW")
                    .build();
        }

        // Get the single result row
        Object[] row = results.get(0);

        long totalTransactions = row[0] != null ? ((Number) row[0]).longValue() : 0L;

        // SUM can return BigDecimal or Double depending on JPA — handle both
        BigDecimal totalAmount = BigDecimal.ZERO;
        if (row[1] != null) {
            totalAmount = row[1] instanceof BigDecimal
                    ? (BigDecimal) row[1]
                    : BigDecimal.valueOf(((Number) row[1]).doubleValue());
        }

        // AVG always comes back as Double from JPA
        BigDecimal avgAmount = BigDecimal.ZERO;
        if (row[2] != null) {
            avgAmount = BigDecimal.valueOf(((Number) row[2]).doubleValue())
                    .setScale(2, RoundingMode.HALF_UP);
        }

        long flaggedCount = row[3] != null ? ((Number) row[3]).longValue() : 0L;

        String riskProfile = "LOW";
        if (totalTransactions > 0) {
            double flaggedRatio = (double) flaggedCount / totalTransactions;
            if (flaggedRatio >= 0.5)      riskProfile = "HIGH";
            else if (flaggedRatio >= 0.2) riskProfile = "MEDIUM";
        }

        return UserActivitySummary.builder()
                .userId(userId)
                .totalTransactions(totalTransactions)
                .totalAmount(totalAmount)
                .averageAmount(avgAmount)
                .flaggedTransactions(flaggedCount)
                .riskProfile(riskProfile)
                .build();
    }
}