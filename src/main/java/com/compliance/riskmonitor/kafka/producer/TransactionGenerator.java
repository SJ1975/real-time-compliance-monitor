package com.compliance.riskmonitor.kafka.producer;

import com.compliance.riskmonitor.dto.TransactionEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Random;
import java.util.UUID;
                                                                  //#runs on a schedule and sends mock transactions automatically.
@Slf4j
@Component
@RequiredArgsConstructor
public class TransactionGenerator {

    private final TransactionProducer transactionProducer;
    private final Random random = new Random();

    // Mock data pools
    private static final List<String> USER_IDS = List.of(
            "user-001", "user-002", "user-003",
            "user-004", "user-005", "user-006"
    );

    private static final List<String> MERCHANTS = List.of(
            "Amazon", "Netflix", "Walmart", "Apple Store",
            "Casino Royale", "Unknown Vendor", "Shell Gas",
            "Starbucks", "Best Buy", "Steam"
    );

    private static final List<String> LOCATIONS = List.of(
            "New York", "Los Angeles", "Chicago", "Houston",
            "Iran",         // suspicious — triggers rule
            "North Korea",  // suspicious — triggers rule
            "London", "Toronto", "Sydney", "Dubai"
    );

    private static final List<String> CURRENCIES = List.of(
            "USD", "EUR", "GBP", "AUD"
    );

    /**
     * Generates a random transaction every 5 seconds.
     * Disabled by default — enable via application.yml
     */
    @Scheduled(fixedRateString = "${app.generator.rate-ms:5000}")
    public void generateTransaction() {
        try {
            TransactionEvent event = buildRandomEvent();
            transactionProducer.sendTransaction(event);
            log.info("Generated transaction | User: {} | Amount: {} | Location: {}",
                    event.getUserId(), event.getAmount(), event.getLocation());
        } catch (Exception e) {
            log.error("Transaction generation failed: {}", e.getMessage());
        }
    }

    private TransactionEvent buildRandomEvent() {
        // 10% chance of high amount (triggers HIGH_AMOUNT rule)
        BigDecimal amount = random.nextInt(10) == 0
                ? randomAmount(10001, 50000)
                : randomAmount(10, 5000);

        return TransactionEvent.builder()
                .transactionId(UUID.randomUUID().toString())
                .userId(randomFrom(USER_IDS))
                .amount(amount)
                .currency(randomFrom(CURRENCIES))
                .merchant(randomFrom(MERCHANTS))
                .location(randomFrom(LOCATIONS))
                .timestamp(LocalDateTime.now())
                .build();
    }

    private BigDecimal randomAmount(int min, int max) {
        double value = min + (max - min) * random.nextDouble();
        return BigDecimal.valueOf(value).setScale(2, RoundingMode.HALF_UP);
    }

    private <T> T randomFrom(List<T> list) {
        return list.get(random.nextInt(list.size()));
    }
}