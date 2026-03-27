package com.compliance.riskmonitor.dto;

import lombok.*;
import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SearchRequest {

    private String userId;
    private String location;
    private String merchant;
    private String riskLevel;      // LOW, MEDIUM, HIGH, CRITICAL
    private Boolean flagged;
    private BigDecimal minAmount;
    private BigDecimal maxAmount;
    private Integer size;          // max results to return (default 50)

    @Override
    public String toString() {
        return String.format(
                "SearchRequest{userId=%s, location=%s, merchant=%s, " +
                        "riskLevel=%s, flagged=%s, minAmount=%s, maxAmount=%s}",
                userId, location, merchant, riskLevel, flagged, minAmount, maxAmount
        );
    }
}