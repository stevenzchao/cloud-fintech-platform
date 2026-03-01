package com.stevechao.tx_service;

import com.stevechao.tx_service.dto.CreateTransactionRequest;
import com.stevechao.tx_service.entity.IdempotencyRecord;
import com.stevechao.tx_service.entity.TransactionEntity;
import com.stevechao.tx_service.error.IdempotencyConflictException;
import com.stevechao.tx_service.repository.IdempotencyRecordRepository;
import com.stevechao.tx_service.repository.TransactionRepository;
import com.stevechao.tx_service.service.TransactionService;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TransactionServiceTest {

  @Mock
  private TransactionRepository transactionRepository;

  @Mock
  private IdempotencyRecordRepository idempotencyRecordRepository;

  @InjectMocks
  private TransactionService transactionService;

  @Test
  void returnsExistingTransactionWhenIdempotencyKeyAndPayloadMatch() {
    CreateTransactionRequest request = new CreateTransactionRequest(new BigDecimal("10.00"), "USD", "ext-1", "test");
    UUID transactionId = UUID.randomUUID();

    IdempotencyRecord record = new IdempotencyRecord();
    record.setIdempotencyKey("idem-1");
    record.setRequestHash("db3c05fd50ebe058871317ef3cacd7c0016f3d6ad52887cc4dc4e316dd981827");
    record.setTransactionId(transactionId);

    TransactionEntity existing = new TransactionEntity();
    existing.setId(transactionId);
    existing.setAmount(new BigDecimal("10.00"));
    existing.setCurrency("USD");
    existing.setExternalReference("ext-1");
    existing.setDescription("test");
    existing.setStatus("CREATED");
    existing.setCreatedAt(Instant.now());

    when(idempotencyRecordRepository.findById("idem-1")).thenReturn(Optional.of(record));
    when(transactionRepository.findById(transactionId)).thenReturn(Optional.of(existing));

    TransactionEntity result = transactionService.create(request, "idem-1");

    assertThat(result.getId()).isEqualTo(transactionId);
    verify(transactionRepository, never()).save(any(TransactionEntity.class));
  }

  @Test
  void throwsConflictWhenIdempotencyPayloadDiffers() {
    CreateTransactionRequest request = new CreateTransactionRequest(new BigDecimal("10.00"), "USD", "ext-1", "test");

    IdempotencyRecord record = new IdempotencyRecord();
    record.setIdempotencyKey("idem-1");
    record.setRequestHash("different-hash");
    record.setTransactionId(UUID.randomUUID());

    when(idempotencyRecordRepository.findById("idem-1")).thenReturn(Optional.of(record));

    assertThatThrownBy(() -> transactionService.create(request, "idem-1"))
        .isInstanceOf(IdempotencyConflictException.class);
  }
}
