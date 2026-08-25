package com.compliance.riskmonitor.repository;

import com.compliance.riskmonitor.entity.Transaction;
import com.compliance.riskmonitor.entity.Transaction.RiskLevel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, String> {

    // Get all transactions for a user
    List<Transaction> findByUserIdOrderByTimestampDesc(String userId);

    // Get all flagged transactions
    List<Transaction> findByFlaggedTrueOrderByRiskScoreDesc();

    // Get flagged transactions for a specific user
    List<Transaction> findByUserIdAndFlaggedTrue(String userId);

    // Count user transactions within a time window (for velocity check)
    @Query("""
        SELECT COUNT(t) FROM Transaction t
        WHERE t.userId = :userId
        AND t.timestamp >= :windowStart
        """)
    long countRecentTransactionsByUser(
            @Param("userId") String userId,
            @Param("windowStart") LocalDateTime windowStart
    );

    // Get transactions above a certain risk score
    List<Transaction> findByRiskScoreGreaterThanEqualOrderByRiskScoreDesc(Integer minScore);

    // Get transactions by risk level
    List<Transaction> findByRiskLevelOrderByTimestampDesc(RiskLevel riskLevel);

    // User activity summary query
    @Query("""
        SELECT COUNT(t), SUM(t.amount), AVG(t.amount),
               SUM(CASE WHEN t.flagged = true THEN 1 ELSE 0 END)
        FROM Transaction t
        WHERE t.userId = :userId
        """)
    List<Object[]> getUserActivitySummary(@Param("userId") String userId);

    // Transactions in a time range
    List<Transaction> findByTimestampBetweenOrderByTimestampDesc(
            LocalDateTime start, LocalDateTime end
    );

    // Daily trends
    @Query(value = """
    SELECT
        CAST(timestamp AS DATE) as date,
        COUNT(*) as total_transactions,
        COALESCE(SUM(amount), 0) as total_amount,
        COALESCE(AVG(amount), 0) as avg_amount,
        COALESCE(SUM(CASE WHEN flagged = true THEN 1 ELSE 0 END), 0) as flagged_count
    FROM transactions
    GROUP BY CAST(timestamp AS DATE)
    ORDER BY CAST(timestamp AS DATE) DESC
    """, nativeQuery = true)
    List<Object[]> getDailyTrends();


    // Hourly patterns
    @Query(value = """
    SELECT
        EXTRACT(HOUR FROM timestamp) as hour,
        COUNT(*) as transaction_count,
        COALESCE(AVG(amount), 0) as avg_amount,
        COALESCE(SUM(CASE WHEN flagged = true THEN 1 ELSE 0 END), 0) as flagged_count
    FROM transactions
    GROUP BY EXTRACT(HOUR FROM timestamp)
    ORDER BY EXTRACT(HOUR FROM timestamp)
    """, nativeQuery = true)
    List<Object[]> getHourlyPatterns();


    // Location analytics
    @Query(value = """
    SELECT
        location,
        COUNT(*) as total_transactions,
        COALESCE(SUM(CASE WHEN flagged = true THEN 1 ELSE 0 END), 0) as flagged_count,
        COALESCE(AVG(risk_score), 0) as avg_risk_score
    FROM transactions
    GROUP BY location
    ORDER BY COUNT(*) DESC
    """, nativeQuery = true)
    List<Object[]> getLocationAnalytics();
}