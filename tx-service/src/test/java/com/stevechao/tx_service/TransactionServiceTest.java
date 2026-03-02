package com.stevechao.tx_service;

import com.stevechao.tx_service.dto.CreateTransactionRequest;
import com.stevechao.tx_service.entity.IdempotencyRecord;
import com.stevechao.tx_service.entity.TransactionEntity;
import com.stevechao.tx_service.error.IdempotencyConflictException;
import com.stevechao.tx_service.error.TransactionNotFoundException;
import com.stevechao.tx_service.repository.IdempotencyRecordRepository;
import com.stevechao.tx_service.repository.TransactionRepository;
import com.stevechao.tx_service.service.TransactionService;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.atLeastOnce;
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
  void testGetByIdSuccess() {
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
    existing.setMerchant("ext-1");
    existing.setDescription("test");
    existing.setStatus("CREATED");
    existing.setCreatedAt(Instant.now());

    when(transactionRepository.findById(transactionId)).thenReturn(Optional.of(existing));

    TransactionEntity result = transactionService.getById(transactionId);

    assertThat(result.getId()).isEqualTo(transactionId);
    assertThat(result.getAmount()).isEqualTo(new BigDecimal("10.00"));
  }

  @Test
  void testGetByIdNotFound() {

    UUID id = UUID.randomUUID();

    when(transactionRepository.findById(id)).thenReturn(Optional.empty());

    assertThatThrownBy(() -> transactionService.getById(id)).isInstanceOf(
        TransactionNotFoundException.class);
  }


  @Test
  void testCreateSuccess() {
    CreateTransactionRequest request = new CreateTransactionRequest(new BigDecimal("10.00"), "USD", "ext-1", "test");
    UUID transactionId = UUID.randomUUID();

    // mock: no existing idempotency record
    when(idempotencyRecordRepository.findById("idem-1"))
        .thenReturn(Optional.empty());

    // mock: give back  entity with id when save transaction
    when(transactionRepository.save(any(TransactionEntity.class)))
        .thenAnswer(invocation -> {
          TransactionEntity e = invocation.getArgument(0);
          e.setId(transactionId);
          return e;
        });

    TransactionEntity result = transactionService.create(request, "idem-1");

    verify(transactionRepository, atLeastOnce()).save(any(TransactionEntity.class));
    verify(idempotencyRecordRepository, atLeastOnce()).save(any(IdempotencyRecord.class));

    ArgumentCaptor<TransactionEntity> captor = ArgumentCaptor.forClass(TransactionEntity.class);
    verify(transactionRepository).save(captor.capture());

    TransactionEntity savedArg = captor.getValue();
    assertThat(savedArg.getAmount()).isEqualByComparingTo("10.00");
    assertThat(savedArg.getCurrency()).isEqualTo("USD");
    assertThat(savedArg.getMerchant()).isEqualTo("ext-1");
    assertThat(savedArg.getStatus()).isEqualTo("CREATED");

    ArgumentCaptor<IdempotencyRecord> idemCaptor = ArgumentCaptor.forClass(IdempotencyRecord.class);
    verify(idempotencyRecordRepository).save(idemCaptor.capture());

    assertThat(idemCaptor.getValue().getIdempotencyKey()).isEqualTo("idem-1");
    assertThat(idemCaptor.getValue().getTransactionId()).isEqualTo(transactionId);

  }

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
    existing.setMerchant("ext-1");
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
