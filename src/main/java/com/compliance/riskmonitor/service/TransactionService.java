package com.compliance.riskmonitor.service;

import com.compliance.riskmonitor.dto.TransactionRequest;
import com.compliance.riskmonitor.dto.TransactionResponse;
import com.compliance.riskmonitor.dto.UserActivitySummary;

import java.util.List;

public interface TransactionService {

    TransactionResponse createTransaction(TransactionRequest request);

    TransactionResponse getTransactionById(String transactionId);

    List<TransactionResponse> getAllTransactions();

    List<TransactionResponse> getFlaggedTransactions();

    List<TransactionResponse> getTransactionsByUser(String userId);

    UserActivitySummary getUserActivitySummary(String userId);
}