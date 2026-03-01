CREATE TABLE transactions (
  id UUID PRIMARY KEY,
  amount NUMERIC(18,2) NOT NULL,
  currency VARCHAR(3) NOT NULL,
  merchant VARCHAR(100) NOT NULL,
  description VARCHAR(255),
  status VARCHAR(20) NOT NULL,
  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE idempotency_keys (
  idempotency_key VARCHAR(100) PRIMARY KEY,
  request_hash VARCHAR(64) NOT NULL,
  transaction_id UUID NOT NULL REFERENCES transactions(id),
  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);