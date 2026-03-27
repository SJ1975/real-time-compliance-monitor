package com.compliance.riskmonitor.exception;

public class TransactionNotFoundException extends RuntimeException {

    public TransactionNotFoundException(String transactionId) {
        super("Transaction not found with ID: " + transactionId);
    }
}
