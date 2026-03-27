package com.compliance.riskmonitor.service;

import com.compliance.riskmonitor.document.TransactionDocument;
import com.compliance.riskmonitor.dto.SearchRequest;
import com.compliance.riskmonitor.entity.Transaction;
import com.compliance.riskmonitor.repository.TransactionSearchRepository;
import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch._types.query_dsl.*;
import co.elastic.clients.elasticsearch.core.SearchResponse;
import co.elastic.clients.elasticsearch.core.search.Hit;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class TransactionSearchService {

    private final TransactionSearchRepository searchRepository;
    private final ElasticsearchClient elasticsearchClient;

    // Index a single transaction document
    public void indexTransaction(Transaction transaction) {
        try {
            TransactionDocument doc = toDocument(transaction);
            searchRepository.save(doc);
            log.info("Transaction {} indexed in ElasticSearch", transaction.getTransactionId());
        } catch (Exception e) {
            // Log but don't fail the main flow if ES indexing fails
            log.error("Failed to index transaction {}: {}",
                    transaction.getTransactionId(), e.getMessage());
        }
    }

    // Advanced search with multiple optional filters
    public List<TransactionDocument> search(SearchRequest request) throws IOException {
        log.info("Executing ES search with filters: {}", request);

        List<Query> mustQueries = new ArrayList<>();

        // Filter: userId
        if (request.getUserId() != null && !request.getUserId().isBlank()) {
            mustQueries.add(Query.of(q -> q
                    .term(t -> t.field("userId").value(request.getUserId()))
            ));
        }

        // Filter: location
        if (request.getLocation() != null && !request.getLocation().isBlank()) {
            mustQueries.add(Query.of(q -> q
                    .term(t -> t.field("location").value(request.getLocation()))
            ));
        }

        // Filter: riskLevel
        if (request.getRiskLevel() != null && !request.getRiskLevel().isBlank()) {
            mustQueries.add(Query.of(q -> q
                    .term(t -> t.field("riskLevel").value(request.getRiskLevel()))
            ));
        }

        // Filter: flagged only
        if (request.getFlagged() != null) {
            mustQueries.add(Query.of(q -> q
                    .term(t -> t.field("flagged").value(request.getFlagged()))
            ));
        }

        // Filter: merchant (full-text search)
        if (request.getMerchant() != null && !request.getMerchant().isBlank()) {
            mustQueries.add(Query.of(q -> q
                    .match(m -> m.field("merchant").query(request.getMerchant()))
            ));
        }

        // Filter: amount range
        if (request.getMinAmount() != null || request.getMaxAmount() != null) {
            mustQueries.add(Query.of(q -> q
                    .range(r -> {
                        r.field("amount");
                        if (request.getMinAmount() != null) {
                            r.gte(co.elastic.clients.json.JsonData.of(request.getMinAmount()));
                        }
                        if (request.getMaxAmount() != null) {
                            r.lte(co.elastic.clients.json.JsonData.of(request.getMaxAmount()));
                        }
                        return r;
                    })
            ));
        }

        // Build final query
        Query finalQuery = mustQueries.isEmpty()
                ? Query.of(q -> q.matchAll(m -> m))   // no filters = return all
                : Query.of(q -> q.bool(b -> b.must(mustQueries)));

        // Execute search
        SearchResponse<TransactionDocument> response = elasticsearchClient.search(
                s -> s.index("transactions")
                        .query(finalQuery)
                        .size(request.getSize() != null ? request.getSize() : 50),
                TransactionDocument.class
        );

        return response.hits().hits().stream()
                .map(Hit::source)
                .collect(Collectors.toList());
    }

    // Convert JPA Entity → ES Document
    public TransactionDocument toDocument(Transaction transaction) {
        return TransactionDocument.builder()
                .transactionId(transaction.getTransactionId())
                .userId(transaction.getUserId())
                .amount(transaction.getAmount())
                .currency(transaction.getCurrency())
                .merchant(transaction.getMerchant())
                .location(transaction.getLocation())
                .timestamp(transaction.getTimestamp())
                .flagged(transaction.isFlagged())
                .riskScore(transaction.getRiskScore())
                .flagReasons(transaction.getFlagReasons())
                .riskLevel(transaction.getRiskLevel() != null
                        ? transaction.getRiskLevel().name()
                        : "LOW")
                .processedAt(transaction.getProcessedAt())
                .build();
    }

    public TransactionSearchRepository getSearchRepository() {
        return searchRepository;
    }
}