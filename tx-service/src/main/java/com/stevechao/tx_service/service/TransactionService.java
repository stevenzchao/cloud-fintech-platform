package com.stevechao.tx_service.service;

import com.stevechao.tx_service.dto.CreateTransactionRequest;
import com.stevechao.tx_service.entity.IdempotencyRecord;
import com.stevechao.tx_service.entity.TransactionEntity;
import com.stevechao.tx_service.error.IdempotencyConflictException;
import com.stevechao.tx_service.error.TransactionNotFoundException;
import com.stevechao.tx_service.repository.IdempotencyRecordRepository;
import com.stevechao.tx_service.repository.TransactionRepository;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;
import java.util.Optional;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class TransactionService {

  private final TransactionRepository transactionRepository;
  private final IdempotencyRecordRepository idempotencyRecordRepository;

  public TransactionService(TransactionRepository transactionRepository,
      IdempotencyRecordRepository idempotencyRecordRepository) {
    this.transactionRepository = transactionRepository;
    this.idempotencyRecordRepository = idempotencyRecordRepository;
  }

  @Transactional
  public TransactionEntity create(CreateTransactionRequest request, String idempotencyKey) {
    String requestHash = hashRequest(request);
//    System.out.println("requestHash: " + requestHash);
    if (idempotencyKey != null && !idempotencyKey.isBlank()) {
      Optional<IdempotencyRecord> existingRecord = idempotencyRecordRepository.findById(idempotencyKey);
      if (existingRecord.isPresent()) {
        IdempotencyRecord record = existingRecord.get();
        if (!record.getRequestHash().equals(requestHash)) {
          throw new IdempotencyConflictException(idempotencyKey);
        }
        return getById(record.getTransactionId());
      }
    }

    TransactionEntity entity = new TransactionEntity();
    entity.setAmount(request.amount());
    entity.setCurrency(request.currency());
    entity.setExternalReference(request.externalReference());
    entity.setDescription(request.description());
    entity.setStatus("CREATED");

    TransactionEntity saved = transactionRepository.save(entity);

    if (idempotencyKey != null && !idempotencyKey.isBlank()) {
      IdempotencyRecord record = new IdempotencyRecord();
      record.setIdempotencyKey(idempotencyKey);
      record.setRequestHash(requestHash);
      record.setTransactionId(saved.getId());
      idempotencyRecordRepository.save(record);
    }

    return saved;
  }

  @Transactional(readOnly = true)
  public TransactionEntity getById(UUID id) {
    return transactionRepository.findById(id)
        .orElseThrow(() -> new TransactionNotFoundException(id.toString()));
  }

  private String hashRequest(CreateTransactionRequest request) {
    String payload = request.amount().toPlainString() + "|" + request.currency() + "|" + request.externalReference()
        + "|" + (request.description() == null ? "" : request.description());
//    System.out.println("payload: " + payload);
    try {
      MessageDigest digest = MessageDigest.getInstance("SHA-256");
      return HexFormat.of().formatHex(digest.digest(payload.getBytes(StandardCharsets.UTF_8)));
    } catch (NoSuchAlgorithmException e) {
      throw new IllegalStateException("SHA-256 not available", e);
    }
  }
}