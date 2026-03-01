package com.stevechao.tx_service.error;

public class TransactionNotFoundException extends RuntimeException {
  public TransactionNotFoundException(String id) {
    super("transaction not found: " + id);
  }
}