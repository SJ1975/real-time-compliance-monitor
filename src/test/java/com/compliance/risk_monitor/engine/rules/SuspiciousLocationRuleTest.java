package com.compliance.risk_monitor.engine.rules;

import com.compliance.riskmonitor.config.AppConfig;
import com.compliance.riskmonitor.engine.RuleResult;
import com.compliance.riskmonitor.engine.rules.SuspiciousLocationRule;
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
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@DisplayName("SuspiciousLocationRule")
class SuspiciousLocationRuleTest {

    @Mock private AppConfig appConfig;
    @Mock private AppConfig.Compliance compliance;
    @Mock private AppConfig.Compliance.Rules rules;
    @Mock private AppConfig.Compliance.RiskScore riskScore;

    private SuspiciousLocationRule rule;

    private static final List<String> SUSPICIOUS_LOCATIONS =
            List.of("North Korea", "Iran", "Syria");
    private static final int SCORE_WEIGHT = 25;

    @BeforeEach
    void setUp() {
        when(appConfig.getCompliance()).thenReturn(compliance);
        when(compliance.getRules()).thenReturn(rules);
        when(compliance.getRiskScore()).thenReturn(riskScore);
        when(rules.getSuspiciousLocations()).thenReturn(SUSPICIOUS_LOCATIONS);
        when(riskScore.getSuspiciousLocationWeight()).thenReturn(SCORE_WEIGHT);
        rule = new SuspiciousLocationRule(appConfig);
    }

    private Transaction buildTransaction(String location) {
        return Transaction.builder()
                .transactionId(UUID.randomUUID().toString())
                .userId("user-001")
                .amount(new BigDecimal("200.00"))
                .currency("USD")
                .merchant("Unknown Vendor")
                .location(location)
                .timestamp(LocalDateTime.now())
                .flagged(false)
                .riskScore(0)
                .riskLevel(Transaction.RiskLevel.LOW)
                .build();
    }

    @ParameterizedTest(name = "location = \"{0}\"")
    @ValueSource(strings = {"North Korea", "Iran", "Syria"})
    @DisplayName("fires for each listed suspicious location")
    void fires_forEachListedSuspiciousLocation(String location) {
        // Arrange
        Transaction tx = buildTransaction(location);
        // Act
        RuleResult result = rule.evaluate(tx);
        // Assert
        assertThat(result.isTriggered()).isTrue();
        assertThat(result.getScoreContribution()).isEqualTo(SCORE_WEIGHT);
        assertThat(result.getRuleName()).isEqualTo("SUSPICIOUS_LOCATION");
        assertThat(result.getReason()).contains(location);
    }

    @ParameterizedTest(name = "location = \"{0}\"")
    @ValueSource(strings = {"London", "New York", "Tokyo", "Dubai", "Sydney"})
    @DisplayName("does not fire for normal locations")
    void doesNotFire_forNormalLocations(String location) {
        // Arrange
        Transaction tx = buildTransaction(location);
        // Act
        RuleResult result = rule.evaluate(tx);
        // Assert
        assertThat(result.isTriggered()).isFalse();
        assertThat(result.getScoreContribution()).isZero();
    }

    @ParameterizedTest(name = "location = \"{0}\"")
    @ValueSource(strings = {"iran", "IRAN", "Iran", "iRaN"})
    @DisplayName("fires regardless of case — rule uses equalsIgnoreCase")
    void fires_caseInsensitive(String location) {
        // Arrange
        Transaction tx = buildTransaction(location);
        // Act
        RuleResult result = rule.evaluate(tx);
        // Assert
        assertThat(result.isTriggered()).isTrue();
    }

    @Test
    @DisplayName("BUG: does not fire when transaction location has trailing whitespace")
    void bug_doesNotFire_whenLocationHasTrailingWhitespace() {
        // Arrange — "Iran " has trailing space
        Transaction tx = buildTransaction("Iran ");
        // Act
        RuleResult result = rule.evaluate(tx);
        // Assert — documents the bug: trim() applied to config value only, not tx location
        // Fix would be: transaction.getLocation().trim().equalsIgnoreCase(location.trim())
        assertThat(result.isTriggered())
                .as("BUG: 'Iran ' not caught because trim() " +
                        "is only applied to the config value, not transaction.getLocation()")
                .isFalse();
    }

    @Test
    @DisplayName("BUG: does not fire when transaction location has leading whitespace")
    void bug_doesNotFire_whenLocationHasLeadingWhitespace() {
        // Arrange — " Iran" has leading space
        Transaction tx = buildTransaction(" Iran");
        // Act
        RuleResult result = rule.evaluate(tx);
        // Assert — same bug as above
        assertThat(result.isTriggered())
                .as("BUG: ' Iran' not caught — same root cause as trailing whitespace")
                .isFalse();
    }

    @Test
    @DisplayName("getRuleName returns SUSPICIOUS_LOCATION")
    void getRuleName_returnsSuspiciousLocation() {
        assertThat(rule.getRuleName()).isEqualTo("SUSPICIOUS_LOCATION");
    }
}