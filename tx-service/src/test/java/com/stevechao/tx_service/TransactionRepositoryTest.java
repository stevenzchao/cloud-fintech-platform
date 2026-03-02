package com.stevechao.tx_service;

import static org.assertj.core.api.Assertions.assertThat;

import com.stevechao.tx_service.entity.TransactionEntity;
import com.stevechao.tx_service.repository.IdempotencyRecordRepository;
import com.stevechao.tx_service.repository.TransactionRepository;
import java.math.BigDecimal;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

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

}