package com.compliance.risk_monitor.engine.rules;

import com.compliance.riskmonitor.config.AppConfig;
import com.compliance.riskmonitor.engine.RuleResult;
import com.compliance.riskmonitor.engine.rules.VelocityRule;
import com.compliance.riskmonitor.entity.Transaction;
import com.compliance.riskmonitor.repository.TransactionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@DisplayName("VelocityRule")
class VelocityRuleTest {

    @Mock private AppConfig appConfig;
    @Mock private AppConfig.Compliance compliance;
    @Mock private AppConfig.Compliance.Rules rules;
    @Mock private AppConfig.Compliance.RiskScore riskScore;
    @Mock private TransactionRepository transactionRepository;

    private VelocityRule rule;

    private static final int    WINDOW_MINUTES   = 10;
    private static final int    MAX_TRANSACTIONS = 5;
    private static final int    SCORE_WEIGHT     = 35;
    private static final String USER_ID          = "user-001";

    @BeforeEach
    void setUp() {
        when(appConfig.getCompliance()).thenReturn(compliance);
        when(compliance.getRules()).thenReturn(rules);
        when(compliance.getRiskScore()).thenReturn(riskScore);
        when(rules.getVelocityWindowMinutes()).thenReturn(WINDOW_MINUTES);
        when(rules.getVelocityMaxTransactions()).thenReturn(MAX_TRANSACTIONS);
        when(riskScore.getVelocityWeight()).thenReturn(SCORE_WEIGHT);
        rule = new VelocityRule(appConfig, transactionRepository);
    }

    private Transaction buildTransaction(String userId, LocalDateTime timestamp) {
        return Transaction.builder()
                .transactionId(UUID.randomUUID().toString())
                .userId(userId)
                .amount(new BigDecimal("500.00"))
                .currency("USD")
                .merchant("Amazon")
                .location("New York")
                .timestamp(timestamp)
                .flagged(false)
                .riskScore(0)
                .riskLevel(Transaction.RiskLevel.LOW)
                .build();
    }

    private Transaction buildTransaction(String userId) {
        return buildTransaction(userId, LocalDateTime.now());
    }

    @Test
    @DisplayName("does not fire when user has 4 previous transactions in the window")
    void doesNotFire_whenUserHas4PreviousTransactions() {
        // Arrange
        Transaction tx = buildTransaction(USER_ID);
        when(transactionRepository.countRecentTransactionsByUser(
                eq(USER_ID), any(LocalDateTime.class))).thenReturn(4L);
        // Act
        RuleResult result = rule.evaluate(tx);
        // Assert
        assertThat(result.isTriggered()).isFalse();
        assertThat(result.getScoreContribution()).isZero();
    }

    @Test
    @DisplayName("does not fire when user has 0 previous transactions")
    void doesNotFire_whenUserHasNoTransactions() {
        // Arrange
        Transaction tx = buildTransaction(USER_ID);
        when(transactionRepository.countRecentTransactionsByUser(
                eq(USER_ID), any(LocalDateTime.class))).thenReturn(0L);
        // Act
        RuleResult result = rule.evaluate(tx);
        // Assert
        assertThat(result.isTriggered()).isFalse();
    }

    @Test
    @DisplayName("fires when user has exactly 5 previous transactions in the window")
    void fires_whenUserHas5PreviousTransactions() {
        // Arrange
        Transaction tx = buildTransaction(USER_ID);
        when(transactionRepository.countRecentTransactionsByUser(
                eq(USER_ID), any(LocalDateTime.class))).thenReturn(5L);
        // Act
        RuleResult result = rule.evaluate(tx);
        // Assert
        assertThat(result.isTriggered()).isTrue();
        assertThat(result.getScoreContribution()).isEqualTo(SCORE_WEIGHT);
        assertThat(result.getRuleName()).isEqualTo("VELOCITY");
        assertThat(result.getReason()).contains(USER_ID);
    }

    @Test
    @DisplayName("fires when user has more than 5 previous transactions")
    void fires_whenUserHasMoreThan5PreviousTransactions() {
        // Arrange
        Transaction tx = buildTransaction(USER_ID);
        when(transactionRepository.countRecentTransactionsByUser(
                eq(USER_ID), any(LocalDateTime.class))).thenReturn(10L);
        // Act
        RuleResult result = rule.evaluate(tx);
        // Assert
        assertThat(result.isTriggered()).isTrue();
        assertThat(result.getScoreContribution()).isEqualTo(SCORE_WEIGHT);
    }

    @Test
    @DisplayName("passes window start time correctly — exactly 10 minutes before transaction timestamp")
    void passesCorrectWindowStart() {
        // Arrange
        LocalDateTime txTime      = LocalDateTime.of(2026, 1, 1, 12, 0, 0);
        LocalDateTime windowStart = txTime.minusMinutes(WINDOW_MINUTES);
        Transaction tx = buildTransaction(USER_ID, txTime);
        ArgumentCaptor<LocalDateTime> windowCaptor =
                ArgumentCaptor.forClass(LocalDateTime.class);
        when(transactionRepository.countRecentTransactionsByUser(
                eq(USER_ID), windowCaptor.capture())).thenReturn(0L);
        // Act
        rule.evaluate(tx);
        // Assert
        assertThat(windowCaptor.getValue()).isEqualTo(windowStart);
    }

    @Test
    @DisplayName("queries only the transaction's own userId")
    void queriesOnlyTransactionUserId() {
        // Arrange
        Transaction tx = buildTransaction(USER_ID);
        ArgumentCaptor<String> userCaptor = ArgumentCaptor.forClass(String.class);
        when(transactionRepository.countRecentTransactionsByUser(
                userCaptor.capture(), any(LocalDateTime.class))).thenReturn(0L);
        // Act
        rule.evaluate(tx);
        // Assert
        assertThat(userCaptor.getValue()).isEqualTo(USER_ID);
        verify(transactionRepository)
                .countRecentTransactionsByUser(eq(USER_ID), any(LocalDateTime.class));
    }

    @Test
    @DisplayName("does not fire for the transaction's user when their count is below threshold")
    void doesNotFire_whenCountBelowThreshold() {
        // Arrange
        Transaction tx = buildTransaction(USER_ID);
        when(transactionRepository.countRecentTransactionsByUser(
                eq(USER_ID), any(LocalDateTime.class))).thenReturn(0L);
        // Act
        RuleResult result = rule.evaluate(tx);
        // Assert
        assertThat(result.isTriggered()).isFalse();
    }

    @Test
    @DisplayName("getRuleName returns VELOCITY")
    void getRuleName_returnsVelocity() {
        assertThat(rule.getRuleName()).isEqualTo("VELOCITY");
    }
}