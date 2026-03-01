package com.stevechao.tx_service.error;

public class IdempotencyConflictException extends RuntimeException {
  public IdempotencyConflictException(String key) {
    super("idempotency key already used with a different payload: " + key);
  }
}
