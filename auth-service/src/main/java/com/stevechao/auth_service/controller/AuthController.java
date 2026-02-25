package com.stevechao.auth_service.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.util.Map;

@RestController
public class AuthController {

  @GetMapping("/health")
  public Map<String, Object> health() {
    return Map.of(
        "service", "auth-service",
        "status", "UP",
        "timestamp", Instant.now().toString()
    );
  }

  @GetMapping("/v1/auth/ping")
  public Map<String, Object> ping() {
    return Map.of(
        "message", "auth-service pong",
        "timestamp", Instant.now().toString()
    );
  }
}
