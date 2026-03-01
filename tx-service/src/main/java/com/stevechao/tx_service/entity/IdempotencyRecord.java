package com.stevechao.tx_service.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;
import lombok.Data;
import lombok.ToString;


@Data
@ToString(callSuper = true)
@Entity
@Table(name = "idempotency_keys")
public class IdempotencyRecord {

  @Id
  @Column(name = "idempotency_key", length = 100)
  private String idempotencyKey;

  @Column(name = "request_hash", nullable = false, length = 64)
  private String requestHash;

  @Column(name = "transaction_id", nullable = false)
  private UUID transactionId;

  @Column(name = "created_at", nullable = false, updatable = false)
  private Instant createdAt;

  @PrePersist
  void onCreate() { this.createdAt = Instant.now(); }

}
