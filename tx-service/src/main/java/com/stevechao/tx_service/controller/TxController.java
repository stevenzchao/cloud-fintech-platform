package com.stevechao.tx_service.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.util.Map;

@RestController
public class TxController {

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
