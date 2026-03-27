package com.compliance.riskmonitor.engine.rules;

import com.compliance.riskmonitor.config.AppConfig;
import com.compliance.riskmonitor.engine.ComplianceRule;
import com.compliance.riskmonitor.engine.RuleResult;
import com.compliance.riskmonitor.entity.Transaction;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class HighAmountRule implements ComplianceRule {

    private final AppConfig appConfig;

    @Override
    public String getRuleName() {
        return "HIGH_AMOUNT";
    }

    @Override
    public RuleResult evaluate(Transaction transaction) {
        var threshold = appConfig.getCompliance().getRules().getHighAmountThreshold();

        if (transaction.getAmount().compareTo(threshold) > 0) {
            String reason = String.format(
                    "Transaction amount %.2f exceeds threshold %.2f",
                    transaction.getAmount(), threshold
            );
            log.warn("HIGH_AMOUNT rule triggered | TransactionId: {} | Amount: {}",
                    transaction.getTransactionId(), transaction.getAmount());

            return RuleResult.triggered(
                    getRuleName(),
                    appConfig.getCompliance().getRiskScore().getHighAmountWeight(),
                    reason
            );
        }

        return RuleResult.notTriggered(getRuleName());
    }
}