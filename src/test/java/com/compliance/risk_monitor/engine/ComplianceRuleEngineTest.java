package com.compliance.risk_monitor.engine;

import com.compliance.riskmonitor.engine.ComplianceRule;
import com.compliance.riskmonitor.engine.ComplianceRuleEngine;
import com.compliance.riskmonitor.engine.RuleResult;
import com.compliance.riskmonitor.entity.Transaction;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("ComplianceRuleEngine")
class ComplianceRuleEngineTest {

    private Transaction buildTransaction() {
        return Transaction.builder()
                .transactionId(UUID.randomUUID().toString())
                .userId("user-001")
                .amount(new BigDecimal("500.00"))
                .currency("USD")
                .merchant("Amazon")
                .location("New York")
                .timestamp(LocalDateTime.now())
                .flagged(false)
                .riskScore(0)
                .riskLevel(Transaction.RiskLevel.LOW)
                .build();
    }

    private RuleResult triggered(String name, int score) {
        return RuleResult.triggered(name, score, "Test reason for " + name);
    }

    private RuleResult notTriggered(String name) {
        return RuleResult.notTriggered(name);
    }

    @Test
    @DisplayName("transaction stays clean when no rule fires")
    void transactionStaysClean_whenNoRuleFires() {
        // Arrange
        ComplianceRule ruleA = mock(ComplianceRule.class);
        ComplianceRule ruleB = mock(ComplianceRule.class);
        Transaction tx = buildTransaction();
        when(ruleA.evaluate(tx)).thenReturn(notTriggered("RULE_A"));
        when(ruleB.evaluate(tx)).thenReturn(notTriggered("RULE_B"));
        ComplianceRuleEngine engine = new ComplianceRuleEngine(List.of(ruleA, ruleB));
        // Act
        engine.evaluate(tx);
        // Assert
        assertThat(tx.isFlagged()).isFalse();
        assertThat(tx.getRiskScore()).isZero();
        assertThat(tx.getRiskLevel()).isEqualTo(Transaction.RiskLevel.LOW);
        assertThat(tx.getFlagReasons()).isNull();
    }

    @Test
    @DisplayName("score 40 level MEDIUM when only HIGH_AMOUNT fires")
    void score40_levelMedium_whenOnlyHighAmountFires() {
        // Arrange
        ComplianceRule highAmount = mock(ComplianceRule.class);
        Transaction tx = buildTransaction();
        when(highAmount.evaluate(tx)).thenReturn(triggered("HIGH_AMOUNT", 40));
        ComplianceRuleEngine engine = new ComplianceRuleEngine(List.of(highAmount));
        // Act
        engine.evaluate(tx);
        // Assert
        assertThat(tx.isFlagged()).isTrue();
        assertThat(tx.getRiskScore()).isEqualTo(40);
        assertThat(tx.getRiskLevel()).isEqualTo(Transaction.RiskLevel.MEDIUM);
        assertThat(tx.getFlagReasons()).isEqualTo("HIGH_AMOUNT");
    }

    @Test
    @DisplayName("score 35 level MEDIUM when only VELOCITY fires")
    void score35_levelMedium_whenOnlyVelocityFires() {
        // Arrange
        ComplianceRule velocity = mock(ComplianceRule.class);
        Transaction tx = buildTransaction();
        when(velocity.evaluate(tx)).thenReturn(triggered("VELOCITY", 35));
        ComplianceRuleEngine engine = new ComplianceRuleEngine(List.of(velocity));
        // Act
        engine.evaluate(tx);
        // Assert
        assertThat(tx.isFlagged()).isTrue();
        assertThat(tx.getRiskScore()).isEqualTo(35);
        assertThat(tx.getRiskLevel()).isEqualTo(Transaction.RiskLevel.MEDIUM);
    }

    @Test
    @DisplayName("score 25 level LOW when only SUSPICIOUS_LOCATION fires")
    void score25_levelLow_whenOnlySuspiciousLocationFires() {
        // Arrange
        ComplianceRule location = mock(ComplianceRule.class);
        Transaction tx = buildTransaction();
        when(location.evaluate(tx)).thenReturn(triggered("SUSPICIOUS_LOCATION", 25));
        ComplianceRuleEngine engine = new ComplianceRuleEngine(List.of(location));
        // Act
        engine.evaluate(tx);
        // Assert
        assertThat(tx.isFlagged()).isTrue();
        assertThat(tx.getRiskScore()).isEqualTo(25);
        assertThat(tx.getRiskLevel()).isEqualTo(Transaction.RiskLevel.LOW);
    }

