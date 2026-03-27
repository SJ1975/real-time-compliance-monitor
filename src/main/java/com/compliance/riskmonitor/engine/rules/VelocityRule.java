package com.compliance.riskmonitor.engine.rules;

import com.compliance.riskmonitor.config.AppConfig;
import com.compliance.riskmonitor.engine.ComplianceRule;
import com.compliance.riskmonitor.engine.RuleResult;
import com.compliance.riskmonitor.entity.Transaction;
import com.compliance.riskmonitor.repository.TransactionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Slf4j
@Component
@RequiredArgsConstructor
public class VelocityRule implements ComplianceRule {

    private final AppConfig appConfig;
    private final TransactionRepository transactionRepository;

    @Override
    public String getRuleName() {
        return "VELOCITY";
    }

    @Override
    public RuleResult evaluate(Transaction transaction) {
        var rules = appConfig.getCompliance().getRules();

        int windowMinutes = rules.getVelocityWindowMinutes();
        int maxAllowed    = rules.getVelocityMaxTransactions();

        // Look back X minutes from this transaction's timestamp
        LocalDateTime windowStart = transaction.getTimestamp()
                .minusMinutes(windowMinutes);

        long recentCount = transactionRepository.countRecentTransactionsByUser(
                transaction.getUserId(), windowStart
        );

        if (recentCount >= maxAllowed) {
            String reason = String.format(
                    "User %s made %d transactions in the last %d minutes (max allowed: %d)",
                    transaction.getUserId(), recentCount, windowMinutes, maxAllowed
            );
            log.warn("VELOCITY rule triggered | UserId: {} | Count: {} in {} mins",
                    transaction.getUserId(), recentCount, windowMinutes);

            return RuleResult.triggered(
                    getRuleName(),
                    appConfig.getCompliance().getRiskScore().getVelocityWeight(),
                    reason
            );
        }

        return RuleResult.notTriggered(getRuleName());
    }
}