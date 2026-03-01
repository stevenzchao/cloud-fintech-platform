package com.stevechao.tx_service.controller;

import com.stevechao.tx_service.dto.CreateTransactionRequest;
import com.stevechao.tx_service.dto.TransactionResponse;
import com.stevechao.tx_service.entity.TransactionEntity;
import com.stevechao.tx_service.service.TransactionService;
import jakarta.validation.Valid;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.util.Map;

@RestController
public class TxController {

  private final TransactionService transactionService;

  public TxController(TransactionService transactionService) {
    this.transactionService = transactionService;
  }

  @PostMapping("/v1/transactions")
  @ResponseStatus(HttpStatus.CREATED)
  public TransactionResponse createTransaction(
      @Valid @RequestBody CreateTransactionRequest request,
      @RequestHeader(value = "Idempotency-Key", required = false) String idempotencyKey
  ) {
    return toResponse(transactionService.create(request, idempotencyKey));
  }

  @GetMapping("/v1/transactions/{id}")
  public TransactionResponse getTransaction(@PathVariable UUID id) {
    return toResponse(transactionService.getById(id));
  }

  private TransactionResponse toResponse(TransactionEntity tx) {
    return new TransactionResponse(
        tx.getId(),
        tx.getAmount(),
        tx.getCurrency(),
        tx.getExternalReference(),
        tx.getDescription(),
        tx.getStatus(),
        tx.getCreatedAt()
    );
  }

  @GetMapping("/health")
  public Map<String, Object> health() {
    return Map.of(
        "service", "tx-service",
        "status", "UP",
        "timestamp", Instant.now().toString()
    );
  }

  @GetMapping("/v1/tx/ping")
  public Map<String, Object> ping() {
    return Map.of(
        "message", "tx-service pong",
        "timestamp", Instant.now().toString()
    );
  }
}
