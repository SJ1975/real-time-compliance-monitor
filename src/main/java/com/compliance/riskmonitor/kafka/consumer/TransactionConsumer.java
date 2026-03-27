package com.compliance.riskmonitor.kafka.consumer;

import com.compliance.riskmonitor.dto.TransactionEvent;
import com.compliance.riskmonitor.engine.ComplianceRuleEngine;
import com.compliance.riskmonitor.entity.Transaction;
import com.compliance.riskmonitor.repository.TransactionRepository;
import com.compliance.riskmonitor.service.TransactionSearchService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class TransactionConsumer {

    private final TransactionRepository transactionRepository;
    private final ObjectMapper objectMapper;
    private final ComplianceRuleEngine complianceRuleEngine;
    private final TransactionSearchService transactionSearchService;

    @KafkaListener(
            topics = "${app.kafka.topic.transactions}",
            groupId = "${spring.kafka.consumer.group-id}"
    )
    public void consumeTransaction(
            @Payload String message,
            @Header(KafkaHeaders.RECEIVED_PARTITION) int partition,
            @Header(KafkaHeaders.OFFSET) long offset) {

        log.info("Received Kafka message | Partition: {} | Offset: {}", partition, offset);

        try {
            TransactionEvent event = objectMapper.readValue(message, TransactionEvent.class);

            // Idempotency check
            if (transactionRepository.existsById(event.getTransactionId())) {
                log.warn("Transaction {} already processed — skipping",
                        event.getTransactionId());
                return;
            }

            // Map event → entity
            Transaction transaction = Transaction.builder()
                    .transactionId(event.getTransactionId())
                    .userId(event.getUserId())
                    .amount(event.getAmount())
                    .currency(event.getCurrency())
                    .merchant(event.getMerchant())
                    .location(event.getLocation())
                    .timestamp(event.getTimestamp())
                    .flagged(false)
                    .riskScore(0)
                    .riskLevel(Transaction.RiskLevel.LOW)
                    .build();

            // ← Run compliance rules BEFORE saving
            complianceRuleEngine.evaluate(transaction);

            // Save with compliance results applied
            transactionRepository.save(transaction);         // Save to PostgreSQL

            transactionSearchService.indexTransaction(transaction);        // Index in ElasticSearch

            if (transaction.isFlagged()) {
                log.warn("FLAGGED transaction saved | ID: {} | Score: {} | Level: {} | Reasons: {}",
                        transaction.getTransactionId(),
                        transaction.getRiskScore(),
                        transaction.getRiskLevel(),
                        transaction.getFlagReasons());
            } else {
                log.info("Clean transaction saved | ID: {}",
                        transaction.getTransactionId());
            }

        } catch (JsonProcessingException e) {
            log.error("Deserialization failed: {}", e.getMessage());
        } catch (Exception e) {
            log.error("Transaction processing failed: {}", e.getMessage(), e);
        }
    }
}