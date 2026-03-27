package com.compliance.riskmonitor.engine;

import com.compliance.riskmonitor.entity.Transaction;

public interface ComplianceRule {

    // Evaluate the rule against a transaction
    RuleResult evaluate(Transaction transaction);

    // Rule name identifier
    String getRuleName();
}