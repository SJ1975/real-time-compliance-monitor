package com.compliance.riskmonitor.engine;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RuleResult {

    private String ruleName;       // e.g. "HIGH_AMOUNT"
    private boolean triggered;     // did this rule fire?
    private int scoreContribution; // how much it adds to risk score
    private String reason;         // human-readable explanation

    // Convenience factory — rule did NOT fire
    public static RuleResult notTriggered(String ruleName) {
        return RuleResult.builder()
                .ruleName(ruleName)
                .triggered(false)
                .scoreContribution(0)
                .build();
    }

    // Convenience factory — rule fired
    public static RuleResult triggered(String ruleName, int score, String reason) {
        return RuleResult.builder()
                .ruleName(ruleName)
                .triggered(true)
                .scoreContribution(score)
                .reason(reason)
                .build();
    }
}