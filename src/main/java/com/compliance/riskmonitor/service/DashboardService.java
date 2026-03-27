package com.compliance.riskmonitor.service;

import com.compliance.riskmonitor.dto.DashboardSummary;
import com.compliance.riskmonitor.entity.Transaction;
import com.compliance.riskmonitor.repository.TransactionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class DashboardService {

    private final TransactionRepository transactionRepository;

    @Transactional(readOnly = true)
    public DashboardSummary getSummary() {
        log.info("Building dashboard summary");

        List<Transaction> all = transactionRepository.findAll();

        long total = all.size();
        long flagged = all.stream().filter(Transaction::isFlagged).count();
        long clean = total - flagged;

        // Total volume
        BigDecimal totalVolume = all.stream()
                .map(Transaction::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // Average amount
        BigDecimal avgAmount = total > 0
                ? totalVolume.divide(
                BigDecimal.valueOf(total), 2, RoundingMode.HALF_UP)
                : BigDecimal.ZERO;

        // Flagged percentage
        double flaggedPct = total > 0
                ? (double) flagged / total * 100
                : 0.0;

        // Count by risk level
        Map<String, Long> byRiskLevel = all.stream()
                .collect(Collectors.groupingBy(
                        t -> t.getRiskLevel().name(),
                        Collectors.counting()
                ));

        // Top 5 flagged users
        Map<String, Long> topFlaggedUsers = all.stream()
                .filter(Transaction::isFlagged)
                .collect(Collectors.groupingBy(
                        Transaction::getUserId,
                        Collectors.counting()
                ))
                .entrySet().stream()
                .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
                .limit(5)
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        Map.Entry::getValue,
                        (e1, e2) -> e1,
                        LinkedHashMap::new
                ));

        return DashboardSummary.builder()
                .totalTransactions(total)
                .flaggedTransactions(flagged)
                .cleanTransactions(clean)
                .flaggedPercentage(
                        Math.round(flaggedPct * 100.0) / 100.0)
                .totalVolume(totalVolume)
                .averageTransactionAmount(avgAmount)
                .transactionsByRiskLevel(byRiskLevel)
                .topFlaggedUsers(topFlaggedUsers)
                .build();
    }

    @Transactional(readOnly = true)
    public Map<String, Long> getRiskBreakdown() {
        List<Transaction> all = transactionRepository.findAll();

        // Ordered map: CRITICAL first
        Map<String, Long> breakdown = new LinkedHashMap<>();
        breakdown.put("CRITICAL", 0L);
        breakdown.put("HIGH", 0L);
        breakdown.put("MEDIUM", 0L);
        breakdown.put("LOW", 0L);

        all.forEach(t -> breakdown.merge(
                t.getRiskLevel().name(), 1L, Long::sum)
        );

        return breakdown;
    }
}