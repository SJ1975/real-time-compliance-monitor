package com.compliance.riskmonitor.controller;

import com.compliance.riskmonitor.dto.*;
import com.compliance.riskmonitor.repository.TransactionRepository;
import com.compliance.riskmonitor.service.TransactionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@RestController
@RequestMapping("/api/v1/transactions")
@RequiredArgsConstructor
public class TransactionController {

    private final TransactionService transactionService;
    private final TransactionRepository transactionRepository;
    private final TransactionMapper transactionMapper;

    // POST /api/v1/transactions
    @PostMapping
    public ResponseEntity<ApiResponse<TransactionResponse>> createTransaction(
            @Valid @RequestBody TransactionRequest request) {

        log.info("POST /api/v1/transactions - userId: {}", request.getUserId());
        TransactionResponse response = transactionService.createTransaction(request);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success("Transaction created successfully", response));
    }

    // GET /api/v1/transactions/{id}
    @GetMapping("/{transactionId}")
    public ResponseEntity<ApiResponse<TransactionResponse>> getTransaction(
            @PathVariable String transactionId) {

        log.info("GET /api/v1/transactions/{}", transactionId);
        TransactionResponse response = transactionService.getTransactionById(transactionId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    // GET /api/v1/transactions
    @GetMapping
    public ResponseEntity<ApiResponse<List<TransactionResponse>>> getAllTransactions() {

        log.info("GET /api/v1/transactions");
        List<TransactionResponse> transactions = transactionService.getAllTransactions();
        return ResponseEntity.ok(
                ApiResponse.success("Found " + transactions.size() + " transactions", transactions)
        );
    }

    // GET /api/v1/transactions/flagged
    @GetMapping("/flagged")
    public ResponseEntity<ApiResponse<List<TransactionResponse>>> getFlaggedTransactions() {

        log.info("GET /api/v1/transactions/flagged");
        List<TransactionResponse> flagged = transactionService.getFlaggedTransactions();
        return ResponseEntity.ok(
                ApiResponse.success("Found " + flagged.size() + " flagged transactions", flagged)
        );
    }

    // GET /api/v1/transactions/user/{userId}
    @GetMapping("/user/{userId}")
    public ResponseEntity<ApiResponse<List<TransactionResponse>>> getUserTransactions(
            @PathVariable String userId) {

        log.info("GET /api/v1/transactions/user/{}", userId);
        List<TransactionResponse> transactions = transactionService.getTransactionsByUser(userId);
        return ResponseEntity.ok(
                ApiResponse.success("Found " + transactions.size() + " transactions for user: " + userId, transactions)
        );
    }

    // GET /api/v1/transactions/user/{userId}/summary
    @GetMapping("/user/{userId}/summary")
    public ResponseEntity<ApiResponse<UserActivitySummary>> getUserSummary(
            @PathVariable String userId) {

        log.info("GET /api/v1/transactions/user/{}/summary", userId);
        UserActivitySummary summary = transactionService.getUserActivitySummary(userId);
        return ResponseEntity.ok(ApiResponse.success(summary));
    }

    // GET /api/v1/transactions/high-risk?minScore=60
    @GetMapping("/high-risk")
    public ResponseEntity<ApiResponse<List<TransactionResponse>>> getHighRiskTransactions(
            @RequestParam(defaultValue = "60") Integer minScore) {

        log.info("GET /api/v1/transactions/high-risk?minScore={}", minScore);

        List<TransactionResponse> transactions = transactionRepository
                .findByRiskScoreGreaterThanEqualOrderByRiskScoreDesc(minScore)
                .stream()
                .map(transactionMapper::toResponse)
                .collect(Collectors.toList());

        return ResponseEntity.ok(
                ApiResponse.success(
                        "Found " + transactions.size() + " high-risk transactions",
                        transactions
                )
        );
    }
}
