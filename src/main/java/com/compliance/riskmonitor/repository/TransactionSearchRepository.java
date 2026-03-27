package com.compliance.riskmonitor.repository;

import com.compliance.riskmonitor.document.TransactionDocument;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TransactionSearchRepository
        extends ElasticsearchRepository<TransactionDocument, String> {

    // Find by userId
    List<TransactionDocument> findByUserId(String userId);

    // Find all flagged
    List<TransactionDocument> findByFlaggedTrue();

    // Find by risk level
    List<TransactionDocument> findByRiskLevel(String riskLevel);

    // Find by location (exact match)
    List<TransactionDocument> findByLocation(String location);

    // Find by merchant (full-text)
    List<TransactionDocument> findByMerchantContaining(String merchant);
}