    @Test
    @DisplayName("score 75 level HIGH when HIGH_AMOUNT + VELOCITY fire (40+35)")
    void score75_levelHigh_whenHighAmountAndVelocityFire() {
        // Arrange
        ComplianceRule highAmount = mock(ComplianceRule.class);
        ComplianceRule velocity   = mock(ComplianceRule.class);
        Transaction tx = buildTransaction();
        when(highAmount.evaluate(tx)).thenReturn(triggered("HIGH_AMOUNT", 40));
        when(velocity.evaluate(tx)).thenReturn(triggered("VELOCITY", 35));
        ComplianceRuleEngine engine = new ComplianceRuleEngine(List.of(highAmount, velocity));
        // Act
        engine.evaluate(tx);
        // Assert
        assertThat(tx.isFlagged()).isTrue();
        assertThat(tx.getRiskScore()).isEqualTo(75);
        assertThat(tx.getRiskLevel()).isEqualTo(Transaction.RiskLevel.HIGH);
    }

    @Test
    @DisplayName("score 65 level HIGH when HIGH_AMOUNT + SUSPICIOUS_LOCATION fire (40+25)")
    void score65_levelHigh_whenHighAmountAndLocationFire() {
        // Arrange
        ComplianceRule highAmount = mock(ComplianceRule.class);
        ComplianceRule location   = mock(ComplianceRule.class);
        Transaction tx = buildTransaction();
        when(highAmount.evaluate(tx)).thenReturn(triggered("HIGH_AMOUNT", 40));
        when(location.evaluate(tx)).thenReturn(triggered("SUSPICIOUS_LOCATION", 25));
        ComplianceRuleEngine engine = new ComplianceRuleEngine(List.of(highAmount, location));
        // Act
        engine.evaluate(tx);
        // Assert
        assertThat(tx.getRiskScore()).isEqualTo(65);
        assertThat(tx.getRiskLevel()).isEqualTo(Transaction.RiskLevel.HIGH);
    }

    @Test
    @DisplayName("score 60 level MEDIUM when VELOCITY + SUSPICIOUS_LOCATION fire (35+25)")
    void score60_levelMedium_whenVelocityAndLocationFire() {
        // Arrange
        ComplianceRule velocity = mock(ComplianceRule.class);
        ComplianceRule location = mock(ComplianceRule.class);
        Transaction tx = buildTransaction();
        when(velocity.evaluate(tx)).thenReturn(triggered("VELOCITY", 35));
        when(location.evaluate(tx)).thenReturn(triggered("SUSPICIOUS_LOCATION", 25));
        ComplianceRuleEngine engine = new ComplianceRuleEngine(List.of(velocity, location));
        // Act
        engine.evaluate(tx);
        // Assert
        assertThat(tx.getRiskScore()).isEqualTo(60);
        assertThat(tx.getRiskLevel()).isEqualTo(Transaction.RiskLevel.MEDIUM);
    }

    @Test
    @DisplayName("score 100 level CRITICAL when all three rules fire (40+35+25)")
    void score100_levelCritical_whenAllThreeRulesFire() {
        // Arrange
        ComplianceRule highAmount = mock(ComplianceRule.class);
        ComplianceRule velocity   = mock(ComplianceRule.class);
        ComplianceRule location   = mock(ComplianceRule.class);
        Transaction tx = buildTransaction();
        when(highAmount.evaluate(tx)).thenReturn(triggered("HIGH_AMOUNT", 40));
        when(velocity.evaluate(tx)).thenReturn(triggered("VELOCITY", 35));
        when(location.evaluate(tx)).thenReturn(triggered("SUSPICIOUS_LOCATION", 25));
        ComplianceRuleEngine engine = new ComplianceRuleEngine(
                List.of(highAmount, velocity, location));
        // Act
        engine.evaluate(tx);
        // Assert
        assertThat(tx.isFlagged()).isTrue();
        assertThat(tx.getRiskScore()).isEqualTo(100);
        assertThat(tx.getRiskLevel()).isEqualTo(Transaction.RiskLevel.CRITICAL);
        assertThat(tx.getFlagReasons()).contains("HIGH_AMOUNT", "VELOCITY", "SUSPICIOUS_LOCATION");
    }

