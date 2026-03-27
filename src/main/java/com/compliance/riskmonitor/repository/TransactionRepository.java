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
}