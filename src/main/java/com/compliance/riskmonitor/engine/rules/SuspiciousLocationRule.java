package com.compliance.riskmonitor.engine.rules;

import com.compliance.riskmonitor.config.AppConfig;
import com.compliance.riskmonitor.engine.ComplianceRule;
import com.compliance.riskmonitor.engine.RuleResult;
import com.compliance.riskmonitor.entity.Transaction;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class SuspiciousLocationRule implements ComplianceRule {

    private final AppConfig appConfig;

    @Override
    public String getRuleName() {
        return "SUSPICIOUS_LOCATION";
    }

    @Override
    public RuleResult evaluate(Transaction transaction) {
        List<String> suspiciousLocations = appConfig.getCompliance()
                .getRules()
                .getSuspiciousLocations();

        boolean isSuspicious = suspiciousLocations.stream()
                .anyMatch(location ->
                        transaction.getLocation()
                                .equalsIgnoreCase(location.trim())
                );

        if (isSuspicious) {
            String reason = String.format(
                    "Transaction originated from flagged location: %s",
                    transaction.getLocation()
            );
            log.warn("SUSPICIOUS_LOCATION rule triggered | TransactionId: {} | Location: {}",
                    transaction.getTransactionId(), transaction.getLocation());

            return RuleResult.triggered(
                    getRuleName(),
                    appConfig.getCompliance().getRiskScore().getSuspiciousLocationWeight(),
                    reason
            );
        }

        return RuleResult.notTriggered(getRuleName());
    }
}