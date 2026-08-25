package com.compliance.riskmonitor.service;

import com.compliance.riskmonitor.dto.DailyTrendDTO;
import com.compliance.riskmonitor.dto.DashboardSummary;
import com.compliance.riskmonitor.dto.HourlyPatternDTO;
import com.compliance.riskmonitor.dto.LocationAnalyticsDTO;
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

    @Transactional(readOnly = true)
    public List<DailyTrendDTO> getDailyTrends() {
        log.info("Building daily trends report");

        List<Object[]> results = transactionRepository.getDailyTrends();

        return results.stream().map(row -> {
            BigDecimal totalAmount = row[2] != null
                    ? new BigDecimal(row[2].toString())
                    : BigDecimal.ZERO;
            BigDecimal avgAmount = row[3] != null
                    ? new BigDecimal(row[3].toString())
                    .setScale(2, RoundingMode.HALF_UP)
                    : BigDecimal.ZERO;

            return DailyTrendDTO.builder()
                    .date(row[0] != null ? row[0].toString() : "Unknown")
                    .totalTransactions(((Number) row[1]).longValue())
                    .totalAmount(totalAmount)
                    .averageAmount(avgAmount)
                    .flaggedCount(((Number) row[4]).longValue())
                    .build();
        }).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<HourlyPatternDTO> getHourlyPatterns() {
        log.info("Building hourly patterns report");

        List<Object[]> results = transactionRepository.getHourlyPatterns();

        return results.stream().map(row -> {
            int hour = ((Number) row[0]).intValue();
            BigDecimal avgAmount = row[2] != null
                    ? new BigDecimal(row[2].toString())
                    .setScale(2, RoundingMode.HALF_UP)
                    : BigDecimal.ZERO;

            return HourlyPatternDTO.builder()
                    .hour(hour)
                    .timeLabel(String.format("%02d:00 - %02d:00", hour, hour + 1))
                    .transactionCount(((Number) row[1]).longValue())
                    .avgAmount(avgAmount)
                    .flaggedCount(((Number) row[3]).longValue())
                    .build();
        }).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<LocationAnalyticsDTO> getLocationAnalytics() {
        log.info("Building location analytics report");

        List<Object[]> results = transactionRepository.getLocationAnalytics();

        return results.stream().map(row -> {
            long total   = ((Number) row[1]).longValue();
            long flagged = ((Number) row[2]).longValue();

            BigDecimal avgRiskScore = row[3] != null
                    ? new BigDecimal(row[3].toString())
                    .setScale(2, RoundingMode.HALF_UP)
                    : BigDecimal.ZERO;

            BigDecimal flaggedPct = total > 0
                    ? BigDecimal.valueOf(flagged * 100.0 / total)
                    .setScale(2, RoundingMode.HALF_UP)
                    : BigDecimal.ZERO;

            // Classify location risk
            String riskLabel;
            if (flaggedPct.doubleValue() >= 50)       riskLabel = "DANGEROUS";
            else if (flaggedPct.doubleValue() >= 20)  riskLabel = "MODERATE";
            else                                       riskLabel = "SAFE";

            return LocationAnalyticsDTO.builder()
                    .location(row[0] != null ? row[0].toString() : "Unknown")
                    .totalTransactions(total)
                    .flaggedCount(flagged)
                    .flaggedPercentage(flaggedPct)
                    .avgRiskScore(avgRiskScore)
                    .riskLabel(riskLabel)
                    .build();
        }).collect(Collectors.toList());
    }
}