    @Test
    @DisplayName("score is capped at 100 even when rules sum above 100")
    void score_isCappedAt100() {
        // Arrange
        ComplianceRule ruleA = mock(ComplianceRule.class);
        ComplianceRule ruleB = mock(ComplianceRule.class);
        Transaction tx = buildTransaction();
        when(ruleA.evaluate(tx)).thenReturn(triggered("RULE_A", 80));
        when(ruleB.evaluate(tx)).thenReturn(triggered("RULE_B", 80));
        ComplianceRuleEngine engine = new ComplianceRuleEngine(List.of(ruleA, ruleB));
        // Act
        engine.evaluate(tx);
        // Assert
        assertThat(tx.getRiskScore()).isEqualTo(100);
        assertThat(tx.getRiskLevel()).isEqualTo(Transaction.RiskLevel.CRITICAL);
    }

    @Test
    @DisplayName("score 30 maps to LOW (upper boundary)")
    void score30_mapsToLow() {
        ComplianceRule rule = mock(ComplianceRule.class);
        Transaction tx = buildTransaction();
        when(rule.evaluate(tx)).thenReturn(triggered("RULE", 30));
        new ComplianceRuleEngine(List.of(rule)).evaluate(tx);
        assertThat(tx.getRiskLevel()).isEqualTo(Transaction.RiskLevel.LOW);
    }

    @Test
    @DisplayName("score 31 maps to MEDIUM (lower boundary)")
    void score31_mapsToMedium() {
        ComplianceRule rule = mock(ComplianceRule.class);
        Transaction tx = buildTransaction();
        when(rule.evaluate(tx)).thenReturn(triggered("RULE", 31));
        new ComplianceRuleEngine(List.of(rule)).evaluate(tx);
        assertThat(tx.getRiskLevel()).isEqualTo(Transaction.RiskLevel.MEDIUM);
    }

    @Test
    @DisplayName("score 60 maps to MEDIUM (upper boundary)")
    void score60_mapsToMedium() {
        ComplianceRule rule = mock(ComplianceRule.class);
        Transaction tx = buildTransaction();
        when(rule.evaluate(tx)).thenReturn(triggered("RULE", 60));
        new ComplianceRuleEngine(List.of(rule)).evaluate(tx);
        assertThat(tx.getRiskLevel()).isEqualTo(Transaction.RiskLevel.MEDIUM);
    }

    @Test
    @DisplayName("score 61 maps to HIGH (lower boundary)")
    void score61_mapsToHigh() {
        ComplianceRule rule = mock(ComplianceRule.class);
        Transaction tx = buildTransaction();
        when(rule.evaluate(tx)).thenReturn(triggered("RULE", 61));
        new ComplianceRuleEngine(List.of(rule)).evaluate(tx);
        assertThat(tx.getRiskLevel()).isEqualTo(Transaction.RiskLevel.HIGH);
    }

    @Test
    @DisplayName("score 85 maps to HIGH (upper boundary)")
    void score85_mapsToHigh() {
        ComplianceRule rule = mock(ComplianceRule.class);
        Transaction tx = buildTransaction();
        when(rule.evaluate(tx)).thenReturn(triggered("RULE", 85));
        new ComplianceRuleEngine(List.of(rule)).evaluate(tx);
        assertThat(tx.getRiskLevel()).isEqualTo(Transaction.RiskLevel.HIGH);
    }

    @Test
    @DisplayName("score 86 maps to CRITICAL (lower boundary)")
    void score86_mapsToCritical() {
        ComplianceRule rule = mock(ComplianceRule.class);
        Transaction tx = buildTransaction();
        when(rule.evaluate(tx)).thenReturn(triggered("RULE", 86));
        new ComplianceRuleEngine(List.of(rule)).evaluate(tx);
        assertThat(tx.getRiskLevel()).isEqualTo(Transaction.RiskLevel.CRITICAL);
    }
}