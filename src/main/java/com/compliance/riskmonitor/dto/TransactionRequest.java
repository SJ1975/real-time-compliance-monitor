package com.compliance.riskmonitor.dto;

import jakarta.validation.constraints.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TransactionRequest {

    @NotBlank(message = "User ID is required")
    private String userId;

    @NotNull(message = "Amount is required")
    @DecimalMin(value = "0.01", message = "Amount must be greater than 0")
    private BigDecimal amount;

    @NotBlank(message = "Currency is required")
    @Size(min = 3, max = 3, message = "Currency must be a 3-letter code (e.g. USD)")
    private String currency;

    @NotBlank(message = "Merchant is required")
    private String merchant;

    @NotBlank(message = "Location is required")
    private String location;

    private LocalDateTime timestamp;  // Optional — defaults to now if null
}