package com.compliance.riskmonitor.engine;

import com.compliance.riskmonitor.entity.Transaction;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Component
@RequiredArgsConstructor
public class ComplianceRuleEngine {

    // Spring auto-injects ALL ComplianceRule implementations
    private final List<ComplianceRule> rules;

    public void evaluate(Transaction transaction) {
        log.info("Running compliance rules on transaction: {}",
                transaction.getTransactionId());

        // Run every rule and collect triggered ones
        List<RuleResult> triggeredRules = rules.stream()
                .map(rule -> rule.evaluate(transaction))
                .filter(RuleResult::isTriggered)
                .collect(Collectors.toList());

        if (triggeredRules.isEmpty()) {
            log.info("Transaction {} passed all compliance rules — no flags",
                    transaction.getTransactionId());
            return;
        }

        // Calculate total risk score (cap at 100)
        int totalScore = triggeredRules.stream()
                .mapToInt(RuleResult::getScoreContribution)
                .sum();
        totalScore = Math.min(totalScore, 100);

        // Build flag reasons string
        String flagReasons = triggeredRules.stream()
                .map(RuleResult::getRuleName)
                .collect(Collectors.joining(","));

        // Determine risk level from score
        Transaction.RiskLevel riskLevel = calculateRiskLevel(totalScore);

        // Apply results to transaction
        transaction.setFlagged(true);
        transaction.setRiskScore(totalScore);
        transaction.setFlagReasons(flagReasons);
        transaction.setRiskLevel(riskLevel);

        log.warn("Transaction {} FLAGGED | Score: {} | Level: {} | Reasons: {}",
                transaction.getTransactionId(),
                totalScore,
                riskLevel,
                flagReasons);
    }

    private Transaction.RiskLevel calculateRiskLevel(int score) {
        if (score >= 86) return Transaction.RiskLevel.CRITICAL;
        if (score >= 61) return Transaction.RiskLevel.HIGH;
        if (score >= 31) return Transaction.RiskLevel.MEDIUM;
        return Transaction.RiskLevel.LOW;
    }
}