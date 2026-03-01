package com.stevechao.tx_service.dto;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record TransactionResponse(
    UUID id,
    BigDecimal amount,
    String currency,
    String externalReference,
    String description,
    String status,
    Instant createdAt
) {
}
