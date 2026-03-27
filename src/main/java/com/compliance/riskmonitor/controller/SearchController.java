// src/main/java/com/compliance/riskmonitor/controller/SearchController.java

package com.compliance.riskmonitor.controller;

import com.compliance.riskmonitor.document.TransactionDocument;
import com.compliance.riskmonitor.dto.ApiResponse;
import com.compliance.riskmonitor.dto.SearchRequest;
import com.compliance.riskmonitor.service.TransactionSearchService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/v1/search")
@RequiredArgsConstructor
public class SearchController {

    private final TransactionSearchService searchService;

    // POST /api/v1/search/transactions
    // Body contains optional filters
    @PostMapping("/transactions")
    public ResponseEntity<ApiResponse<List<TransactionDocument>>> search(
            @RequestBody SearchRequest request) throws IOException {

        log.info("Search request received: {}", request);
        List<TransactionDocument> results = searchService.search(request);

        return ResponseEntity.ok(
                ApiResponse.success(
                        "Found " + results.size() + " transactions",
                        results
                )
        );
    }

    // GET /api/v1/search/transactions/user/{userId}
    @GetMapping("/transactions/user/{userId}")
    public ResponseEntity<ApiResponse<List<TransactionDocument>>> searchByUser(
            @PathVariable String userId) {

        log.info("ES search by userId: {}", userId);
        List<TransactionDocument> results = searchService
                .getSearchRepository()
                .findByUserId(userId);

        return ResponseEntity.ok(
                ApiResponse.success("Found " + results.size() + " transactions", results)
        );
    }

    // GET /api/v1/search/transactions/flagged
    @GetMapping("/transactions/flagged")
    public ResponseEntity<ApiResponse<List<TransactionDocument>>> searchFlagged() {

        log.info("ES search for flagged transactions");
        List<TransactionDocument> results = searchService
                .getSearchRepository()
                .findByFlaggedTrue();

        return ResponseEntity.ok(
                ApiResponse.success("Found " + results.size() + " flagged transactions", results)
        );
    }

    // GET /api/v1/search/transactions/risk/{level}
    @GetMapping("/transactions/risk/{level}")
    public ResponseEntity<ApiResponse<List<TransactionDocument>>> searchByRiskLevel(
            @PathVariable String level) {

        log.info("ES search by risk level: {}", level);
        List<TransactionDocument> results = searchService
                .getSearchRepository()
                .findByRiskLevel(level.toUpperCase());

        return ResponseEntity.ok(
                ApiResponse.success("Found " + results.size() + " transactions", results)
        );
    }
}