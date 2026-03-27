package com.compliance.riskmonitor.kafka.producer;

import com.compliance.riskmonitor.dto.TransactionEvent;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Component;

import java.util.concurrent.CompletableFuture;

@Slf4j
@Component
@RequiredArgsConstructor
public class TransactionProducer {

    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;

    @Value("${app.kafka.topic.transactions}")
    private String transactionsTopic;

    public void sendTransaction(TransactionEvent event) {
        try {
            // Serialize event to JSON string
            String message = objectMapper.writeValueAsString(event);

            // Use userId as key — ensures same user's transactions
            // go to the same partition (ordering guarantee)
            CompletableFuture<SendResult<String, String>> future =
                    kafkaTemplate.send(transactionsTopic, event.getUserId(), message);

            future.whenComplete((result, ex) -> {
                if (ex == null) {
                    log.info("Transaction sent to Kafka | ID: {} | Partition: {} | Offset: {}",
                            event.getTransactionId(),
                            result.getRecordMetadata().partition(),
                            result.getRecordMetadata().offset());
                } else {
                    log.error("Failed to send transaction to Kafka | ID: {} | Error: {}",
                            event.getTransactionId(), ex.getMessage());
                }
            });

        } catch (JsonProcessingException e) {
            log.error("Failed to serialize transaction event: {}", e.getMessage());
            throw new RuntimeException("Failed to serialize transaction event", e);
        }
    }
}