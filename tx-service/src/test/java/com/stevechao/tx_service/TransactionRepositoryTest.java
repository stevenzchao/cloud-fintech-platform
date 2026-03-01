package com.stevechao.tx_service;

import com.stevechao.tx_service.entity.IdempotencyRecord;
import com.stevechao.tx_service.entity.TransactionEntity;
import com.stevechao.tx_service.repository.IdempotencyRecordRepository;
import com.stevechao.tx_service.repository.TransactionRepository;
import java.math.BigDecimal;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

@DataJpaTest
@ActiveProfiles("test")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class TransactionRepositoryTest {

  @Autowired
  private TransactionRepository transactionRepository;

  @Autowired
  private IdempotencyRecordRepository idempotencyRecordRepository;

  @Test
  void savesAndLoadsTransaction() {
    TransactionEntity tx = new TransactionEntity();
    tx.setAmount(new BigDecimal("120.50"));
    tx.setCurrency("USD");
    tx.setMerchant("ext-123");
    tx.setDescription("salary");
    tx.setStatus("CREATED");

    TransactionEntity saved = transactionRepository.saveAndFlush(tx);

    assertThat(transactionRepository.findById(saved.getId())).isPresent();
  }

  @Test
  void enforcesUniqueIdempotencyKey() {
    TransactionEntity tx = new TransactionEntity();
    tx.setAmount(new BigDecimal("5.00"));
    tx.setCurrency("USD");
    tx.setMerchant("ext-456");
    tx.setDescription("coffee");
    tx.setStatus("CREATED");
    TransactionEntity saved = transactionRepository.saveAndFlush(tx);

    IdempotencyRecord first = new IdempotencyRecord();
    first.setIdempotencyKey("key-1");
    first.setRequestHash("hash-1");
    first.setTransactionId(saved.getId());
    idempotencyRecordRepository.saveAndFlush(first);

    IdempotencyRecord duplicate = new IdempotencyRecord();
    duplicate.setIdempotencyKey("key-1");
    duplicate.setRequestHash("hash-2");
    duplicate.setTransactionId(saved.getId());

    assertThatThrownBy(() -> idempotencyRecordRepository.saveAndFlush(duplicate))
        .isInstanceOf(DataIntegrityViolationException.class);
  }
}