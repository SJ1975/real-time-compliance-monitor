package com.compliance.risk_monitor.engine.rules;

import com.compliance.riskmonitor.config.AppConfig;
import com.compliance.riskmonitor.engine.RuleResult;
import com.compliance.riskmonitor.engine.rules.HighAmountRule;
import com.compliance.riskmonitor.entity.Transaction;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@DisplayName("HighAmountRule")
class HighAmountRuleTest {

    @Mock private AppConfig appConfig;
    @Mock private AppConfig.Compliance compliance;
    @Mock private AppConfig.Compliance.Rules rules;
    @Mock private AppConfig.Compliance.RiskScore riskScore;

    private HighAmountRule rule;

    private static final BigDecimal THRESHOLD   = new BigDecimal("10000.00");
    private static final int        SCORE_WEIGHT = 40;

    @BeforeEach
    void setUp() {
        when(appConfig.getCompliance()).thenReturn(compliance);
        when(compliance.getRules()).thenReturn(rules);
        when(compliance.getRiskScore()).thenReturn(riskScore);
        when(rules.getHighAmountThreshold()).thenReturn(THRESHOLD);
        when(riskScore.getHighAmountWeight()).thenReturn(SCORE_WEIGHT);
        rule = new HighAmountRule(appConfig);
    }

    private Transaction buildTransaction(BigDecimal amount) {
        return Transaction.builder()
                .transactionId(UUID.randomUUID().toString())
                .userId("user-001")
                .amount(amount)
                .currency("USD")
                .merchant("Test Merchant")
                .location("New York")
                .timestamp(LocalDateTime.now())
                .flagged(false)
                .riskScore(0)
                .riskLevel(Transaction.RiskLevel.LOW)
                .build();
    }

    @Test
    @DisplayName("does not fire when amount is below threshold (9999.99)")
    void doesNotFire_whenAmountIsBelowThreshold() {
        // Arrange
        Transaction tx = buildTransaction(new BigDecimal("9999.99"));
        // Act
        RuleResult result = rule.evaluate(tx);
        // Assert
        assertThat(result.isTriggered()).isFalse();
        assertThat(result.getScoreContribution()).isZero();
    }

    @Test
    @DisplayName("does not fire when amount equals threshold exactly (10000.00)")
    void doesNotFire_whenAmountEqualsThreshold() {
        // Arrange
        Transaction tx = buildTransaction(new BigDecimal("10000.00"));
        // Act
        RuleResult result = rule.evaluate(tx);
        // Assert
        assertThat(result.isTriggered()).isFalse();
        assertThat(result.getScoreContribution()).isZero();
    }

    @Test
    @DisplayName("does not fire when amount is zero")
    void doesNotFire_whenAmountIsZero() {
        // Arrange
        Transaction tx = buildTransaction(BigDecimal.ZERO);
        // Act
        RuleResult result = rule.evaluate(tx);
        // Assert
        assertThat(result.isTriggered()).isFalse();
    }

    @Test
    @DisplayName("fires when amount is one cent above threshold (10000.01)")
    void fires_whenAmountIsOneAboveThreshold() {
        // Arrange
        Transaction tx = buildTransaction(new BigDecimal("10000.01"));
        // Act
        RuleResult result = rule.evaluate(tx);
        // Assert
        assertThat(result.isTriggered()).isTrue();
        assertThat(result.getScoreContribution()).isEqualTo(SCORE_WEIGHT);
        assertThat(result.getRuleName()).isEqualTo("HIGH_AMOUNT");
        assertThat(result.getReason()).isNotBlank();
    }

    @ParameterizedTest(name = "amount = {0}")
    @ValueSource(strings = {"10000.01", "15000.00", "25000.00", "100000.00"})
    @DisplayName("fires for several amounts clearly above threshold")
    void fires_forAmountsClearlyAboveThreshold(String amount) {
        // Arrange
        Transaction tx = buildTransaction(new BigDecimal(amount));
        // Act
        RuleResult result = rule.evaluate(tx);
        // Assert
        assertThat(result.isTriggered()).isTrue();
        assertThat(result.getScoreContribution()).isEqualTo(SCORE_WEIGHT);
    }

    @Test
    @DisplayName("getRuleName returns HIGH_AMOUNT")
    void getRuleName_returnsHighAmount() {
        assertThat(rule.getRuleName()).isEqualTo("HIGH_AMOUNT");
    }
}